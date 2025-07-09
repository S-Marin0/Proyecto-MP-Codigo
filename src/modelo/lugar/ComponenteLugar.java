
package modelo.lugar;

import java.util.Date;

public interface ComponenteLugar {
    String getNombre();
    int obtenerCapacidad();
    boolean estaDisponible(Date fecha, FranjaHoraria franja);
    boolean reservar(Date fecha, FranjaHoraria franja);
    boolean cancelarReserva(Date fecha, FranjaHoraria franja);
    // Podríamos añadir un método para obtener las reservas, si es necesario para la lógica externa.
    // Map<Date, Set<FranjaHoraria>> getReservas();
}
