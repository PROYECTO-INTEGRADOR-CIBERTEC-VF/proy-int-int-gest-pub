package pe.gob.saip.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.saip.backend.model.entity.MiembroTTAIP;

import java.util.List;

@Repository
public interface MiembroTTAIPRepository extends JpaRepository<MiembroTTAIP, Long> {
    List<MiembroTTAIP> findByActivoTrue();
}
