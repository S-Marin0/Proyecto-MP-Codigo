
package modelo.entrada;

import java.util.List;
import java.util.ArrayList; // Importar si se va a crear una copia

public class EntradaConMerchandising extends EntradaDecorator {
    private List<String> itemsIncluidos;

    public EntradaConMerchandising(Entrada entrada, List<String> items) {
        super(entrada);
        if (items == null || items.isEmpty()) {
            // Podría ser una lista vacía si no hay items específicos, o lanzar error si se espera al menos uno.
            // Por ahora, permitimos lista vacía pero creamos una instancia para evitar NPE.
            this.itemsIncluidos = new ArrayList<>();
        } else {
            this.itemsIncluidos = new ArrayList<>(items); // Guardar copia
        }
    }

    @Override
    public double getPrecio() {
        // Asumimos que el merchandising incluido no altera el precio base de la entrada decorada.
        // Si el merchandising tuviera un costo adicional que se suma aquí, se añadiría:
        // return entradaDecorada.getPrecio() + calcularCostoAdicionalMerchandising();
        return entradaDecorada.getPrecio();
    }

    @Override
    public String getDescripcion() {
        if (itemsIncluidos.isEmpty()) {
            return entradaDecorada.getDescripcion() + " (con merchandising especial)"; // Mensaje genérico si no hay items específicos listados
        }
        return entradaDecorada.getDescripcion() + " (incluye merchandising: " + String.join(", ", itemsIncluidos) + ")";
    }

    public List<String> getItems() {
        return new ArrayList<>(itemsIncluidos); // Devuelve copia
    }
}
