
package modelo.compra;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID; // Para generar IDs únicos

import modelo.usuario.Usuario;
import modelo.evento.Evento;
import modelo.entrada.Entrada; // La compra es de una o más 'Entrada' (decoradas o base)
import modelo.entrada.TipoEntrada; // Para saber qué tipo de entrada se compró
import modelo.comando.Command;

public class Compra {
    private final String id; // ID único para la compra
    private Usuario usuario;
    private Evento evento;
    private List<Entrada> entradasCompradas; // Las instancias específicas de Entrada generadas
    private TipoEntrada tipoEntradaBase; // El tipo base sobre el cual se generaron/decoraron las entradas
    private int cantidad; // Cantidad de este tipo de entrada
    private double totalPagado;
    private Date fechaCompra;
    private EstadoCompra estado;
    private MetodoPago metodoPagoUtilizado;
    private List<Command> operacionesRealizadas; // Para el historial de comandos sobre esta compra

    public Compra(Usuario usuario, Evento evento, TipoEntrada tipoEntradaBase, int cantidad, List<Entrada> entradasCompradas, double totalPagado, MetodoPago metodoPago) {
        if (usuario == null) throw new IllegalArgumentException("Usuario no puede ser nulo.");
        if (evento == null) throw new IllegalArgumentException("Evento no puede ser nulo.");
        if (tipoEntradaBase == null) throw new IllegalArgumentException("Tipo de entrada base no puede ser nulo.");
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser positiva.");
        if (entradasCompradas == null || entradasCompradas.isEmpty() || entradasCompradas.size() != cantidad) {
            throw new IllegalArgumentException("La lista de entradas compradas no es válida o no coincide con la cantidad.");
        }
        if (totalPagado < 0) throw new IllegalArgumentException("Total pagado no puede ser negativo.");
        // MetodoPago puede ser nulo si la compra está PENDIENTE y aún no se ha pagado

        this.id = UUID.randomUUID().toString(); // Genera un ID único
        this.usuario = usuario;
        this.evento = evento;
        this.tipoEntradaBase = tipoEntradaBase;
        this.cantidad = cantidad;
        this.entradasCompradas = new ArrayList<>(entradasCompradas); // Copia de la lista
        this.totalPagado = totalPagado;
        this.metodoPagoUtilizado = metodoPago;
        this.fechaCompra = new Date(); // Fecha y hora actual
        this.estado = EstadoCompra.PENDIENTE; // Estado inicial
        this.operacionesRealizadas = new ArrayList<>();
    }

    // Getters
    public String getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Evento getEvento() { return evento; }
    public List<Entrada> getEntradasCompradas() { return new ArrayList<>(entradasCompradas); } // Devuelve copia
    public TipoEntrada getTipoEntradaBase() { return tipoEntradaBase; }
    public int getCantidad() { return cantidad; }
    public double getTotalPagado() { return totalPagado; }
    public Date getFechaCompra() { return (Date) fechaCompra.clone(); } // Devuelve copia
    public EstadoCompra getEstado() { return estado; }
    public MetodoPago getMetodoPagoUtilizado() { return metodoPagoUtilizado; }
    public List<Command> getOperacionesRealizadas() { return new ArrayList<>(operacionesRealizadas); } // Devuelve copia

    // Setters (selectivos)
    public void setEstado(EstadoCompra nuevoEstado) {
        if (nuevoEstado == null) throw new IllegalArgumentException("El nuevo estado no puede ser nulo.");
        // Aquí podría haber lógica de transición de estados si fuera necesario
        // Por ejemplo, no se puede pasar de CONFIRMADA a PENDIENTE directamente.
        this.estado = nuevoEstado;
        System.out.println("Compra ID " + this.id + " cambió a estado: " + nuevoEstado);
    }

    public void setMetodoPagoUtilizado(MetodoPago metodoPagoUtilizado) {
        // Se permite establecer/cambiar el método de pago si la compra aún está pendiente, por ejemplo.
        this.metodoPagoUtilizado = metodoPagoUtilizado;
    }

    public void setTotalPagado(double totalPagado) {
        // Podría ser necesario ajustar el total si se aplican descuentos después de iniciar la compra
        if (totalPagado < 0) throw new IllegalArgumentException("Total pagado no puede ser negativo.");
        this.totalPagado = totalPagado;
    }


    // Métodos de negocio (placeholders por ahora, se implementarán con comandos/facade)
    public void generarComprobante() {
        System.out.println("Generando comprobante para la compra ID: " + this.id);
        // Lógica para crear un PDF, email, etc.
    }

    public boolean solicitarReembolso() { // 'Rembolso' tiene 'e', no 'o'
        System.out.println("Solicitando reembolso para la compra ID: " + this.id);
        // Esto típicamente crearía y ejecutaría un ReembolsarCompraCommand
        return false; // Placeholder
    }

    public boolean cancelarCompra() {
        System.out.println("Solicitando cancelación para la compra ID: " + this.id);
        // Esto típicamente crearía y ejecutaría un CancelarCompraCommand
        return false; // Placeholder
    }

    public void agregarOperacion(Command operacion) {
        if (operacion != null) {
            this.operacionesRealizadas.add(operacion);
        }
    }

    @Override
    public String toString() {
        return "Compra [ID=" + id + ", Usuario=" + usuario.getNombre() + ", Evento=" + evento.getNombre() +
               ", TipoEntrada=" + tipoEntradaBase.getNombre() + ", Cantidad=" + cantidad +
               ", Total=" + totalPagado + ", Estado=" + estado + ", Fecha=" + fechaCompra + "]";
    }
}
