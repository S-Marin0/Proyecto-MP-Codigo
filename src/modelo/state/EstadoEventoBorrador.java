
package modelo.state;

import modelo.evento.Evento;
import modelo.notificacion.SistemaNotificaciones; // Para enviar notificaciones

public class EstadoEventoBorrador implements EstadoEventos {

    @Override
    public void publicar(Evento evento) {
        // Un evento en borrador puede ser publicado.
        // Validaciones adicionales podrían ir aquí (ej. ¿tiene toda la info necesaria?)
        evento.setEstado(new EstadoEventoPublicado());
        System.out.println("Evento '" + evento.getNombre() + "' ha sido PUBLICADO.");
        // Notificar sobre la publicación (ej. a seguidores del organizador o a un canal general)
        // SistemaNotificaciones.getInstance().notificarObservers("El evento '" + evento.getNombre() + "' acaba de ser publicado!");
        // O una notificación más específica si el evento mismo es un Subject para ciertos usuarios:
        if (evento instanceof modelo.usuario.Subject) { // Si Evento implementa Subject
            ((modelo.usuario.Subject) evento).notificarObservers("El evento '" + evento.getNombre() + "' ha sido publicado y ya está disponible.");
        }
         // También podríamos usar el SistemaNotificaciones para notificar a los interesados en este evento específico
        SistemaNotificaciones.getInstance().notificarCambioEvento(evento, "El evento ha sido publicado y ya está disponible.");
    }

    @Override
    public void cancelar(Evento evento) {
        // Un evento en borrador puede ser cancelado (efectivamente eliminado o archivado).
        evento.setEstado(new EstadoEventoCancelado());
        System.out.println("Evento '" + evento.getNombre() + "' (que estaba en borrador) ha sido CANCELADO/DESCARTADO.");
        // Generalmente no se notifica masivamente la cancelación de un borrador,
        // a menos que el organizador quiera ser notificado.
    }

    @Override
    public void iniciar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' está en estado BORRADOR y no puede iniciarse directamente.");
        // No cambia de estado.
    }

    @Override
    public void finalizar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' está en estado BORRADOR y no puede finalizarse.");
        // No cambia de estado.
    }

    @Override
    public String toString() {
        return "Borrador";
    }
}
