package controlador;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

import modelo.evento.Evento;
import modelo.evento.EventoBuilder;
import modelo.evento.Imagen;
import modelo.evento.Video;
// import modelo.recomendacion.RepositorioEventoMemoria; // Ya no se usa
import modelo.repositorio.EventoRepository; // Nuevo
import modelo.repositorio.UsuarioRepository; // Necesario para Organizador
import modelo.repositorio.LugarRepository;   // Necesario para Lugar
import modelo.usuario.Organizador;
import modelo.lugar.Lugar;
import modelo.entrada.TipoEntrada;
import modelo.notificacion.SistemaNotificaciones;
import modelo.excepciones.ValidacionException;
import modelo.excepciones.EntidadNoEncontradaException;
import modelo.excepciones.OperacionInvalidaException;
import modelo.state.EstadoEventoBorrador; // Para el estado inicial

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


// @Service
public class EventoController { // Podría renombrarse a EventoService
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository; // Para obtener Organizador gestionado
    private final LugarRepository lugarRepository;     // Para obtener/persistir Lugar gestionado
    private final SistemaNotificaciones sistemaNotificaciones;

    // @Autowired
    public EventoController(EventoRepository eventoRepository,
                            UsuarioRepository usuarioRepository,
                            LugarRepository lugarRepository,
                            SistemaNotificaciones sn) {
        if (eventoRepository == null) throw new IllegalArgumentException("EventoRepository no puede ser nulo.");
        if (usuarioRepository == null) throw new IllegalArgumentException("UsuarioRepository no puede ser nulo.");
        if (lugarRepository == null) throw new IllegalArgumentException("LugarRepository no puede ser nulo.");
        if (sn == null) throw new IllegalArgumentException("SistemaNotificaciones no puede ser nulo.");

        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.lugarRepository = lugarRepository;
        this.sistemaNotificaciones = sn;
    }

    // @Transactional
    public Evento crearEvento(Long organizadorId, // Se pasa ID del organizador
                              // String id, // El ID del evento ahora lo genera la BD (Long)
                              String nombre, String descripcion, String categoria,
                              Date fecha, String hora,
                              Lugar lugarInput, // Lugar puede ser nuevo o existente
                              int capacidad,
                              List<Imagen> imagenesInput, List<Video> videosInput,
                              List<TipoEntrada> tiposDeEntradaConfigInput) {

        Organizador organizador = usuarioRepository.findById(organizadorId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Organizador con ID " + organizadorId + " no encontrado."));

        if (nombre == null || nombre.trim().isEmpty()) { // ID de evento ya no se valida aquí
            throw new ValidacionException("Nombre del evento es obligatorio y no puede estar vacío.");
        }
        // Validar si ya existe un evento con el mismo nombre y fecha para el mismo organizador (opcional)
        // if (eventoRepository.findByNombreAndFechaAndOrganizador(nombre, fecha, organizador).isPresent()) {
        //    throw new OperacionInvalidaException("Ya existe un evento con el mismo nombre y fecha para este organizador.");
        // }

        // Manejo del Lugar: Si tiene ID, intentar cargarlo. Si no, se asume nuevo y se persistirá en cascada.
        Lugar lugarPersistente;
        if (lugarInput.getId() != null) { // Asumiendo que Lugar tiene getId() y es Long
            lugarPersistente = lugarRepository.findById(lugarInput.getId())
                .orElseThrow(() -> new EntidadNoEncontradaException("Lugar con ID " + lugarInput.getId() + " no encontrado. No se puede asociar al evento."));
        } else {
            // Si el lugar es nuevo y no tiene ID, se persistirá en cascada con el Evento si Evento.lugar tiene CascadeType.PERSIST o ALL.
            // Es importante que el objeto Lugar esté correctamente configurado.
            // Opcionalmente, guardarlo explícitamente si no hay cascada o se quiere asegurar el ID antes.
            // lugarPersistente = lugarRepository.save(lugarInput);
            lugarPersistente = lugarInput; // Confiar en la cascada desde Evento
        }

        // El ID del evento (String) ya no se pasa al builder, será generado por la BD como Long
        EventoBuilder builder = new EventoBuilder(nombre) // Adaptar constructor de EventoBuilder si es necesario
                                .withDescripcion(descripcion)
                                .withCategoria(categoria)
                                .withFecha(fecha)
                                .withHora(hora)
                                .withLugar(lugarPersistente)
                                .withCapacidad(capacidad)
                                .withOrganizador(organizador); // Organizador ya es una entidad gestionada

        // Las colecciones de Imagen, Video, TipoEntrada deben tener la relación bidireccional establecida
        // y se guardarán en cascada si Evento tiene CascadeType.ALL en esas relaciones.
        if (imagenesInput != null) {
            // EventoBuilder.withImagenes debe asegurar que cada Imagen tenga la referencia al Evento
            builder.withImagenes(imagenesInput.stream().peek(img -> img.setEvento(null)).collect(Collectors.toList()));
        }

        if (videosInput != null) {
             builder.withVideosPromocionales(videosInput.stream().peek(vid -> vid.setEvento(null)).collect(Collectors.toList()));
        }

        if (tiposDeEntradaConfigInput != null && !tiposDeEntradaConfigInput.isEmpty()) {
            builder.withTiposEntrada(tiposDeEntradaConfigInput.stream().peek(te -> te.setEvento(null)).collect(Collectors.toList()));
        }

        Evento nuevoEvento = builder.build(); // El builder ahora no setea el ID String
        // El estado inicial se setea en el constructor de Evento o en el build() del builder.
        // nuevoEvento.setEstado(new EstadoEventoBorrador()); // Asegurar que el estado inicial se maneje

        // Lógica para establecer el estado inicial si no lo hace el constructor/builder de Evento
        if (nuevoEvento.getEstadoActualObj() == null) { // Usar getEstadoActualObj()
            nuevoEvento.setEstadoActualObj(new EstadoEventoBorrador()); // Usar setEstadoActualObj()
        }


        Evento eventoGuardado = eventoRepository.save(nuevoEvento);
        // Actualizar las referencias en las colecciones después de que 'eventoGuardado' tiene un ID
        // y está gestionado, para que las FK se establezcan correctamente.
        final Evento refEventoGuardado = eventoGuardado;
        if(eventoGuardado.getImagenes() != null) eventoGuardado.getImagenes().forEach(img -> img.setEvento(refEventoGuardado));
        if(eventoGuardado.getVideosPromocionales() != null) eventoGuardado.getVideosPromocionales().forEach(vid -> vid.setEvento(refEventoGuardado));
        if(eventoGuardado.getTiposEntrada() != null) eventoGuardado.getTiposEntrada().forEach(te -> te.setEvento(refEventoGuardado));

        eventoGuardado = eventoRepository.save(eventoGuardado); // Guardar de nuevo para persistir las relaciones actualizadas


        // La lógica de organizador.crearEvento(builder) se reemplaza por la interacción directa con el repo.
        // El organizador ya está asociado a través del builder.

        sistemaNotificaciones.notificarObservers("¡Nuevo evento disponible! '" + eventoGuardado.getNombre() + "' por " + organizador.getNombre());
        return eventoGuardado;
    }

    // @Transactional(readOnly = true)
    public Evento buscarEventoPorIdConException(Long id) { // ID ahora es Long
        return eventoRepository.findById(id)
            .orElseThrow(() -> new EntidadNoEncontradaException("Evento con ID '" + id + "' no encontrado."));
    }

    // @Transactional(readOnly = true)
    public Optional<Evento> buscarEventoPorId(Long id) { // ID ahora es Long
        return eventoRepository.findById(id);
    }

    // @Transactional(readOnly = true)
    public List<Evento> listarTodosLosEventos() {
        return eventoRepository.findAll(); // findAll() es de JpaRepository
    }

    // @Transactional
    public void eliminarEvento(Long idEvento, Long organizadorIdActual) { // IDs son Long
        if (idEvento == null ) {
            throw new ValidacionException("El ID del evento no puede ser nulo.");
        }
        if (organizadorIdActual == null) {
            throw new ValidacionException("El ID del organizador actual no puede ser nulo.");
        }

        Evento evento = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new EntidadNoEncontradaException("No se encontró evento con ID: " + idEvento + " para eliminar."));

        if (evento.getOrganizador() == null || !evento.getOrganizador().getId().equals(organizadorIdActual)) {
            throw new OperacionInvalidaException("El organizador actual no tiene permiso para eliminar el evento ID: " + idEvento);
        }

        // La eliminación en cascada se encargará de las entidades relacionadas si está configurada.
        // La lógica de organizador.eliminarEvento(evento) ya no es necesaria aquí si la relación es bidireccional
        // y se usa orphanRemoval=true o se maneja la eliminación de la lista del organizador.
        eventoRepository.deleteById(idEvento);

        // Notificar
        // El objeto 'evento' podría estar detached después de la eliminación,
        // así que es mejor usar los datos que teníamos antes de eliminar para la notificación.
        sistemaNotificaciones.notificarCambioEvento(evento, "El evento '" + evento.getNombre() + "' ha sido eliminado por el organizador.");
    }

    private void actualizarEstadoYGuardar(Evento evento, Runnable accionEstado) {
        accionEstado.run(); // Ejecuta la acción de cambio de estado (ej. evento.publicar())
        // El estado (String) en la entidad Evento debe actualizarse antes de guardar.
        // Esto debería hacerse dentro de los métodos de cambio de estado del patrón State.
        // Ej. En EstadoEventoBorrador.publicar(Evento e): e.setEstadoActualNombre("PUBLICADO"); e.setEstado(nuevoEstadoPublicado);
        eventoRepository.save(evento);
    }

    // @Transactional
    public void publicarEvento(Long idEvento, Long organizadorIdActual) {
        Evento evento = obtenerYVerificarPermisoEvento(idEvento, organizadorIdActual, "publicar");
        actualizarEstadoYGuardar(evento, evento::publicar);
    }

    // @Transactional
    public void cancelarEvento(Long idEvento, Long organizadorIdActual) {
        Evento evento = obtenerYVerificarPermisoEvento(idEvento, organizadorIdActual, "cancelar");
        actualizarEstadoYGuardar(evento, evento::cancelar);
    }

    // @Transactional
    public void iniciarEvento(Long idEvento, Long organizadorIdActual) {
        Evento evento = obtenerYVerificarPermisoEvento(idEvento, organizadorIdActual, "iniciar");
        actualizarEstadoYGuardar(evento, evento::iniciarEvento);
    }

    // @Transactional
    public void finalizarEvento(Long idEvento, Long organizadorIdActual) {
        Evento evento = obtenerYVerificarPermisoEvento(idEvento, organizadorIdActual, "finalizar");
        actualizarEstadoYGuardar(evento, evento::finalizarEvento);
    }

    // @Transactional(readOnly = true)
    private Evento obtenerYVerificarPermisoEvento(Long idEvento, Long organizadorIdActual, String accion) {
        if (idEvento == null) {
            throw new ValidacionException("El ID del evento no puede ser nulo para la acción: " + accion);
        }
        if (organizadorIdActual == null) {
            throw new ValidacionException("El ID del organizador actual no puede ser nulo para la acción: " + accion);
        }

        Evento evento = eventoRepository.findById(idEvento)
            .orElseThrow(() -> new EntidadNoEncontradaException("No se encontró evento con ID: " + idEvento + " para " + accion + "."));

        // Reconstruir el objeto de estado si es necesario (ej. después de cargar desde BD)
        // Esto debería hacerse idealmente con un @PostLoad en la entidad Evento
        if (evento.getEstadoActualObj() == null && evento.getEstadoActualNombre() != null) {
            evento.reconstruirEstadoDesdeNombre();
        }


        if (evento.getOrganizador() == null || !evento.getOrganizador().getId().equals(organizadorIdActual)) {
            throw new OperacionInvalidaException("El organizador actual no tiene permiso para " + accion + " el evento ID: " + idEvento);
        }
        return evento;
    }

    // @Transactional
    public Evento editarEvento(Long idEvento, EventoDTO datosNuevos, Long organizadorIdActual) { // IDs son Long
        if (datosNuevos == null || !datosNuevos.tieneDatosParaActualizar()) {
            throw new ValidacionException("No se proporcionaron datos para actualizar el evento.");
        }

        Evento evento = obtenerYVerificarPermisoEvento(idEvento, organizadorIdActual, "editar");

        // Reconstruir el objeto de estado si es necesario (ej. después de cargar desde BD)
        if (evento.getEstadoActualObj() == null && evento.getEstadoActualNombre() != null) {
            evento.reconstruirEstadoDesdeNombre();
        }

        String estadoSimple = evento.getEstadoActualObj().getClass().getSimpleName();
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
            if (datosNuevos.getCapacidad() < evento.getEntradasVendidas()) {
                throw new ValidacionException("La nueva capacidad (" + datosNuevos.getCapacidad() + ") no puede ser menor a las entradas ya vendidas (" + evento.getEntradasVendidas() + ").");
            }
            evento.setCapacidad(datosNuevos.getCapacidad());
            modificado = true;
        }

        if (modificado) {
            eventoRepository.save(evento); // Guardar los cambios
            sistemaNotificaciones.notificarCambioEvento(evento, "El evento '" + evento.getNombre() + "' ha sido modificado.");
        }

        return evento;
    }
}
