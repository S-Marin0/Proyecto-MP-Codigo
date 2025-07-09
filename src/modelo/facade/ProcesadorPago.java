
package modelo.facade;

import modelo.compra.MetodoPago;

public class ProcesadorPago {
    // El monto a procesar ahora es un parámetro
    public boolean procesar(MetodoPago metodoPago, double monto) {
        if (metodoPago == null) {
            System.err.println("Error: Método de pago no proporcionado.");
            return false;
        }
        if (monto <= 0) {
            System.err.println("Error: El monto a pagar debe ser positivo.");
            return false;
        }

        System.out.println(String.format("Procesando pago de %.2f € con método: %s (Titular: %s)",
                           monto, metodoPago.getTipo(), metodoPago.getTitular()));

        // Aquí iría la lógica real de interacción con una pasarela de pago.
        // Por ejemplo, validar los detalles de la tarjeta, verificar fondos, etc.
        // Para la simulación, simplemente devolvemos true.

        // Simulación de posibles fallos basados en el tipo de tarjeta (ejemplo muy simple)
        if ("TARJETA_CREDITO".equalsIgnoreCase(metodoPago.getTipo()) && metodoPago.getNumeroTarjeta() != null && metodoPago.getNumeroTarjeta().endsWith("1111")) {
            System.err.println("Simulación: Pago rechazado para tarjeta terminada en 1111.");
            return false;
        }

        System.out.println("Pago procesado exitosamente.");
        return true;
    }

    // Podría tener un método para revertir pagos si la lógica de compensación es necesaria
    public boolean revertirPago(String transaccionId, double monto) {
        System.out.println(String.format("Intentando revertir pago para transacción ID: %s por monto: %.2f", transaccionId, monto));
        // Lógica de reversión
        return true; // Simulación
    }
}
