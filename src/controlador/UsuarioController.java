package controlador;

import modelo.usuario.Usuario;
import modelo.usuario.Asistente;
import modelo.usuario.Organizador;
import modelo.usuario.RepositorioUsuarioMemoria;
import modelo.recomendacion.GestorRecomendacionesStrategy; // Para crear Asistente
import modelo.recomendacion.IRepositorioEvento; // Para el GestorRecomendaciones
import modelo.recomendacion.ConfiguradorStrategys; // Para el GestorRecomendaciones
import modelo.recomendacion.RecomendacionPorPopularidad; // Estrategia inicial por defecto


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
            System.err.println("UsuarioController: Datos de registro de asistente inválidos.");
            return null;
        }

        if (repositorioUsuario.buscarPorEmail(email).isPresent()) {
            System.err.println("UsuarioController: El email '" + email + "' ya está registrado.");
            return null;
        }

        String id = UUID.randomUUID().toString();
        // Crear un GestorRecomendaciones para este asistente.
        // Podría ser compartido o único por asistente. Aquí creamos uno nuevo.
        // La estrategia inicial podría ser por popularidad.
        GestorRecomendacionesStrategy gestorRecomendaciones =
            new GestorRecomendacionesStrategy(repositorioEvento, new RecomendacionPorPopularidad(), configuradorStrategys);

        Asistente nuevoAsistente = new Asistente(id, nombre, email, password, gestorRecomendaciones);
        repositorioUsuario.guardarUsuario(nuevoAsistente);
        System.out.println("UsuarioController: Asistente '" + nombre + "' registrado con éxito. ID: " + id);
        return nuevoAsistente;
    }

    public Usuario registrarOrganizador(String nombre, String email, String password, String infoContacto) {
        if (nombre == null || nombre.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty() ||
            infoContacto == null || infoContacto.trim().isEmpty()) {
            System.err.println("UsuarioController: Datos de registro de organizador inválidos.");
            return null;
        }

        if (repositorioUsuario.buscarPorEmail(email).isPresent()) {
            System.err.println("UsuarioController: El email '" + email + "' ya está registrado.");
            return null;
        }

        String id = UUID.randomUUID().toString();
        Organizador nuevoOrganizador = new Organizador(id, nombre, email, password, infoContacto);
        repositorioUsuario.guardarUsuario(nuevoOrganizador);
        System.out.println("UsuarioController: Organizador '" + nombre + "' registrado con éxito. ID: " + id);
        return nuevoOrganizador;
    }

    public Usuario autenticarUsuario(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("UsuarioController: Email o password no proporcionados para autenticación.");
            return null;
        }

        Optional<Usuario> optUsuario = repositorioUsuario.buscarPorEmail(email);
        if (optUsuario.isPresent()) {
            Usuario usuario = optUsuario.get();
            if (usuario.autenticar(password)) { // El método autenticar está en Usuario y Asistente/Organizador
                System.out.println("UsuarioController: Usuario '" + usuario.getNombre() + "' autenticado con éxito.");
                return usuario;
            } else {
                System.err.println("UsuarioController: Contraseña incorrecta para el email '" + email + "'.");
                return null;
            }
        } else {
            System.err.println("UsuarioController: No se encontró usuario con el email '" + email + "'.");
            return null;
        }
    }

    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        return repositorioUsuario.buscarPorEmail(email);
    }

    public Optional<Usuario> buscarUsuarioPorId(String id) {
        return repositorioUsuario.buscarPorId(id);
    }
}
