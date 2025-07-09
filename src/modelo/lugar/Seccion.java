
package modelo.lugar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Seccion implements ComponenteLugar {
    private String nombre;
    private List<ComponenteLugar> componentes; // Puede contener Asientos u otras Secciones

    public Seccion(String nombre) {
        this.nombre = nombre;
        this.componentes = new ArrayList<>();
    }

    public void agregarComponente(ComponenteLugar componente) {
        if (componente != null) {
            this.componentes.add(componente);
        }
    }

    public void eliminarComponente(ComponenteLugar componente) {
        this.componentes.remove(componente);
    }

    public List<ComponenteLugar> getComponentes() {
        return new ArrayList<>(componentes); // Devuelve una copia para proteger la lista interna
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public int obtenerCapacidad() {
        int capacidadTotal = 0;
        for (ComponenteLugar componente : componentes) {
            capacidadTotal += componente.obtenerCapacidad();
        }
        return capacidadTotal;
    }

    @Override
    public boolean estaDisponible(Date fecha, FranjaHoraria franja) {
        if (fecha == null || franja == null) return false;
        // Una sección está disponible si al menos uno de sus componentes está disponible.
        for (ComponenteLugar componente : componentes) {
            if (componente.estaDisponible(fecha, franja)) {
                return true;
            }
        }
        return false; // Ningún componente está disponible
    }

    @Override
    public boolean reservar(Date fecha, FranjaHoraria franja) {
        // La reserva directa en una Sección es compleja: ¿qué asiento/subcomponente reservar?
        // Normalmente, la selección de un asiento específico se haría a un nivel superior.
        // Si se quisiera implementar, se podría intentar reservar el primer componente disponible.
        // Por ahora, indicamos que la operación no es soportada directamente a este nivel de granularidad
        // o que la reserva debe hacerse en un componente específico (Asiento).
        // Alternativamente, si una sección puede reservarse en su totalidad (ej. una zona no numerada)
        // entonces necesitaría su propio sistema de 'reservas' como Asiento.
        // Dado el modelo actual (Seccion contiene Asientos), la reserva es a nivel de Asiento.
        System.err.println("La reserva debe realizarse en un Asiento específico dentro de la sección '" + nombre + "'.");
        return false;
        // throw new UnsupportedOperationException("La reserva debe realizarse en un Asiento específico dentro de la sección.");
    }

    @Override
    public boolean cancelarReserva(Date fecha, FranjaHoraria franja) {
        // Similar a reservar, la cancelación directa en una Sección es compleja.
        // Se debe especificar qué asiento/subcomponente cancelar.
        System.err.println("La cancelación de reserva debe realizarse en un Asiento específico dentro de la sección '" + nombre + "'.");
        return false;
        // throw new UnsupportedOperationException("La cancelación de reserva debe realizarse en un Asiento específico.");
    }

    @Override
    public String toString() {
        return "Seccion [nombre=" + nombre + ", componentes=" + componentes.size() + ", capacidadTotal=" + obtenerCapacidad() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seccion seccion = (Seccion) o;
        return Objects.equals(nombre, seccion.nombre) &&
               Objects.equals(componentes, seccion.componentes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, componentes);
    }
}
