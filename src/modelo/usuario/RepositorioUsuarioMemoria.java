package modelo.usuario;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RepositorioUsuarioMemoria {
    private final Map<String, Usuario> usuariosPorId;
    private final Map<String, Usuario> usuariosPorEmail;

    public RepositorioUsuarioMemoria() {
        this.usuariosPorId = new ConcurrentHashMap<>();
        this.usuariosPorEmail = new ConcurrentHashMap<>();
    }

    public void guardarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null || usuario.getEmail() == null) {
            System.err.println("RepositorioUsuarioMemoria: No se puede guardar usuario nulo o sin ID/Email.");
            return;
        }
        if (usuariosPorEmail.containsKey(usuario.getEmail()) && !usuariosPorId.containsKey(usuario.getId())) {
             System.err.println("RepositorioUsuarioMemoria: Ya existe un usuario con el email " + usuario.getEmail());
             // Podría lanzar una excepción aquí.
             return;
        }
        usuariosPorId.put(usuario.getId(), usuario);
        usuariosPorEmail.put(usuario.getEmail().toLowerCase(), usuario); // Guardar email en minúsculas para búsqueda insensible
        System.out.println("RepositorioUsuarioMemoria: Usuario '" + usuario.getNombre() + "' guardado.");
    }

    public Optional<Usuario> buscarPorId(String id) {
        return Optional.ofNullable(usuariosPorId.get(id));
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        return Optional.ofNullable(usuariosPorEmail.get(email.toLowerCase()));
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return new ArrayList<>(usuariosPorId.values());
    }

    public List<Asistente> obtenerTodosLosAsistentes() {
        List<Asistente> asistentes = new ArrayList<>();
        for (Usuario u : usuariosPorId.values()) {
            if (u instanceof Asistente) {
                asistentes.add((Asistente) u);
            }
        }
        return asistentes;
    }

    public List<Organizador> obtenerTodosLosOrganizadores() {
        List<Organizador> organizadores = new ArrayList<>();
        for (Usuario u : usuariosPorId.values()) {
            if (u instanceof Organizador) {
                organizadores.add((Organizador) u);
            }
        }
        return organizadores;
    }

    public boolean eliminarUsuario(String id) {
        Usuario usuario = usuariosPorId.remove(id);
        if (usuario != null) {
            usuariosPorEmail.remove(usuario.getEmail().toLowerCase());
            System.out.println("RepositorioUsuarioMemoria: Usuario ID '" + id + "' eliminado.");
            return true;
        }
        System.out.println("RepositorioUsuarioMemoria: No se encontró usuario con ID '" + id + "' para eliminar.");
        return false;
    }

    public void limpiarRepositorio() {
        usuariosPorId.clear();
        usuariosPorEmail.clear();
        System.out.println("RepositorioUsuarioMemoria: Repositorio de usuarios limpiado.");
    }
}
