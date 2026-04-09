package pe.gob.saip.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.saip.backend.model.entity.Entidad;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntidadRepository extends JpaRepository<Entidad, Long> {
    Optional<Entidad> findByAcronimo(String acronimo);
    List<Entidad> findByActivaTrue();
}
