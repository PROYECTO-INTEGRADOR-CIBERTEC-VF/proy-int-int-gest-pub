package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transparencia.api.model.entity.MiembroTTAIP;
import com.transparencia.api.repository.MiembroTTAIPRepository;

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
