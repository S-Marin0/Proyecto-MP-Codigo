package controlador;

// Imports de Spring (simulados si no estamos en un entorno Spring Boot completo)
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

import modelo.usuario.Usuario;
import modelo.usuario.Asistente;
import modelo.usuario.Organizador;
// import modelo.usuario.RepositorioUsuarioMemoria; // Ya no se usa
import modelo.repositorio.UsuarioRepository; // Nuevo repositorio JPA
import modelo.recomendacion.GestorRecomendacionesStrategy;
import modelo.recomendacion.IRepositorioEvento;
import modelo.recomendacion.ConfiguradorStrategys;
import modelo.recomendacion.RecomendacionPorPopularidad;
import modelo.excepciones.ValidacionException;
import modelo.excepciones.EntidadNoEncontradaException;
import modelo.excepciones.OperacionInvalidaException;

import java.util.Optional;
// import java.util.UUID; // Ya no es necesario si el ID lo genera la BD

// @Service // Anotación de Spring para marcarlo como un bean de servicio
public class UsuarioController { // Podría renombrarse a UsuarioService

    private final UsuarioRepository usuarioRepository; // Repositorio JPA
    private final IRepositorioEvento repositorioEvento; // Para GestorRecomendaciones
    private final ConfiguradorStrategys configuradorStrategys; // Para GestorRecomendaciones

    // @Autowired // Para inyección de dependencias de Spring
    public UsuarioController(UsuarioRepository usuarioRepository,
                             IRepositorioEvento repoEvento,
                             ConfiguradorStrategys configStrategys) {
        if (usuarioRepository == null) throw new IllegalArgumentException("UsuarioRepository no puede ser nulo.");
        if (repoEvento == null) throw new IllegalArgumentException("IRepositorioEvento no puede ser nulo.");
        if (configStrategys == null) throw new IllegalArgumentException("ConfiguradorStrategys no puede ser nulo.");

        this.usuarioRepository = usuarioRepository;
        this.repositorioEvento = repoEvento;
        this.configuradorStrategys = configStrategys;
    }

    // @Transactional // Los métodos que escriben en BD deben ser transaccionales
    public Usuario registrarAsistente(String nombre, String email, String password) {
        if (nombre == null || nombre.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            throw new ValidacionException("Los datos de registro del asistente (nombre, email, password) no pueden ser vacíos.");
        }

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new OperacionInvalidaException("El email '" + email + "' ya está registrado.");
        }

        // El ID ahora lo genera la BD. La entidad Usuario ya no toma ID String en constructor.
        // Asistente nuevoAsistente = new Asistente(nombre, email, password, gestorRecomendaciones);
        // La creación de GestorRecomendaciones sigue igual ya que es transitoria y no afecta a JPA directamente.
        GestorRecomendacionesStrategy gestorRecomendaciones =
            new GestorRecomendacionesStrategy(repositorioEvento, new RecomendacionPorPopularidad(), configuradorStrategys);

        // El constructor de Asistente fue adaptado en el paso de entidades para no tomar ID.
        Asistente nuevoAsistente = new Asistente(nombre, email, password, gestorRecomendaciones);

        return usuarioRepository.save(nuevoAsistente);
    }

    // @Transactional
    public Usuario registrarOrganizador(String nombre, String email, String password, String infoContacto) {
        if (nombre == null || nombre.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty() ||
            infoContacto == null || infoContacto.trim().isEmpty()) {
            throw new ValidacionException("Los datos de registro del organizador (nombre, email, password, infoContacto) no pueden ser vacíos.");
        }

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new OperacionInvalidaException("El email '" + email + "' ya está registrado.");
        }

        // El constructor de Organizador fue adaptado en el paso de entidades.
        Organizador nuevoOrganizador = new Organizador(nombre, email, password, infoContacto);
        return usuarioRepository.save(nuevoOrganizador);
    }

    // @Transactional(readOnly = true) // Transacción de solo lectura para búsquedas
    public Usuario autenticarUsuario(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new ValidacionException("Email y password son requeridos para la autenticación.");
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontró usuario con el email '" + email + "'."));

        // La lógica de autenticar (comparar hash de password) estaría en la entidad Usuario
        // o en un servicio de autenticación dedicado si se usa Spring Security.
        // Por ahora, mantenemos la lógica simple de la entidad.
        if (usuario.autenticar(password)) {
            return usuario;
        } else {
            throw new OperacionInvalidaException("Contraseña incorrecta para el email '" + email + "'.");
        }
    }

    // @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorEmailConException(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntidadNoEncontradaException("Usuario con email '" + email + "' no encontrado."));
    }

    // @Transactional(readOnly = true)
    public Optional<Usuario> buscarUsuarioPorEmail(String email) {
        // Este método puede seguir existiendo si se necesita explícitamente un Optional
        return usuarioRepository.findByEmail(email);
    }

    // @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorIdConException(Long id) { // ID ahora es Long
         return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("Usuario con ID '" + id + "' no encontrado."));
    }

    // @Transactional(readOnly = true)
    public Optional<Usuario> buscarUsuarioPorId(Long id) { // ID ahora es Long
        return usuarioRepository.findById(id);
    }
}
