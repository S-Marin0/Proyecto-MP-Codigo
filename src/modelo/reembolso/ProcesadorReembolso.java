
package modelo.reembolso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import modelo.compra.Compra;
import modelo.compra.EstadoCompra;
import modelo.usuario.Usuario;
import modelo.notificacion.SistemaNotificaciones; // Asumiendo que será inyectado o accesible

public class ProcesadorReembolso {
    private Map<String, Double> tarifasComisionPorMotivo; // Ej: "CancelacionUsuario" -> 0.05 (5%)
    private List<String> metodosReembolsoDisponibles;
    private int diasLimiteReembolsoEvento; // Días antes del evento para permitir reembolso
    private SistemaNotificaciones sistemaNotificaciones;

    public ProcesadorReembolso(SistemaNotificaciones sistemaNotificaciones) {
        this.tarifasComisionPorMotivo = new HashMap<>();
        this.metodosReembolsoDisponibles = new ArrayList<>();
        this.metodosReembolsoDisponibles.add("CREDITO_TIENDA");
        this.metodosReembolsoDisponibles.add("MISMO_METODO_PAGO"); // Puede ser más complejo
        this.diasLimiteReembolsoEvento = 7; // Por defecto, 7 días antes del evento
        this.sistemaNotificaciones = sistemaNotificaciones;

        // Comisiones de ejemplo
        this.tarifasComisionPorMotivo.put("CANCELACION_USUARIO_ANTICIPADA", 0.0); // Sin comisión si es muy anticipada
        this.tarifasComisionPorMotivo.put("CANCELACION_USUARIO_ESTANDAR", 0.05); // 5% comisión
        this.tarifasComisionPorMotivo.put("EVENTO_CANCELADO_ORGANIZADOR", 0.0); // Sin comisión si el organizador cancela
    }

    public boolean procesarReembolso(Compra compra, String motivo, String metodoReembolsoPreferido) {
        if (compra == null) {
            System.err.println("ProcesadorReembolso: La compra no puede ser nula.");
            return false;
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            System.err.println("ProcesadorReembolso: El motivo del reembolso no puede ser nulo o vacío.");
            return false;
        }

        // 1. Validar elegibilidad
        if (!validarElegibilidadReembolso(compra, motivo)) {
            System.err.println("ProcesadorReembolso: La compra ID " + compra.getId() + " no es elegible para reembolso.");
            // Se podría notificar al usuario la razón específica.
            return false;
        }

        // 2. Calcular monto a reembolsar (considerando comisiones)
        double comision = calcularComisionReembolso(compra.getTotalPagado(), motivo);
        double montoFinalAReembolsar = compra.getTotalPagado() - comision;

        if (montoFinalAReembolsar <= 0 && compra.getTotalPagado() > 0) {
            System.err.println("ProcesadorReembolso: El monto a reembolsar es cero o negativo después de comisiones para compra ID " + compra.getId());
            return false;
        }

        // 3. Procesar la transacción de reembolso (simulación)
        String transaccionId = procesarTransaccionExternaReembolso(montoFinalAReembolsar,
                                                                compra.getMetodoPagoUtilizado() != null ? compra.getMetodoPagoUtilizado().getTipo() : "DESCONOCIDO",
                                                                metodoReembolsoPreferido);
        if (transaccionId == null) {
            System.err.println("ProcesadorReembolso: Falló la transacción externa de reembolso para compra ID " + compra.getId());
            return false;
        }
        System.out.println("ProcesadorReembolso: Transacción de reembolso " + transaccionId + " procesada por " + montoFinalAReembolsar);

        // 4. Actualizar estado de la compra
        compra.setEstado(EstadoCompra.REEMBOLSADA);
        // Aquí también se debería actualizar la disponibilidad de las entradas (TipoEntrada.incrementarDisponibilidad)
        // Esto es crucial y debería hacerse como parte de este proceso o en el comando que lo llama.
        // compra.getTipoEntradaBase().incrementarDisponibilidad(compra.getCantidad()); // Ejemplo conceptual

        // 5. Registrar el reembolso
        registrarReembolsoEnSistema(compra, montoFinalAReembolsar, motivo, transaccionId);

        // 6. Notificar al usuario
        if (this.sistemaNotificaciones != null) {
            this.sistemaNotificaciones.enviarNotificacionReembolso(compra.getUsuario(), compra, montoFinalAReembolsar);
        } else {
            System.out.println("ProcesadorReembolso: Sistema de notificaciones no disponible. No se envió notificación de reembolso.");
        }

        // 7. Generar comprobante de reembolso (opcional)
        generarComprobanteReembolso(compra, montoFinalAReembolsar);

        return true;
    }

    public boolean validarElegibilidadReembolso(Compra compra, String motivo) {
        if (compra.getEstado() != EstadoCompra.CONFIRMADA) {
            System.out.println("ValidacionElegibilidad: Compra ID " + compra.getId() + " no está confirmada. Estado actual: " + compra.getEstado());
            return false; // Solo se pueden reembolsar compras confirmadas
        }

        // Lógica de elegibilidad basada en el tiempo antes del evento
        Date fechaActual = new Date();
        Date fechaEvento = compra.getEvento().getFecha(); // Asumiendo que Evento tiene getFecha()
        long diffInMillis = fechaEvento.getTime() - fechaActual.getTime();
        long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

        if (diffInDays < this.diasLimiteReembolsoEvento && "CANCELACION_USUARIO_ESTANDAR".equals(motivo)) {
             System.out.println("ValidacionElegibilidad: Compra ID " + compra.getId() + ". Demasiado tarde para reembolso por cancelación de usuario (" + diffInDays + " días restantes, límite " + this.diasLimiteReembolsoEvento + ").");
            return false;
        }

        // Otras validaciones (ej. si el evento fue cancelado por el organizador, siempre es elegible)
        if ("EVENTO_CANCELADO_ORGANIZADOR".equals(motivo)) {
            return true;
        }

        System.out.println("ValidacionElegibilidad: Compra ID " + compra.getId() + " es elegible para reembolso.");
        return true;
    }

    public double calcularComisionReembolso(double montoOriginal, String motivo) {
        double tasaComision = this.tarifasComisionPorMotivo.getOrDefault(motivo, 0.10); // 10% comisión por defecto si el motivo no está
        double comision = montoOriginal * tasaComision;
        System.out.println(String.format("CalculoComision: Monto Original: %.2f, Motivo: %s, Tasa: %.2f, Comisión: %.2f", montoOriginal, motivo, tasaComision, comision));
        return comision;
    }

    private String procesarTransaccionExternaReembolso(double monto, String metodoPagoOriginal, String metodoReembolsoPreferido) {
        // Simulación de interacción con una pasarela de pagos para procesar el reembolso
        System.out.println(String.format("Procesando reembolso externo de %.2f al método original %s (preferido %s)", monto, metodoPagoOriginal, metodoReembolsoPreferido));
        // En un caso real, aquí se llamaría a la API de la pasarela de pago.
        // Devolvería un ID de transacción si es exitoso.
        return "REEMBOLSO_TXN_" + UUID.randomUUID().toString();
    }

    public void registrarReembolsoEnSistema(Compra compra, double montoReembolsado, String motivo, String transaccionId) {
        // Lógica para guardar un registro del reembolso en la base de datos o sistema de logging.
        System.out.println(String.format("Reembolso Registrado: CompraID=%s, Monto=%.2f, Motivo=%s, TransaccionID=%s, Usuario=%s",
                           compra.getId(), montoReembolsado, motivo, transaccionId, compra.getUsuario().getNombre()));
    }

    public String generarComprobanteReembolso(Compra compra, double montoReembolsado) {
        String comprobante = "COMPROBANTE_REEMBOLSO: IDCompra=" + compra.getId() + ", MontoReembolsado=" + montoReembolsado;
        System.out.println(comprobante);
        return comprobante;
    }

    // Getters y Setters para configuración
    public List<String> getMetodosReembolsoDisponibles() { return new ArrayList<>(metodosReembolsoDisponibles); }
    public void agregarMetodoReembolsoDisponible(String metodo) { if(metodo!=null && !metodo.trim().isEmpty()) this.metodosReembolsoDisponibles.add(metodo); }
    public void configurarTarifaComision(String motivo, double tarifa) {
        if (tarifa < 0 || tarifa > 1) throw new IllegalArgumentException("La tarifa de comisión debe estar entre 0 y 1.");
        this.tarifasComisionPorMotivo.put(motivo, tarifa);
    }
    public void setDiasLimiteReembolsoEvento(int dias) {
        if (dias < 0) throw new IllegalArgumentException("Días límite no puede ser negativo.");
        this.diasLimiteReembolsoEvento = dias;
    }

    // Los siguientes métodos del esqueleto original no se implementaron completamente
    // porque su funcionalidad está integrada o simplificada en los métodos anteriores:
    // public void notificarReembolso(Usuario usuario, double monto) {} // Integrado en procesarReembolso
    // public boolean validarMontoMaximo(double monto, Compra compra) { return true; } // Se asume que el monto es el de la compra
    // public String procesarTransaccionBancaria(double monto, String metodo) { return ""; } // Integrado en procesarTransaccionExternaReembolso
}
