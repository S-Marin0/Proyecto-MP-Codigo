
package modelo.comando;

import java.util.Date;
// import java.util.List; // No se usa directamente List<Entrada> aquí
import modelo.compra.Compra;
import modelo.compra.EstadoCompra;
// import modelo.entrada.Entrada; // No se usa directamente
import modelo.reembolso.ProcesadorReembolso;

public class CancelarCompraCommand implements Command {
    private final Compra compraACancelar;
    private final String motivoCancelacion;
    private final ProcesadorReembolso procesadorReembolso; // Para manejar el reembolso asociado

    private Date tiempoEjecucion;
    private boolean ejecutadoConExito = false;
    private EstadoCompra estadoAnteriorCompra;
    // private double montoReembolsado; // El procesador de reembolso se encarga del monto
    // private List<Entrada> entradasCanceladas; // La Compra ya tiene las entradas

    public CancelarCompraCommand(Compra compra, String motivo, ProcesadorReembolso procesador) {
        if (compra == null || motivo == null || motivo.trim().isEmpty() || procesador == null) {
            throw new IllegalArgumentException("Argumentos para CancelarCompraCommand no pueden ser nulos o vacíos.");
        }
        this.compraACancelar = compra;
        this.motivoCancelacion = motivo;
        this.procesadorReembolso = procesador;
    }

    @Override
    public boolean execute() {
        if (ejecutadoConExito) {
            System.out.println("Comando de cancelación de compra ya ejecutado exitosamente.");
            return true;
        }
        this.tiempoEjecucion = new Date();
        System.out.println(getDescription() + " ejecutándose a las " + tiempoEjecucion);

        if (compraACancelar.getEstado() != EstadoCompra.CONFIRMADA) {
            System.err.println("No se puede cancelar la compra ID " + compraACancelar.getId() +
                               " porque no está en estado CONFIRMADA. Estado actual: " + compraACancelar.getEstado());
            return false;
        }

        this.estadoAnteriorCompra = compraACancelar.getEstado();

        // Usar el ProcesadorReembolso para manejar la lógica del reembolso.
        // El motivo aquí podría ser más específico, como "CANCELACION_POR_USUARIO_COMMAND"
        boolean reembolsoProcesado = procesadorReembolso.procesarReembolso(
            compraACancelar,
            this.motivoCancelacion, // O un motivo estandarizado para cancelación vía comando
            "MISMO_METODO_PAGO" // Método de reembolso preferido (ejemplo)
        );

        if (reembolsoProcesado) {
            // Si el reembolso fue exitoso, el estado de la compra ya fue cambiado a REEMBOLSADA
            // por el ProcesadorReembolso. Aquí confirmamos o ajustamos si es necesario.
            // compraACancelar.setEstado(EstadoCompra.CANCELADA); // O REEMBOLSADA según la lógica

            // Incrementar disponibilidad de las entradas
            // Esta es una responsabilidad CRUCIAL de la cancelación.
            if (compraACancelar.getTipoEntradaBase() != null) {
                boolean disponibilidadIncrementada = compraACancelar.getTipoEntradaBase().incrementarDisponibilidad(compraACancelar.getCantidad());
                if (!disponibilidadIncrementada) {
                    // Esto es un problema: el reembolso se hizo pero no se pudo devolver la entrada al stock.
                    // Requiere manejo de error/compensación.
                    System.err.println("Error CRÍTICO al cancelar compra: No se pudo incrementar la disponibilidad de TipoEntrada " +
                                       compraACancelar.getTipoEntradaBase().getNombre());
                    // Se podría intentar revertir el reembolso o marcar la situación para revisión manual.
                } else {
                     System.out.println("Disponibilidad de TipoEntrada '" + compraACancelar.getTipoEntradaBase().getNombre() + "' incrementada en " + compraACancelar.getCantidad());
                }
            }

            this.ejecutadoConExito = true;
            System.out.println("Cancelación de compra ID " + compraACancelar.getId() + " procesada con éxito.");
            return true;
        } else {
            System.err.println("Falló el procesamiento del reembolso para la cancelación de compra ID " + compraACancelar.getId());
            // No cambiar el estado de la compra si el reembolso falla.
            return false;
        }
    }

    @Override
    public boolean undo() {
        if (!ejecutadoConExito) {
            System.err.println("No se puede deshacer la cancelación: el comando no fue ejecutado exitosamente.");
            return false;
        }
        // Deshacer una cancelación implicaría:
        // 1. Revertir el reembolso (si es posible, muy complejo).
        // 2. Cambiar el estado de la Compra de nuevo a estadoAnteriorCompra (CONFIRMADA).
        // 3. Reducir la disponibilidad de TipoEntrada nuevamente.
        // Esto es muy complejo y a menudo no se implementa un "undo" para cancelaciones
        // que involucran transacciones financieras reales.
        // En su lugar, se podría crear una nueva compra si el usuario se arrepiente.
        System.out.println("Deshacer la cancelación de compra (ID: " + compraACancelar.getId() + ") no está soportado o es una operación compleja.");
        // Simulación simple de revertir estado y disponibilidad (sin revertir el reembolso monetario)
        // compraACancelar.setEstado(this.estadoAnteriorCompra);
        // if (compraACancelar.getTipoEntradaBase() != null) {
        //     compraACancelar.getTipoEntradaBase().reducirDisponibilidad(compraACancelar.getCantidad());
        // }
        // ejecutadoConExito = false; // Permitir re-ejecución (redo de la cancelación)
        // return true;
        return false; // Por ahora, no soportamos undo de cancelación.
    }

    @Override
    public String getDescription() {
        return "Cancelar compra ID: " + compraACancelar.getId() + " por motivo: " + motivoCancelacion;
    }

    @Override
    public Date getTiempoEjecucion() {
        return tiempoEjecucion != null ? (Date) tiempoEjecucion.clone() : null;
    }
}
