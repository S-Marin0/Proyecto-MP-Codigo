
package modelo.recomendacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import modelo.usuario.Usuario;
import modelo.evento.Evento;
import modelo.compra.Compra; // Necesario para acceder al historial

public class RecomendacionPorHistorial implements IEstrategiaRecomendacion {

    private static final int MAX_RECOMENDACIONES = 5;

    @Override
    public List<Evento> recomendarEventos(Usuario usuario, List<Evento> todosLosEventos) {
        if (usuario == null || todosLosEventos == null || todosLosEventos.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> categoriasPreferidas = analizarHistorialCompras(usuario);
        if (categoriasPreferidas.isEmpty()) {
            // Si no hay historial o preferencias claras, podríamos devolver vacío o usar un fallback.
            // Por ahora, devolvemos vacío si no hay categorías del historial.
            // Alternativa: usar las preferencias explícitas del usuario si las tiene.
            // List<String> prefsExplícitas = usuario.getPreferencias().stream().map(p -> p.getCategoria()).collect(Collectors.toList());
            // if(!prefsExplícitas.isEmpty()) categoriasPreferidas.addAll(prefsExplícitas); else return Collections.emptyList();
            // O delegar a otra estrategia como popularidad si el historial es vacío.
            System.out.println("RecomendacionPorHistorial: No hay suficiente historial de compras para generar recomendaciones para " + usuario.getNombre());
            return Collections.emptyList();
        }

        System.out.println("RecomendacionPorHistorial: Categorías preferidas para " + usuario.getNombre() + ": " + categoriasPreferidas);

        // Filtrar eventos por estas categorías y que el usuario no haya comprado ya.
        Set<String> idsEventosComprados = obtenerIdsEventosComprados(usuario);

        List<Evento> recomendacionesFiltradas = filtrarYOrdenarEventos(
            todosLosEventos,
            categoriasPreferidas,
            idsEventosComprados
        );

        // Limitar al número máximo de recomendaciones
        return recomendacionesFiltradas.stream().limit(MAX_RECOMENDACIONES).collect(Collectors.toList());
    }

    private List<String> analizarHistorialCompras(Usuario usuario) {
        List<Compra> historial = usuario.getHistorialCompras();
        if (historial == null || historial.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Integer> frecuenciaCategorias = new HashMap<>();
        for (Compra compra : historial) {
            Evento eventoComprado = compra.getEvento();
            if (eventoComprado != null && eventoComprado.getCategoria() != null) {
                frecuenciaCategorias.put(
                    eventoComprado.getCategoria(),
                    frecuenciaCategorias.getOrDefault(eventoComprado.getCategoria(), 0) + 1
                );
            }
        }

        // Ordenar categorías por frecuencia (más frecuentes primero)
        return frecuenciaCategorias.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Set<String> obtenerIdsEventosComprados(Usuario usuario) {
        Set<String> ids = new HashSet<>();
        if (usuario.getHistorialCompras() != null) {
            for (Compra compra : usuario.getHistorialCompras()) {
                if (compra.getEvento() != null && compra.getEvento().getId() != null) {
                    ids.add(compra.getEvento().getId());
                }
            }
        }
        return ids;
    }

    private List<Evento> filtrarYOrdenarEventos(List<Evento> todosLosEventos, List<String> categoriasPreferidas, Set<String> idsEventosComprados) {
        // Filtrar eventos que coincidan con las categorías preferidas y no hayan sido comprados
        List<Evento> eventosFiltrados = todosLosEventos.stream()
            .filter(evento -> evento.getCategoria() != null &&
                              categoriasPreferidas.contains(evento.getCategoria()) &&
                              !idsEventosComprados.contains(evento.getId()) &&
                              evento.getEstadoActual() != null && // Asumiendo que tenemos estados
                              (evento.getEstadoActual().getClass().getSimpleName().equals("EstadoEventoPublicado") || // Idealmente comparar con la instancia del estado
                               evento.getEstadoActual().getClass().getSimpleName().equals("EstadoEventoEnCurso")) // O un método evento.esComprable()
                              )
            .collect(Collectors.toList());

        // Opcional: Ordenar los eventos filtrados (ej. por fecha más próxima, popularidad dentro de esas categorías)
        // Por ahora, se devuelven en el orden en que aparecen después del filtro.
        // ejemplo de ordenación por fecha (asumiendo que getFecha() devuelve Date y es comparable)
        // Collections.sort(eventosFiltrados, Comparator.comparing(Evento::getFecha, Comparator.nullsLast(Comparator.naturalOrder())));

        return eventosFiltrados;
    }
}
