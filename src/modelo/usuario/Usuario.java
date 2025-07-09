
package modelo.usuario;

import java.util.ArrayList;
import java.util.List;
import modelo.evento.Evento;
import modelo.compra.Compra; // Importar la clase Compra
import modelo.entrada.TipoEntrada; // Para solicitarCompra

public abstract class Usuario implements Observer { // Implementar Observer
    protected String id;
    protected String nombre;
    protected String email;
    protected String password; // Considerar almacenar un hash en lugar de texto plano
    protected List<Preferencia> preferencias;
    protected List<Compra> historialCompras;
    protected ConfiguracionNotificaciones configuracionNotificaciones;

    public Usuario(String id, String nombre, String email, String password) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID de usuario no puede ser nulo o vacío.");
        if (nombre == null || nombre.trim().isEmpty()) throw new IllegalArgumentException("Nombre de usuario no puede ser nulo o vacío.");
        if (email == null || email.trim().isEmpty()) throw new IllegalArgumentException("Email de usuario no puede ser nulo o vacío."); // Podría añadirse validación de formato de email
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password no puede ser nula o vacía.");

        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.preferencias = new ArrayList<>();
        this.historialCompras = new ArrayList<>();
        this.configuracionNotificaciones = new ConfiguracionNotificaciones(); // Configuración por defecto
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public List<Preferencia> getPreferencias() { return new ArrayList<>(preferencias); } // Devuelve copia
    public List<Compra> getHistorialCompras() { return new ArrayList<>(historialCompras); } // Devuelve copia
    public ConfiguracionNotificaciones getConfiguracionNotificaciones() { return configuracionNotificaciones; }

    // Setters
    public void setNombre(String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) this.nombre = nombre;
    }
    public void setEmail(String email) {
        // Añadir validación de formato si es necesario
        if (email != null && !email.trim().isEmpty()) this.email = email;
    }

    // Cambiar contraseña requeriría lógica de hashing y posiblemente confirmación
    public void setPassword(String nuevaPassword) {
        if (nuevaPassword != null && !nuevaPassword.isEmpty()) this.password = nuevaPassword;
    }

    public void setPreferencias(List<Preferencia> preferencias) {
        this.preferencias = new ArrayList<>(preferencias); // Almacena copia
    }

    public void addPreferencia(Preferencia preferencia) {
        if (preferencia != null && !this.preferencias.contains(preferencia)) {
            this.preferencias.add(preferencia);
        }
    }

    public void removePreferencia(Preferencia preferencia) {
        this.preferencias.remove(preferencia);
    }

    public void addCompraAlHistorial(Compra compra) {
        if (compra != null) {
            this.historialCompras.add(compra);
        }
    }

    public void setConfiguracionNotificaciones(ConfiguracionNotificaciones config) {
        if (config != null) {
            this.configuracionNotificaciones = config;
        }
    }

    // Métodos abstractos
    public abstract boolean autenticar(String password);
    public abstract List<Evento> obtenerRecomendaciones(); // Podría tomar parámetros adicionales

    // Se ajusta la firma para incluir TipoEntrada, ya que es crucial para una compra.
    public abstract void solicitarCompra(Evento evento, TipoEntrada tipoEntrada, int cantidad);

    // Implementación de Observer
    @Override
    public void actualizar(String mensaje) {
        // Lógica base para recibir una notificación.
        // Podría ser tan simple como imprimir en consola o más complejo.
        System.out.println("Notificación para " + this.nombre + " (" + this.email + "): " + mensaje);
        // Aquí se podría verificar this.configuracionNotificaciones para enviar email/app si está activado.
        if (this.configuracionNotificaciones.isNotificacionesEmail()) {
            System.out.println("Enviando email a: " + this.email + " con mensaje: " + mensaje);
            // Lógica real de envío de email iría aquí
        }
        if (this.configuracionNotificaciones.isNotificacionesApp()) {
            System.out.println("Enviando notificación in-app a: " + this.nombre + " con mensaje: " + mensaje);
            // Lógica real de notificación in-app iría aquí
        }
    }

    @Override
    public String toString() {
        return "Usuario [id=" + id + ", nombre=" + nombre + ", email=" + email + "]";
    }
}
