
package modelo.state;

import modelo.evento.Evento;
import modelo.notificacion.SistemaNotificaciones; // Para enviar notificaciones

public class EstadoEventoBorrador implements EstadoEventos {

    @Override
    public void publicar(Evento evento) {
        // Un evento en borrador puede ser publicado.
        EstadoEventos nuevoEstado = new EstadoEventoPublicado();
        evento.setEstadoActualObj(nuevoEstado); // Esto también actualiza evento.estadoActualNombre
        // System.out.println("Evento '" + evento.getNombre() + "' ha sido PUBLICADO.");

        // Notificaciones
        if (evento instanceof modelo.usuario.Subject) {
            ((modelo.usuario.Subject) evento).notificarObservers("El evento '" + evento.getNombre() + "' ha sido publicado y ya está disponible.");
        }
        SistemaNotificaciones.getInstance().notificarCambioEvento(evento, "El evento ha sido publicado y ya está disponible.");
    }

    @Override
    public void cancelar(Evento evento) {
        // Un evento en borrador puede ser cancelado (efectivamente eliminado o archivado).
        EstadoEventos nuevoEstado = new EstadoEventoCancelado();
        evento.setEstadoActualObj(nuevoEstado);
        // System.out.println("Evento '" + evento.getNombre() + "' (que estaba en borrador) ha sido CANCELADO/DESCARTADO.");
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
