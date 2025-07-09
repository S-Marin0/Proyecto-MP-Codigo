
package modelo.facade;

import modelo.usuario.Usuario;
import modelo.evento.Evento;
import modelo.entrada.TipoEntrada;
import modelo.entrada.Entrada;
import modelo.entrada.EntradaBase;
import modelo.compra.Compra;
import modelo.compra.MetodoPago;
import modelo.compra.EstadoCompra;
import modelo.validacion.ValidacionHandler;
import modelo.validacion.ValidacionContext;
// import modelo.notificacion.SistemaNotificaciones; // Ya no es necesario directamente
import modelo.mediador.MediadorCompras;
import modelo.excepciones.ValidacionException;
import modelo.excepciones.OperacionInvalidaException;

import java.util.List;
import java.util.ArrayList;

public class ProcesoCompraFacade {
    private final ProcesadorPago procesadorPago;
    private final GeneradorEntradas generadorEntradas;
    private final ValidacionHandler cadenaValidacionGlobal;
    private final MediadorCompras mediador;

    public ProcesoCompraFacade(ProcesadorPago procesadorPago,
                               GeneradorEntradas generadorEntradas,
                               ValidacionHandler cadenaValidacionGlobal,
                               MediadorCompras mediador) {
        if (procesadorPago == null) throw new IllegalArgumentException("ProcesadorPago no puede ser nulo.");
        if (generadorEntradas == null) throw new IllegalArgumentException("GeneradorEntradas no puede ser nulo.");
        if (cadenaValidacionGlobal == null) throw new IllegalArgumentException("CadenaValidacionGlobal no puede ser nula.");
        if (mediador == null) throw new IllegalArgumentException("MediadorCompras no puede ser nulo.");

        this.procesadorPago = procesadorPago;
        this.generadorEntradas = generadorEntradas;
        this.cadenaValidacionGlobal = cadenaValidacionGlobal;
        this.mediador = mediador;
    }

    public Compra ejecutarProcesoCompra(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad, MetodoPago metodoPago, String codigoDescuento) {

        // Validaciones previas de argumentos
        if (usuario == null) throw new ValidacionException("Usuario no puede ser nulo para la compra.");
        if (evento == null) throw new ValidacionException("Evento no puede ser nulo para la compra.");
        if (tipoEntrada == null) throw new ValidacionException("TipoEntrada no puede ser nulo para la compra.");
        if (cantidad <= 0) throw new ValidacionException("La cantidad de entradas debe ser mayor a cero.");
        if (metodoPago == null) throw new ValidacionException("Método de pago no puede ser nulo.");


        ValidacionContext contextoValidacion = new ValidacionContext(usuario, evento, tipoEntrada, cantidad);
        if (!cadenaValidacionGlobal.procesarValidacion(contextoValidacion)) {
            // La cadena de validación debería idealmente lanzar una excepción específica si falla,
            // o getMensajeError() debería ser más estructurado.
            // Por ahora, lanzamos una ValidacionException genérica con el mensaje del handler.
            throw new ValidacionException("Error de validación en la compra: " + cadenaValidacionGlobal.getMensajeError());
        }

        List<Entrada> entradasParaComprar = new ArrayList<>();
        double precioUnitario = tipoEntrada.getPrecio();
        for (int i = 0; i < cantidad; i++) {
            entradasParaComprar.add(new EntradaBase(tipoEntrada, evento));
        }
        double totalCalculado = precioUnitario * cantidad;

        if (codigoDescuento != null && !codigoDescuento.isEmpty()) {
            totalCalculado = aplicarDescuento(totalCalculado, codigoDescuento);
        }

        Compra nuevaCompra = new Compra(usuario, evento, tipoEntrada, cantidad, entradasParaComprar, totalCalculado, metodoPago);
        // System.out.println("ProcesoCompraFacade: Compra iniciada ID: " + nuevaCompra.getId() + " por " + totalCalculado);

        if (!procesadorPago.procesar(metodoPago, nuevaCompra.getTotalPagado())) {
            nuevaCompra.setEstado(EstadoCompra.CANCELADA);
            // mediador.notificarFalloPago(usuario, nuevaCompra); // Considerar si el mediador debe ser notificado aquí.
            throw new OperacionInvalidaException("Error en el procesamiento del pago para la compra ID: " + nuevaCompra.getId());
        }
        nuevaCompra.setEstado(EstadoCompra.CONFIRMADA);
        // System.out.println("ProcesoCompraFacade: Pago procesado y compra confirmada ID: " + nuevaCompra.getId());

        // Este es un punto crítico. Si falla, el pago se hizo pero no se asignaron entradas.
        // Requiere una lógica de compensación (ej. rollback del pago, o marcar la compra para revisión manual).
        if (!tipoEntrada.reducirDisponibilidad(cantidad)) {
            nuevaCompra.setEstado(EstadoCompra.ERROR_DISPONIBILIDAD_POST_PAGO); // Un nuevo estado podría ser útil
            // Lógica de compensación:
            // procesadorPago.revertirPago(nuevaCompra.getIdTransaccionPago(), nuevaCompra.getTotalPagado()); // Asumiendo que Compra tiene ID de trans.
            // mediador.notificarErrorCriticoCompra(nuevaCompra, "Fallo al reducir disponibilidad post-pago");
            throw new OperacionInvalidaException("Error CRÍTICO: No se pudo reducir la disponibilidad de entradas después del pago para compra ID: " + nuevaCompra.getId() + ". Se requiere intervención manual o reversión del pago.");
        }
        generadorEntradas.generar(evento, tipoEntrada, cantidad, nuevaCompra);
        // System.out.println("ProcesoCompraFacade: Entradas generadas para compra ID: " + nuevaCompra.getId());

        mediador.notificarCompraExitosa(usuario, nuevaCompra);
        nuevaCompra.generarComprobante();

        return nuevaCompra;
    }

    private double aplicarDescuento(double totalActual, String codigo) {
        // Lógica para validar el código y aplicar el descuento.
        // Esta lógica podría moverse a un servicio de promociones más sofisticado.
        if ("DESC10".equals(codigo)) {
            // System.out.println("Aplicando descuento del 10% con código: " + codigo);
            return totalActual * 0.90;
        }
        // System.out.println("Código de descuento '" + codigo + "' no válido o no aplicable.");
        return totalActual;
    }

    // Los métodos comentados abajo sugieren una descomposición previa que ahora está integrada
    // principalmente dentro de ejecutarProcesoCompra. Si se necesitaran como pasos separados
    // externamente, podrían refactorizarse para usar el nuevo manejo de excepciones también.
}
