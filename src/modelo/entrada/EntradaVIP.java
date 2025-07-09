
package modelo.entrada;

import java.util.ArrayList;
import java.util.List;

public class EntradaVIP extends TipoEntrada {
    private List<String> beneficiosAdicionales;

    public EntradaVIP(String nombre, String descripcionTipo, double precio, int cantidadTotal, int limiteCompraPorUsuario, List<String> beneficiosAdicionales) {
        super(nombre, descripcionTipo, precio, cantidadTotal, limiteCompraPorUsuario);
        this.beneficiosAdicionales = beneficiosAdicionales != null ? new ArrayList<>(beneficiosAdicionales) : new ArrayList<>();
    }

    // Constructor simplificado si algunos beneficios son estándar o la descripción es genérica
    public EntradaVIP(String nombre, double precio, int cantidadTotal, int limiteCompraPorUsuario, List<String> beneficiosAdicionales) {
        super(nombre, "Entrada VIP con acceso y beneficios exclusivos", precio, cantidadTotal, limiteCompraPorUsuario);
        this.beneficiosAdicionales = beneficiosAdicionales != null ? new ArrayList<>(beneficiosAdicionales) : new ArrayList<>();
    }

    public List<String> getBeneficiosAdicionales() {
        // Devuelve una copia para evitar modificaciones externas de la lista interna
        return new ArrayList<>(beneficiosAdicionales);
    }

    public void agregarBeneficio(String beneficio) {
        if (beneficio != null && !beneficio.trim().isEmpty()) {
            this.beneficiosAdicionales.add(beneficio);
        }
    }

    @Override
    public String getDescripcionTipo() {
        // Se podría sobreescribir para incluir dinámicamente los beneficios en la descripción
        String descSuper = super.getDescripcionTipo();
        if (beneficiosAdicionales != null && !beneficiosAdicionales.isEmpty()) {
            return descSuper + ". Beneficios VIP: " + String.join(", ", beneficiosAdicionales);
        }
        return descSuper;
    }
}
