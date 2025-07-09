
package modelo.factory;

import modelo.entrada.EntradaGeneral;
import modelo.entrada.TipoEntrada;

public class EntradaGeneralFactory implements TipoEntradaFactory {
    @Override
    public TipoEntrada crearTipoEntrada(String nombre,
                                        String descripcionTipo,
                                        double precio,
                                        int cantidadTotal,
                                        int limiteCompraPorUsuario) {
        // Llama al constructor de EntradaGeneral que toma todos los parámetros.
        // Si EntradaGeneral tuviera constructores más simples que establecen algunos valores por defecto,
        // esta fábrica podría optar por usarlos si la información completa no siempre está disponible
        // o si se quieren estandarizar ciertos tipos de EntradaGeneral.
        return new EntradaGeneral(nombre, descripcionTipo, precio, cantidadTotal, limiteCompraPorUsuario);
    }
}
