package modelo.repositorio;

import modelo.lugar.Lugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LugarRepository extends JpaRepository<Lugar, Long> {
    // Podríamos añadir búsquedas por nombre, dirección, etc. si es necesario
    Optional<Lugar> findByNombreAndDireccion(String nombre, String direccion);
}
