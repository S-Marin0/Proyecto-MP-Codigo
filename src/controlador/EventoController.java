package controlador;

import modelo.evento.Evento;
import modelo.evento.EventoBuilder;
import modelo.evento.Imagen;
import modelo.recomendacion.RepositorioEventoMemoria;
import modelo.usuario.Organizador;
import modelo.lugar.Lugar;
import modelo.entrada.TipoEntrada;
import modelo.notificacion.SistemaNotificaciones;
import modelo.excepciones.ValidacionException;
import modelo.excepciones.EntidadNoEncontradaException;
import modelo.excepciones.OperacionInvalidaException;


import java.util.Date;
import java.util.List;
import java.util.Optional;
// ArrayList ya no es necesario aquí si EventoBuilder lo maneja internamente.

public class EventoController {
    private final RepositorioEventoMemoria repositorioEvento;
    private final SistemaNotificaciones sistemaNotificaciones;

    public EventoController(RepositorioEventoMemoria repositorioEvento, SistemaNotificaciones sn) {
        if (repositorioEvento == null) throw new IllegalArgumentException("RepositorioEventoMemoria no puede ser nulo.");
        if (sn == null) throw new IllegalArgumentException("SistemaNotificaciones no puede ser nulo.");
        this.repositorioEvento = repositorioEvento;
        this.sistemaNotificaciones = sn;
    }

    public Evento crearEvento(Organizador organizador,
                              String id, String nombre, String descripcion, String categoria,
                              Date fecha, String hora, Lugar lugar, int capacidad,
                              List<Imagen> imagenes, List<Video> videos, // Cambiado aquí
                              List<TipoEntrada> tiposDeEntradaConfig) {

        if (organizador == null) {
            throw new ValidacionException("Un organizador válido es requerido para crear un evento.");
        }
        if (id == null || id.trim().isEmpty() || nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("ID y Nombre del evento son obligatorios y no pueden estar vacíos.");
        }
        if (repositorioEvento.obtenerEventoPorId(id).isPresent()){
            throw new OperacionInvalidaException("Ya existe un evento con el ID: " + id);
        }
        // Otras validaciones (fecha futura, capacidad > 0, etc.) podrían añadirse aquí.

        EventoBuilder builder = new EventoBuilder(id, nombre)
                                .withDescripcion(descripcion)
                                .withCategoria(categoria)
                                .withFecha(fecha)
                                .withHora(hora)
                                .withLugar(lugar)
                                .withCapacidad(capacidad)
                                .withOrganizador(organizador);

        if (imagenes != null) {
            builder.withImagenes(imagenes); // Asumiendo que EventoBuilder tiene withImagenes
        }

        if (videos != null) {
             builder.withVideosPromocionales(videos);
        }

        if (tiposDeEntradaConfig != null && !tiposDeEntradaConfig.isEmpty()) {
            builder.withTiposEntrada(tiposDeEntradaConfig);
        } else {
            // Considerar si esto debe ser una ValidacionException si se requiere al menos un tipo de entrada.
            // Por ahora, se permite crear sin tipos, pero se podría loggear una advertencia.
            // System.out.println("Advertencia: Creando evento sin tipos de entrada especificados.");
        }

        Evento nuevoEvento = organizador.crearEvento(builder);
        repositorioEvento.agregarEvento(nuevoEvento);

        // Log de éxito (considerar un sistema de logging formal)
        // System.out.println("Evento '" + nombre + "' creado con éxito por " + organizador.getNombre());

        sistemaNotificaciones.notificarObservers("¡Nuevo evento disponible! '" + nuevoEvento.getNombre() + "' por " + organizador.getNombre());
        return nuevoEvento;
    }

    public Evento buscarEventoPorIdConException(String id) {
        return repositorioEvento.obtenerEventoPorId(id)
            .orElseThrow(() -> new EntidadNoEncontradaException("Evento con ID '" + id + "' no encontrado."));
    }

    public Optional<Evento> buscarEventoPorId(String id) {
        return repositorioEvento.obtenerEventoPorId(id);
    }

    public List<Evento> listarTodosLosEventos() {
        return repositorioEvento.obtenerEventos();
    }

    public void eliminarEvento(String idEvento, Organizador organizadorActual) {
        if (idEvento == null || idEvento.trim().isEmpty()) {
            throw new ValidacionException("El ID del evento no puede ser nulo o vacío.");
        }
        if (organizadorActual == null) {
            throw new ValidacionException("El organizador actual no puede ser nulo.");
        }

        Evento evento = repositorioEvento.obtenerEventoPorId(idEvento)
            .orElseThrow(() -> new EntidadNoEncontradaException("No se encontró evento con ID: " + idEvento + " para eliminar."));

        if (evento.getOrganizador() == null || !evento.getOrganizador().equals(organizadorActual)) {
            throw new OperacionInvalidaException("El organizador actual no tiene permiso para eliminar el evento ID: " + idEvento);
        }

        // El método eliminarEvento en Organizador lo quita de su lista interna
        boolean eliminadoDeOrganizador = organizadorActual.eliminarEvento(evento);
        repositorioEvento.eliminarEvento(idEvento); // También eliminar del repositorio global

        if (eliminadoDeOrganizador) {
            // System.out.println("Evento '" + evento.getNombre() + "' eliminado con éxito.");
            // Notificar cancelación si estaba publicado/en curso
            if (evento.getEstadoActual() != null &&
                !(evento.getEstadoActual().toString().equals("Borrador") || evento.getEstadoActual().toString().equals("Cancelado"))){
                // evento.cancelar(); // Esto cambiaría el estado y podría notificar también.
                                   // La notificación de cambio de evento es más genérica aquí.
                sistemaNotificaciones.notificarCambioEvento(evento, "El evento '" + evento.getNombre() + "' ha sido eliminado por el organizador.");
            }
        } else {
            // Esto podría indicar un estado inconsistente si el evento estaba en el repo global pero no en la lista del organizador.
            // Considerar loggear una advertencia seria aquí.
            // System.err.println("Advertencia: Evento '" + evento.getNombre() + "' eliminado del repositorio global, pero no pudo ser eliminado de la lista del organizador.");
        }
    }

    public void publicarEvento(String idEvento, Organizador organizadorActual) {
        Evento evento = obtenerYVerificarPermisoEvento(idEvento, organizadorActual, "publicar");
        evento.publicar(); // El estado se encarga de la lógica y notificaciones
        // System.out.println("Evento '" + evento.getNombre() + "' publicado.");
    }

    public void cancelarEvento(String idEvento, Organizador organizadorActual) {
        Evento evento = obtenerYVerificarPermisoEvento(idEvento, organizadorActual, "cancelar");
        evento.cancelar();
        // System.out.println("Evento '" + evento.getNombre() + "' cancelado.");
    }

    public void iniciarEvento(String idEvento, Organizador organizadorActual) {
        Evento evento = obtenerYVerificarPermisoEvento(idEvento, organizadorActual, "iniciar");
        evento.iniciarEvento();
        // System.out.println("Evento '" + evento.getNombre() + "' iniciado.");
    }

    public void finalizarEvento(String idEvento, Organizador organizadorActual) {
        Evento evento = obtenerYVerificarPermisoEvento(idEvento, organizadorActual, "finalizar");
        evento.finalizarEvento();
        // System.out.println("Evento '" + evento.getNombre() + "' finalizado.");
    }

    // Método de utilidad para reducir duplicación en publicar, cancelar, etc.
    private Evento obtenerYVerificarPermisoEvento(String idEvento, Organizador organizadorActual, String accion) {
        if (idEvento == null || idEvento.trim().isEmpty()) {
            throw new ValidacionException("El ID del evento no puede ser nulo o vacío para la acción: " + accion);
        }
        if (organizadorActual == null) {
            throw new ValidacionException("El organizador actual no puede ser nulo para la acción: " + accion);
        }

        Evento evento = repositorioEvento.obtenerEventoPorId(idEvento)
            .orElseThrow(() -> new EntidadNoEncontradaException("No se encontró evento con ID: " + idEvento + " para " + accion + "."));

        if (evento.getOrganizador() == null || !evento.getOrganizador().equals(organizadorActual)) {
            throw new OperacionInvalidaException("El organizador actual no tiene permiso para " + accion + " el evento ID: " + idEvento);
        }
        return evento;
    }

    public Evento editarEvento(String idEvento, EventoDTO datosNuevos, Organizador organizadorActual) {
        if (datosNuevos == null || !datosNuevos.tieneDatosParaActualizar()) {
            throw new ValidacionException("No se proporcionaron datos para actualizar el evento.");
        }

        Evento evento = obtenerYVerificarPermisoEvento(idEvento, organizadorActual, "editar");

        // Solo se pueden editar eventos en estado Borrador o Publicado (quizás con restricciones)
        // Esta lógica podría ser más compleja y depender del estado específico.
        // Por ejemplo, un evento EnCurso o Finalizado no debería ser editable en muchos de sus campos.
        String estadoSimple = evento.getEstadoActual().getClass().getSimpleName();
        if (!(estadoSimple.equals("EstadoEventoBorrador") || estadoSimple.equals("EstadoEventoPublicado"))) {
            throw new OperacionInvalidaException("El evento con ID " + idEvento + " no se puede editar en su estado actual: " + estadoSimple);
        }

        boolean modificado = false;

        if (datosNuevos.getNombre() != null) {
            evento.setNombre(datosNuevos.getNombre());
            modificado = true;
        }
        if (datosNuevos.getDescripcion() != null) {
            evento.setDescripcion(datosNuevos.getDescripcion());
            modificado = true;
        }
        if (datosNuevos.getCategoria() != null) {
            evento.setCategoria(datosNuevos.getCategoria());
            modificado = true;
        }
        if (datosNuevos.getFecha() != null) {
            evento.setFecha(datosNuevos.getFecha());
            modificado = true;
        }
        if (datosNuevos.getHora() != null) {
            evento.setHora(datosNuevos.getHora());
            modificado = true;
        }
        if (datosNuevos.getCapacidad() != null) {
            // Validar que la nueva capacidad no sea menor a las entradas ya vendidas.
            if (datosNuevos.getCapacidad() < evento.getEntradasVendidas()) {
                throw new ValidacionException("La nueva capacidad (" + datosNuevos.getCapacidad() + ") no puede ser menor a las entradas ya vendidas (" + evento.getEntradasVendidas() + ").");
            }
            evento.setCapacidad(datosNuevos.getCapacidad());
            modificado = true;
        }

        // Nota: La edición de Lugar, Imágenes, Videos, Tipos de Entrada no está incluida aquí.
        // Requeriría una lógica más compleja, posiblemente métodos dedicados o un DTO más elaborado.

        if (modificado) {
            // No es necesario volver a guardar en RepositorioEventoMemoria si el objeto Evento es el mismo
            // y las modificaciones se hacen directamente sobre él, ya que el repositorio guarda la referencia.
            // repositorioEvento.agregarEvento(evento); // Esto podría causar duplicados o errores si el ID ya existe.
            // Si RepositorioEventoMemoria usara un Map<String, Evento>, actualizar sería automático.
            // Si es una List, y se quisiera "re-guardar", se debería eliminar el viejo y añadir el nuevo,
            // o tener un método "actualizar" en el repositorio.
            // Para la implementación actual de RepositorioEventoMemoria (que usa una List y busca por ID),
            // las modificaciones al objeto Evento recuperado se reflejan directamente.

            sistemaNotificaciones.notificarCambioEvento(evento, "El evento '" + evento.getNombre() + "' ha sido modificado.");
            // System.out.println("Evento '" + evento.getNombre() + "' modificado con éxito.");
        } else {
            // System.out.println("No se realizaron modificaciones en el evento '" + evento.getNombre() + "'.");
        }

        return evento;
    }
}
