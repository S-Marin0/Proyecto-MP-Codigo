
package modelo.comando;

import java.util.Date;
import modelo.compra.Compra;
import modelo.compra.EstadoCompra;
import modelo.reembolso.ProcesadorReembolso;

public class ReembolsarCompraCommand implements Command {
    private final Compra compraAReembolsar;
    private final String motivoReembolso;
    // montoReembolso es opcional; si no se provee, se asume reembolso total.
    // El ProcesadorReembolso podría manejar la lógica de si es parcial o total.
    private final double montoEspecificoReembolso; // -1 u otro valor sentinel si es reembolso total
    private final ProcesadorReembolso procesadorReembolso;

    private Date tiempoEjecucion;
    private boolean ejecutadoConExito = false;
    private EstadoCompra estadoAnteriorCompra;

    public ReembolsarCompraCommand(Compra compra, String motivo, ProcesadorReembolso procesador) {
        this(compra, motivo, -1, procesador); // Asume reembolso total por defecto
    }

    public ReembolsarCompraCommand(Compra compra, String motivo, double montoEspecifico, ProcesadorReembolso procesador) {
        if (compra == null || motivo == null || motivo.trim().isEmpty() || procesador == null) {
            throw new IllegalArgumentException("Argumentos para ReembolsarCompraCommand no pueden ser nulos o vacíos.");
        }
        if (montoEspecifico < 0 && montoEspecifico != -1) {
            throw new IllegalArgumentException("Monto específico de reembolso no puede ser negativo (excepto -1 para total).");
        }
        this.compraAReembolsar = compra;
        this.motivoReembolso = motivo;
        this.montoEspecificoReembolso = montoEspecifico;
        this.procesadorReembolso = procesador;
    }

    @Override
    public boolean execute() {
        if (ejecutadoConExito) {
            System.out.println("Comando de reembolso de compra ya ejecutado exitosamente.");
            return true;
        }
        this.tiempoEjecucion = new Date();
        System.out.println(getDescription() + " ejecutándose a las " + tiempoEjecucion);

        if (compraAReembolsar.getEstado() != EstadoCompra.CONFIRMADA) {
            System.err.println("No se puede reembolsar la compra ID " + compraAReembolsar.getId() +
                               " porque no está en estado CONFIRMADA. Estado actual: " + compraAReembolsar.getEstado());
            return false;
        }

        this.estadoAnteriorCompra = compraAReembolsar.getEstado();

        // El ProcesadorReembolso se encarga de la lógica de montos y comisiones.
        // Si montoEspecificoReembolso es -1, el procesador debería entenderlo como reembolso total.
        // O, el procesador podría tener un método procesarReembolsoTotal y procesarReembolsoParcial.
        // Por simplicidad, pasamos el motivo. El procesador usará compra.getTotalPagado() si es total.
        // La implementación actual de procesadorReembolso.procesarReembolso no toma monto, calcula internamente.

        boolean reembolsoProcesado = procesadorReembolso.procesarReembolso(
            compraAReembolsar,
            this.motivoReembolso,
            "MISMO_METODO_PAGO" // Método de reembolso preferido (ejemplo)
        );

        if (reembolsoProcesado) {
            // El estado de la compra ya fue cambiado a REEMBOLSADA por el ProcesadorReembolso.
            // Incrementar disponibilidad de las entradas
            if (compraAReembolsar.getTipoEntradaBase() != null) {
                boolean disponibilidadIncrementada = compraAReembolsar.getTipoEntradaBase().incrementarDisponibilidad(compraAReembolsar.getCantidad());
                if (!disponibilidadIncrementada) {
                    System.err.println("Error CRÍTICO al reembolsar compra: No se pudo incrementar la disponibilidad de TipoEntrada " +
                                       compraAReembolsar.getTipoEntradaBase().getNombre());
                } else {
                    System.out.println("Disponibilidad de TipoEntrada '" + compraAReembolsar.getTipoEntradaBase().getNombre() + "' incrementada en " + compraAReembolsar.getCantidad());
                }
            }

            this.ejecutadoConExito = true;
            System.out.println("Reembolso de compra ID " + compraAReembolsar.getId() + " procesado con éxito.");
            return true;
        } else {
            System.err.println("Falló el procesamiento del reembolso para la compra ID " + compraAReembolsar.getId());
            return false;
        }
    }

    @Override
    public boolean undo() {
        if (!ejecutadoConExito) {
            System.err.println("No se puede deshacer el reembolso: el comando no fue ejecutado exitosamente.");
            return false;
        }
        // Deshacer un reembolso es aún más complejo que deshacer una cancelación.
        // Implicaría volver a cobrar al usuario, etc. Generalmente no se soporta.
        System.out.println("Deshacer el reembolso de compra (ID: " + compraAReembolsar.getId() + ") no está soportado.");
        return false;
    }

    @Override
    public String getDescription() {
        String desc = "Reembolsar compra ID: " + compraAReembolsar.getId() + " por motivo: " + motivoReembolso;
        if (montoEspecificoReembolso != -1) {
            desc += String.format(" (Monto específico: %.2f)", montoEspecificoReembolso);
        } else {
            desc += " (Reembolso total)";
        }
        return desc;
    }

    @Override
    public Date getTiempoEjecucion() {
        return tiempoEjecucion != null ? (Date) tiempoEjecucion.clone() : null;
    }
}
