
package modelo.compra;

public class MetodoPago {
    private String tipo; // Ej: "TARJETA_CREDITO", "PAYPAL", "PSE"
    private String numeroTarjeta; // Últimos 4 dígitos o referencia encriptada
    private String titular;
    private String fechaVencimiento; // MM/YY
    private String codigoSeguridad; // CVV/CVC, no almacenar después de la transacción inicial si es posible

    // Constructor para un método de pago genérico (ej. PayPal, efectivo en punto)
    public MetodoPago(String tipo, String titular) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de método de pago no puede ser nulo o vacío.");
        }
        this.tipo = tipo;
        this.titular = titular; // Puede ser el email de PayPal o nombre para referencia
    }

    // Constructor específico para tarjetas
    public MetodoPago(String tipo, String numeroTarjeta, String titular, String fechaVencimiento, String codigoSeguridad) {
        this(tipo, titular); // Llama al constructor más genérico
        if (!tipo.toUpperCase().contains("TARJETA")) { // Simple check, podría ser más robusto
            // throw new IllegalArgumentException("Este constructor es para métodos de pago tipo TARJETA.");
        }
        if (numeroTarjeta == null || numeroTarjeta.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de tarjeta no puede ser nulo o vacío.");
        }
        if (fechaVencimiento == null || fechaVencimiento.trim().isEmpty()) { // Podría validar formato MM/YY
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser nula o vacía.");
        }
        // CVV podría ser opcional o no almacenado
        this.numeroTarjeta = numeroTarjeta;
        this.fechaVencimiento = fechaVencimiento;
        this.codigoSeguridad = codigoSeguridad; // Considerar no almacenar este campo a largo plazo
    }

    // Getters
    public String getTipo() {
        return tipo;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public String getTitular() {
        return titular;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public String getCodigoSeguridad() {
        // Por seguridad, este getter podría no existir o devolver un valor enmascarado/nulo
        // si el código no se debe acceder después de la transacción inicial.
        return codigoSeguridad;
    }

    // Setters podrían no ser necesarios si el objeto es inmutable una vez creado,
    // o solo para ciertos campos y con validaciones.
    public void setTipo(String tipo) {
        if (tipo != null && !tipo.trim().isEmpty()) {
            this.tipo = tipo;
        }
    }

    // No se suelen proporcionar setters para detalles sensibles de la tarjeta una vez establecidos.

    @Override
    public String toString() {
        String detalles = "MetodoPago [tipo=" + tipo + ", titular=" + titular;
        if (numeroTarjeta != null) {
            detalles += ", numeroTarjeta=****" + (numeroTarjeta.length() > 4 ? numeroTarjeta.substring(numeroTarjeta.length() - 4) : numeroTarjeta);
        }
        if (fechaVencimiento != null) {
            detalles += ", fechaVencimiento=" + fechaVencimiento;
        }
        detalles += "]";
        return detalles;
    }
}
