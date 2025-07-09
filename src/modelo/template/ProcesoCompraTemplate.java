
package modelo.template;

import modelo.usuario.Usuario;
import modelo.evento.Evento;
import modelo.entrada.TipoEntrada;
import modelo.compra.Compra;
import modelo.compra.MetodoPago;
import modelo.validacion.ValidacionContext; // Necesario para el contexto de validación

public abstract class ProcesoCompraTemplate {

    // Método final que define el esqueleto del algoritmo de compra.
    // Ahora también acepta un código de descuento.
    public final Compra realizarCompraCompleta(Usuario usuario,
                                               Evento evento,
                                               TipoEntrada tipoEntrada,
                                               int cantidad,
                                               MetodoPago metodoPago,
                                               String codigoDescuento) {

        // 1. Iniciar la compra: crear el objeto Compra con estado inicial.
        Compra compra = iniciarCompra(usuario, evento, tipoEntrada, cantidad, metodoPago);
        if (compra == null) {
            System.err.println("ProcesoCompraTemplate: Falló la inicialización de la compra.");
            return null;
        }

        // 2. Validar disponibilidad y otras condiciones.
        // Se crea el contexto para las validaciones.
        ValidacionContext contextoValidacion = new ValidacionContext(usuario, evento, tipoEntrada, cantidad, compra);
        if (!validarDisponibilidadYCondiciones(compra, contextoValidacion)) {
            // El mensaje de error debería estar en el handler que falló o en la compra.
            System.err.println("ProcesoCompraTemplate: Validación de disponibilidad y condiciones falló.");
            // Aquí se podría establecer el estado de la compra a CANCELADA o similar.
            // compra.setEstado(EstadoCompra.CANCELADA);
            return compra; // Devolver la compra en estado fallido/pendiente
        }

        // 3. Aplicar promociones/descuentos.
        // Este método ahora puede modificar el total de la compra.
        double totalAntesDePromociones = compra.getTotalPagado();
        double totalDespuesDePromociones = aplicarPromocionesSiCorresponde(compra, codigoDescuento);
        // compra.setTotalPagado(totalDespuesDePromociones); // El método aplicarPromociones ya debería hacerlo.
        if (totalDespuesDePromociones < totalAntesDePromociones) {
            System.out.println("ProcesoCompraTemplate: Total actualizado después de promociones a: " + totalDespuesDePromociones);
        }


        // 4. Procesar el pago.
        // Este método debería actualizar el estado de la compra internamente.
        if (!procesarPago(compra)) {
            System.err.println("ProcesoCompraTemplate: Procesamiento de pago falló.");
            // El estado de la compra debería haber sido actualizado por procesarPago (ej. a CANCELADA)
            return compra; // Devolver la compra en estado fallido
        }

        // 5. Generar entradas y actualizar disponibilidad.
        // Solo si el pago fue exitoso (Compra debería estar en estado CONFIRMADA o similar).
        // Este método también actualiza la disponibilidad del TipoEntrada.
        generarEntradasYActualizarDisponibilidad(compra);

        // 6. Notificar al usuario.
        notificarUsuario(compra);

        // 7. Finalizar la compra (ej. añadir a historial, logging).
        finalizarCompra(compra);

        return compra;
    }

    // Métodos abstractos (primitivas) a ser implementados por subclases concretas.
    protected abstract Compra iniciarCompra(Usuario usr, Evento evt, TipoEntrada tipo, int cant, MetodoPago metodoPago);

    protected abstract boolean validarDisponibilidadYCondiciones(Compra compra, ValidacionContext context);

    // Devuelve el nuevo total después de aplicar promociones. La Compra también se actualiza.
    protected abstract double aplicarPromocionesSiCorresponde(Compra compra, String codigoDescuento);

    // Procesa el pago y actualiza el estado de la Compra. Devuelve true si el pago fue exitoso.
    protected abstract boolean procesarPago(Compra compra);

    protected abstract void generarEntradasYActualizarDisponibilidad(Compra compra);

    protected abstract void notificarUsuario(Compra compra);

    protected abstract void finalizarCompra(Compra compra);
}
