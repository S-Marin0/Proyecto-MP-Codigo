
package modelo.entrada;

import modelo.evento.Evento;

public class EntradaBase implements Entrada {
    private TipoEntrada tipoEntradaConfig; // Configuración base del tipo de entrada (precio, nombre, etc.)
    private Evento eventoAsociado; // Evento al que pertenece esta entrada específica

    public EntradaBase(TipoEntrada tipoEntradaConfig, Evento eventoAsociado) {
        if (tipoEntradaConfig == null) {
            throw new IllegalArgumentException("La configuración del tipo de entrada no puede ser nula.");
        }
        if (eventoAsociado == null) {
            throw new IllegalArgumentException("El evento asociado no puede ser nulo.");
        }
        this.tipoEntradaConfig = tipoEntradaConfig;
        this.eventoAsociado = eventoAsociado;
    }

    @Override
    public double getPrecio() {
        return tipoEntradaConfig.getPrecio();
    }

    @Override
    public String getDescripcion() {
        // Construye una descripción más informativa
        return "Entrada '" + tipoEntradaConfig.getNombre() +
               "' para el evento '" + eventoAsociado.getNombre() +
               "' (" + tipoEntradaConfig.getDescripcionTipo() + ")";
    }

    @Override
    public TipoEntrada getTipoEntrada() {
        // Devuelve la configuración del tipo de entrada
        return tipoEntradaConfig;
    }

    public Evento getEventoAsociado() {
        return eventoAsociado;
    }

    // Podríamos añadir un ID único para cada instancia de Entrada si fuera necesario,
    // por ejemplo, un UUID generado en el constructor.
    // private final String idInstanciaEntrada;
    // public String getIdInstanciaEntrada() { return idInstanciaEntrada; }
}
