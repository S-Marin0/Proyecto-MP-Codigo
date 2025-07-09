package controlador;

import modelo.evento.Evento;
import modelo.evento.EventoBuilder;
import modelo.evento.Imagen;
import modelo.evento.Video;
import modelo.usuario.Organizador;
import modelo.lugar.Lugar;
// import modelo.entrada.TipoEntrada; // No se usa directamente en estos tests
// import modelo.entrada.EntradaGeneralFactory; // No se usa directamente en estos tests
import modelo.notificacion.SistemaNotificaciones;
// import modelo.recomendacion.RepositorioEventoMemoria; // Ya no se usa
import modelo.repositorio.EventoRepository;
import modelo.repositorio.UsuarioRepository;
import modelo.repositorio.LugarRepository;
import modelo.excepciones.*;
import modelo.state.EstadoEventoBorrador;
import modelo.state.EstadoEventoEnCurso;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong; // Para anyLong()
import static org.mockito.ArgumentMatchers.eq;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EventoControllerTests {

    @Mock
    private EventoRepository mockEventoRepository;
    @Mock
    private UsuarioRepository mockUsuarioRepository;
    @Mock
    private LugarRepository mockLugarRepository;
    @Mock
    private SistemaNotificaciones mockSistemaNotificaciones;

    @InjectMocks
    private EventoController eventoController;

    private Organizador organizadorPrueba;
    private Organizador otroOrganizador;
    private Lugar lugarPrueba;
    private Evento eventoDePrueba; // Para reutilizar en tests de edición/estado

    @BeforeEach
    void setUp() {
        // Los mocks son inicializados por MockitoExtension
        organizadorPrueba = new Organizador("Organizador Prueba", "org@test.com", "pass", "Contacto");
        organizadorPrueba.setId(1L); // Simular ID de BD

        otroOrganizador = new Organizador("Otro Organizador", "otro@test.com", "pass", "Otro Contacto");
        otroOrganizador.setId(2L); // Simular ID de BD

        lugarPrueba = new Lugar("Lugar Test", "Dirección Test");
        lugarPrueba.setId(10L); // Simular ID de BD

        // Crear un evento base para pruebas de edición y cambio de estado
        // Es importante que este evento tenga su estado inicializado correctamente.
        eventoDePrueba = new Evento(); // Usa el constructor que inicializa el estado a Borrador
        eventoDePrueba.setId(100L);
        eventoDePrueba.setNombre("Evento Base");
        eventoDePrueba.setOrganizadorInternal(organizadorPrueba); // Asumiendo que tenemos este método para tests
                                                              // o que el constructor de Evento lo maneja
        eventoDePrueba.setLugar(lugarPrueba);
        // eventoDePrueba.setEstadoActualObj(new EstadoEventoBorrador()); // Asegurar estado inicial
    }

    // Helper para crear un Evento con el builder para tests de creación
    // Ya no se usa el ID String en el builder. El nombre es el campo principal ahora.
    private EventoBuilder crearBuilderEventoPrueba(String nombre, Organizador org, Lugar lug) {
        return new EventoBuilder(nombre)
            .withOrganizador(org)
            .withDescripcion("Descripción de prueba")
            .withCategoria("Test")
            .withFecha(new Date(System.currentTimeMillis() + 100000)) // Fecha futura
            .withHora("20:00")
            .withLugar(lug)
            .withCapacidad(100);
    }


    @Test
    void testCrearEvento_Exitoso() {
        when(mockUsuarioRepository.findById(1L)).thenReturn(Optional.of(organizadorPrueba));
        when(mockLugarRepository.findById(anyLong())).thenReturn(Optional.empty()); // Asumir lugar nuevo o no se busca por ID si es nuevo
        when(mockLugarRepository.save(any(Lugar.class))).thenAnswer(inv -> inv.getArgument(0)); // Si se guarda el lugar
        when(mockEventoRepository.save(any(Evento.class))).thenAnswer(invocation -> {
            Evento e = invocation.getArgument(0);
            e.setId(101L); // Simular ID asignado por BD
            // Asegurar que las colecciones tengan la referencia al evento padre (simulado)
            if (e.getImagenes() != null) e.getImagenes().forEach(img -> img.setEvento(e));
            if (e.getVideosPromocionales() != null) e.getVideosPromocionales().forEach(vid -> vid.setEvento(e));
            if (e.getTiposEntrada() != null) e.getTiposEntrada().forEach(te -> te.setEvento(e));
            return e;
        });

        List<Imagen> imagenes = new ArrayList<>();
        imagenes.add(new Imagen("url_img", "desc_img"));
        List<Video> videos = new ArrayList<>();
        videos.add(new Video("url_vid", "tit_vid"));
        List<TipoEntrada> tipos = new ArrayList<>();
        // tipos.add(new TipoEntrada(...)); // Asumir que TipoEntrada tiene constructor adecuado

        Evento eventoCreado = eventoController.crearEvento(
            organizadorPrueba.getId(), "Evento Creación Test", "Desc", "Cat",
            new Date(), "18:00", lugarPrueba, 50,
            imagenes, videos, tipos);

        assertNotNull(eventoCreado);
        assertEquals(organizadorPrueba, eventoCreado.getOrganizador());
        assertNotNull(eventoCreado.getId()); // Verificamos que tiene un ID (simulado)
        verify(mockEventoRepository, times(2)).save(any(Evento.class)); // Una para inicial, otra para relaciones
        verify(mockSistemaNotificaciones).notificarObservers(anyString());
    }

    @Test
    void testCrearEvento_OrganizadorNoEncontrado_LanzaEntidadNoEncontradaException() {
        when(mockUsuarioRepository.findById(99L)).thenReturn(Optional.empty());

        Executable action = () -> eventoController.crearEvento(
            99L, "Evento Sin Org", "D", "C", new Date(), "H", lugarPrueba, 10, null, null, null);

        assertThrows(EntidadNoEncontradaException.class, action);
        verify(mockEventoRepository, never()).save(any(Evento.class));
    }

    // Ya no se pasa ID de evento al crear, la BD lo genera.
    // La unicidad podría basarse en nombre+fecha+organizador, lo que requeriría un método en EventoRepository.
    // @Test
    // void testCrearEvento_IdDuplicado_LanzaOperacionInvalidaException() { ... }


    @Test
    void testEditarEvento_Exitoso() {
        when(mockEventoRepository.findById(100L)).thenReturn(Optional.of(eventoDePrueba));
        when(mockEventoRepository.save(any(Evento.class))).thenReturn(eventoDePrueba); // save devuelve la entidad actualizada

        EventoDTO dto = new EventoDTO();
        dto.setNombre("Nombre Editado");
        dto.setDescripcion("Descripción Editada");
        dto.setCapacidad(150);

        Evento eventoEditado = eventoController.editarEvento(100L, dto, organizadorPrueba.getId());

        assertNotNull(eventoEditado);
        assertEquals("Nombre Editado", eventoEditado.getNombre());
        assertEquals("Descripción Editada", eventoEditado.getDescripcion());
        assertEquals(150, eventoEditado.getCapacidad());
        verify(mockSistemaNotificaciones).notificarCambioEvento(eq(eventoEditado), anyString());
        verify(mockEventoRepository).save(eventoDePrueba);
    }

    @Test
    void testEditarEvento_NoEsPropietario_LanzaOperacionInvalidaException() {
        when(mockEventoRepository.findById(100L)).thenReturn(Optional.of(eventoDePrueba));
        // organizadorPrueba (ID 1L) es el propietario, otroOrganizador (ID 2L) intenta editar

        EventoDTO dto = new EventoDTO();
        dto.setNombre("Intento de Edición");

        Executable action = () -> eventoController.editarEvento(100L, dto, otroOrganizador.getId());
        assertThrows(OperacionInvalidaException.class, action);
        verify(mockEventoRepository, never()).save(any(Evento.class));
    }

    @Test
    void testEditarEvento_EstadoNoEditable_LanzaOperacionInvalidaException() {
        // Simular que el evento está EnCurso
        eventoDePrueba.setEstadoActualObj(new EstadoEventoEnCurso());
        // Necesitamos asegurar que el estadoActualNombre se actualice también
        // eventoDePrueba.reconstruirEstadoDesdeNombre(); // No, setEstadoActualObj ya lo hace.

        when(mockEventoRepository.findById(100L)).thenReturn(Optional.of(eventoDePrueba));

        EventoDTO dto = new EventoDTO();
        dto.setNombre("Intento Edición Estado Malo");

        Executable action = () -> eventoController.editarEvento(100L, dto, organizadorPrueba.getId());
        assertThrows(OperacionInvalidaException.class, action);
    }


    @Test
    void testEliminarEvento_Exitoso() {
        // Para que la notificación de cambio de evento funcione correctamente al eliminar,
        // el evento debe tener un estado que no sea Borrador o Cancelado.
        eventoDePrueba.setEstadoActualObj(new modelo.state.EstadoEventoPublicado());
        when(mockEventoRepository.findById(100L)).thenReturn(Optional.of(eventoDePrueba));
        doNothing().when(mockEventoRepository).deleteById(100L);

        assertDoesNotThrow(() -> eventoController.eliminarEvento(100L, organizadorPrueba.getId()));

        verify(mockEventoRepository).deleteById(100L);
        verify(mockSistemaNotificaciones).notificarCambioEvento(eq(eventoDePrueba), anyString());
    }

    @Test
    void testPublicarEvento_Exitoso() {
        // eventoDePrueba está en Borrador por defecto
        when(mockEventoRepository.findById(100L)).thenReturn(Optional.of(eventoDePrueba));
        when(mockEventoRepository.save(any(Evento.class))).thenReturn(eventoDePrueba);

        assertDoesNotThrow(() -> eventoController.publicarEvento(100L, organizadorPrueba.getId()));

        assertEquals("EstadoEventoPublicado", eventoDePrueba.getEstadoActualNombre());
        verify(mockEventoRepository).save(eventoDePrueba);
        // La notificación la hace el objeto Estado.
    }

}
