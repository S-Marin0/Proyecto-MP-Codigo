package modelo.factory;

import modelo.entrada.EntradaEarlyAccess;
import modelo.entrada.TipoEntrada;

public class EntradaEarlyAccessFactory implements TipoEntradaFactory {

    // Podríamos tener minutos de anticipación por defecto si la fábrica se configura.
    private int minutosAnticipacionPorDefecto;

    public EntradaEarlyAccessFactory() {
        // Establecer un valor por defecto general si no se especifica otro.
        this.minutosAnticipacionPorDefecto = 30; // Ej: 30 minutos por defecto
    }

    public EntradaEarlyAccessFactory(int minutosAnticipacionPorDefecto) {
        if (minutosAnticipacionPorDefecto < 0) {
            throw new IllegalArgumentException("Los minutos de anticipación por defecto no pueden ser negativos.");
        }
        this.minutosAnticipacionPorDefecto = minutosAnticipacionPorDefecto;
    }

    @Override
    public TipoEntrada crearTipoEntrada(String nombre,
                                        String descripcionTipo,
                                        double precio,
                                        int cantidadTotal,
                                        int limiteCompraPorUsuario) {
        // Similar a EntradaVIPFactory, necesitamos los minutos de anticipación.
        // Usaremos el valor por defecto configurado en esta fábrica.
        // En un caso real, la 'descripcionTipo' podría contener esta info para parsear,
        // o se podría inferir del 'nombre', pero usar un default de la factory es más simple aquí.

        int minutosAnticipacion = this.minutosAnticipacionPorDefecto;

        // Ejemplo de inferencia simple (opcional, podría sobrescribir el default):
        if (nombre.toLowerCase().contains("super early")) {
            minutosAnticipacion = 60;
        } else if (nombre.toLowerCase().contains("early bird")) {
            minutosAnticipacion = 45;
        }

        return new EntradaEarlyAccess(nombre, descripcionTipo, precio, cantidadTotal, limiteCompraPorUsuario, minutosAnticipacion);
    }

    // Método específico para crear EntradaEarlyAccess pasando explícitamente los minutos.
    public EntradaEarlyAccess crearEntradaEarlyAccessConMinutos(String nombre,
                                                              String descripcionTipo,
                                                              double precio,
                                                              int cantidadTotal,
                                                              int limiteCompraPorUsuario,
                                                              int minutosAnticipacion) {
        return new EntradaEarlyAccess(nombre, descripcionTipo, precio, cantidadTotal, limiteCompraPorUsuario, minutosAnticipacion);
    }
}
