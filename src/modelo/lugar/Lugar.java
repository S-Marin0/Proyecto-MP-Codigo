
package modelo.lugar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Lugar implements ComponenteLugar { // Lugar también puede ser un ComponenteLugar si se quisiera anidar Lugares
    private String nombre;
    private String direccion;
    private List<String> tiposDeEventosAdmitidos; // Nuevo campo requerido
    private List<ComponenteLugar> componentes; // Secciones u otros sub-componentes del lugar

    public Lugar(String nombre, String direccion) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.tiposDeEventosAdmitidos = new ArrayList<>();
        this.componentes = new ArrayList<>();
    }

    // Getters y Setters para los atributos básicos
    @Override
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<String> getTiposDeEventosAdmitidos() {
        return new ArrayList<>(tiposDeEventosAdmitidos); // Devuelve copia
    }

    public void setTiposDeEventosAdmitidos(List<String> tiposDeEventosAdmitidos) {
        this.tiposDeEventosAdmitidos = new ArrayList<>(tiposDeEventosAdmitidos); // Almacena copia
    }

    public void addTipoDeEventoAdmitido(String tipoEvento) {
        if (tipoEvento != null && !tipoEvento.trim().isEmpty()) {
            this.tiposDeEventosAdmitidos.add(tipoEvento);
        }
    }

    // Métodos para manejar los componentes (patrón Composite)
    public void agregarComponente(ComponenteLugar componente) {
        if (componente != null) {
            this.componentes.add(componente);
        }
    }

    public void eliminarComponente(ComponenteLugar componente) {
        this.componentes.remove(componente);
    }

    public List<ComponenteLugar> getComponentes() {
        return new ArrayList<>(componentes); // Devuelve una copia
    }

    // Implementación de los métodos de ComponenteLugar
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
        // Un lugar está disponible si al menos uno de sus componentes (secciones/asientos) está disponible.
        if (componentes.isEmpty()) {
            // Si un lugar no tiene componentes definidos, ¿se considera no disponible o disponible con capacidad 0?
            // Por ahora, si no hay componentes, no hay capacidad, por lo tanto no está "disponible" para albergar algo.
            return false;
        }
        for (ComponenteLugar componente : componentes) {
            if (componente.estaDisponible(fecha, franja)) {
                return true;
            }
        }
        return false; // Ningún componente está disponible
    }

    @Override
    public boolean reservar(Date fecha, FranjaHoraria franja) {
        // La reserva directa en un Lugar (como en Seccion) es generalmente demasiado abstracta.
        // ¿Qué parte específica del lugar se está reservando?
        // Esta operación usualmente se haría sobre un Asiento o una Seccion si esta última
        // representa un área de admisión general que se reserva como un todo.
        // Si el Lugar en sí mismo puede ser reservado (ej. para un único gran evento),
        // entonces necesitaría su propio mecanismo de reserva similar a Asiento.
        // Por ahora, se asume que las reservas son más granulares.
        System.err.println("La reserva debe realizarse en un componente específico (Asiento o Sección de admisión general) dentro del lugar '" + nombre + "'.");
        return false;
        // throw new UnsupportedOperationException("La reserva debe realizarse en un componente específico del lugar.");
    }

    @Override
    public boolean cancelarReserva(Date fecha, FranjaHoraria franja) {
        // Similar a reservar.
        System.err.println("La cancelación de reserva debe realizarse en un componente específico del lugar '" + nombre + "'.");
        return false;
        // throw new UnsupportedOperationException("La cancelación de reserva debe realizarse en un componente específico del lugar.");
    }

    @Override
    public String toString() {
        return "Lugar [nombre=" + nombre + ", direccion=" + direccion +
               ", capacidadTotal=" + obtenerCapacidad() +
               ", componentes=" + componentes.size() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lugar lugar = (Lugar) o;
        return Objects.equals(nombre, lugar.nombre) &&
               Objects.equals(direccion, lugar.direccion) &&
               Objects.equals(tiposDeEventosAdmitidos, lugar.tiposDeEventosAdmitidos) &&
               Objects.equals(componentes, lugar.componentes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, direccion, tiposDeEventosAdmitidos, componentes);
    }
}
