package controlador;

import modelo.usuario.Usuario;
import modelo.usuario.Asistente;
import modelo.usuario.Organizador;
import modelo.usuario.RepositorioUsuarioMemoria;
import modelo.recomendacion.IRepositorioEvento;
import modelo.recomendacion.ConfiguradorStrategys;
import modelo.excepciones.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioControllerTests {

    private UsuarioController usuarioController;
    private RepositorioUsuarioMemoria mockRepoUsuario;
    private IRepositorioEvento mockRepoEvento;
    private ConfiguradorStrategys mockConfigStrategys;

    @BeforeEach
    void setUp() {
        mockRepoUsuario = new RepositorioUsuarioMemoria(); // Usar implementación en memoria
        mockRepoEvento = mock(IRepositorioEvento.class);
        mockConfigStrategys = mock(ConfiguradorStrategys.class);
        usuarioController = new UsuarioController(mockRepoUsuario, mockRepoEvento, mockConfigStrategys);
    }

    @Test
    void testRegistrarAsistente_Exitoso() {
        Usuario asistente = usuarioController.registrarAsistente("Asistente Test", "asist@test.com", "pass123");
        assertNotNull(asistente);
        assertTrue(asistente instanceof Asistente);
        assertEquals("Asistente Test", asistente.getNombre());
        assertTrue(mockRepoUsuario.buscarPorEmail("asist@test.com").isPresent());
    }

    @Test
    void testRegistrarAsistente_EmailDuplicado_LanzaOperacionInvalidaException() {
        usuarioController.registrarAsistente("Asistente Uno", "duplicado@test.com", "pass");
        Executable action = () -> usuarioController.registrarAsistente("Asistente Dos", "duplicado@test.com", "pass");
        assertThrows(OperacionInvalidaException.class, action, "El email 'duplicado@test.com' ya está registrado.");
    }

    @Test
    void testRegistrarAsistente_DatosInvalidos_LanzaValidacionException() {
        Executable action = () -> usuarioController.registrarAsistente(null, "email@test.com", "pass");
        assertThrows(ValidacionException.class, action);
        Executable action2 = () -> usuarioController.registrarAsistente("Nombre", "", "pass");
        assertThrows(ValidacionException.class, action2);
    }

    @Test
    void testRegistrarOrganizador_Exitoso() {
        Usuario organizador = usuarioController.registrarOrganizador("Organizador Test", "orgtest@test.com", "pass456", "Contacto Test");
        assertNotNull(organizador);
        assertTrue(organizador instanceof Organizador);
        assertEquals("Organizador Test", organizador.getNombre());
        assertTrue(mockRepoUsuario.buscarPorEmail("orgtest@test.com").isPresent());
    }

    @Test
    void testAutenticarUsuario_Exitoso() {
        usuarioController.registrarAsistente("Usuario Auth", "auth@test.com", "passwordCorrecto");
        Usuario autenticado = usuarioController.autenticarUsuario("auth@test.com", "passwordCorrecto");
        assertNotNull(autenticado);
        assertEquals("Usuario Auth", autenticado.getNombre());
    }

    @Test
    void testAutenticarUsuario_EmailNoEncontrado_LanzaEntidadNoEncontradaException() {
        Executable action = () -> usuarioController.autenticarUsuario("noexiste@test.com", "pass");
        assertThrows(EntidadNoEncontradaException.class, action);
    }

    @Test
    void testAutenticarUsuario_PasswordIncorrecta_LanzaOperacionInvalidaException() {
        usuarioController.registrarAsistente("Usuario PassErr", "passerr@test.com", "passwordReal");
        Executable action = () -> usuarioController.autenticarUsuario("passerr@test.com", "passwordFalsa");
        assertThrows(OperacionInvalidaException.class, action);
    }

    @Test
    void testAutenticarUsuario_DatosInvalidos_LanzaValidacionException() {
        Executable action = () -> usuarioController.autenticarUsuario("", "pass");
        assertThrows(ValidacionException.class, action);
    }
}
