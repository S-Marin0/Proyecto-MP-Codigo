
package modelo.factory;

import modelo.entrada.TipoEntrada;

public interface TipoEntradaFactory {
    TipoEntrada crearTipoEntrada(String nombre,
                                 String descripcionTipo,
                                 double precio,
                                 int cantidadTotal,
                                 int limiteCompraPorUsuario);
}
