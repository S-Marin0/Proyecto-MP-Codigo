
package modelo.usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import modelo.evento.Evento;
import modelo.evento.EventoBuilder;
import modelo.entrada.TipoEntrada; // Necesario para la firma de solicitarCompra

public class Organizador extends Usuario {
    private String infoContacto;
    private List<Evento> eventosCreados;

    public Organizador(String id, String nombre, String email, String password, String infoContacto) {
        super(id, nombre, email, password);
        if (infoContacto == null || infoContacto.trim().isEmpty()) {
            throw new IllegalArgumentException("La información de contacto no puede ser nula o vacía.");
        }
        this.infoContacto = infoContacto;
        this.eventosCreados = new ArrayList<>();
    }

    public String getInfoContacto() {
        return infoContacto;
    }

    public void setInfoContacto(String infoContacto) {
        if (infoContacto != null && !infoContacto.trim().isEmpty()) {
            this.infoContacto = infoContacto;
        }
    }

    public List<Evento> getEventosCreados() {
        return new ArrayList<>(eventosCreados); // Devuelve copia
    }

    public Evento crearEvento(EventoBuilder builder) {
        // Aquí podríamos añadir lógica adicional, como asignar este organizador al evento.
        // builder.withOrganizador(this); // Si EventoBuilder tiene withOrganizador y Evento tiene setOrganizador
        Evento evento = builder.build();
        // Asignar el organizador al evento si no se hizo en el builder
        if (evento.getOrganizador() == null) {
             // evento.setOrganizador(this); // Suponiendo que Evento tiene un setOrganizador(Organizador)
        }
        this.eventosCreados.add(evento);
        return evento;
    }

    public boolean editarEvento(Evento eventoAntiguo, EventoBuilder datosNuevosBuilder) {
        // La edición es compleja. Se podría reemplazar el objeto o modificarlo.
        // Aquí un ejemplo simple: si el evento está en la lista, lo removemos y añadimos el "nuevo" (construido).
        // Se necesitaría una forma más robusta de identificar y actualizar.
        if (eventoAntiguo == null || !this.eventosCreados.contains(eventoAntiguo)) {
            return false; // Evento no encontrado o nulo
        }
        // datosNuevosBuilder.withOrganizador(this); // Asegurar que el organizador siga siendo este
        Evento eventoNuevo = datosNuevosBuilder.build();
        // Asignar el organizador al evento nuevo si no se hizo en el builder
        if (eventoNuevo.getOrganizador() == null) {
            // eventoNuevo.setOrganizador(this);
        }

        // Actualizar la lista (esto es una simplificación, podría necesitar IDs para reemplazar)
        int index = this.eventosCreados.indexOf(eventoAntiguo);
        if (index != -1) {
            this.eventosCreados.set(index, eventoNuevo);
            return true;
        }
        return false; // No debería pasar si contains fue true, pero por seguridad.
    }

    public boolean eliminarEvento(Evento evento) {
        if (evento != null) {
            // Aquí se podría añadir lógica para verificar si el evento puede ser eliminado
            // (ej. no tiene entradas vendidas, no está en curso, etc.)
            // También, si el evento está referenciado en otras partes, manejar esas referencias.
            return this.eventosCreados.remove(evento);
        }
        return false;
    }

    public String verEstadisticas(Evento evento) {
        if (evento == null || !this.eventosCreados.contains(evento)) {
            return "Estadísticas no disponibles: Evento no encontrado o no pertenece a este organizador.";
        }
        // Lógica para calcular/obtener estadísticas:
        // - Entradas vendidas: evento.getEntradasVendidas() (ya lo tenemos en Evento)
        // - Ingresos: Se necesitaría iterar sobre los TiposDeEntrada del evento,
        //   multiplicar precio por cantidad vendida de cada tipo (esto es más complejo,
        //   ya que Evento solo tiene total de entradas vendidas, no por tipo).
        //   Alternativamente, las Compras en historialCompras podrían usarse si se asocian a eventos.
        // - Asistencia esperada: podría ser igual a entradasVendidas o un cálculo diferente.

        int entradasVendidas = evento.getEntradasVendidas();
        // Cálculo de ingresos (simplificado, asume un precio promedio o que el precio está en Evento, lo cual no es el caso)
        // double ingresosEstimados = entradasVendidas * algúnPrecioPromedio;
        // Para un cálculo real de ingresos, necesitaríamos saber cuántas entradas de cada TipoEntrada se vendieron.
        // Esto implicaría que la clase Compra o Evento deben tener más detalle.
        // Por ahora, mostraremos solo las entradas vendidas.

        // Supongamos que el evento tiene una lista de tipos de entrada y cada tipo de entrada
        // sabe cuántas se vendieron de ese tipo (esto no está implementado actualmente en TipoEntrada).
        // Si lo estuviera:
        double ingresosTotales = 0;
        // for (TipoEntrada te : evento.getTiposEntrada()) {
        //    ingresosTotales += te.getPrecio() * (te.getCantidadTotal() - te.getCantidadDisponible());
        // }


        return String.format("Estadísticas para Evento '%s':\n - Entradas Vendidas: %d/%d\n - Ingresos Estimados: %.2f € (Cálculo pendiente de detalle por tipo de entrada)\n - Asistencia Esperada: %d",
                evento.getNombre(), entradasVendidas, evento.getCapacidad(), ingresosTotales, entradasVendidas);
    }

    // Implementación de métodos abstractos de Usuario
    @Override
    public boolean autenticar(String password) {
        // Compara el password proporcionado con el almacenado.
        // En una aplicación real, this.password sería un hash.
        return Objects.equals(this.password, password);
    }

    @Override
    public List<Evento> obtenerRecomendaciones() {
        // Los organizadores generalmente no reciben recomendaciones de eventos para asistir.
        // Podrían recibir recomendaciones sobre cómo mejorar sus eventos, pero eso es otra funcionalidad.
        System.out.println("La obtención de recomendaciones no aplica a Organizadores.");
        return new ArrayList<>(); // Devuelve lista vacía
    }

    @Override
    public void solicitarCompra(Evento evento, TipoEntrada tipoEntrada, int cantidad) {
        // Los organizadores no compran entradas de la misma forma que los asistentes.
        System.out.println("La solicitud de compra de entradas no aplica directamente a Organizadores.");
        // Podría lanzar UnsupportedOperationException si se prefiere.
    }

    // actualizar(String mensaje) es heredado de Usuario (que implementa Observer)
    // y su implementación base ya imprime y "envía" notificaciones.
    // Si Organizador necesita un manejo especial de notificaciones, se puede sobrescribir.
    // @Override
    // public void actualizar(String mensaje) {
    //    super.actualizar(mensaje); // Llama a la lógica base
    //    System.out.println("Manejo específico de notificación para Organizador: " + mensaje);
    // }
}
