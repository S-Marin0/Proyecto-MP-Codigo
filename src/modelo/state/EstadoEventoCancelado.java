
package modelo.state;

import modelo.evento.Evento;
// import modelo.notificacion.SistemaNotificaciones;

public class EstadoEventoCancelado implements EstadoEventos {

    @Override
    public void publicar(Evento evento) {
        // En algunos sistemas, un evento cancelado podría ser "re-programado" o "re-publicado".
        // Esto implicaría cambiar su estado de nuevo a Borrador o Publicado,
        // posiblemente con nueva fecha y notificación a interesados.
        // Por ahora, mantenemos la cancelación como un estado más definitivo.
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' está CANCELADO y no puede ser publicado directamente. Considere crear un nuevo evento o una función de 'reprogramar'.");
    }

    @Override
    public void cancelar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' ya está CANCELADO.");
    }

    @Override
    public void iniciar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' está CANCELADO y no puede iniciarse.");
    }

    @Override
    public void finalizar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' está CANCELADO y no puede finalizarse.");
    }

    @Override
    public String toString() {
        return "Cancelado";
    }
}
