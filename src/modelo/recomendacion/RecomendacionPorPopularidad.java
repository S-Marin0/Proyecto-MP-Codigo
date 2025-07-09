
package modelo.recomendacion;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet; // Para los IDs de eventos comprados
import java.util.stream.Collectors;

import modelo.usuario.Usuario;
import modelo.evento.Evento;
import modelo.compra.Compra; // Para obtener historial y no recomendar ya comprados

public class RecomendacionPorPopularidad implements IEstrategiaRecomendacion {

    private static final int MAX_RECOMENDACIONES = 5;

    @Override
    public List<Evento> recomendarEventos(Usuario usuario, List<Evento> todosLosEventos) {
        if (todosLosEventos == null || todosLosEventos.isEmpty()) {
            return Collections.emptyList();
        }

        // Obtener IDs de eventos ya comprados por el usuario para no volver a recomendarlos.
        Set<String> idsEventosComprados = new HashSet<>();
        if (usuario != null && usuario.getHistorialCompras() != null) {
            for (Compra compra : usuario.getHistorialCompras()) {
                if (compra.getEvento() != null && compra.getEvento().getId() != null) {
                    idsEventosComprados.add(compra.getEvento().getId());
                }
            }
        }

        // Filtrar eventos que no hayan sido comprados y estén en estado comprable
        List<Evento> eventosCandidatos = todosLosEventos.stream()
            .filter(evento -> !idsEventosComprados.contains(evento.getId()) &&
                               evento.getEstadoActual() != null &&
                               (evento.getEstadoActual().getClass().getSimpleName().equals("EstadoEventoPublicado") ||
                                evento.getEstadoActual().getClass().getSimpleName().equals("EstadoEventoEnCurso")))
            .collect(Collectors.toList());


        // Ordenar los eventos candidatos por popularidad (mayor número de entradas vendidas primero)
        // Si las entradas vendidas son iguales, se podría usar un segundo criterio (ej. fecha más próxima)
        eventosCandidatos.sort(Comparator.comparingInt(Evento::getEntradasVendidas).reversed());
        // Ejemplo de segundo criterio (fecha más próxima, si getFecha() existe y es comparable)
        // .thenComparing(Evento::getFecha, Comparator.nullsLast(Comparator.naturalOrder())));


        // Aplicar "tendencias" podría ser simplemente tomar el top N, o una lógica más compleja.
        // Por ahora, tomamos el top N.
        List<Evento> recomendacionesFinales = eventosCandidatos.stream()
                                                .limit(MAX_RECOMENDACIONES)
                                                .collect(Collectors.toList());

        if (!recomendacionesFinales.isEmpty()) {
             System.out.println("RecomendacionPorPopularidad: Recomendando " + recomendacionesFinales.size() + " eventos populares para " + (usuario != null ? usuario.getNombre() : "usuario genérico") + ".");
        } else {
             System.out.println("RecomendacionPorPopularidad: No se encontraron eventos populares adecuados para recomendar.");
        }

        return recomendacionesFinales;
    }

    // Los métodos privados originales `ordenarPorPopularidad` y `aplicarTendencias`
    // se han integrado en la lógica de `recomendarEventos` para esta implementación.
    // Podrían extraerse si la lógica se vuelve más compleja.
}
