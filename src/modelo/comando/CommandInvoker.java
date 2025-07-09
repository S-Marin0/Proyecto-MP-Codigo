
package modelo.comando;

import java.util.ArrayList;
import java.util.List;
// HashMap y Map no se usan en esta implementación simplificada de historial, pero podrían ser útiles para otras características.
// import java.util.HashMap;
// import java.util.Map;

public class CommandInvoker {
    private final List<Command> historialComandos;
    private int comandoActualPtr; // Puntero al último comando ejecutado/deshecho para redo. -1 si no hay nada que rehacer.
                                // O podría ser el índice del SIGUIENTE comando a ejecutar en un redo.
                                // Usaremos una implementación más simple para undo: siempre el último de la lista.
                                // Para redo, necesitaremos una lista separada o un puntero más complejo.

    // Para un undo/redo más robusto, se suelen usar dos pilas o un puntero.
    // Aquí simplificaremos: `historialComandos` guarda los que se pueden deshacer.
    // `comandosDeshechos` guarda los que se pueden rehacer.
    private final List<Command> comandosDeshechos;

    private int maxHistorial; // Opcional: limitar tamaño del historial

    public CommandInvoker() {
        this(Integer.MAX_VALUE); // Historial "ilimitado" por defecto
    }

    public CommandInvoker(int maxHistorial) {
        if (maxHistorial <= 0) throw new IllegalArgumentException("Max historial debe ser positivo.");
        this.maxHistorial = maxHistorial;
        this.historialComandos = new ArrayList<>();
        this.comandosDeshechos = new ArrayList<>();
        // this.comandosPendientes = new HashMap<>(); // No implementado en esta versión
        // this.modoTransaccional = false; // No implementado en esta versión
    }

    // Renombrado de ejecutarComando a setCommandAndExecute para claridad o simplemente 'execute'
    public boolean executeCommand(Command comando) {
        if (comando == null) return false;

        boolean resultado = comando.execute();
        if (resultado) {
            agregarAlHistorial(comando);
            // Cuando se ejecuta un nuevo comando, el historial de "redo" se invalida.
            comandosDeshechos.clear();
        }
        return resultado;
    }

    // Este es el método que Asistente llama "deshacerUltimaOperacion"
    public boolean undoCommand() {
        if (historialComandos.isEmpty()) {
            System.out.println("No hay comandos para deshacer.");
            return false;
        }
        // Obtener el último comando ejecutado
        Command ultimoComando = historialComandos.remove(historialComandos.size() - 1);
        boolean resultadoUndo = ultimoComando.undo();

        if (resultadoUndo) {
            comandosDeshechos.add(ultimoComando); // Añadir a la pila de redo
            System.out.println("Comando '" + ultimoComando.getDescription() + "' deshecho.");
        } else {
            // Si el undo falla, se podría intentar volver a añadirlo al historial,
            // pero esto puede ser complejo. Por ahora, simplemente no se añade a deshechos.
            System.err.println("Falló el intento de deshacer el comando: " + ultimoComando.getDescription());
            // Podríamos optar por reinsertarlo en el historial si el undo falla y es crítico.
            // historialComandos.add(ultimoComando); // Cuidado con el orden.
        }
        return resultadoUndo;
    }

    public boolean redoCommand() {
        if (comandosDeshechos.isEmpty()) {
            System.out.println("No hay comandos para rehacer.");
            return false;
        }
        // Obtener el último comando deshecho
        Command comandoARehacer = comandosDeshechos.remove(comandosDeshechos.size() - 1);
        // Volver a ejecutar el comando. Su estado interno debería permitir la re-ejecución.
        // La implementación de execute() en ComprarEntradasCommand previene la re-ejecución si ya fue exitoso,
        // pero el undo() lo marca como no ejecutado.
        boolean resultadoRedo = comandoARehacer.execute();

        if (resultadoRedo) {
            historialComandos.add(comandoARehacer); // Añadir de nuevo al historial de undo
            System.out.println("Comando '" + comandoARehacer.getDescription() + "' rehecho.");
        } else {
            System.err.println("Falló el intento de rehacer el comando: " + comandoARehacer.getDescription());
            // Si el redo falla, no se añade de nuevo a comandosDeshechos.
        }
        return resultadoRedo;
    }

    private void agregarAlHistorial(Command comando) {
        if (historialComandos.size() >= maxHistorial) {
            historialComandos.remove(0); // Eliminar el más antiguo si se alcanza el límite
        }
        historialComandos.add(comando);
    }

    public List<Command> getHistorialComandos() { // Para ver qué se puede deshacer
        return new ArrayList<>(historialComandos); // Devuelve copia
    }

    public List<Command> getComandosDeshechos() { // Para ver qué se puede rehacer
        return new ArrayList<>(comandosDeshechos); // Devuelve copia
    }

    public void limpiarHistorial() {
        historialComandos.clear();
        comandosDeshechos.clear();
        System.out.println("Historial de comandos y de deshechos limpiado.");
    }

    public void setMaxHistorial(int max) {
        if (max > 0) this.maxHistorial = max;
    }

    // Los siguientes métodos no están completamente implementados o son placeholders
    // public void habilitarModoTransaccional(boolean habilitado) { this.modoTransaccional = habilitado; }
    // public boolean ejecutarTransacción(List<Command> comandos) { return false; } // Necesitaría lógica de rollback para todos si uno falla
    public int contarComandosParaDeshacer() { return historialComandos.size(); }
    public int contarComandosParaRehacer() { return comandosDeshechos.size(); }

    public Command obtenerUltimoComandoParaDeshacer() {
        if (!historialComandos.isEmpty()) {
            return historialComandos.get(historialComandos.size() - 1);
        }
        return null;
    }

    // limpiarComandosPosteriores y validarCapacidadHistorial eran de una implementación más compleja con puntero.
    // No son directamente necesarios con la doble lista (historialComandos y comandosDeshechos).
}
