
package modelo.entrada;

public class EntradaGeneral extends TipoEntrada {
    // Constructor específico para EntradaGeneral
    public EntradaGeneral(String nombre, String descripcionTipo, double precio, int cantidadTotal, int limiteCompraPorUsuario) {
        super(nombre, descripcionTipo, precio, cantidadTotal, limiteCompraPorUsuario);
        // No hay atributos adicionales específicos para EntradaGeneral en este momento,
        // pero podrían añadirse aquí si fuera necesario.
    }

    // Si se necesita un constructor más simple con valores por defecto para algunos parámetros:
    public EntradaGeneral(String nombre, double precio, int cantidadTotal, int limiteCompraPorUsuario) {
        super(nombre, "Entrada general estándar", precio, cantidadTotal, limiteCompraPorUsuario);
    }

    // Ejemplo de un constructor aún más simple si se asumen más defaults:
    // public EntradaGeneral(double precio, int cantidadTotal) {
    //     super("General", "Entrada general estándar", precio, cantidadTotal, 10); // Límite por defecto de 10
    // }
}
