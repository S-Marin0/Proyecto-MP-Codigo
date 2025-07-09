
package modelo.entrada;

public class EntradaEarlyAccess extends TipoEntrada {
    private int minutosAnticipacionAcceso; // Tiempo en minutos antes de la apertura general

    public EntradaEarlyAccess(String nombre, String descripcionTipo, double precio, int cantidadTotal, int limiteCompraPorUsuario, int minutosAnticipacionAcceso) {
        super(nombre, descripcionTipo, precio, cantidadTotal, limiteCompraPorUsuario);
        if (minutosAnticipacionAcceso < 0) {
            throw new IllegalArgumentException("Los minutos de anticipación para Early Access no pueden ser negativos.");
        }
        this.minutosAnticipacionAcceso = minutosAnticipacionAcceso;
    }

    // Constructor simplificado
    public EntradaEarlyAccess(String nombre, double precio, int cantidadTotal, int limiteCompraPorUsuario, int minutosAnticipacionAcceso) {
        super(nombre, "Entrada con acceso anticipado al evento", precio, cantidadTotal, limiteCompraPorUsuario);
        if (minutosAnticipacionAcceso < 0) {
            throw new IllegalArgumentException("Los minutos de anticipación para Early Access no pueden ser negativos.");
        }
        this.minutosAnticipacionAcceso = minutosAnticipacionAcceso;
    }

    public int getMinutosAnticipacionAcceso() {
        return minutosAnticipacionAcceso;
    }

    public void setMinutosAnticipacionAcceso(int minutosAnticipacionAcceso) {
        if (minutosAnticipacionAcceso < 0) {
            throw new IllegalArgumentException("Los minutos de anticipación para Early Access no pueden ser negativos.");
        }
        this.minutosAnticipacionAcceso = minutosAnticipacionAcceso;
    }

    @Override
    public String getDescripcionTipo() {
        String descSuper = super.getDescripcionTipo();
        return descSuper + ". Acceso permitido " + minutosAnticipacionAcceso + " minutos antes.";
    }
}
