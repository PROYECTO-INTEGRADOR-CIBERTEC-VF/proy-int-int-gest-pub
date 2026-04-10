package pe.gob.saip.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.saip.backend.model.entity.MiembroTTAIP;
import pe.gob.saip.backend.repository.MiembroTTAIPRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MiembroTTAIPService {

    private final MiembroTTAIPRepository miembroTTAIPRepository;

    public MiembroTTAIPService(MiembroTTAIPRepository miembroTTAIPRepository) {
        this.miembroTTAIPRepository = miembroTTAIPRepository;
    }

    @Transactional(readOnly = true)
    public Optional<MiembroTTAIP> obtenerMiembroTTAIPPorId(Long id) {
        return miembroTTAIPRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<MiembroTTAIP> obtenerMiembrosTTAIPActivos() {
        return miembroTTAIPRepository.findByActivoTrue();
    }
}
