package com.transparencia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.transparencia.api.model.entity.Documento;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findBySolicitud_IdSolicitud(Long solicitudId);

    List<Documento> findByRespuesta_IdRespuesta(Long respuestaId);
}
