package modelo.reporte;

import java.util.Date;
import java.util.Map;
// import modelo.evento.Evento; // Asumiendo que un reporte puede ser sobre un evento
// Otros imports según qué contenga el reporte: Usuario, Compra, etc.

public class Reporte {
    private String idReporte;
    private String titulo;
    private Date fechaGeneracion;
    private String tipoReporte; // Ej: "VentasEvento", "AsistenciaUsuario", "PopularidadCategoria"
    private Object datosReporte; // Podría ser un Map<String, Object>, un JSON String, o un objeto específico
    private String generadoPor; // ID o nombre del usuario/sistema que generó el reporte

    // Constructor (ejemplo)
    public Reporte(String idReporte, String titulo, String tipoReporte, Object datos, String generadoPor) {
        if (idReporte == null || idReporte.trim().isEmpty()) throw new IllegalArgumentException("ID de reporte no puede ser nulo o vacío.");
        if (titulo == null || titulo.trim().isEmpty()) throw new IllegalArgumentException("Título de reporte no puede ser nulo o vacío.");
        if (tipoReporte == null || tipoReporte.trim().isEmpty()) throw new IllegalArgumentException("Tipo de reporte no puede ser nulo o vacío.");

        this.idReporte = idReporte;
        this.titulo = titulo;
        this.tipoReporte = tipoReporte;
        this.datosReporte = datos; // Podría necesitar una copia profunda si es mutable
        this.generadoPor = generadoPor;
        this.fechaGeneracion = new Date();
    }

    // Getters
    public String getIdReporte() { return idReporte; }
    public String getTitulo() { return titulo; }
    public Date getFechaGeneracion() { return fechaGeneracion != null ? (Date) fechaGeneracion.clone() : null; } // Devuelve copia
    public String getTipoReporte() { return tipoReporte; }
    public Object getDatosReporte() { return datosReporte; } // Considerar devolver copia o hacerlo inmutable
    public String getGeneradoPor() { return generadoPor; }

    // Setters (selectivos, si son necesarios)
    public void setTitulo(String titulo) {
        if (titulo != null && !titulo.trim().isEmpty()) this.titulo = titulo;
    }
    public void setDatosReporte(Object datosReporte) {
        this.datosReporte = datosReporte; // Considerar copia
    }


    // Métodos para visualizar o exportar el reporte (ejemplos)
    public void mostrarEnConsola() {
        System.out.println("\n--- Reporte ---");
        System.out.println("ID: " + idReporte);
        System.out.println("Título: " + titulo);
        System.out.println("Tipo: " + tipoReporte);
        System.out.println("Generado el: " + (fechaGeneracion != null ? fechaGeneracion.toString() : "N/A"));
        System.out.println("Generado por: " + (generadoPor != null ? generadoPor : "N/A"));
        System.out.println("Datos del Reporte:");

        if (datosReporte == null) {
            System.out.println("  (No hay datos disponibles para este reporte)");
        } else if (datosReporte instanceof Map) {
            @SuppressWarnings("unchecked") // Necesario si se trabaja con Map<?,?> genérico
            Map<Object, Object> mapDatos = (Map<Object, Object>) datosReporte;
            if (mapDatos.isEmpty()) {
                System.out.println("  (El mapa de datos está vacío)");
            } else {
                mapDatos.forEach((key, value) -> System.out.println("  " + key + ": " + value));
            }
        } else {
            // Para otros tipos de objeto, simplemente usar toString()
            System.out.println("  " + datosReporte.toString());
        }
        System.out.println("--- Fin Reporte ---");
    }

    public String exportarACSV() {
        // Lógica para convertir datosReporte a formato CSV
        // Esto es una simulación muy básica y dependería de la estructura de datosReporte
        System.out.println("Exportando reporte '" + titulo + "' a CSV (simulación)...");
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("ID_Reporte,Titulo,Tipo,Fecha_Generacion,Generado_Por,Datos\n");
        csvBuilder.append(String.join(",",
            escapeCsv(idReporte),
            escapeCsv(titulo),
            escapeCsv(tipoReporte),
            escapeCsv(fechaGeneracion != null ? fechaGeneracion.toString() : ""),
            escapeCsv(generadoPor != null ? generadoPor : ""),
            escapeCsv(datosReporte != null ? datosReporte.toString() : "") // Simplificación de datos
        ));
        return csvBuilder.toString();
    }

    private String escapeCsv(String data) {
        if (data == null) return "";
        String escapedData = data.replace("\"", "\"\""); // Escapar comillas dobles
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            return "\"" + escapedData + "\""; // Envolver en comillas si contiene caracteres especiales
        }
        return escapedData;
    }
}
