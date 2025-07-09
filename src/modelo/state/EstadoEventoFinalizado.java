
package modelo.state;

import modelo.evento.Evento;
// import modelo.notificacion.SistemaNotificaciones; // No se suelen enviar notificaciones desde este estado

public class EstadoEventoFinalizado implements EstadoEventos {

    @Override
    public void publicar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' ya ha FINALIZADO y no puede ser publicado.");
    }

    @Override
    public void cancelar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' ya ha FINALIZADO y no puede ser cancelado.");
        // Podría haber una lógica de "archivar" o "eliminar datos" post-finalización, pero no es "cancelar".
    }

    @Override
    public void iniciar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' ya ha FINALIZADO y no puede volver a iniciarse.");
    }

    @Override
    public void finalizar(Evento evento) {
        System.out.println("Acción no permitida: El evento '" + evento.getNombre() + "' ya está FINALIZADO.");
    }

    @Override
    public String toString() {
        return "Finalizado";
    }
}
