package controlador;

import modelo.usuario.Usuario;
import modelo.usuario.Asistente;
import modelo.usuario.Organizador;
// import modelo.usuario.RepositorioUsuarioMemoria; // Ya no se usa
import modelo.repositorio.UsuarioRepository; // Se usa el mock de esta interfaz
import modelo.recomendacion.IRepositorioEvento;
import modelo.recomendacion.ConfiguradorStrategys;
// import modelo.recomendacion.GestorRecomendacionesStrategy; // No se usa directamente en estos tests
// import modelo.recomendacion.RecomendacionPorPopularidad; // No se usa directamente en estos tests
import modelo.excepciones.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith; // Para habilitar Mockito
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks; // Para inyectar mocks en el objeto bajo prueba
import org.mockito.Mock; // Para crear mocks
import org.mockito.junit.jupiter.MockitoExtension; // Extensión de JUnit 5 para Mockito

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any; // Para argument matchers


@ExtendWith(MockitoExtension.class) // Habilita la inyección de mocks con @Mock e @InjectMocks
public class UsuarioControllerTests {

    @Mock // Mockito creará un mock de esta interfaz
    private UsuarioRepository mockUsuarioRepository;

    @Mock
    private IRepositorioEvento mockRepoEvento;

    @Mock
    private ConfiguradorStrategys mockConfigStrategys;

    @InjectMocks // Mockito intentará inyectar los mocks declarados con @Mock en esta instancia
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        // La inicialización de mocks y la inyección en usuarioController
        // es manejada por @ExtendWith(MockitoExtension.class), @Mock, y @InjectMocks.
    }

    @Test
    void testRegistrarAsistente_Exitoso() {
        String nombre = "Asistente Test";
        String email = "asist@test.com";
        String password = "pass123";

        when(mockUsuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(mockUsuarioRepository.save(any(Asistente.class))).thenAnswer(invocation -> {
            Asistente a = invocation.getArgument(0);
            // En un test real con una entidad JPA, el ID sería asignado por la BD.
            // Si Asistente tuviera un setId(Long), podríamos simularlo: a.setId(1L);
            return a;
        });

        Usuario asistente = usuarioController.registrarAsistente(nombre, email, password);

        assertNotNull(asistente);
        assertTrue(asistente instanceof Asistente);
        assertEquals(nombre, asistente.getNombre());
        assertEquals(email, asistente.getEmail());

        verify(mockUsuarioRepository).findByEmail(email);
        verify(mockUsuarioRepository).save(any(Asistente.class));
    }

    @Test
    void testRegistrarAsistente_EmailDuplicado_LanzaOperacionInvalidaException() {
        String email = "duplicado@test.com";
        // Simulamos que el repositorio encuentra un usuario con ese email
        when(mockUsuarioRepository.findByEmail(email)).thenReturn(Optional.of(mock(Asistente.class)));

        Executable action = () -> usuarioController.registrarAsistente("Asistente Dos", email, "pass");

        assertThrows(OperacionInvalidaException.class, action);
        verify(mockUsuarioRepository).findByEmail(email); // Verificar que se llamó a findByEmail
        verify(mockUsuarioRepository, never()).save(any(Usuario.class)); // Asegurar que save no se llamó
    }

    @Test
    void testRegistrarAsistente_DatosInvalidos_LanzaValidacionException() {
        Executable action = () -> usuarioController.registrarAsistente(null, "email@test.com", "pass");
        assertThrows(ValidacionException.class, action);
        verifyNoInteractions(mockUsuarioRepository);
    }

    @Test
    void testRegistrarOrganizador_Exitoso() {
        String nombre = "Organizador Test";
        String email = "orgtest@test.com";
        String password = "pass456";
        String infoContacto = "Contacto Test";

        when(mockUsuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(mockUsuarioRepository.save(any(Organizador.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario organizador = usuarioController.registrarOrganizador(nombre, email, password, infoContacto);

        assertNotNull(organizador);
        assertTrue(organizador instanceof Organizador);
        assertEquals(nombre, organizador.getNombre());
        assertEquals(email, organizador.getEmail());
        assertEquals(infoContacto, ((Organizador) organizador).getInfoContacto());

        verify(mockUsuarioRepository).findByEmail(email);
        verify(mockUsuarioRepository).save(any(Organizador.class));
    }

    @Test
    void testAutenticarUsuario_Exitoso() {
        String email = "auth@test.com";
        String password = "passwordCorrecto";
        Asistente mockAsistentePersistido = mock(Asistente.class); // Este es el usuario que el repo devolvería

        when(mockAsistentePersistido.autenticar(password)).thenReturn(true); // El método autenticar de la entidad es el que valida
        when(mockUsuarioRepository.findByEmail(email)).thenReturn(Optional.of(mockAsistentePersistido));

        Usuario autenticado = usuarioController.autenticarUsuario(email, password);

        assertNotNull(autenticado);
        assertEquals(mockAsistentePersistido, autenticado);

        verify(mockUsuarioRepository).findByEmail(email);
        verify(mockAsistentePersistido).autenticar(password);
    }

    @Test
    void testAutenticarUsuario_EmailNoEncontrado_LanzaEntidadNoEncontradaException() {
        String email = "noexiste@test.com";
        when(mockUsuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

        Executable action = () -> usuarioController.autenticarUsuario(email, "pass");

        assertThrows(EntidadNoEncontradaException.class, action);
        verify(mockUsuarioRepository).findByEmail(email);
    }

    @Test
    void testAutenticarUsuario_PasswordIncorrecta_LanzaOperacionInvalidaException() {
        String email = "passerr@test.com";
        String passwordFalsa = "passwordFalsa";
        Asistente mockAsistentePersistido = mock(Asistente.class);

        when(mockAsistentePersistido.autenticar(passwordFalsa)).thenReturn(false);
        when(mockUsuarioRepository.findByEmail(email)).thenReturn(Optional.of(mockAsistentePersistido));

        Executable action = () -> usuarioController.autenticarUsuario(email, passwordFalsa);

        assertThrows(OperacionInvalidaException.class, action);
        verify(mockUsuarioRepository).findByEmail(email);
        verify(mockAsistentePersistido).autenticar(passwordFalsa);
    }

    @Test
    void testAutenticarUsuario_DatosInvalidos_LanzaValidacionException() {
        Executable action = () -> usuarioController.autenticarUsuario("", "pass");
        assertThrows(ValidacionException.class, action);
        verifyNoInteractions(mockUsuarioRepository);
    }

    @Test
    void testBuscarUsuarioPorIdConException_Exitoso() {
        Long id = 1L;
        Asistente mockAsistente = mock(Asistente.class); // Simula la entidad encontrada
        when(mockUsuarioRepository.findById(id)).thenReturn(Optional.of(mockAsistente));

        Usuario encontrado = usuarioController.buscarUsuarioPorIdConException(id);
        assertNotNull(encontrado);
        assertEquals(mockAsistente, encontrado);
        verify(mockUsuarioRepository).findById(id);
    }

    @Test
    void testBuscarUsuarioPorIdConException_NoEncontrado_LanzaEntidadNoEncontradaException() {
        Long id = 99L;
        when(mockUsuarioRepository.findById(id)).thenReturn(Optional.empty());

        Executable action = () -> usuarioController.buscarUsuarioPorIdConException(id);
        assertThrows(EntidadNoEncontradaException.class, action);
        verify(mockUsuarioRepository).findById(id);
    }
}
