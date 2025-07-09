
package modelo.notificacion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelo.usuario.Observer;
import modelo.usuario.Usuario; // Necesario para enviar notificaciones directas y para recomendaciones
import modelo.evento.Evento; // Necesario para notificar cambios de evento y recomendaciones
import modelo.compra.Compra; // Para notificar confirmación de compra

// Implementa Subject para las notificaciones generales (broadcast)
public class SistemaNotificaciones implements modelo.usuario.Subject {
    private static SistemaNotificaciones instancia;

    // Para notificaciones generales (broadcast a todos los que se suscriban globalmente)
    private final List<Observer> observadoresGlobales = new ArrayList<>();

    // Para notificaciones específicas de eventos (observers suscritos a un eventoId particular)
    private final Map<String, List<Observer>> observadoresPorEventoId = new HashMap<>();

    private SistemaNotificaciones() {}

    public static synchronized SistemaNotificaciones getInstance() {
        if (instancia == null) {
            instancia = new SistemaNotificaciones();
        }
        return instancia;
    }

    // Métodos de Subject para observadores globales
    @Override
    public void registrarObserver(Observer o) {
        if (o != null && !observadoresGlobales.contains(o)) {
            observadoresGlobales.add(o);
            System.out.println("Observer global registrado: " + o.getClass().getSimpleName());
        }
    }

    @Override
    public void eliminarObserver(Observer o) {
        observadoresGlobales.remove(o);
        System.out.println("Observer global eliminado: " + o.getClass().getSimpleName());
    }

    @Override
    public void notificarObservers(String mensaje) { // Notifica a TODOS los observadores globales
        System.out.println("SistemaNotificaciones: Enviando notificación global: '" + mensaje + "' a " + observadoresGlobales.size() + " observer(s).");
        for (Observer observer : new ArrayList<>(observadoresGlobales)) { // Iterar sobre copia por si hay modificaciones concurrentes
            observer.actualizar(mensaje);
        }
    }

    // Métodos para suscripción a eventos específicos
    public void suscribirAEvento(String eventoId, Observer observer) {
        if (eventoId == null || eventoId.trim().isEmpty() || observer == null) return;
        observadoresPorEventoId.putIfAbsent(eventoId, new ArrayList<>());
        List<Observer> observersDelEvento = observadoresPorEventoId.get(eventoId);
        if (!observersDelEvento.contains(observer)) {
            observersDelEvento.add(observer);
            System.out.println("Observer " + observer.getClass().getSimpleName() + " suscrito al evento ID: " + eventoId);
        }
    }

    public void desuscribirDeEvento(String eventoId, Observer observer) {
        if (eventoId == null || observer == null) return;
        List<Observer> observersDelEvento = observadoresPorEventoId.get(eventoId);
        if (observersDelEvento != null) {
            observersDelEvento.remove(observer);
            System.out.println("Observer " + observer.getClass().getSimpleName() + " desuscrito del evento ID: " + eventoId);
            if (observersDelEvento.isEmpty()) {
                observadoresPorEventoId.remove(eventoId); // Limpiar mapa si no quedan observers
            }
        }
    }

    // Métodos de notificación específicos
    public void enviarNotificacionDirecta(Usuario usuario, String mensaje) {
        if (usuario == null || mensaje == null) return;
        System.out.println("SistemaNotificaciones: Enviando notificación directa a " + usuario.getNombre() + ": '" + mensaje + "'");
        usuario.actualizar(mensaje); // Usuario implementa Observer
    }

    public void notificarCambioEvento(Evento evento, String mensajeCambio) {
        if (evento == null || evento.getId() == null || mensajeCambio == null) return;

        String mensajeCompleto = "Actualización sobre el evento '" + evento.getNombre() + "': " + mensajeCambio;
        System.out.println("SistemaNotificaciones: Notificando cambio en evento ID " + evento.getId() + ": '" + mensajeCambio + "'");

        List<Observer> observersDelEvento = observadoresPorEventoId.get(evento.getId());
        if (observersDelEvento != null && !observersDelEvento.isEmpty()) {
            System.out.println("Enviando a " + observersDelEvento.size() + " observer(s) específicos del evento.");
            for (Observer observer : new ArrayList<>(observersDelEvento)) {
                observer.actualizar(mensajeCompleto);
            }
        } else {
            System.out.println("No hay observers específicos suscritos al evento ID " + evento.getId() + " para notificar cambio.");
        }
        // Adicionalmente, se podría enviar una notificación global si el cambio es muy importante,
        // o a usuarios que hayan comprado entradas para este evento (requeriría acceso al sistema de compras).
        // notificarObservers("Información importante sobre el evento: " + evento.getNombre() + " - " + mensajeCambio);
    }

    public void enviarRecomendaciones(Usuario usuario, List<Evento> recomendaciones) {
        if (usuario == null || recomendaciones == null || recomendaciones.isEmpty()) return;

        StringBuilder mensajeBuilder = new StringBuilder("Hola " + usuario.getNombre() + ", ¡tenemos algunas recomendaciones de eventos para ti!\n");
        for (Evento evento : recomendaciones) {
            mensajeBuilder.append("- ").append(evento.getNombre()).append(" (").append(evento.getCategoria()).append(") el ").append(evento.getFecha()).append("\n");
        }
        System.out.println("SistemaNotificaciones: Enviando recomendaciones a " + usuario.getNombre());
        usuario.actualizar(mensajeBuilder.toString());
    }

    public void enviarConfirmacionCompra(Usuario usuario, Compra compra) {
        if (usuario == null || compra == null) return;
        String mensaje = String.format("¡Gracias por tu compra, %s! Tu compra ID %s para el evento '%s' (Total: %.2f €) ha sido confirmada.",
                                       usuario.getNombre(), compra.getId(), compra.getEvento().getNombre(), compra.getTotalPagado());
        System.out.println("SistemaNotificaciones: Enviando confirmación de compra a " + usuario.getNombre());
        usuario.actualizar(mensaje);

        // El usuario (Asistente) podría querer suscribirse automáticamente a notificaciones de este evento tras la compra.
        if (compra.getEvento() != null && compra.getEvento().getId() != null) {
            suscribirAEvento(compra.getEvento().getId(), usuario);
        }
    }

    // Nuevo método para notificar reembolso, usado por ProcesadorReembolso
    public void enviarNotificacionReembolso(Usuario usuario, Compra compra, double montoReembolsado) {
        if (usuario == null || compra == null) return;
        String mensaje = String.format("Hola %s, se ha procesado un reembolso de %.2f € para tu compra ID %s del evento '%s'.",
                                       usuario.getNombre(), montoReembolsado, compra.getId(), compra.getEvento().getNombre());
        System.out.println("SistemaNotificaciones: Enviando notificación de reembolso a " + usuario.getNombre());
        usuario.actualizar(mensaje);
    }
}
