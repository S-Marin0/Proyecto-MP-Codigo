
package modelo.multimedia;

import java.util.HashMap;
import java.util.Map;
import modelo.usuario.Usuario;

public class ProxyMultimedia implements RecursoMultimedia {
    private RecursoReal recursoReal; // El objeto real se creará bajo demanda (lazy initialization)
    private final String url;
    private final String tipoRecurso; // Ej. ControlAcceso.RECURSO_VIDEO_PROMOCIONAL
    private final ControlAcceso controlAcceso;

    // Caché simple en memoria. Para un sistema real, se usaría una caché más robusta (ej. EHCache, Guava Cache).
    // La clave podría ser la URL o un identificador único del recurso.
    private static final Map<String, byte[]> cacheContenido = new HashMap<>(); // Static para compartir caché entre proxies de la misma URL

    public ProxyMultimedia(String url, String tipoRecurso, ControlAcceso controlAcceso) {
        if (url == null || url.trim().isEmpty()) throw new IllegalArgumentException("URL no puede ser nula o vacía.");
        if (tipoRecurso == null || tipoRecurso.trim().isEmpty()) throw new IllegalArgumentException("Tipo de recurso no puede ser nulo o vacío.");
        if (controlAcceso == null) throw new IllegalArgumentException("ControlAcceso no puede ser nulo.");

        this.url = url;
        this.tipoRecurso = tipoRecurso;
        this.controlAcceso = controlAcceso;
        // this.recursoReal no se inicializa aquí (lazy loading).
    }

    // El método mostrar ahora necesita el usuario para verificar permisos.
    public byte[] mostrar(Usuario usuario) {
        // El contexto adicional podría ser el Evento al que está asociado este recurso,
        // si ControlAcceso lo necesita para una validación más fina.
        Object contextoAdicionalParaPermiso = null; // Ejemplo: podría ser un objeto Evento

        if (!verificarPermiso(usuario, contextoAdicionalParaPermiso)) {
            controlAcceso.registrarAcceso(usuario, this.url, false); // Registrar intento fallido
            System.err.println("ProxyMultimedia: Acceso denegado para usuario " + (usuario != null ? usuario.getNombre() : "Anónimo") + " al recurso: " + this.url);
            return null; // O lanzar una excepción de acceso denegado
        }

        // Si tiene permiso, intentar cargar/servir el recurso
        byte[] contenido = cargarRecursoConCache();

        if (contenido != null) {
            controlAcceso.registrarAcceso(usuario, this.url, true); // Registrar acceso exitoso
        } else {
            // Si el contenido es nulo después de intentar cargar (ej. recurso no encontrado en RecursoReal)
            controlAcceso.registrarAcceso(usuario, this.url, false); // Registrar como fallido si no se pudo cargar
        }
        return contenido;
    }

    // Implementación del método de la interfaz sin usuario (podría denegar o usar un usuario "anónimo")
    @Override
    public byte[] mostrar() {
        System.out.println("ProxyMultimedia: Llamada a mostrar() sin usuario. Se intentará con acceso anónimo.");
        return mostrar(null); // Llama a la versión con usuario, pasando null como usuario (anónimo)
    }


    private boolean verificarPermiso(Usuario usuario, Object contextoAdicional) {
        return controlAcceso.verificarPermisoUsuario(usuario, this.tipoRecurso, contextoAdicional);
    }

    private byte[] cargarRecursoConCache() {
        if (cacheContenido.containsKey(this.url)) {
            System.out.println("ProxyMultimedia: Sirviendo '" + this.url + "' desde la caché.");
            return cacheContenido.get(this.url);
        }

        System.out.println("ProxyMultimedia: '" + this.url + "' no encontrado en caché. Cargando recurso real...");
        if (this.recursoReal == null) {
            // Lazy initialization del RecursoReal
            this.recursoReal = new RecursoReal(this.url);
        }

        byte[] contenidoReal = this.recursoReal.mostrar();

        if (contenidoReal != null) {
            // Solo cachear si el contenido es válido y no demasiado grande (lógica de caché más avanzada)
            cacheContenido.put(this.url, contenidoReal);
            System.out.println("ProxyMultimedia: '" + this.url + "' cargado y añadido a la caché.");
        } else {
            System.err.println("ProxyMultimedia: No se pudo cargar el contenido real para '" + this.url + "'.");
        }
        return contenidoReal;
    }

    @Override
    public String getUrlRecurso() {
        return this.url;
    }

    public String getTipoRecurso() {
        return this.tipoRecurso;
    }

    // Método para limpiar la caché (para pruebas o gestión)
    public static void limpiarCache() {
        cacheContenido.clear();
        System.out.println("ProxyMultimedia: Caché de contenido limpiada.");
    }
}
