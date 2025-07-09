
package modelo.template;

import modelo.compra.Compra;
import modelo.compra.MetodoPago;
import modelo.compra.EstadoCompra;
import modelo.evento.Evento;
import modelo.usuario.Usuario;
import modelo.entrada.TipoEntrada;
import modelo.entrada.EntradaBase; // Para crear la lista de entradas para la Compra
import modelo.facade.ProcesoCompraFacade; // Para delegar partes del proceso
import modelo.notificacion.SistemaNotificaciones; // Para notificar
import modelo.facade.GeneradorEntradas; // Para generar entradas
import modelo.facade.ProcesadorPago; // Para procesar pago
import modelo.validacion.ValidacionContext; // Para validaciones
import modelo.validacion.ValidacionHandler; // Para validaciones


import java.util.ArrayList;
import java.util.List;

public class ProcesoCompraEstandar extends ProcesoCompraTemplate {

    // Estas dependencias serían normalmente inyectadas o obtenidas de algún registro de servicios.
    // Para simplificar, las pasamos al constructor o las instanciamos aquí (menos ideal para pruebas).
    // Si usamos el Facade, el Template podría no necesitar conocer estas dependencias individuales.
    private final ProcesoCompraFacade compraFacade; // Opción 1: Usar el Facade directamente

    // Opción 2: Tener las dependencias individuales si el Template orquesta más finamente
    // private final ProcesadorPago procesadorPago;
    // private final GeneradorEntradas generadorEntradas;
    // private final SistemaNotificaciones sistemaNotificaciones;
    // private final ValidacionHandler cadenaValidacion;


    // Constructor usando el Facade
    public ProcesoCompraEstandar(ProcesoCompraFacade compraFacade) {
        if (compraFacade == null) {
            throw new IllegalArgumentException("ProcesoCompraFacade no puede ser nulo.");
        }
        this.compraFacade = compraFacade;
    }

    // Constructor si se gestionan las dependencias individualmente (menos probable si ya existe Facade)
    // public ProcesoCompraEstandar(ProcesadorPago pp, GeneradorEntradas ge, SistemaNotificaciones sn, ValidacionHandler cv) {
    //     this.procesadorPago = pp;
    //     this.generadorEntradas = ge;
    //     this.sistemaNotificaciones = sn;
    //     this.cadenaValidacion = cv;
    //     this.compraFacade = null; // No se usaría en este caso
    // }


    @Override
    protected Compra iniciarCompra(Usuario usr, Evento evt, TipoEntrada tipo, int cant, MetodoPago metodoPago) {
        // El facade ya crea la compra internamente como parte de su proceso.
        // Aquí, el template method podría simplemente preparar los datos y luego
        // el método principal `realizarCompraCompleta` llamaría al facade.
        // O, si el facade tiene un método específico para "iniciar" una compra (crear el objeto Compra):
        // return this.compraFacade.iniciarCompra(usr, evt, tipo, cant, metodoPago);
        // Por ahora, el constructor de Compra es público y podemos crearla aquí.
        // El facade se encargará de la orquestación completa en `realizarCompraCompleta`.
        // Este método en el template es para crear el objeto Compra inicial.

        List<EntradaBase> entradasBase = new ArrayList<>();
        for (int i = 0; i < cant; i++) {
            entradasBase.add(new EntradaBase(tipo, evt));
        }
        // El total se calculará y posiblemente se ajustará por descuentos más adelante.
        // El facade lo hace, así que podemos pasar un total inicial o 0.
        double totalInicial = tipo.getPrecio() * cant;

        // La lista de Entrada (no EntradaBase) se pasa al constructor de Compra.
        // Convertimos List<EntradaBase> a List<Entrada>
        List<modelo.entrada.Entrada> entradasParaCompra = new ArrayList<>(entradasBase);

        return new Compra(usr, evt, tipo, cant, entradasParaCompra, totalInicial, metodoPago);
    }

    @Override
    protected boolean validarDisponibilidadYCondiciones(Compra compra, ValidacionContext context) {
        // El facade tiene la cadena de validación.
        // El contexto ya debería estar construido por el método principal del template.
        if (this.compraFacade != null) { // Asumiendo que el facade expone su cadena o un método de validación
             // boolean esValido = this.compraFacade.validarCompraGlobalmente(context);
             // El facade actual no expone su cadena directamente, sino que la usa en ejecutarProcesoCompra.
             // Para que el template method funcione como está diseñado, donde cada paso es llamado,
             // el facade necesitaría exponer sub-operaciones o el template necesitaría las dependencias.
             // Por ahora, la validación principal se hará una vez en el facade.
             // Este método podría ser para validaciones adicionales específicas del template.
            System.out.println("Validaciones adicionales específicas del template (si las hay)...");
            return true; // Asumimos que la validación principal la hace el facade.
        }
        // Si no hay facade y tenemos la cadena directamente:
        // return this.cadenaValidacion.procesarValidacion(context);
        return true; // Placeholder si no se usa facade aquí
    }

    @Override
    protected double aplicarPromocionesSiCorresponde(Compra compra, String codigoDescuento) {
        // Esta lógica podría estar en el facade o aquí.
        // Si está en el facade, este método podría simplemente llamar al facade.
        // double totalConDescuento = this.compraFacade.aplicarCodigoDescuento(compra.getTotalPagado(), codigoDescuento);
        // compra.setTotalPagado(totalConDescuento);
        // return totalConDescuento;

        // Simulación local:
        double totalActual = compra.getTotalPagado();
        if (codigoDescuento != null && !codigoDescuento.isEmpty()) {
            if ("DESC15TEM".equals(codigoDescuento)) { // Código específico del template
                System.out.println("ProcesoCompraEstandar: Aplicando descuento del 15% con código: " + codigoDescuento);
                totalActual = totalActual * 0.85;
                compra.setTotalPagado(totalActual);
            } else {
                System.out.println("ProcesoCompraEstandar: Código de descuento '" + codigoDescuento + "' no reconocido por este template.");
            }
        }
        return totalActual;
    }

    @Override
    protected boolean procesarPago(Compra compra) {
        // Delegar al facade o al procesador de pago directamente.
        // return this.compraFacade.procesarPago(compra);
        // Si el facade no tiene un método procesarPago que tome Compra,
        // necesitamos el ProcesadorPago y el MetodoPago.
        if (this.compraFacade != null && compra.getMetodoPagoUtilizado() != null) {
            // Asumimos que ProcesadorPago es accesible o el facade tiene un método.
            // El facade actual no expone procesarPago(Compra), sino que lo hace internamente.
            // Esto significa que el método `realizarCompraCompleta` del Template llamará al facade
            // que hará todo. Los pasos individuales del template son más difíciles de mapear 1:1
            // si el facade encapsula demasiado.
            // Por ahora, este método en el template es un paso lógico. El facade lo ejecutará.
            System.out.println("ProcesoCompraEstandar: El pago será manejado por el Facade.");
            // La simulación real del pago y cambio de estado de la compra la hará el facade.
            return true; // Asumimos que el facade lo manejará.
        }
        // Si tuviéramos procesadorPago aquí:
        // boolean pagoExitoso = this.procesadorPago.procesar(compra.getMetodoPagoUtilizado(), compra.getTotalPagado());
        // if (pagoExitoso) compra.setEstado(EstadoCompra.CONFIRMADA); else compra.setEstado(EstadoCompra.CANCELADA);
        // return pagoExitoso;
        return false; // No se puede procesar sin facade o procesador de pago.
    }

    @Override
    protected void generarEntradasYActualizarDisponibilidad(Compra compra) {
        // Similar al pago, el facade se encarga.
        // this.compraFacade.generarEntradasYActualizarDisponibilidad(compra);
        System.out.println("ProcesoCompraEstandar: Generación de entradas y actualización de disponibilidad será manejada por el Facade.");
        // Si tuviéramos generadorEntradas y acceso al TipoEntrada:
        // if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
        //     compra.getTipoEntradaBase().reducirDisponibilidad(compra.getCantidad());
        //     this.generadorEntradas.generar(compra.getEvento(), compra.getTipoEntradaBase(), compra.getCantidad(), compra);
        // }
    }

    @Override
    protected void notificarUsuario(Compra compra) {
        // this.compraFacade.notificarConfirmacion(compra);
        System.out.println("ProcesoCompraEstandar: Notificación al usuario será manejada por el Facade.");
        // Si tuviéramos sistemaNotificaciones:
        // if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
        //     this.sistemaNotificaciones.enviarConfirmacionCompra(compra.getUsuario(), compra);
        // }
    }

    @Override
    protected void finalizarCompra(Compra compra) {
        System.out.println("ProcesoCompraEstandar: Finalizando la compra ID: " + compra.getId());
        if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
            compra.getUsuario().addCompraAlHistorial(compra); // Asegurar que esté en el historial
            System.out.println("Compra ID: " + compra.getId() + " añadida al historial del usuario " + compra.getUsuario().getNombre());
        } else {
            System.out.println("Compra ID: " + compra.getId() + " no fue confirmada. Estado final: " + compra.getEstado());
        }
        // Cualquier otra lógica de limpieza o logging.
    }
}
