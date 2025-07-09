
package modelo.evento;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import modelo.entrada.TipoEntrada;
import modelo.lugar.Lugar;
import modelo.usuario.Organizador;
// Asumiendo que EstadoEventos y una implementación inicial como EstadoEventoBorrador existen
// import modelo.estado.EstadoEventoBorrador;
// import modelo.estado.EstadoEventos;


public class EventoBuilder {
    private String id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private Date fecha;
    private String hora;
    private Lugar lugar;
    private int capacidad;
    private Organizador organizador;
    private List<Imagen> imagenes = new ArrayList<>();
    private List<Video> videosPromocionales = new ArrayList<>(); // Cambiado a List<Video>
    private List<TipoEntrada> tiposEntrada = new ArrayList<>();
    private EstadoEventos estadoActual;

    public EventoBuilder(String id, String nombre) {
        this.id = id; // ID y nombre podrían ser mínimos para empezar a construir
        this.nombre = nombre;
        // Por defecto, un evento nuevo podría estar en estado Borrador
        // this.estadoActual = new EstadoEventoBorrador();
    }

    public EventoBuilder withDescripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public EventoBuilder withCategoria(String categoria) {
        this.categoria = categoria;
        return this;
    }

    public EventoBuilder withFecha(Date fecha) {
        this.fecha = fecha;
        return this;
    }

    public EventoBuilder withHora(String hora) {
        this.hora = hora;
        return this;
    }

    public EventoBuilder withLugar(Lugar lugar) {
        this.lugar = lugar;
        return this;
    }

    public EventoBuilder withCapacidad(int capacidad) {
        this.capacidad = capacidad;
        return this;
    }

    public EventoBuilder withOrganizador(Organizador organizador) {
        this.organizador = organizador;
        return this;
    }

    public EventoBuilder addImagen(Imagen imagen) {
        this.imagenes.add(imagen);
        return this;
    }

    public EventoBuilder withImagenes(List<Imagen> imagenes) {
        this.imagenes = imagenes;
        return this;
    }

    public EventoBuilder addVideoPromocional(Video video) { // Cambiado a objeto Video
        if (video != null) {
            this.videosPromocionales.add(video);
        }
        return this;
    }

    public EventoBuilder withVideosPromocionales(List<Video> videosPromocionales) { // Cambiado a List<Video>
        if (videosPromocionales != null) {
            this.videosPromocionales = videosPromocionales;
        } else {
            this.videosPromocionales = new ArrayList<>(); // Evitar null pointer si se pasa null
        }
        return this;
    }

    public EventoBuilder addTipoEntrada(TipoEntrada tipoEntrada) {
        this.tiposEntrada.add(tipoEntrada);
        return this;
    }

    public EventoBuilder withTiposEntrada(List<TipoEntrada> tiposEntrada) {
        this.tiposEntrada = tiposEntrada;
        return this;
    }

    public EventoBuilder withEstadoActual(EstadoEventos estado) {
        this.estadoActual = estado;
        return this;
    }

    public Evento build() {
        Evento evento = new Evento();
        evento.setId(this.id);
        evento.setNombre(this.nombre);
        evento.setDescripcion(this.descripcion);
        evento.setCategoria(this.categoria);
        evento.setFecha(this.fecha);
        evento.setHora(this.hora);
        evento.setLugar(this.lugar);
        evento.setCapacidad(this.capacidad);
        evento.setOrganizador(this.organizador);
        evento.setImagenes(this.imagenes);
        evento.setVideosPromocionales(this.videosPromocionales);
        evento.setTiposEntrada(this.tiposEntrada);

        if (this.estadoActual != null) {
            evento.setEstado(this.estadoActual);
        } else {
            // Si no se especificó un estado, podríamos establecer uno por defecto aquí.
            // Por ejemplo: evento.setEstado(new EstadoEventoBorrador());
            // Esto depende de si EstadoEventoBorrador está disponible y es la lógica deseada.
            // Por ahora, si no se provee, el estado en Evento permanecerá null hasta que se setee explícitamente.
            // O, si el constructor de Evento ya lo inicializa, esta lógica podría no ser necesaria aquí.
        }
        // entradasVendidas se inicializa en 0 en el constructor de Evento.

        return evento;
    }
}
