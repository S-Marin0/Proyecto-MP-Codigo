
package modelo.validacion;

import modelo.usuario.Usuario;
import modelo.evento.Evento;
import modelo.entrada.TipoEntrada;
import modelo.compra.Compra; // Podría ser útil pasar la compra en construcción

// Contexto de validación para pasar múltiples parámetros fácilmente
class ValidacionContext {
    Usuario usuario;
    Evento evento;
    TipoEntrada tipoEntrada;
    int cantidad;
    Compra compraEnConstruccion; // Opcional, si algunas validaciones necesitan info de la compra

    public ValidacionContext(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad, Compra compra) {
        this.usuario = usuario;
        this.evento = evento;
        this.tipoEntrada = tipoEntrada;
        this.cantidad = cantidad;
        this.compraEnConstruccion = compra;
    }
     public ValidacionContext(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad) {
        this(usuario, evento, tipoEntrada, cantidad, null);
    }
}


public abstract class ValidacionHandler {
    protected ValidacionHandler next;
    protected String mensajeError; // Para almacenar el mensaje si la validación falla

    public ValidacionHandler setNextHandler(ValidacionHandler siguiente) {
        this.next = siguiente;
        return siguiente; // Devuelve el siguiente para encadenamiento fluido
    }

    // Método principal para iniciar la validación en la cadena
    public boolean procesarValidacion(ValidacionContext context) {
        if (!validar(context)) {
            // Si esta validación falla, no continuar con la cadena (o sí, dependiendo de la lógica deseada)
            // Aquí asumimos que si una falla, el proceso se detiene.
            // El mensaje de error se establece en la implementación de validar().
            return false;
        }
        if (next != null) {
            return next.procesarValidacion(context);
        }
        return true; // Todas las validaciones en la cadena pasaron
    }

    // Método abstracto para ser implementado por cada manejador concreto
    // Devuelve true si la validación es exitosa, false si falla.
    // Si falla, debería establecer this.mensajeError.
    protected abstract boolean validar(ValidacionContext context);

    public String getMensajeError() {
        return mensajeError;
    }
}
