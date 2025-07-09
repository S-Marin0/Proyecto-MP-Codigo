
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
    private List<Video> videosPromocionales;
    private List<TipoEntrada> tiposEntrada;
    private int entradasVendidas;

    // Para persistencia JPA del estado
    // @Column(name = "estado_evento") // Esta anotación irá en la entidad JPA real
    private String estadoActualNombre;

    // @Transient // Esta anotación irá en la entidad JPA real
    private EstadoEventos estadoActualObj;


    // Constructor package-private para ser usado por EventoBuilder
    // El ID String se elimina, se usará Long generado por BD en la entidad JPA.
    // El estado inicial se establecerá explícitamente.
    Evento() {
        this.imagenes = new ArrayList<>();
        this.videosPromocionales = new ArrayList<>();
        this.tiposEntrada = new ArrayList<>();
        this.entradasVendidas = 0;
        // El estadoActualObj se inicializará a Borrador por defecto,
        // y estadoActualNombre se sincronizará.
        setEstadoActualObj(new EstadoEventoBorrador());
    }

    // Getters
    // public String getId() { return id; } // Se cambiará a Long id en la entidad JPA
    public Long getId() { return null; } // Placeholder, el ID real será Long en la entidad JPA
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public Date getFecha() { return fecha; }
    public String getHora() { return hora; }
    public Lugar getLugar() { return lugar; }
    public int getCapacidad() { return capacidad; }
    public Organizador getOrganizador() { return organizador; }
    public List<Imagen> getImagenes() { return new ArrayList<>(imagenes); }
    public List<Video> getVideosPromocionales() { return new ArrayList<>(videosPromocionales); }
    public List<TipoEntrada> getTiposEntrada() { return new ArrayList<>(tiposEntrada); }
    public int getEntradasVendidas() { return entradasVendidas; }

    public EstadoEventos getEstadoActualObj() { // Getter para el objeto estado
        if (this.estadoActualObj == null && this.estadoActualNombre != null) {
            reconstruirEstadoDesdeNombre(); // Intentar reconstruir si es nulo pero hay nombre
        }
        return estadoActualObj;
    }
    public String getEstadoActualNombre() { return estadoActualNombre; } // Getter para el nombre del estado

    // Setters (package-private, para ser usados principalmente por el Builder)
    // void setId(String id) { this.id = id; } // El ID será Long y manejado por JPA
    public void setId(Long id) { /* this.id = id; */ } // Placeholder para el ID Long de JPA

    // setNombre, setDescripcion, etc. ahora son públicos para edición
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

    // Setter para el objeto de estado, actualiza también el nombre del estado
    public void setEstadoActualObj(EstadoEventos nuevoEstado) {
        this.estadoActualObj = nuevoEstado;
        if (nuevoEstado != null) {
            this.estadoActualNombre = nuevoEstado.getClass().getSimpleName();
        } else {
            this.estadoActualNombre = null;
        }
    }

    // Usado internamente por el Builder o JPA (si se configura)
    public void setEstadoActualNombre(String nombreEstado) {
        this.estadoActualNombre = nombreEstado;
        // Podría llamar a reconstruirEstadoDesdeNombre() aquí, pero es mejor hacerlo
        // explícitamente o con @PostLoad para evitar problemas de ciclo de vida.
    }

    // @PostLoad // Anotación JPA para ejecutar después de cargar la entidad
    public void reconstruirEstadoDesdeNombre() {
        if (this.estadoActualNombre == null) {
            // Si no hay nombre de estado, podría ser un nuevo evento o un error.
            // Establecer un estado por defecto o lanzar excepción.
            this.estadoActualObj = new EstadoEventoBorrador(); // Por defecto
            this.estadoActualNombre = this.estadoActualObj.getClass().getSimpleName();
            return;
        }
        switch (this.estadoActualNombre) {
            case "EstadoEventoBorrador":
                this.estadoActualObj = new modelo.state.EstadoEventoBorrador();
                break;
            case "EstadoEventoPublicado":
                this.estadoActualObj = new modelo.state.EstadoEventoPublicado();
                break;
            case "EstadoEventoEnCurso":
                this.estadoActualObj = new modelo.state.EstadoEventoEnCurso();
                break;
            case "EstadoEventoFinalizado":
                this.estadoActualObj = new modelo.state.EstadoEventoFinalizado();
                break;
            case "EstadoEventoCancelado":
                this.estadoActualObj = new modelo.state.EstadoEventoCancelado();
                break;
            default:
                // Podría lanzar una excepción si el nombre del estado no es reconocido
                // o asignar un estado por defecto.
                System.err.println("Advertencia: Nombre de estado desconocido '" + this.estadoActualNombre + "'. Se usará Borrador por defecto.");
                this.estadoActualObj = new EstadoEventoBorrador();
                this.estadoActualNombre = this.estadoActualObj.getClass().getSimpleName();
        }
    }


    // Métodos para el patrón State (delegados al objeto de estado actual)
    public void publicar() {
        if (getEstadoActualObj() == null) throw new IllegalStateException("Estado actual no inicializado para el evento: " + nombre);
        getEstadoActualObj().publicar(this);
    }
    public void cancelar() {
        if (getEstadoActualObj() == null) throw new IllegalStateException("Estado actual no inicializado para el evento: " + nombre);
        getEstadoActualObj().cancelar(this);
    }
    public void iniciarEvento() {
        if (getEstadoActualObj() == null) throw new IllegalStateException("Estado actual no inicializado para el evento: " + nombre);
        getEstadoActualObj().iniciar(this);
    }
    public void finalizarEvento() {
        if (getEstadoActualObj() == null) throw new IllegalStateException("Estado actual no inicializado para el evento: " + nombre);
        getEstadoActualObj().finalizar(this);
    }

    // Métodos de negocio adicionales
    public void agregarImagen(Imagen imagen) {
        if (imagen == null) return;
        if (this.imagenes == null) this.imagenes = new ArrayList<>();
        this.imagenes.add(imagen);
        imagen.setEvento(this); // Mantener relación bidireccional
    }

    public void eliminarImagen(Imagen imagen) {
        if (imagen != null && this.imagenes != null) {
            this.imagenes.remove(imagen);
            // imagen.setEvento(null); // Opcional, depende de si se quiere romper la relación
        }
    }

    public void agregarVideoPromocional(Video video) {
        if (video == null) return;
        if (this.videosPromocionales == null) {
            this.videosPromocionales = new ArrayList<>();
        }
        this.videosPromocionales.add(video);
        video.setEvento(this); // Mantener relación bidireccional
    }

    public void eliminarVideoPromocional(Video video) {
        if (video != null && this.videosPromocionales != null) {
            this.videosPromocionales.remove(video);
            // video.setEvento(null);
        }
    }

    public void agregarTipoEntrada(TipoEntrada tipoEntrada) {
        if (tipoEntrada == null) return;
        if (this.tiposEntrada == null) {
            this.tiposEntrada = new ArrayList<>();
        }
        this.tiposEntrada.add(tipoEntrada);
        tipoEntrada.setEvento(this); // Mantener relación bidireccional
    }

    public void eliminarTipoEntrada(TipoEntrada tipoEntrada) {
        if (tipoEntrada != null && this.tiposEntrada != null) {
            this.tiposEntrada.remove(tipoEntrada);
            // tipoEntrada.setEvento(null);
        }
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
