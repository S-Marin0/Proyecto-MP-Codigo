package controlador;

import modelo.usuario.Usuario;
import modelo.usuario.Asistente;
import modelo.usuario.Organizador;
import modelo.usuario.RepositorioUsuarioMemoria;
import modelo.recomendacion.GestorRecomendacionesStrategy; // Para crear Asistente
import modelo.recomendacion.IRepositorioEvento; // Para el GestorRecomendaciones
import modelo.recomendacion.ConfiguradorStrategys; // Para el GestorRecomendaciones
import modelo.recomendacion.RecomendacionPorPopularidad; // Estrategia inicial por defecto
import modelo.excepciones.ValidacionException;
import modelo.excepciones.EntidadNoEncontradaException;
import modelo.excepciones.OperacionInvalidaException;


import java.util.Optional;
import java.util.UUID;

public class UsuarioController {
    private final RepositorioUsuarioMemoria repositorioUsuario;
    private final IRepositorioEvento repositorioEvento; // Necesario para el GestorRecomendaciones del Asistente
    private final ConfiguradorStrategys configuradorStrategys; // Para el GestorRecomendaciones

    public UsuarioController(RepositorioUsuarioMemoria repositorioUsuario, IRepositorioEvento repoEvento, ConfiguradorStrategys configStrategys) {
        if (repositorioUsuario == null) throw new IllegalArgumentException("RepositorioUsuarioMemoria no puede ser nulo.");
        if (repoEvento == null) throw new IllegalArgumentException("IRepositorioEvento no puede ser nulo.");
        if (configStrategys == null) throw new IllegalArgumentException("ConfiguradorStrategys no puede ser nulo.");

        this.repositorioUsuario = repositorioUsuario;
        this.repositorioEvento = repoEvento;
        this.configuradorStrategys = configStrategys;
    }

    public Usuario registrarAsistente(String nombre, String email, String password) {
        if (nombre == null || nombre.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            throw new ValidacionException("Los datos de registro del asistente (nombre, email, password) no pueden ser vacíos.");
        }

        if (repositorioUsuario.buscarPorEmail(email).isPresent()) {
            throw new OperacionInvalidaException("El email '" + email + "' ya está registrado.");
        }

        String id = UUID.randomUUID().toString();
        GestorRecomendacionesStrategy gestorRecomendaciones =
            new GestorRecomendacionesStrategy(repositorioEvento, new RecomendacionPorPopularidad(), configuradorStrategys);

        Asistente nuevoAsistente = new Asistente(id, nombre, email, password, gestorRecomendaciones);
        repositorioUsuario.guardarUsuario(nuevoAsistente);
        // System.out.println("UsuarioController: Asistente '" + nombre + "' registrado con éxito. ID: " + id); // Log en consola, considerar un sistema de logging más formal si es necesario
        return nuevoAsistente;
    }

    public Usuario registrarOrganizador(String nombre, String email, String password, String infoContacto) {
        if (nombre == null || nombre.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty() ||
            infoContacto == null || infoContacto.trim().isEmpty()) {
            throw new ValidacionException("Los datos de registro del organizador (nombre, email, password, infoContacto) no pueden ser vacíos.");
        }

        if (repositorioUsuario.buscarPorEmail(email).isPresent()) {
            throw new OperacionInvalidaException("El email '" + email + "' ya está registrado.");
        }

        String id = UUID.randomUUID().toString();
        Organizador nuevoOrganizador = new Organizador(id, nombre, email, password, infoContacto);
        repositorioUsuario.guardarUsuario(nuevoOrganizador);
        // System.out.println("UsuarioController: Organizador '" + nombre + "' registrado con éxito. ID: " + id);
        return nuevoOrganizador;
    }

    public Usuario autenticarUsuario(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new ValidacionException("Email y password son requeridos para la autenticación.");
        }

        Usuario usuario = repositorioUsuario.buscarPorEmail(email)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontró usuario con el email '" + email + "'."));

        if (usuario.autenticar(password)) {
            // System.out.println("UsuarioController: Usuario '" + usuario.getNombre() + "' autenticado con éxito.");
            return usuario;
        } else {
            throw new OperacionInvalidaException("Contraseña incorrecta para el email '" + email + "'.");
        }
    }

    public Usuario buscarUsuarioPorEmailConException(String email) {
        return repositorioUsuario.buscarPorEmail(email)
                .orElseThrow(() -> new EntidadNoEncontradaException("Usuario con email '" + email + "' no encontrado."));
    }

    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        return repositorioUsuario.buscarPorEmail(email);
    }


    public Usuario buscarUsuarioPorIdConException(String id) {
         return repositorioUsuario.buscarPorId(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Usuario con ID '" + id + "' no encontrado."));
    }

    public Optional<Usuario> buscarUsuarioPorId(String id) {
        return repositorioUsuario.buscarPorId(id);
    }
}
