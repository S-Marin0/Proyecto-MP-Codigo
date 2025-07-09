
package modelo.lugar;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Asiento implements ComponenteLugar {
    private String nombre;
    // Mapa para almacenar las franjas horarias reservadas para cada fecha.
    // La clave es la Date (representando un día específico).
    // El valor es un Set de FranjaHoraria que están reservadas para ese día.
    private Map<Date, Set<FranjaHoraria>> reservas;

    public Asiento(String nombre) {
        this.nombre = nombre;
        this.reservas = new HashMap<>();
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public int obtenerCapacidad() {
        return 1; // Un asiento individual siempre tiene capacidad 1.
    }

    @Override
    public boolean estaDisponible(Date fecha, FranjaHoraria franja) {
        if (fecha == null || franja == null) {
            // Considerar lanzar IllegalArgumentException o manejarlo según la política de errores
            return false;
        }
        Set<FranjaHoraria> franjasReservadasParaFecha = reservas.get(fecha);
        if (franjasReservadasParaFecha == null) {
            return true; // No hay ninguna reserva para esta fecha, por lo tanto está disponible.
        }
        return !franjasReservadasParaFecha.contains(franja); // Disponible si NO está en el set de reservadas.
    }

    @Override
    public boolean reservar(Date fecha, FranjaHoraria franja) {
        if (fecha == null || franja == null) {
            // Considerar lanzar IllegalArgumentException
            return false;
        }
        if (!estaDisponible(fecha, franja)) {
            return false; // Ya está reservado o no es una entrada válida
        }

        reservas.putIfAbsent(fecha, new HashSet<>());
        // Se añade la franja al conjunto de franjas reservadas para esa fecha.
        // El método add de Set devuelve true si el elemento no estaba ya presente.
        return reservas.get(fecha).add(franja);
    }

    @Override
    public boolean cancelarReserva(Date fecha, FranjaHoraria franja) {
        if (fecha == null || franja == null) {
            // Considerar lanzar IllegalArgumentException
            return false;
        }
        Set<FranjaHoraria> franjasReservadasParaFecha = reservas.get(fecha);
        if (franjasReservadasParaFecha == null) {
            return false; // No hay reservas para esta fecha, nada que cancelar.
        }
        // El método remove de Set devuelve true si el elemento estaba presente y fue eliminado.
        boolean  removido = franjasReservadasParaFecha.remove(franja);

        // Si después de remover, el conjunto de franjas para esa fecha queda vacío,
        // podemos opcionalmente remover la entrada del mapa para mantenerlo limpio.
        if (franjasReservadasParaFecha.isEmpty()) {
            reservas.remove(fecha);
        }
        return removido;
    }

    // Método para obtener una copia de las reservas, podría ser útil para debugging o visualización.
    // No es parte de ComponenteLugar, pero puede ser específico de Asiento.
    public Map<Date, Set<FranjaHoraria>> getReservas() {
        // Devolver una copia para evitar modificaciones externas no controladas.
        Map<Date, Set<FranjaHoraria>> copiaReservas = new HashMap<>();
        for (Map.Entry<Date, Set<FranjaHoraria>> entry : reservas.entrySet()) {
            copiaReservas.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copiaReservas;
    }

    @Override
    public String toString() {
        return "Asiento [nombre=" + nombre + "]";
    }
}
