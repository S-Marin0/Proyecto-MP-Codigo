
package modelo.facade;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

import modelo.usuario.Usuario;
import modelo.evento.Evento;
import modelo.entrada.TipoEntrada;
import modelo.entrada.Entrada; // Asumiendo que Entrada no es una entidad JPA independiente, sino parte de Compra
import modelo.entrada.EntradaBase; // Idem
import modelo.compra.Compra;
import modelo.compra.MetodoPago; // MetodoPago podría ser @Embeddable o una entidad simple
import modelo.compra.EstadoCompra;
import modelo.validacion.ValidacionHandler;
import modelo.validacion.ValidacionContext;
import modelo.mediador.MediadorCompras;
import modelo.excepciones.ValidacionException;
import modelo.excepciones.OperacionInvalidaException;
import modelo.excepciones.EntidadNoEncontradaException;

import modelo.repositorio.CompraRepository;
import modelo.repositorio.EventoRepository;
import modelo.repositorio.UsuarioRepository;
import modelo.repositorio.TipoEntradaRepository;


import java.util.List;
import java.util.ArrayList;

// @Service
public class ProcesoCompraFacade { // Podría renombrarse a CompraService
    private final ProcesadorPago procesadorPago; // Simulado, no interactúa con BD
    private final GeneradorEntradas generadorEntradas; // Simulado, no interactúa con BD
    private final ValidacionHandler cadenaValidacionGlobal;
    private final MediadorCompras mediador;

    private final CompraRepository compraRepository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoEntradaRepository tipoEntradaRepository;


    // @Autowired
    public ProcesoCompraFacade(ProcesadorPago procesadorPago,
                               GeneradorEntradas generadorEntradas,
                               ValidacionHandler cadenaValidacionGlobal,
                               MediadorCompras mediador,
                               CompraRepository compraRepository,
                               EventoRepository eventoRepository,
                               UsuarioRepository usuarioRepository,
                               TipoEntradaRepository tipoEntradaRepository) {
        // Validaciones de argumentos nulos...
        this.procesadorPago = procesadorPago;
        this.generadorEntradas = generadorEntradas;
        this.cadenaValidacionGlobal = cadenaValidacionGlobal;
        this.mediador = mediador;
        this.compraRepository = compraRepository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tipoEntradaRepository = tipoEntradaRepository;
    }

    // @Transactional
    public Compra ejecutarProcesoCompra(Long usuarioId, Long eventoId, Long tipoEntradaId, int cantidad, MetodoPago metodoPago, String codigoDescuento) {

        if (usuarioId == null) throw new ValidacionException("ID de Usuario no puede ser nulo para la compra.");
        if (eventoId == null) throw new ValidacionException("ID de Evento no puede ser nulo para la compra.");
        if (tipoEntradaId == null) throw new ValidacionException("ID de TipoEntrada no puede ser nulo para la compra.");
        if (cantidad <= 0) throw new ValidacionException("La cantidad de entradas debe ser mayor a cero.");
        if (metodoPago == null) throw new ValidacionException("Método de pago no puede ser nulo.");

        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Usuario con ID " + usuarioId + " no encontrado."));
        Evento evento = eventoRepository.findById(eventoId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Evento con ID " + eventoId + " no encontrado."));
        TipoEntrada tipoEntrada = tipoEntradaRepository.findById(tipoEntradaId)
            .orElseThrow(() -> new EntidadNoEncontradaException("Tipo de Entrada con ID " + tipoEntradaId + " no encontrado."));


        ValidacionContext contextoValidacion = new ValidacionContext(usuario, evento, tipoEntrada, cantidad);
        if (!cadenaValidacionGlobal.procesarValidacion(contextoValidacion)) {
            throw new ValidacionException("Error de validación en la compra: " + cadenaValidacionGlobal.getMensajeError());
        }

        // La creación de objetos Entrada (EntradaBase) puede simplificarse si no son entidades persistentes independientes.
        // Si Compra tiene una colección de Strings o IDs de entradas generadas, sería diferente.
        // Asumiendo que la entidad Compra maneja la lista de Entrada internamente y no son entidades separadas.
        List<Entrada> entradasParaComprar = new ArrayList<>();
        double precioUnitario = tipoEntrada.getPrecio();
        for (int i = 0; i < cantidad; i++) {
            // Si EntradaBase no es una entidad, esto es solo para la lógica de la compra.
            entradasParaComprar.add(new EntradaBase(tipoEntrada, evento));
        }
        double totalCalculado = precioUnitario * cantidad;

        if (codigoDescuento != null && !codigoDescuento.isEmpty()) {
            totalCalculado = aplicarDescuento(totalCalculado, codigoDescuento);
        }

        Compra nuevaCompra = new Compra(usuario, evento, tipoEntrada, cantidad, entradasParaComprar, totalCalculado, metodoPago);
        // El ID de nuevaCompra será generado por la BD al guardar.

        if (!procesadorPago.procesar(metodoPago, nuevaCompra.getTotalPagado())) {
            nuevaCompra.setEstado(EstadoCompra.CANCELADA);
            // No guardamos la compra si el pago falla, o la guardamos como CANCELADA si es necesario registrar el intento.
            // compraRepository.save(nuevaCompra); // Opcional
            throw new OperacionInvalidaException("Error en el procesamiento del pago para la compra.");
        }
        nuevaCompra.setEstado(EstadoCompra.CONFIRMADA);

        // Persistir la compra primero para obtener su ID y asegurar que la transacción principal está en curso
        Compra compraGuardada = compraRepository.save(nuevaCompra);


        if (!tipoEntrada.reducirDisponibilidad(cantidad)) {
            // Este es un punto crítico. El pago se hizo, pero no se pueden asignar entradas.
            // Se debe revertir la transacción o marcar para intervención.
            // JPA con @Transactional debería hacer rollback si lanzamos una excepción aquí.
            compraGuardada.setEstado(EstadoCompra.ERROR_DISPONIBILIDAD_POST_PAGO);
            compraRepository.save(compraGuardada); // Guardar el estado de error
            throw new OperacionInvalidaException("Error CRÍTICO: No se pudo reducir la disponibilidad de entradas después del pago para compra ID: " + compraGuardada.getId() + ". Se requiere intervención manual o reversión del pago.");
        }
        tipoEntradaRepository.save(tipoEntrada); // Guardar el cambio en la disponibilidad del TipoEntrada

        evento.registrarVentaEntradas(cantidad); // Actualizar contador en Evento
        eventoRepository.save(evento); // Guardar el cambio en Evento

        generadorEntradas.generar(evento, tipoEntrada, cantidad, compraGuardada); // Simulado

        mediador.notificarCompraExitosa(usuario, compraGuardada);
        compraGuardada.generarComprobante(); // Simulado

        return compraGuardada;
    }

    private double aplicarDescuento(double totalActual, String codigo) {
        // Lógica para validar el código y aplicar el descuento.
        // Esta lógica podría moverse a un servicio de promociones más sofisticado.
        if ("DESC10".equals(codigo)) {
            // System.out.println("Aplicando descuento del 10% con código: " + codigo);
            return totalActual * 0.90;
        }
        // System.out.println("Código de descuento '" + codigo + "' no válido o no aplicable.");
        return totalActual;
    }

    // Los métodos comentados abajo sugieren una descomposición previa que ahora está integrada
    // principalmente dentro de ejecutarProcesoCompra. Si se necesitaran como pasos separados
    // externamente, podrían refactorizarse para usar el nuevo manejo de excepciones también.
}
