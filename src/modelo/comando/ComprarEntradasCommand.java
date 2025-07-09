
package modelo.comando;

import modelo.usuario.Usuario;
import modelo.evento.Evento;
import modelo.entrada.TipoEntrada;
import modelo.compra.Compra;
import modelo.compra.MetodoPago; // Necesario para ejecutar el proceso de compra
import modelo.facade.ProcesoCompraFacade;
// import modelo.template.ProcesoCompraTemplate; // Alternativa si el comando usa el Template directamente

import java.util.Date;

public class ComprarEntradasCommand implements Command {
    private final Usuario usuario;
    private final Evento evento;
    private final TipoEntrada tipoEntrada;
    private final int cantidad;
    private final MetodoPago metodoPago; // Añadido para el proceso de compra
    private String codigoDescuento; // Opcional

    private final ProcesoCompraFacade procesoCompraFacade;
    // Alternativa: private final ProcesoCompraTemplate procesoCompraTemplate;

    private Compra compraRealizada;
    private Date tiempoEjecucion;
    private boolean ejecutadoConExito = false;

    public ComprarEntradasCommand(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad,
                                  MetodoPago metodoPago, ProcesoCompraFacade procesoCompraFacade) {
        if (usuario == null || evento == null || tipoEntrada == null || metodoPago == null || procesoCompraFacade == null) {
            throw new IllegalArgumentException("Los argumentos para ComprarEntradasCommand no pueden ser nulos (excepto codigoDescuento).");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva.");
        }
        this.usuario = usuario;
        this.evento = evento;
        this.tipoEntrada = tipoEntrada;
        this.cantidad = cantidad;
        this.metodoPago = metodoPago;
        this.procesoCompraFacade = procesoCompraFacade;
        // this.procesoCompraTemplate = procesoCompraTemplate; // Si se usa el template
    }

    @Override
    public boolean execute() {
        if (ejecutadoConExito) { // Evitar re-ejecución si ya fue exitoso
            System.out.println("Comando de compra ya ejecutado exitosamente.");
            return true;
        }

        this.tiempoEjecucion = new Date();
        System.out.println(getDescription() + " ejecutándose a las " + tiempoEjecucion);

        // Usar el Facade para realizar la compra
        this.compraRealizada = procesoCompraFacade.ejecutarProcesoCompra(
            this.usuario, this.evento, this.tipoEntrada, this.cantidad, this.metodoPago, this.codigoDescuento
        );

        // Alternativa: usar el Template Method directamente
        // this.compraRealizada = procesoCompraTemplate.realizarCompraCompleta(
        //     this.usuario, this.evento, this.tipoEntrada, this.cantidad, this.metodoPago, this.codigoDescuento
        // );

        if (this.compraRealizada != null && this.compraRealizada.getEstado() == modelo.compra.EstadoCompra.CONFIRMADA) {
            this.ejecutadoConExito = true;
            System.out.println("Compra realizada con éxito. ID de Compra: " + this.compraRealizada.getId());
            // La compra ya se añade al historial del usuario dentro del facade o template.
            return true;
        } else {
            System.err.println("Falló la ejecución del comando de compra.");
            if(this.compraRealizada != null) {
                System.err.println("Estado final de la compra: " + this.compraRealizada.getEstado());
            }
            this.ejecutadoConExito = false;
            return false;
        }
    }

    @Override
    public boolean undo() {
        if (!ejecutadoConExito || this.compraRealizada == null) {
            System.err.println("No se puede deshacer el comando: no fue ejecutado exitosamente o no hay compra registrada.");
            return false;
        }

        System.out.println("Deshaciendo compra ID: " + this.compraRealizada.getId());
        // La lógica de UNDO es compleja:
        // 1. Verificar si la compra puede ser cancelada/reembolsada (políticas de tiempo, estado del evento).
        // 2. Cambiar el estado de la Compra a CANCELADA o REEMBOLSADA.
        // 3. Incrementar la disponibilidad de TipoEntrada.
        // 4. Revertir el pago (si es posible y aplica).
        // 5. Notificar al usuario.
        // Esto podría ser manejado por un CancelarCompraCommand o ReembolsarCompraCommand,
        // o una lógica específica en ProcesoCompraFacade o ProcesadorReembolso.

        // Simulación simple:
        System.out.println("Simulación: Reembolsando y cancelando la compra " + this.compraRealizada.getId());
        boolean reembolsoExitoso = true; // procesadorReembolso.procesarReembolso(this.compraRealizada, this.compraRealizada.getTotalPagado());

        if (reembolsoExitoso) {
            this.tipoEntrada.incrementarDisponibilidad(this.cantidad);
            this.compraRealizada.setEstado(modelo.compra.EstadoCompra.REEMBOLSADA); // O CANCELADA
            // Remover la compra del historial del usuario o marcarla como cancelada.
            // usuario.getHistorialCompras().remove(this.compraRealizada); // Cuidado con la concurrencia y la forma de eliminar
            System.out.println("Compra deshecha y disponibilidad de entradas restaurada.");
            ejecutadoConExito = false; // Marcar como no ejecutado para posible re-ejecución (redo)
            return true;
        } else {
            System.err.println("No se pudo procesar el reembolso para deshacer la compra.");
            return false;
        }
    }

    @Override
    public String getDescription() {
        return String.format("Comprar %d entradas de tipo '%s' para el evento '%s' por usuario '%s'",
                             cantidad, tipoEntrada.getNombre(), evento.getNombre(), usuario.getNombre());
    }

    @Override
    public Date getTiempoEjecucion() {
        return tiempoEjecucion != null ? (Date) tiempoEjecucion.clone() : null;
    }

    public Compra getCompraRealizada() {
        return compraRealizada;
    }

    public void setCodigoDescuento(String codigo) {
        // Solo permitir cambiar si el comando no ha sido ejecutado
        if (!ejecutadoConExito) {
            this.codigoDescuento = codigo;
        } else {
            System.err.println("No se puede cambiar el código de descuento de un comando ya ejecutado.");
        }
    }
}
