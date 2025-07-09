
package modelo.recomendacion;

import java.util.Collections;
import java.util.List;
import modelo.usuario.Usuario;
import modelo.evento.Evento;

public class GestorRecomendacionesStrategy {
    private IEstrategiaRecomendacion estrategiaRecomendacion;
    private final IRepositorioEvento repositorioEventos; // Debe ser final si se pasa en constructor y no cambia
    private final ConfiguradorStrategys configuradorEstrategias; // Para cambiar estrategia por tipo

    public GestorRecomendacionesStrategy(IRepositorioEvento repoEventos,
                                         IEstrategiaRecomendacion estrategiaInicial,
                                         ConfiguradorStrategys configurador) {
        if (repoEventos == null) throw new IllegalArgumentException("El repositorio de eventos no puede ser nulo.");
        if (estrategiaInicial == null) throw new IllegalArgumentException("La estrategia inicial de recomendación no puede ser nula.");
        if (configurador == null) throw new IllegalArgumentException("El configurador de estrategias no puede ser nulo.");

        this.repositorioEventos = repoEventos;
        this.estrategiaRecomendacion = estrategiaInicial;
        this.configuradorEstrategias = configurador;
    }

    // Permite cambiar la estrategia en tiempo de ejecución
    public void setEstrategiaRecomendacion(IEstrategiaRecomendacion nuevaEstrategia) {
        if (nuevaEstrategia == null) {
            System.err.println("GestorRecomendaciones: Intento de establecer una estrategia nula. Se mantiene la actual.");
            return;
        }
        this.estrategiaRecomendacion = nuevaEstrategia;
        System.out.println("GestorRecomendaciones: Estrategia de recomendación cambiada a: " + nuevaEstrategia.getClass().getSimpleName());
    }

    // Cambia la estrategia basándose en un tipo (String) usando el ConfiguradorStrategys
    public boolean cambiarEstrategiaPorTipo(String tipoEstrategia) {
        if (tipoEstrategia == null || tipoEstrategia.trim().isEmpty()) {
            System.err.println("GestorRecomendaciones: Tipo de estrategia no puede ser nulo o vacío.");
            return false;
        }
        IEstrategiaRecomendacion nuevaEstrategia = configuradorEstrategias.crearEstrategia(tipoEstrategia);
        if (nuevaEstrategia != null) {
            setEstrategiaRecomendacion(nuevaEstrategia);
            return true;
        } else {
            System.err.println("GestorRecomendaciones: No se pudo crear una estrategia para el tipo: " + tipoEstrategia + ". Se mantiene la actual.");
            return false;
        }
    }

    // Obtiene la estrategia preferida para un usuario y la establece
    public void establecerEstrategiaParaUsuario(Usuario usuario) {
        if (usuario == null) {
            System.err.println("GestorRecomendaciones: Usuario nulo, no se puede establecer estrategia específica.");
            return;
        }
        IEstrategiaRecomendacion estrategiaUsuario = configuradorEstrategias.obtenerEstrategiasPorUsuario(usuario);
        if (estrategiaUsuario != null) {
            setEstrategiaRecomendacion(estrategiaUsuario);
        } else {
            // Si no hay estrategia específica para el usuario, podría mantenerse la actual o usar una por defecto.
            System.out.println("GestorRecomendaciones: No se encontró estrategia específica para " + usuario.getNombre() + ", se mantiene la actual: " + this.estrategiaRecomendacion.getClass().getSimpleName());
        }
    }


    public List<Evento> generarRecomendaciones(Usuario usuario) {
        if (usuario == null) {
            System.err.println("GestorRecomendaciones: Usuario no puede ser nulo para generar recomendaciones.");
            return Collections.emptyList();
        }
        if (this.estrategiaRecomendacion == null) {
            System.err.println("GestorRecomendaciones: No hay una estrategia de recomendación establecida.");
            return Collections.emptyList();
        }
        if (this.repositorioEventos == null) {
            System.err.println("GestorRecomendaciones: Repositorio de eventos no configurado.");
            return Collections.emptyList();
        }

        List<Evento> todosLosEventos = repositorioEventos.obtenerEventos();
        if (todosLosEventos == null || todosLosEventos.isEmpty()) {
            System.out.println("GestorRecomendaciones: No hay eventos disponibles en el repositorio para generar recomendaciones.");
            return Collections.emptyList();
        }

        System.out.println("GestorRecomendaciones: Generando recomendaciones para " + usuario.getNombre() +
                           " usando la estrategia: " + estrategiaRecomendacion.getClass().getSimpleName());

        return estrategiaRecomendacion.recomendarEventos(usuario, todosLosEventos);
    }
}
