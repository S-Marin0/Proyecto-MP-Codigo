
package modelo.entrada;

public class EntradaConDescuento extends EntradaDecorator {
    private double descuento; // Ej: 0.10 para 10%

    public EntradaConDescuento(Entrada entrada, double descuento) {
        super(entrada);
        if (descuento < 0 || descuento > 1) { // Asumiendo que el descuento es una fracción (0.0 a 1.0)
            throw new IllegalArgumentException("El descuento debe estar entre 0.0 (0%) y 1.0 (100%). Valor proporcionado: " + descuento);
        }
        this.descuento = descuento;
    }

    @Override
    public double getPrecio() {
        double precioOriginal = entradaDecorada.getPrecio();
        return precioOriginal * (1 - this.descuento);
    }

    @Override
    public String getDescripcion() {
        return entradaDecorada.getDescripcion() + String.format(" (con %.0f%% de descuento)", this.descuento * 100);
    }
}
