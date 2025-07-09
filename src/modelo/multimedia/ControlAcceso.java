
package modelo.multimedia;

import modelo.usuario.Usuario;
import modelo.usuario.Asistente; // Para instanceof
import modelo.usuario.Organizador; // Para instanceof
// Asumimos que el evento tiene una forma de saber si un usuario compró entrada para él.
// Esto es complejo y depende de cómo se estructuren las compras y el acceso a esa info.
// import modelo.evento.Evento;
// import modelo.compra.Compra;

public class ControlAcceso {

    // Tipos de recurso (podrían ser enums o constantes)
    public static final String RECURSO_VIDEO_PROMOCIONAL = "VIDEO_PROMOCIONAL";
    public static final String RECURSO_IMAGEN_EVENTO = "IMAGEN_EVENTO";
    public static final String RECURSO_VIDEO_GRABACION_EVENTO = "VIDEO_GRABACION_EVENTO_PAGO";
    public static final String RECURSO_ESTADISTICAS_EVENTO = "ESTADISTICAS_EVENTO_ORGANIZADOR";

    public ControlAcceso() {
        // Constructor podría inicializar reglas de acceso desde una config, etc.
    }

    public String obtenerNivelAcceso(Usuario usuario) {
        if (usuario == null) {
            return "ANONIMO";
        }
        if (usuario instanceof Organizador) {
            return "ORGANIZADOR";
        }
        if (usuario instanceof Asistente) {
            return "ASISTENTE";
        }
        return "USUARIO_REGISTRADO_GENERICO"; // Si hubiera otros tipos de Usuario
    }

    public boolean verificarPermisoUsuario(Usuario usuario, String tipoRecurso, Object contextoAdicional) {
        // contextoAdicional podría ser el Evento al que pertenece el recurso, para validaciones más finas.

        String nivelAcceso = obtenerNivelAcceso(usuario);
        System.out.println("ControlAcceso: Verificando permiso para Usuario (" + (usuario != null ? usuario.getNombre() : "Anónimo") +
                           ", Nivel: " + nivelAcceso + ") sobre TipoRecurso: " + tipoRecurso);

        switch (tipoRecurso) {
            case RECURSO_VIDEO_PROMOCIONAL:
            case RECURSO_IMAGEN_EVENTO:
                // Generalmente, imágenes y videos promocionales son de acceso público o para cualquier usuario registrado.
                return true; // Permitir siempre por ahora

            case RECURSO_VIDEO_GRABACION_EVENTO_PAGO:
                if ("ASISTENTE".equals(nivelAcceso) || "ORGANIZADOR".equals(nivelAcceso)) {
                    // Aquí la lógica real sería más compleja:
                    // 1. Extraer el Evento del 'contextoAdicional'.
                    // 2. Para Asistente: Verificar si ha comprado entrada para ESE evento.
                    //    (ej. usuario.getHistorialCompras().stream().anyMatch(c -> c.getEvento().equals(eventoContexto) && c.getEstado() == EstadoCompra.CONFIRMADA))
                    // 3. Para Organizador: Verificar si es el organizador de ESE evento.
                    //    (ej. ((Organizador)usuario).getEventosCreados().contains(eventoContexto))
                    System.out.println("ControlAcceso: (Simulación) " + nivelAcceso + " tiene permiso para " + tipoRecurso + ". Se necesitaría validación de compra/propiedad del evento.");
                    return true;
                }
                System.out.println("ControlAcceso: Acceso denegado a " + tipoRecurso + " para nivel " + nivelAcceso);
                return false;

            case RECURSO_ESTADISTICAS_EVENTO_ORGANIZADOR:
                if ("ORGANIZADOR".equals(nivelAcceso)) {
                    // Lógica adicional: verificar si el organizador es el propietario del evento de las estadísticas.
                    // Evento eventoContexto = (Evento) contextoAdicional;
                    // if (((Organizador)usuario).getEventosCreados().contains(eventoContexto)) return true;
                    System.out.println("ControlAcceso: (Simulación) " + nivelAcceso + " tiene permiso para " + tipoRecurso + ". Se necesitaría validación de propiedad del evento.");
                    return true;
                }
                System.out.println("ControlAcceso: Acceso denegado a " + tipoRecurso + " para nivel " + nivelAcceso);
                return false;

            default:
                System.out.println("ControlAcceso: Tipo de recurso '" + tipoRecurso + "' desconocido. Denegando acceso por defecto.");
                return false;
        }
    }

    public void registrarAcceso(Usuario usuario, String urlRecurso, boolean accesoPermitido) {
        String nombreUsuario = (usuario != null) ? usuario.getNombre() : "Anónimo";
        String estadoAcceso = accesoPermitido ? "PERMITIDO" : "DENEGADO";
        System.out.println("ControlAcceso LOG: Usuario '" + nombreUsuario + "' intentó acceder a '" + urlRecurso + "'. Acceso: " + estadoAcceso);
        // Aquí se podría escribir a un log de auditoría más persistente.
    }
}
