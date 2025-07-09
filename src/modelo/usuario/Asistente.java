
package modelo.usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import modelo.compra.Compra;
import modelo.compra.MetodoPago;
import modelo.comando.Command;
import modelo.comando.ComprarEntradasCommand;
import modelo.comando.CommandInvoker;
import modelo.evento.Evento;
import modelo.entrada.TipoEntrada;
import modelo.facade.ProcesoCompraFacade;
import modelo.recomendacion.GestorRecomendacionesStrategy;
import modelo.reembolso.ProcesadorReembolso; // Para cancelar/reembolsar compra

public class Asistente extends Usuario {
    private CommandInvoker commandInvoker;
    private GestorRecomendacionesStrategy gestorRecomendaciones;

    public Asistente(String id, String nombre, String email, String password, GestorRecomendacionesStrategy gestorRecomendaciones) {
        super(id, nombre, email, password);
        this.commandInvoker = new CommandInvoker();
        if (gestorRecomendaciones == null) {
            throw new IllegalArgumentException("El GestorRecomendacionesStrategy no puede ser nulo para un Asistente.");
        }
        this.gestorRecomendaciones = gestorRecomendaciones;
    }

    public CommandInvoker getCommandInvoker() {
        return commandInvoker;
    }

    public GestorRecomendacionesStrategy getGestorRecomendaciones() {
        return gestorRecomendaciones;
    }

    public void setGestorRecomendaciones(GestorRecomendacionesStrategy gestorRecomendaciones) {
        if (gestorRecomendaciones != null) {
            this.gestorRecomendaciones = gestorRecomendaciones;
        }
    }

    @Override
    public boolean autenticar(String password) {
        return Objects.equals(this.password, password);
    }

    @Override
    public List<Evento> obtenerRecomendaciones() {
        if (this.gestorRecomendaciones == null) {
            System.err.println(this.nombre + ": Gestor de recomendaciones no disponible.");
            return new ArrayList<>();
        }
        this.gestorRecomendaciones.establecerEstrategiaParaUsuario(this);
        return this.gestorRecomendaciones.generarRecomendaciones(this);
    }

    // Implementación del método abstracto de Usuario (firma básica)
    @Override
    public void solicitarCompra(Evento evento, TipoEntrada tipoEntrada, int cantidad) {
         // Esta versión es llamada si no se proveen MetodoPago y Facade.
         // Podría tener una lógica por defecto o lanzar una excepción.
         System.err.println("Asistente.solicitarCompra: Se requiere MetodoPago y ProcesoCompraFacade para una compra completa. Esta es una solicitud básica.");
         throw new UnsupportedOperationException("Para completar la compra, por favor provea MetodoPago y ProcesoCompraFacade. Utilice la firma completa de solicitarCompra en Asistente.");
    }

    // Método sobrecargado específico de Asistente con todos los parámetros necesarios.
    public void solicitarCompra(Evento evento, TipoEntrada tipoEntrada, int cantidad, MetodoPago metodoPago, ProcesoCompraFacade facade, String codigoDescuento) {
        if (evento == null || tipoEntrada == null || cantidad <= 0 || metodoPago == null || facade == null) {
            System.err.println("Error en Asistente.solicitarCompra (completa): Datos de compra o facade inválidos.");
            return;
        }
        System.out.println(this.nombre + " está solicitando comprar " + cantidad + " entradas de tipo '" +
                           tipoEntrada.getNombre() + "' para el evento '" + evento.getNombre() + "'.");

        ComprarEntradasCommand comprarCommand = new ComprarEntradasCommand(this, evento, tipoEntrada, cantidad, metodoPago, facade);
        if (codigoDescuento != null && !codigoDescuento.isEmpty()) {
           comprarCommand.setCodigoDescuento(codigoDescuento);
        }
        this.commandInvoker.executeCommand(comprarCommand);
    }


    // Métodos específicos de Asistente
    public List<Evento> buscarEvento(Filtro filtro, List<Evento> todosLosEventos) {
        List<Evento> eventosFiltrados = new ArrayList<>();
        if (filtro == null || todosLosEventos == null) return eventosFiltrados;

        System.out.println("Buscando eventos con filtro: Categoria=" + filtro.getCategoria() +
                           ", Fecha=" + filtro.getFecha() + ", Ubicacion=" + filtro.getUbicacion());

        for (Evento ev : todosLosEventos) {
            boolean match = true;
            if (filtro.getCategoria() != null && !filtro.getCategoria().isEmpty() &&
                (ev.getCategoria() == null || !ev.getCategoria().equalsIgnoreCase(filtro.getCategoria()))) {
                match = false;
            }
            // Filtro de fecha simplificado (contiene string)
            if (filtro.getFecha() != null && !filtro.getFecha().isEmpty() &&
                (ev.getFecha() == null || !ev.getFecha().toString().toLowerCase().contains(filtro.getFecha().toLowerCase()))) {
                match = false;
            }
            if (filtro.getUbicacion() != null && !filtro.getUbicacion().isEmpty() &&
                (ev.getLugar() == null || ev.getLugar().getNombre() == null ||
                 !ev.getLugar().getNombre().toLowerCase().contains(filtro.getUbicacion().toLowerCase()))) {
                match = false;
            }
            if (match) {
                eventosFiltrados.add(ev);
            }
        }
        return eventosFiltrados;
    }

    public void cancelarCompra(Compra compra, String motivo, ProcesoReembolso procesadorReembolso) {
        if (compra == null || motivo == null || procesadorReembolso == null) {
             System.err.println("Error en Asistente.cancelarCompra: Datos inválidos.");
            return;
        }
        System.out.println(this.getNombre() + " solicita cancelación para la compra ID: " + compra.getId());
        Command cancelarCmd = new modelo.comando.CancelarCompraCommand(compra, motivo, procesadorReembolso);
        this.commandInvoker.executeCommand(cancelarCmd);
    }

    public void solicitarReembolso(Compra compra, String motivo, ProcesoReembolso procesadorReembolso) {
         if (compra == null || motivo == null || procesadorReembolso == null) {
             System.err.println("Error en Asistente.solicitarReembolso: Datos inválidos.");
            return;
        }
        System.out.println(this.getNombre() + " solicita reembolso para la compra ID: " + compra.getId() + " por motivo: " + motivo);
        Command reembolsarCmd = new modelo.comando.ReembolsarCompraCommand(compra, motivo, procesadorReembolso);
        this.commandInvoker.executeCommand(reembolsarCmd);
    }

    public boolean deshacerUltimaOperacion() {
        System.out.println(this.nombre + " intenta deshacer la última operación.");
        return commandInvoker.undoCommand();
    }

    public boolean rehacerUltimaOperacionDeshecha() {
        System.out.println(this.nombre + " intenta rehacer la última operación deshecha.");
        return commandInvoker.redoCommand();
    }

    public List<String> verHistorialOperaciones() {
        List<String> descripciones = new ArrayList<>();
        for(Command cmd : commandInvoker.getHistorialComandos()){
            descripciones.add(cmd.getDescription() + (cmd.getTiempoEjecucion() != null ? " @ " + cmd.getTiempoEjecucion() : ""));
        }
        if(descripciones.isEmpty()){
            System.out.println(this.nombre + " no tiene historial de operaciones.");
        }
        return descripciones;
    }
}
