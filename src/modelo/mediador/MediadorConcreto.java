
package modelo.mediador;

import java.util.ArrayList;
import java.util.List;
import modelo.usuario.Usuario;
import modelo.usuario.Organizador;
import modelo.compra.Compra;
import modelo.compra.EstadoCompra; // Para verificar el estado de la compra
import modelo.evento.Evento;
import modelo.facade.ProcesoCompraFacade;
import modelo.notificacion.SistemaNotificaciones;
import modelo.recomendacion.GestorRecomendacionesStrategy;

public class MediadorConcreto implements MediadorCompras {
    private ProcesoCompraFacade procesoCompraFacade;
    private final SistemaNotificaciones sistemaNotificaciones;
    private final GestorRecomendacionesStrategy gestorRecomendaciones;

    public MediadorConcreto(SistemaNotificaciones sistemaNotificaciones,
                            GestorRecomendacionesStrategy gestorRecomendaciones) {
        if (sistemaNotificaciones == null) {
            throw new IllegalArgumentException("SistemaNotificaciones no puede ser nulo.");
        }
        if (gestorRecomendaciones == null) {
            throw new IllegalArgumentException("GestorRecomendacionesStrategy no puede ser nulo.");
        }
        this.sistemaNotificaciones = sistemaNotificaciones;
        this.gestorRecomendaciones = gestorRecomendaciones;
    }

    @Override
    public void registrarUsuario(Usuario usuario) {
        System.out.println("Mediador: Usuario '" + (usuario != null ? usuario.getNombre() : "null") + "' considerado/registrado (acción de registro en mediador no implementada).");
    }

    @Override
    public void registrarProcesoCompraFacade(ProcesoCompraFacade proceso) {
        if (proceso == null) {
            throw new IllegalArgumentException("ProcesoCompraFacade no puede ser nulo al registrarlo en el Mediador.");
        }
        this.procesoCompraFacade = proceso;
        System.out.println("Mediador: ProcesoCompraFacade registrado.");
    }

    @Override
    public void notificarCompraExitosa(Usuario usuario, Compra compra) {
        if (usuario == null || compra == null) {
            System.err.println("Mediador: Usuario o Compra nulos al notificar compra exitosa.");
            return;
        }

        System.out.println("Mediador: Procesando acciones post-compra exitosa para Usuario: " + usuario.getNombre() + ", Compra ID: " + compra.getId());

        // 1. Añadir la compra al historial del usuario si la compra fue confirmada.
        if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
            usuario.addCompraAlHistorial(compra);
            System.out.println("Mediador: Compra ID " + compra.getId() + " añadida al historial de " + usuario.getNombre());
        } else {
            System.out.println("Mediador: Compra ID " + compra.getId() + " no fue confirmada (Estado: " + compra.getEstado() + "), no se añade al historial aquí.");
            // Si la compra falló, el facade ya debería haber manejado el estado.
            // El mediador podría tener un método notificarCompraFallida si se necesita coordinación adicional.
        }

        // 2. Notificar al usuario comprador (solo si está confirmada)
        if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
            this.sistemaNotificaciones.enviarConfirmacionCompra(usuario, compra);
        }

        // 3. Notificar al organizador del evento (si la compra está confirmada y aplica)
        if (compra.getEstado() == EstadoCompra.CONFIRMADA) {
            Evento eventoComprado = compra.getEvento();
            if (eventoComprado != null && eventoComprado.getOrganizador() != null) {
                Organizador organizador = eventoComprado.getOrganizador();
                // Asegurarse de que el organizador no sea el mismo usuario que compra (aunque raro, podría pasar)
                if (!organizador.equals(usuario)) {
                    String mensajeOrganizador = "Nueva venta para tu evento '" + eventoComprado.getNombre() +
                                                "'. Comprador: " + usuario.getNombre() +
                                                ", Cantidad: " + compra.getCantidad() +
                                                ", Tipo Entrada: " + compra.getTipoEntradaBase().getNombre();
                    this.sistemaNotificaciones.enviarNotificacionDirecta(organizador, mensajeOrganizador);
                }
            }
        }

        System.out.println("Mediador: Acciones post-compra para " + usuario.getNombre() + " (Compra ID: " + compra.getId() + ") completadas.");
    }

    @Override
    public void solicitarRecomendaciones(Usuario usuario) {
        if (usuario == null) {
            System.err.println("Mediador: Usuario nulo al solicitar recomendaciones.");
            return;
        }
        System.out.println("Mediador: Solicitando recomendaciones para Usuario: " + usuario.getNombre());

        gestorRecomendaciones.establecerEstrategiaParaUsuario(usuario);
        List<Evento> recomendaciones = gestorRecomendaciones.generarRecomendaciones(usuario);

        if (recomendaciones != null && !recomendaciones.isEmpty()) {
            this.sistemaNotificaciones.enviarRecomendaciones(usuario, recomendaciones);
        } else {
            System.out.println("Mediador: No se generaron recomendaciones para " + usuario.getNombre() + " o la lista estaba vacía.");
        }
    }

    public ProcesoCompraFacade getProcesoCompraFacade() {
        if (this.procesoCompraFacade == null) {
            System.err.println("Mediador: ProcesoCompraFacade no ha sido registrado/inicializado.");
        }
        return procesoCompraFacade;
    }
}
