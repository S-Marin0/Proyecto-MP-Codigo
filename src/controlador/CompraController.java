package controlador;

import modelo.usuario.Asistente;
import modelo.evento.Evento;
import modelo.entrada.TipoEntrada;
import modelo.compra.Compra; // Aunque el método devuelva void/boolean, es conceptualmente sobre Compra
import modelo.compra.MetodoPago;
import modelo.facade.ProcesoCompraFacade;
import modelo.reembolso.ProcesadorReembolso; // Para cancelación y reembolso

public class CompraController {
    private final ProcesoCompraFacade procesoCompraFacade;
    private final ProcesadorReembolso procesadorReembolso; // Necesario para cancelación/reembolso

    public CompraController(ProcesoCompraFacade procesoCompraFacade, ProcesadorReembolso procesadorReembolso) {
        if (procesoCompraFacade == null) {
            throw new IllegalArgumentException("ProcesoCompraFacade no puede ser nulo.");
        }
        if (procesadorReembolso == null) {
            throw new IllegalArgumentException("ProcesadorReembolso no puede ser nulo.");
        }
        this.procesoCompraFacade = procesoCompraFacade;
        this.procesadorReembolso = procesadorReembolso;
    }

    // Retorna void porque la ejecución del comando es manejada por el Asistente y su invoker.
    // El resultado (Compra) se podría obtener consultando el historial del Asistente o si el comando lo expone.
    public void procesarNuevaCompra(Asistente asistente, Evento evento, TipoEntrada tipoEntrada,
                                    int cantidad, MetodoPago metodoPago, String codigoDescuento) {
        if (asistente == null || evento == null || tipoEntrada == null || cantidad <= 0 || metodoPago == null) {
            System.err.println("CompraController: Datos inválidos para procesar la compra.");
            // Podría devolver un booleano de fallo o lanzar excepción
            return;
        }

        System.out.println("CompraController: Iniciando proceso de compra para Asistente: " + asistente.getNombre() +
                           ", Evento: " + evento.getNombre() + ", TipoEntrada: " + tipoEntrada.getNombre() +
                           ", Cantidad: " + cantidad +
                           (codigoDescuento != null && !codigoDescuento.isEmpty() ? ", Código Descuento: " + codigoDescuento : ""));

        // Llama al método sobrecargado en Asistente que toma todos los parámetros
        asistente.solicitarCompra(evento, tipoEntrada, cantidad, metodoPago, this.procesoCompraFacade, codigoDescuento);

        System.out.println("CompraController: Solicitud de compra enviada al sistema para Asistente: " + asistente.getNombre());
        // No se devuelve la Compra directamente; se asume que el proceso es manejado por el Command y Facade.
        // La UI/Cliente consultaría el estado o historial para ver el resultado.
    }

    public void solicitarCancelacionCompra(Asistente asistente, Compra compra, String motivo) {
        if (asistente == null || compra == null || motivo == null || motivo.trim().isEmpty()) {
            System.err.println("CompraController: Datos inválidos para solicitar cancelación.");
            return;
        }
        System.out.println("CompraController: Solicitando cancelación de compra ID " + compra.getId() + " para asistente " + asistente.getNombre());
        asistente.cancelarCompra(compra, motivo, this.procesadorReembolso);
    }

    public void solicitarReembolsoDeCompra(Asistente asistente, Compra compra, String motivo) {
         if (asistente == null || compra == null || motivo == null || motivo.trim().isEmpty()) {
            System.err.println("CompraController: Datos inválidos para solicitar reembolso.");
            return;
        }
        System.out.println("CompraController: Solicitando reembolso de compra ID " + compra.getId() + " para asistente " + asistente.getNombre());
        asistente.solicitarReembolso(compra, motivo, this.procesadorReembolso);
    }
}
