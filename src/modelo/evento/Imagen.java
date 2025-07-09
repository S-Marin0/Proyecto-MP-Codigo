
package modelo.evento;

public class Imagen {
    private String url;

    public Imagen(String url) {
        if (url == null || url.trim().isEmpty()) {
            // Podríamos lanzar una IllegalArgumentException o manejarlo de otra forma.
            // Por ahora, se asigna una cadena vacía para evitar NPE si se llama a getUrl()
            // y se asume que los consumidores de la URL pueden manejar una URL vacía si es necesario.
            // Idealmente, se lanzaría una excepción si una URL válida es estrictamente requerida.
            // System.err.println("Advertencia: Se intentó crear un objeto Imagen con URL nula o vacía.");
            this.url = "";
        } else {
            this.url = url;
        }
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Imagen[url='" + url + "']";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Imagen imagen = (Imagen) obj;
        return url != null ? url.equals(imagen.url) : imagen.url == null;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    // Podrían añadirse otros atributos como 'altText', 'dimensiones', etc.
    // O métodos para validar la URL (formato básico), etc.
}
