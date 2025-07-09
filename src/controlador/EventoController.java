package controlador;

import modelo.evento.Evento;
import modelo.evento.EventoBuilder;
import modelo.evento.Imagen; // Si se manejan objetos Imagen
import modelo.recomendacion.RepositorioEventoMemoria; // Usando la implementación en memoria
import modelo.usuario.Organizador;
import modelo.lugar.Lugar; // Para asignar lugar al evento
import modelo.entrada.TipoEntrada; // Para añadir tipos de entrada al evento
import modelo.notificacion.SistemaNotificaciones; // Para notificar creación
// import modelo.mediador.MediadorCompras; // Alternativa para notificaciones/coordinación

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList; // Para inicializar listas en el builder

public class EventoController {
    private final RepositorioEventoMemoria repositorioEvento;
    private final SistemaNotificaciones sistemaNotificaciones;
    // private final MediadorCompras mediador; // Alternativa

    public EventoController(RepositorioEventoMemoria repositorioEvento, SistemaNotificaciones sn) {
        if (repositorioEvento == null) throw new IllegalArgumentException("RepositorioEventoMemoria no puede ser nulo.");
        if (sn == null) throw new IllegalArgumentException("SistemaNotificaciones no puede ser nulo.");
        this.repositorioEvento = repositorioEvento;
        this.sistemaNotificaciones = sn;
        // this.mediador = mediador;
    }

    // Para crear un evento, necesitamos muchos datos. Usamos el patrón Builder.
    // El Organizador es quien crea el evento.
    public Evento crearEvento(Organizador organizador,
                              String id, String nombre, String descripcion, String categoria,
                              Date fecha, String hora, Lugar lugar, int capacidad,
                              List<String> urlsImagenes, List<String> urlsVideosPromocionales,
                              List<TipoEntrada> tiposDeEntradaConfig) {

        if (organizador == null) {
            System.err.println("EventoController: Un organizador debe crear el evento.");
            return null;
        }
        if (id == null || id.trim().isEmpty() || nombre == null || nombre.trim().isEmpty()) {
            System.err.println("EventoController: ID y Nombre del evento son obligatorios.");
            return null;
        }
        if (repositorioEvento.obtenerEventoPorId(id).isPresent()){
            System.err.println("EventoController: Ya existe un evento con el ID: " + id);
            return null;
        }

        EventoBuilder builder = new EventoBuilder(id, nombre)
                                .withDescripcion(descripcion)
                                .withCategoria(categoria)
                                .withFecha(fecha)
                                .withHora(hora)
                                .withLugar(lugar)
                                .withCapacidad(capacidad)
                                .withOrganizador(organizador); // El EventoBuilder necesita withOrganizador

        if (urlsImagenes != null) {
            for (String urlImg : urlsImagenes) {
                builder.addImagen(new Imagen(urlImg)); // Asumiendo que Imagen tiene constructor (String url)
            }
        }
        if (urlsVideosPromocionales != null) {
            builder.withVideosPromocionales(urlsVideosPromocionales);
        }
        if (tiposDeEntradaConfig != null && !tiposDeEntradaConfig.isEmpty()) {
            builder.withTiposEntrada(tiposDeEntradaConfig);
        } else {
            // Podríamos añadir un tipo de entrada general por defecto si no se especifica ninguno
            System.out.println("EventoController: Creando evento sin tipos de entrada especificados. Se recomienda añadir al menos uno.");
            // TipoEntrada defaultGeneral = new EntradaGeneral("General", "Entrada general", 20.0, capacidad, 10);
            // builder.addTipoEntrada(defaultGeneral);
        }

        // El método crearEvento del Organizador usa el builder y añade el evento a su lista.
        Evento nuevoEvento = organizador.crearEvento(builder);
        // El EventoBuilder debería asignar el organizador. Si no, hacerlo aquí:
        // nuevoEvento.setOrganizador(organizador); // Si Evento tiene un setter público o el builder no lo hace.

        repositorioEvento.agregarEvento(nuevoEvento); // Guardar en el repositorio global de eventos

        System.out.println("EventoController: Evento '" + nombre + "' creado con éxito por " + organizador.getNombre());

        // Notificar la creación del nuevo evento
        // Podría ser una notificación global o a usuarios con preferencias coincidentes.
        sistemaNotificaciones.notificarObservers("¡Nuevo evento disponible! '" + nuevoEvento.getNombre() + "' por " + organizador.getNombre());
        // O una notificación más específica a través del mediador si el flujo es más complejo.
        // mediador.notificarNuevoEventoCreado(nuevoEvento);

        return nuevoEvento;
    }

    public Optional<Evento> buscarEventoPorId(String id) {
        return repositorioEvento.obtenerEventoPorId(id);
    }

    public List<Evento> listarTodosLosEventos() {
        return repositorioEvento.obtenerEventos();
    }

    public boolean eliminarEvento(String idEvento, Organizador organizadorActual) {
        Optional<Evento> optEvento = repositorioEvento.obtenerEventoPorId(idEvento);
        if (optEvento.isPresent()) {
            Evento evento = optEvento.get();
            // Verificar que el organizador actual es el propietario del evento
            if (evento.getOrganizador() != null && evento.getOrganizador().equals(organizadorActual)) {
                // El método eliminarEvento en Organizador lo quita de su lista interna
                boolean eliminadoDeOrganizador = organizadorActual.eliminarEvento(evento);
                // También eliminar del repositorio global
                repositorioEvento.eliminarEvento(idEvento);
                if (eliminadoDeOrganizador) {
                     System.out.println("EventoController: Evento '" + evento.getNombre() + "' eliminado con éxito.");
                    // Notificar cancelación si estaba publicado/en curso
                    if (evento.getEstadoActual() != null &&
                        !(evento.getEstadoActual().toString().equals("Borrador") || evento.getEstadoActual().toString().equals("Cancelado"))){
                        // evento.cancelar(); // Esto cambiaría el estado y notificaría
                        sistemaNotificaciones.notificarCambioEvento(evento, "El evento ha sido eliminado por el organizador.");
                    }
                    return true;
                } else {
                    System.err.println("EventoController: Evento '" + evento.getNombre() + "' no pudo ser eliminado de la lista del organizador (podría no estar allí).");
                    // Aún así lo eliminamos del repo global si existe.
                    return true;
                }
            } else {
                System.err.println("EventoController: El organizador actual no tiene permiso para eliminar el evento ID: " + idEvento);
                return false;
            }
        } else {
            System.err.println("EventoController: No se encontró evento con ID: " + idEvento + " para eliminar.");
            return false;
        }
    }

    // Métodos para cambiar estado de un evento (delegando al evento)
    public boolean publicarEvento(String idEvento, Organizador organizadorActual) {
        Optional<Evento> optEvento = repositorioEvento.obtenerEventoPorId(idEvento);
        if (optEvento.isPresent()) {
            Evento evento = optEvento.get();
            if (evento.getOrganizador() != null && evento.getOrganizador().equals(organizadorActual)) {
                evento.publicar(); // El estado se encarga de la lógica y notificaciones
                return true;
            } // else: error de permiso
        } // else: evento no encontrado
        return false;
    }

    public boolean cancelarEvento(String idEvento, Organizador organizadorActual) {
         Optional<Evento> optEvento = repositorioEvento.obtenerEventoPorId(idEvento);
        if (optEvento.isPresent()) {
            Evento evento = optEvento.get();
            if (evento.getOrganizador() != null && evento.getOrganizador().equals(organizadorActual)) {
                evento.cancelar();
                return true;
            }
        }
        return false;
    }
    // Otros métodos para iniciar, finalizar...
}
