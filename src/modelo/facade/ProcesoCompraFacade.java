
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
import modelo.notificacion.SistemaNotificaciones;
import modelo.mediador.MediadorCompras; // Importar el Mediador

import java.util.List;
import java.util.ArrayList;

public class ProcesoCompraFacade {
    private final ProcesadorPago procesadorPago;
    private final GeneradorEntradas generadorEntradas;
    // private final SistemaNotificaciones sistemaNotificaciones; // Ahora manejado por el Mediador
    private final ValidacionHandler cadenaValidacionGlobal;
    private final MediadorCompras mediador; // Referencia al Mediador

    public ProcesoCompraFacade(ProcesadorPago procesadorPago,
                               GeneradorEntradas generadorEntradas,
                               // SistemaNotificaciones sistemaNotificaciones, // Ya no se inyecta directamente
                               ValidacionHandler cadenaValidacionGlobal,
                               MediadorCompras mediador) {
        if (procesadorPago == null) throw new IllegalArgumentException("ProcesadorPago no puede ser nulo.");
        if (generadorEntradas == null) throw new IllegalArgumentException("GeneradorEntradas no puede ser nulo.");
        // if (sistemaNotificaciones == null) throw new IllegalArgumentException("SistemaNotificaciones no puede ser nulo.");
        if (cadenaValidacionGlobal == null) throw new IllegalArgumentException("CadenaValidacionGlobal no puede ser nula.");
        if (mediador == null) throw new IllegalArgumentException("MediadorCompras no puede ser nulo.");

        this.procesadorPago = procesadorPago;
        this.generadorEntradas = generadorEntradas;
        // this.sistemaNotificaciones = sistemaNotificaciones;
        this.cadenaValidacionGlobal = cadenaValidacionGlobal;
        this.mediador = mediador;
    }

    public Compra ejecutarProcesoCompra(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad, MetodoPago metodoPago, String codigoDescuento) {

        ValidacionContext contextoValidacion = new ValidacionContext(usuario, evento, tipoEntrada, cantidad);
        if (!cadenaValidacionGlobal.procesarValidacion(contextoValidacion)) {
            System.err.println("ProcesoCompraFacade: Error de validación - " + cadenaValidacionGlobal.getMensajeError());
            return null;
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
        System.out.println("ProcesoCompraFacade: Compra iniciada ID: " + nuevaCompra.getId() + " por " + totalCalculado);

        if (!procesadorPago.procesar(metodoPago, nuevaCompra.getTotalPagado())) {
            System.err.println("ProcesoCompraFacade: Error en el procesamiento del pago para la compra ID: " + nuevaCompra.getId());
            nuevaCompra.setEstado(EstadoCompra.CANCELADA);
            // Notificar al mediador sobre el fallo de pago si es necesario coordinar algo más.
            // mediador.notificarFalloPago(usuario, nuevaCompra);
            return nuevaCompra;
        }
        nuevaCompra.setEstado(EstadoCompra.CONFIRMADA);
        System.out.println("ProcesoCompraFacade: Pago procesado y compra confirmada ID: " + nuevaCompra.getId());

        if (!tipoEntrada.reducirDisponibilidad(cantidad)) {
            System.err.println("ProcesoCompraFacade: Error CRÍTICO - No se pudo reducir la disponibilidad de entradas después del pago para compra ID: " + nuevaCompra.getId());
            nuevaCompra.setEstado(EstadoCompra.CANCELADA);
            // Lógica de compensación: revertir el pago.
            // procesadorPago.revertirPago(nuevaCompra.getIdTransaccionPago(), nuevaCompra.getTotalPagado()); // Asumiendo que Compra tiene ID de trans.
            return nuevaCompra;
        }
        generadorEntradas.generar(evento, tipoEntrada, cantidad, nuevaCompra);
        System.out.println("ProcesoCompraFacade: Entradas generadas para compra ID: " + nuevaCompra.getId());

        // En lugar de añadir al historial y notificar directamente, notificamos al Mediador.
        mediador.notificarCompraExitosa(usuario, nuevaCompra);

        // La generación del comprobante podría seguir siendo responsabilidad del facade o del mediador.
        // Por ahora, la dejamos aquí, pero el mediador también podría llamarla.
        nuevaCompra.generarComprobante();

        return nuevaCompra;
    }

    private double aplicarDescuento(double totalActual, String codigo) {
        // Lógica para validar el código y aplicar el descuento.
        // Por ejemplo, un 10% de descuento.
        if ("DESC10".equals(codigo)) {
            System.out.println("Aplicando descuento del 10% con código: " + codigo);
            return totalActual * 0.90;
        }
        System.out.println("Código de descuento '" + codigo + "' no válido o no aplicable.");
        return totalActual;
    }

    // Métodos de utilidad que podrían ser llamados externamente si es necesario,
    // pero ejecutarProcesoCompra es el punto de entrada principal.

    // public Compra iniciarCompra(Usuario usuario, Evento evento, TipoEntrada tipo, int cantidad, double total, MetodoPago metodo) {
    //     List<Entrada> entradas = new ArrayList<>();
    //     for(int i=0; i<cantidad; i++) entradas.add(new EntradaBase(tipo, evento));
    //     return new Compra(usuario, evento, tipo, cantidad, entradas, total, metodo);
    // }

    // public boolean procesarPago(Compra compra, MetodoPago metodo) {
    //     if (procesadorPago.procesar(metodo, compra.getTotalPagado())) {
    //         compra.setEstado(EstadoCompra.CONFIRMADA);
    //         return true;
    //     }
    //     return false;
    // }

    // public void generarEntradasYActualizarDisponibilidad(Compra compra) {
    //     if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
    //         compra.getTipoEntradaBase().reducirDisponibilidad(compra.getCantidad());
    //         generadorEntradas.generar(compra.getEvento(), compra.getTipoEntradaBase(), compra.getCantidad(), compra);
    //     }
    // }

    // public void notificarConfirmacion(Compra compra) {
    //     if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
    //         sistemaNotificaciones.enviarConfirmacionCompra(compra.getUsuario(), compra);
    //     }
    // }
}
