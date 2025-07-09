
package modelo.entrada;

public abstract class TipoEntrada {
    protected String nombre; // Ej: "General", "VIP Platea A", "Early Bird Special"
    protected String descripcionTipo; // Descripción más detallada de lo que incluye este tipo de entrada
    protected double precio;
    protected int cantidadTotal; // Cantidad total original de este tipo de entrada
    protected int cantidadDisponible;
    protected int limiteCompraPorUsuario; // Límite de cuántas de estas entradas puede comprar un usuario

    public TipoEntrada(String nombre, String descripcionTipo, double precio, int cantidadTotal, int limiteCompraPorUsuario) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del tipo de entrada no puede ser nulo o vacío.");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (cantidadTotal < 0) {
            throw new IllegalArgumentException("La cantidad total no puede ser negativa.");
        }
        if (limiteCompraPorUsuario <= 0) {
            // Se podría permitir un límite muy alto si no hay restricción, o usar Integer.MAX_VALUE
            throw new IllegalArgumentException("El límite de compra por usuario debe ser positivo.");
        }
        this.nombre = nombre;
        this.descripcionTipo = descripcionTipo;
        this.precio = precio;
        this.cantidadTotal = cantidadTotal;
        this.cantidadDisponible = cantidadTotal; // Inicialmente, todas están disponibles
        this.limiteCompraPorUsuario = limiteCompraPorUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcionTipo() {
        return descripcionTipo;
    }

    public double getPrecio() {
        return precio;
    }

    public int getCantidadTotal() {
        return cantidadTotal;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public int getLimiteCompraPorUsuario() {
        return limiteCompraPorUsuario;
    }

    public boolean reducirDisponibilidad(int cantidad) {
        if (cantidad <= 0) {
            // No se puede reducir por una cantidad no positiva
            return false;
        }
        if (this.cantidadDisponible >= cantidad) {
            this.cantidadDisponible -= cantidad;
            return true;
        }
        return false; // No hay suficientes entradas disponibles
    }

    public boolean incrementarDisponibilidad(int cantidad) {
        if (cantidad <= 0) {
            // No se puede incrementar por una cantidad no positiva
            return false;
        }
        if (this.cantidadDisponible + cantidad <= this.cantidadTotal) {
            this.cantidadDisponible += cantidad;
            return true;
        }
        // No se puede incrementar más allá de la cantidad total original
        // (a menos que haya una lógica de negocio que lo permita, ej. reasignación)
        return false;
    }

    // Setters (generalmente no se exponen para todos los campos si se gestionan internamente)
    // Podrían ser útiles para un administrador para cambiar el precio o la cantidad total.
    public void setPrecio(double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.precio = precio;
    }

    // Podría necesitarse un método para cambiar la cantidad total si el evento se reajusta
    // public void setCantidadTotal(int nuevaCantidadTotal) { ... lógica compleja aquí ... }


    @Override
    public String toString() {
        return "TipoEntrada [nombre=" + nombre + ", precio=" + precio +
               ", disponibles=" + cantidadDisponible + "/" + cantidadTotal +
               ", limitePorUsuario=" + limiteCompraPorUsuario + "]";
    }
}
