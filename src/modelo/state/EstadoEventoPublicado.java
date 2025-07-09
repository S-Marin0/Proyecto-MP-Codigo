
package modelo.state;

import modelo.evento.Evento;
import modelo.notificacion.SistemaNotificaciones;
import java.util.Date; // Para comparar fechas

public class EstadoEventoPublicado implements EstadoEventos {

    @Override
    public void publicar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' ya está PUBLICADO.");
        // No cambia de estado.
    }

    @Override
    public void cancelar(Evento evento) {
        // Un evento publicado puede ser cancelado.
        // Consideraciones: ¿Hay entradas vendidas? ¿Políticas de reembolso?
        // Esta lógica de negocio (reembolsos, etc.) debería ser manejada por otro componente
        // que sería invocado ANTES o COMO PARTE de cambiar el estado.
        // Por ahora, solo cambiamos el estado.
        EstadoEventos nuevoEstado = new EstadoEventoCancelado();
        evento.setEstadoActualObj(nuevoEstado);
        // System.out.println("Evento '" + evento.getNombre() + "' ha sido CANCELADO.");
        SistemaNotificaciones.getInstance().notificarCambioEvento(evento, "El evento ha sido cancelado.");
        // Aquí se debería iniciar el proceso de notificación a los compradores y el proceso de reembolso.
    }

    @Override
    public void iniciar(Evento evento) {
        // Un evento publicado puede iniciar si su fecha/hora ha llegado.
        Date fechaActual = new Date();
        if (evento.getFecha() != null && (fechaActual.after(evento.getFecha()) || fechaActual.equals(evento.getFecha()))) {
            // Aquí también se podría verificar la hora si es relevante.
            EstadoEventos nuevoEstado = new EstadoEventoEnCurso();
            evento.setEstadoActualObj(nuevoEstado);
            // System.out.println("Evento '" + evento.getNombre() + "' ha INICIADO.");
            SistemaNotificaciones.getInstance().notificarCambioEvento(evento, "El evento ha comenzado.");
        } else {
            // System.out.println("Acción no permitida: El evento '" + evento.getNombre() +
            //                    "' no puede iniciar porque su fecha de inicio (" + evento.getFecha() +
            //                    ") aún no ha llegado o es nula.");
            // Considerar lanzar una OperacionInvalidaException aquí.
            throw new modelo.excepciones.OperacionInvalidaException("El evento '" + evento.getNombre() +
                               "' no puede iniciar porque su fecha de inicio (" + evento.getFecha() +
                               ") aún no ha llegado o es nula.");
        }
    }

    @Override
    public void finalizar(Evento evento) {
        System.out.println("Acción no permitida: Un evento PUBLICADO no puede ser finalizado directamente. Debe INICIAR primero.");
        // No cambia de estado.
    }

    @Override
    public String toString() {
        return "Publicado";
    }
}
