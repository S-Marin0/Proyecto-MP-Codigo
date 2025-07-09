
package modelo.state;

import modelo.evento.Evento;
import modelo.notificacion.SistemaNotificaciones;

public class EstadoEventoEnCurso implements EstadoEventos {

    @Override
    public void publicar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' ya está EN CURSO y no puede ser re-publicado.");
    }

    @Override
    public void cancelar(Evento evento) {
        // Cancelar un evento que ya está en curso es usualmente una acción drástica.
        // Podría tener implicaciones diferentes a cancelar un evento solo publicado.
        evento.setEstado(new EstadoEventoCancelado());
        System.out.println("Evento '" + evento.getNombre() + "' (que estaba EN CURSO) ha sido CANCELADO.");
        SistemaNotificaciones.getInstance().notificarCambioEvento(evento, "El evento ha sido cancelado de forma imprevista mientras estaba en curso.");
        // Aquí también se activarían procesos de reembolso y comunicación urgente.
    }

    @Override
    public void iniciar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' ya está EN CURSO.");
    }

    @Override
    public void finalizar(Evento evento) {
        // Un evento en curso puede ser finalizado.
        evento.setEstado(new EstadoEventoFinalizado());
        System.out.println("Evento '" + evento.getNombre() + "' ha FINALIZADO.");
        SistemaNotificaciones.getInstance().notificarCambioEvento(evento, "El evento ha concluido.");
        // Podrían realizarse acciones post-evento aquí, como enviar encuestas.
    }

    @Override
    public String toString() {
        return "En Curso";
    }
}
