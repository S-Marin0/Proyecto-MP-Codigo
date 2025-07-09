package modelo.validacion;

import modelo.compra.Compra;
import modelo.entrada.TipoEntrada;
import modelo.usuario.Usuario;

public class ValidarLimiteCompraUsuarioHandler extends ValidacionHandler {

    @Override
    protected boolean validar(ValidacionContext context) {
        if (context.usuario == null || context.tipoEntrada == null || context.evento == null) {
            this.mensajeError = "Datos insuficientes para validar el límite de compra por usuario (usuario, tipo de entrada o evento nulos).";
            return false;
        }

        int limitePorUsuario = context.tipoEntrada.getLimiteCompraPorUsuario();
        int cantidadSolicitada = context.cantidad;

        if (cantidadSolicitada > limitePorUsuario) {
            this.mensajeError = "La cantidad solicitada (" + cantidadSolicitada +
                                ") excede el límite de compra por usuario (" + limitePorUsuario +
                                ") para el tipo de entrada '" + context.tipoEntrada.getNombre() + "'.";
            return false;
        }

        // Calcular cuántas entradas de este tipo para este evento el usuario ya ha comprado.
        int cantidadYaComprada = 0;
        for (Compra compraAnterior : context.usuario.getHistorialCompras()) {
            // Considerar solo compras confirmadas o no canceladas para el mismo evento y tipo de entrada.
            if (compraAnterior.getEvento().equals(context.evento) && // Asumiendo que Evento tiene equals() bien definido o se compara por ID
                compraAnterior.getTipoEntradaBase().equals(context.tipoEntrada) && // Asumiendo que TipoEntrada tiene equals() o se compara por nombre/ID
                (compraAnterior.getEstado() == modelo.compra.EstadoCompra.CONFIRMADA ||
                 compraAnterior.getEstado() == modelo.compra.EstadoCompra.PENDIENTE)) { // PENDIENTE también cuenta si la compra se va a confirmar
                cantidadYaComprada += compraAnterior.getCantidad();
            }
        }

        if ((cantidadYaComprada + cantidadSolicitada) > limitePorUsuario) {
            this.mensajeError = "Con esta solicitud (" + cantidadSolicitada +
                                "), excedería el límite de compra por usuario (" + limitePorUsuario +
                                "). Usted ya ha adquirido " + cantidadYaComprada +
                                " entradas de este tipo para este evento.";
            return false;
        }

        return true; // La validación pasa
    }
}
