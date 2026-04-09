package pe.gob.saip.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.saip.backend.model.entity.Administrador;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
}
