
package modelo.evento;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import modelo.lugar.Lugar;
import modelo.usuario.Organizador;
import modelo.entrada.TipoEntrada;
import modelo.state.EstadoEventos; // Importar desde el paquete state
import modelo.state.EstadoEventoBorrador; // Importar el estado inicial concreto

public class Evento {
    private String id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private Date fecha;
    private String hora; // Se puede usar java.time.LocalTime para mayor precisión
    private Lugar lugar;
    private int capacidad;
    private Organizador organizador;
    private List<Imagen> imagenes;
    private List<Video> videosPromocionales; // Cambiado de List<String>
    private List<TipoEntrada> tiposEntrada;
    // private double precio; // El precio suele estar asociado al TipoEntrada, no directamente al evento general
    private int entradasVendidas;
    private EstadoEventos estadoActual;

    // Constructor package-private para ser usado por EventoBuilder
    Evento() {
        this.imagenes = new ArrayList<>();
        this.videosPromocionales = new ArrayList<>();
        this.tiposEntrada = new ArrayList<>();
        this.entradasVendidas = 0;
        this.estadoActual = new EstadoEventoBorrador(); // Estado inicial por defecto
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public Date getFecha() { return fecha; }
    public String getHora() { return hora; }
    public Lugar getLugar() { return lugar; }
    public int getCapacidad() { return capacidad; }
    public Organizador getOrganizador() { return organizador; }
    public List<Imagen> getImagenes() { return new ArrayList<>(imagenes); } // Devolver copia
    public List<Video> getVideosPromocionales() { return new ArrayList<>(videosPromocionales); } // Devolver copia
    public List<TipoEntrada> getTiposEntrada() { return new ArrayList<>(tiposEntrada); } // Devolver copia
    public int getEntradasVendidas() { return entradasVendidas; }
    public EstadoEventos getEstadoActual() { return estadoActual; }

    // Setters (package-private, para ser usados principalmente por el Builder)
    void setId(String id) { this.id = id; }
    // setNombre, setDescripcion, etc. ahora son públicos para edición
    // void setNombre(String nombre) { this.nombre = nombre; }
    // void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    // void setCategoria(String categoria) { this.categoria = categoria; }
    // void setFecha(Date fecha) { this.fecha = fecha; }
    // void setHora(String hora) { this.hora = hora; }
    // void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    void setLugar(Lugar lugar) { this.lugar = lugar; }
    void setOrganizador(Organizador organizador) { this.organizador = organizador; }
    void setImagenes(List<Imagen> imagenes) { this.imagenes = imagenes != null ? new ArrayList<>(imagenes) : new ArrayList<>(); }
    void setVideosPromocionales(List<Video> videosPromocionales) { this.videosPromocionales = videosPromocionales != null ? new ArrayList<>(videosPromocionales) : new ArrayList<>(); }
    void setTiposEntrada(List<TipoEntrada> tiposEntrada) { this.tiposEntrada = tiposEntrada != null ? new ArrayList<>(tiposEntrada) : new ArrayList<>(); }
    void setEntradasVendidas(int entradasVendidas) { this.entradasVendidas = entradasVendidas; }


    // Setters públicos para campos editables
    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            this.nombre = nombre;
        }
        // Considerar lanzar ValidacionException si se intenta poner nombre vacío.
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion; // Descripción puede ser vacía/nula intencionalmente
    }

    public void setCategoria(String categoria) {
        if (categoria != null && !categoria.trim().isEmpty()) {
            this.categoria = categoria;
        }
    }

    public void setFecha(Date fecha) {
        // Podría añadirse validación para que la fecha no sea en el pasado,
        // o que no sea nula si es un campo obligatorio post-creación.
        this.fecha = fecha;
    }

    public void setHora(String hora) {
        // Similar a fecha, podría tener validaciones de formato o si es obligatorio.
        this.hora = hora;
    }

    public void setCapacidad(int capacidad) {
        if (capacidad >= 0) { // Capacidad no debería ser negativa.
            // Considerar si la capacidad puede ser menor a entradasVendidas.
            // Esto podría requerir lógica de negocio más compleja o ser manejado por el estado del evento.
            this.capacidad = capacidad;
        }
    }


    public void setEstado(EstadoEventos nuevoEstado) {
        this.estadoActual = nuevoEstado;
        // System.out.println("Evento " + nombre + " ha cambiado al estado: " + nuevoEstado.getClass().getSimpleName());
    }

    // Métodos para el patrón State (delegados al objeto de estado actual)
    // Estos deberían lanzar excepciones si el estado no está inicializado, en lugar de System.err
    public void publicar() {
        if (estadoActual == null) throw new IllegalStateException("Estado actual no inicializado para el evento: " + nombre);
        estadoActual.publicar(this);
    }
    public void cancelar() {
        if (estadoActual == null) throw new IllegalStateException("Estado actual no inicializado para el evento: " + nombre);
        estadoActual.cancelar(this);
    }
    public void iniciarEvento() {
        if (estadoActual == null) throw new IllegalStateException("Estado actual no inicializado para el evento: " + nombre);
        estadoActual.iniciar(this);
    }
    public void finalizarEvento() {
        if (estadoActual == null) throw new IllegalStateException("Estado actual no inicializado para el evento: " + nombre);
        estadoActual.finalizar(this);
    }

    // Métodos de negocio adicionales
    public void agregarImagen(Imagen imagen) {
        if (this.imagenes == null) {
            this.imagenes = new ArrayList<>();
        }
        this.imagenes.add(imagen);
    }

    public void agregarVideoPromocional(Video video) { // Cambiado a objeto Video
        if (video == null) return; // No añadir nulos
        if (this.videosPromocionales == null) { // Asegurar inicialización
            this.videosPromocionales = new ArrayList<>();
        }
        this.videosPromocionales.add(video);
    }

    public void eliminarVideoPromocional(Video video) {
        if (video != null && this.videosPromocionales != null) {
            this.videosPromocionales.remove(video);
        }
    }

    public void agregarTipoEntrada(TipoEntrada tipoEntrada) {
        if (this.tiposEntrada == null) {
            this.tiposEntrada = new ArrayList<>();
        }
        this.tiposEntrada.add(tipoEntrada);
    }

    public boolean hayDisponibilidad(int cantidad) {
        return (capacidad - entradasVendidas) >= cantidad;
    }

    public void registrarVentaEntradas(int cantidad) {
        if (hayDisponibilidad(cantidad)) {
            this.entradasVendidas += cantidad;
        } else {
            // Considerar lanzar una excepción o manejar el error
            System.err.println("No hay suficientes entradas disponibles para este evento.");
        }
    }

    @Override
    public String toString() {
        return "Evento [id=" + id + ", nombre=" + nombre + ", categoria=" + categoria + ", fecha=" + fecha + ", hora=" + hora
                + ", capacidad=" + capacidad + ", entradasVendidas=" + entradasVendidas + "]";
    }
}
