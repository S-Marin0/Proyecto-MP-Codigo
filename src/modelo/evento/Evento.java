
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
    private List<String> videosPromocionales; // Podría ser List<Video> si se crea una clase Video
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
    public List<Imagen> getImagenes() { return imagenes; }
    public List<String> getVideosPromocionales() { return videosPromocionales; }
    public List<TipoEntrada> getTiposEntrada() { return tiposEntrada; }
    public int getEntradasVendidas() { return entradasVendidas; }
    public EstadoEventos getEstadoActual() { return estadoActual; }

    // Setters (package-private, para ser usados principalmente por el Builder)
    void setId(String id) { this.id = id; }
    void setNombre(String nombre) { this.nombre = nombre; }
    void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    void setCategoria(String categoria) { this.categoria = categoria; }
    void setFecha(Date fecha) { this.fecha = fecha; }
    void setHora(String hora) { this.hora = hora; }
    void setLugar(Lugar lugar) { this.lugar = lugar; }
    void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    void setOrganizador(Organizador organizador) { this.organizador = organizador; }
    void setImagenes(List<Imagen> imagenes) { this.imagenes = imagenes; }
    void setVideosPromocionales(List<String> videosPromocionales) { this.videosPromocionales = videosPromocionales; }
    void setTiposEntrada(List<TipoEntrada> tiposEntrada) { this.tiposEntrada = tiposEntrada; }
    void setEntradasVendidas(int entradasVendidas) { this.entradasVendidas = entradasVendidas; }

    public void setEstado(EstadoEventos nuevoEstado) {
        this.estadoActual = nuevoEstado;
        // System.out.println("Evento " + nombre + " ha cambiado al estado: " + nuevoEstado.getClass().getSimpleName());
    }

    // Métodos para el patrón State (delegados al objeto de estado actual)
    public void publicar() {
        if (estadoActual != null) estadoActual.publicar(this);
        else System.err.println("Estado actual no inicializado para el evento: " + nombre);
    }
    public void cancelar() {
        if (estadoActual != null) estadoActual.cancelar(this);
        else System.err.println("Estado actual no inicializado para el evento: " + nombre);
    }
    public void iniciarEvento() {
        if (estadoActual != null) estadoActual.iniciar(this);
        else System.err.println("Estado actual no inicializado para el evento: " + nombre);
    }
    public void finalizarEvento() {
        if (estadoActual != null) estadoActual.finalizar(this);
        else System.err.println("Estado actual no inicializado para el evento: " + nombre);
    }

    // Métodos de negocio adicionales
    public void agregarImagen(Imagen imagen) {
        if (this.imagenes == null) {
            this.imagenes = new ArrayList<>();
        }
        this.imagenes.add(imagen);
    }

    public void agregarVideoPromocional(String urlVideo) {
        if (this.videosPromocionales == null) {
            this.videosPromocionales = new ArrayList<>();
        }
        this.videosPromocionales.add(urlVideo);
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
