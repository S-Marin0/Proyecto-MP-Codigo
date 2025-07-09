package modelo.evento;

import java.util.Objects;

public class Video {
    private String url;
    private String titulo;
    // Podrían añadirse otros atributos como duracion, formato, etc.

    public Video(String url, String titulo) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("La URL del video no puede ser nula o vacía.");
        }
        this.url = url;
        this.titulo = (titulo == null || titulo.trim().isEmpty()) ? "Video promocional" : titulo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (url != null && !url.trim().isEmpty()) {
            this.url = url;
        }
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = (titulo == null || titulo.trim().isEmpty()) ? this.titulo : titulo;
    }

    @Override
    public String toString() {
        return "Video{" +
               "url='" + url + '\'' +
               ", titulo='" + titulo + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return Objects.equals(url, video.url) &&
               Objects.equals(titulo, video.titulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, titulo);
    }
}
