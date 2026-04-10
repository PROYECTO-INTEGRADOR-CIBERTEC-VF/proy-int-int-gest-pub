package pe.gob.saip.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.saip.backend.model.entity.Entidad;
import pe.gob.saip.backend.repository.EntidadRepository;

import java.util.List;
import java.util.Optional;

@Service
public class EntidadService {

    private final EntidadRepository entidadRepository;

    public EntidadService(EntidadRepository entidadRepository) {
        this.entidadRepository = entidadRepository;
    }

    @Transactional(readOnly = true)
    public List<Entidad> findAll() {
        return entidadRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Entidad> findById(Long id) {
        return entidadRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Entidad> findByAcronimo(String acronimo) {
        return entidadRepository.findByAcronimo(acronimo);
    }
}