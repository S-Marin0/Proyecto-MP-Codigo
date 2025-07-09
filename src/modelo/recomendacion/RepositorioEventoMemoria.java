package modelo.recomendacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap; // Para almacenar por ID para fácil acceso/actualización
import java.util.concurrent.ConcurrentHashMap; // Si se prevé acceso concurrente

import modelo.evento.Evento;

public class RepositorioEventoMemoria implements IRepositorioEvento {

    // Usamos un Map para poder añadir/quitar/actualizar eventos por ID fácilmente si es necesario.
    // Para obtenerlos todos, simplemente devolvemos los valores del mapa.
    // ConcurrentHashMap si se espera modificación concurrente de la lista de eventos.
    private final Map<String, Evento> eventosEnMemoria;

    public RepositorioEventoMemoria() {
        this.eventosEnMemoria = new ConcurrentHashMap<>(); // Seguro para concurrencia básica
    }

    public RepositorioEventoMemoria(List<Evento> eventosIniciales) {
        this(); // Llama al constructor por defecto
        if (eventosIniciales != null) {
            for (Evento evento : eventosIniciales) {
                this.agregarEvento(evento);
            }
        }
    }

    public void agregarEvento(Evento evento) {
        if (evento != null && evento.getId() != null) {
            this.eventosEnMemoria.put(evento.getId(), evento);
        } else {
            System.err.println("RepositorioEventoMemoria: No se puede agregar evento nulo o sin ID.");
        }
    }

    public void eliminarEvento(String eventoId) {
        if (eventoId != null) {
            this.eventosEnMemoria.remove(eventoId);
        }
    }

    public Evento obtenerEventoPorId(String eventoId) {
        if (eventoId != null) {
            return this.eventosEnMemoria.get(eventoId);
        }
        return null;
    }

    @Override
    public List<Evento> obtenerEventos() {
        // Devuelve una nueva lista que contiene todos los eventos para evitar
        // modificaciones externas de la lista interna del repositorio.
        if (eventosEnMemoria.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(eventosEnMemoria.values());
    }

    public void limpiarRepositorio() {
        this.eventosEnMemoria.clear();
    }

    public int contarEventos() {
        return this.eventosEnMemoria.size();
    }
}
