
package modelo.validacion;

// No se necesitan imports de Usuario, Evento, etc., aquí si se usa ValidacionContext
// import modelo.usuario.Usuario;
// import modelo.evento.Evento;
// import modelo.entrada.TipoEntrada;

public class ValidarDisponibilidadHandler extends ValidacionHandler {
    @Override
    protected boolean validar(ValidacionContext context) {
        if (context.tipoEntrada == null) {
            this.mensajeError = "Tipo de entrada no especificado para validar disponibilidad.";
            return false;
        }
        if (context.cantidad <= 0) {
            this.mensajeError = "La cantidad de entradas debe ser positiva.";
            return false;
        }

        boolean disponible = context.tipoEntrada.getCantidadDisponible() >= context.cantidad;

        if (!disponible) {
            this.mensajeError = "No hay suficientes entradas disponibles para el tipo '" +
                                context.tipoEntrada.getNombre() + "'. Solicitadas: " + context.cantidad +
                                ", Disponibles: " + context.tipoEntrada.getCantidadDisponible() + ".";
            return false;
        }

        // Si hay un siguiente manejador, pasar la validación.
        // La lógica de llamar al siguiente está en procesarValidacion() de la clase base.
        return true;
    }
}
