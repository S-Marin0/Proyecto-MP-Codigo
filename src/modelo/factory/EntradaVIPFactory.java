package modelo.factory;

import modelo.entrada.EntradaVIP;
import modelo.entrada.TipoEntrada;
import java.util.List;
import java.util.ArrayList; // Necesario si se pasan beneficios por defecto

public class EntradaVIPFactory implements TipoEntradaFactory {
    // Podríamos tener un constructor si la fábrica necesita configuración,
    // por ejemplo, una lista de beneficios por defecto para ciertos tipos de VIP.
    // private List<String> beneficiosPorDefecto;
    // public EntradaVIPFactory(List<String> beneficiosPorDefecto) {
    //    this.beneficiosPorDefecto = beneficiosPorDefecto;
    // }
    // public EntradaVIPFactory() {
    //    this.beneficiosPorDefecto = new ArrayList<>(); // Lista vacía por defecto
    // }


    @Override
    public TipoEntrada crearTipoEntrada(String nombre,
                                        String descripcionTipo,
                                        double precio,
                                        int cantidadTotal,
                                        int limiteCompraPorUsuario) {
        // Para crear una EntradaVIP, necesitamos la lista de beneficios adicionales.
        // La interfaz TipoEntradaFactory no contempla pasar esta lista específica.
        // Hay varias formas de manejar esto:
        // 1. La fábrica podría tener una lista de beneficios por defecto.
        // 2. La fábrica podría requerir que 'descripcionTipo' contenga información parseable sobre los beneficios (no ideal).
        // 3. Podríamos modificar la interfaz TipoEntradaFactory o tener una interfaz más específica para EntradasVIP.

        // Opción 1: Usar una lista de beneficios vacía o predeterminada si no se pueden pasar explícitamente.
        List<String> beneficios = new ArrayList<>();
        // Si tuviéramos beneficiosPorDefecto en el constructor de la factory:
        // beneficios.addAll(this.beneficiosPorDefecto);

        // O podríamos asumir que el nombre/descripción da una pista, ej:
        if (nombre.toLowerCase().contains("gold")) {
            beneficios.add("Acceso a zona Gold");
            beneficios.add("Bebida de cortesía");
        } else if (nombre.toLowerCase().contains("platinum")) {
            beneficios.add("Acceso a zona Platinum");
            beneficios.add("Meet & Greet");
            beneficios.add("Merchandising exclusivo");
        } else {
            beneficios.add("Beneficio VIP estándar"); // Un beneficio genérico
        }

        return new EntradaVIP(nombre, descripcionTipo, precio, cantidadTotal, limiteCompraPorUsuario, beneficios);
    }

    // Si quisiéramos una forma de crear EntradasVIP pasando explícitamente los beneficios,
    // necesitaríamos un método que no esté en la interfaz TipoEntradaFactory,
    // o una interfaz diferente.
    public EntradaVIP crearEntradaVIPConBeneficios(String nombre,
                                                 String descripcionTipo,
                                                 double precio,
                                                 int cantidadTotal,
                                                 int limiteCompraPorUsuario,
                                                 List<String> beneficios) {
        return new EntradaVIP(nombre, descripcionTipo, precio, cantidadTotal, limiteCompraPorUsuario, beneficios);
    }
}
