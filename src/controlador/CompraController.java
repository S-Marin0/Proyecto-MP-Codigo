package controlador;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller; // O @Service si se ve más como un servicio
// import org.springframework.transaction.annotation.Transactional;

import modelo.usuario.Asistente; // Se usará para obtener el ID y pasar al facade
import modelo.usuario.Usuario;
import modelo.evento.Evento;
import modelo.entrada.TipoEntrada;
import modelo.compra.Compra;
import modelo.compra.MetodoPago;
import modelo.facade.ProcesoCompraFacade;
import modelo.reembolso.ProcesadorReembolso;
import modelo.repositorio.UsuarioRepository; // Para cargar Asistente
import modelo.repositorio.CompraRepository;  // Para cargar Compra
import modelo.excepciones.ValidacionException;
import modelo.excepciones.EntidadNoEncontradaException;
// import modelo.comando.CommandInvoker; // Si se mantiene el patrón command

// @Controller o @Service
public class CompraController {
    private final ProcesoCompraFacade procesoCompraFacade;
    private final ProcesadorReembolso procesadorReembolso;
    private final UsuarioRepository usuarioRepository; // Para cargar Asistente por ID
    private final CompraRepository compraRepository;   // Para cargar Compra por ID
    // private final CommandInvoker commandInvoker; // Si se mantiene el patrón command

    // @Autowired
    public CompraController(ProcesoCompraFacade procesoCompraFacade,
                            ProcesadorReembolso procesadorReembolso,
                            UsuarioRepository usuarioRepository,
                            CompraRepository compraRepository
                            /* CommandInvoker commandInvoker */) {
        if (procesoCompraFacade == null) throw new IllegalArgumentException("ProcesoCompraFacade no puede ser nulo.");
        if (procesadorReembolso == null) throw new IllegalArgumentException("ProcesadorReembolso no puede ser nulo.");
        if (usuarioRepository == null) throw new IllegalArgumentException("UsuarioRepository no puede ser nulo.");
        if (compraRepository == null) throw new IllegalArgumentException("CompraRepository no puede ser nulo.");
        // if (commandInvoker == null) throw new IllegalArgumentException("CommandInvoker no puede ser nulo.");

        this.procesoCompraFacade = procesoCompraFacade;
        this.procesadorReembolso = procesadorReembolso;
        this.usuarioRepository = usuarioRepository;
        this.compraRepository = compraRepository;
        // this.commandInvoker = commandInvoker;
    }

    // @Transactional
    public Compra procesarNuevaCompra(Long asistenteId, Long eventoId, Long tipoEntradaId,
                                      int cantidad, MetodoPago metodoPago, String codigoDescuento) {
        // Las validaciones de IDs nulos y cantidad ya se hacen en ProcesoCompraFacade
        // Se podría validar aquí también por defensa en profundidad si se desea.

        // Ya no se llama a asistente.solicitarCompra() directamente aquí.
        // El controller orquesta la llamada al facade/service.
        // Si el patrón Command es para la sesión del usuario (undo/redo),
        // la lógica de crear y ejecutar el comando se haría aquí o en la capa de aplicación/UI.

        // System.out.println("CompraController: Iniciando proceso de compra para Asistente ID: " + asistenteId + ...);

        return procesoCompraFacade.ejecutarProcesoCompra(asistenteId, eventoId, tipoEntradaId, cantidad, metodoPago, codigoDescuento);
        // La Compra devuelta ya está persistida y tiene un ID.
    }

    // @Transactional
    public void solicitarCancelacionCompra(Long asistenteId, Long compraId, String motivo) {
        if (asistenteId == null || compraId == null || motivo == null || motivo.trim().isEmpty()) {
            throw new ValidacionException("Datos inválidos para solicitar cancelación (IDs y motivo son requeridos).");
        }

        Usuario usuario = usuarioRepository.findById(asistenteId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Asistente con ID " + asistenteId + " no encontrado."));
        if (!(usuario instanceof Asistente)) {
             throw new ValidacionException("El usuario con ID " + asistenteId + " no es un asistente.");
        }
        Asistente asistente = (Asistente) usuario;

        Compra compra = compraRepository.findById(compraId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Compra con ID " + compraId + " no encontrada."));

        // Verificar que la compra pertenece al asistente (opcional, pero buena práctica)
        if (!compra.getUsuario().equals(asistente)) {
            throw new modelo.excepciones.OperacionInvalidaException("La compra ID " + compraId + " no pertenece al asistente ID " + asistenteId);
        }

        // System.out.println("CompraController: Solicitando cancelación de compra ID " + compra.getId() + " para asistente " + asistente.getNombre());

        // La lógica del comando se simplifica aquí. El ProcesadorReembolso maneja la lógica real.
        // Si se usa el patrón Command para undo/redo, se crearía y ejecutaría un CancelarCompraCommand aquí.
        // Ejemplo simplificado sin el Command explícito en Asistente:
        procesadorReembolso.procesarCancelacion(compra, motivo);
        // El procesadorReembolso debería actualizar el estado de la compra y guardarla.
        compraRepository.save(compra); // Asegurar que el estado de la compra se guarde
    }

    // @Transactional
    public void solicitarReembolsoDeCompra(Long asistenteId, Long compraId, String motivo) {
         if (asistenteId == null || compraId == null || motivo == null || motivo.trim().isEmpty()) {
            throw new ValidacionException("Datos inválidos para solicitar reembolso (IDs y motivo son requeridos).");
        }

        Usuario usuario = usuarioRepository.findById(asistenteId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Asistente con ID " + asistenteId + " no encontrado."));
        if (!(usuario instanceof Asistente)) {
             throw new ValidacionException("El usuario con ID " + asistenteId + " no es un asistente.");
        }
        Asistente asistente = (Asistente) usuario;

        Compra compra = compraRepository.findById(compraId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Compra con ID " + compraId + " no encontrada."));

        if (!compra.getUsuario().equals(asistente)) {
            throw new modelo.excepciones.OperacionInvalidaException("La compra ID " + compraId + " no pertenece al asistente ID " + asistenteId);
        }

        // System.out.println("CompraController: Solicitando reembolso de compra ID " + compra.getId() + " para asistente " + asistente.getNombre());
        // Similar a cancelar, la lógica del comando se simplifica.
        procesadorReembolso.procesarReembolso(compra, motivo);
        compraRepository.save(compra); // Asegurar que el estado de la compra se guarde
    }
}
