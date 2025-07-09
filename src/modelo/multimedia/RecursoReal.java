
package modelo.multimedia;

import java.nio.charset.StandardCharsets; // Para simular contenido de texto

public class RecursoReal implements RecursoMultimedia {
    private final String url;
    private byte[] contenido; // Contenido del recurso, cargado una vez

    public RecursoReal(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL del recurso real no puede ser nula o vacía.");
        }
        this.url = url;
        this.contenido = cargarContenidoDesdeUrl(url); // Carga pesada simulada
        System.out.println("RecursoReal: Contenido cargado desde URL: " + url + " (Tamaño: " + (this.contenido != null ? this.contenido.length : 0) + " bytes)");
    }

    // Simula la carga "pesada" del recurso desde una URL
    private byte[] cargarContenidoDesdeUrl(String urlOrigen) {
        // En una aplicación real, esto implicaría una conexión de red, lectura de archivo, etc.
        // Simulación:
        System.out.println("RecursoReal: Simulando carga pesada de contenido para: " + urlOrigen);
        try {
            // Simular una demora
            Thread.sleep(100); // Pequeña demora para simular carga
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("RecursoReal: Carga interrumpida para " + urlOrigen);
            return null;
        }
        String dataSimulada = "Este es el contenido simulado para el recurso en " + urlOrigen + ". Timestamp: " + System.currentTimeMillis();
        return dataSimulada.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] mostrar() {
        // Devuelve el contenido ya cargado.
        // Podría añadir una simulación de "streaming" o algo si fuera un video,
        // pero para la interfaz actual, devolver el byte[] completo es suficiente.
        System.out.println("RecursoReal: Sirviendo contenido para URL: " + url);
        return contenido;
    }

    @Override
    public String getUrlRecurso() {
        return url;
    }
}
