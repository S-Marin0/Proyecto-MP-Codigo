package controlador;

import java.util.Date;
// No se incluyen Lugar, Imagenes, Videos, TiposEntrada por simplicidad inicial.
// Estos podrían requerir DTOs propios o una lógica de actualización más compleja.

public class EventoDTO {
    private String nombre;
    private String descripcion;
    private String categoria;
    private Date fecha;
    private String hora;
    private Integer capacidad; // Usar Integer para permitir nulos si no se quiere actualizar

    // Constructor vacío
    public EventoDTO() {}

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    // Método para verificar si algún campo está presente para la actualización
    public boolean tieneDatosParaActualizar() {
        return nombre != null ||
               descripcion != null ||
               categoria != null ||
               fecha != null ||
               hora != null ||
               capacidad != null;
    }
}
