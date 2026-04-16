package com.transparencia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.EstadoApelacion;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApelacionRepository extends JpaRepository<Apelacion, Long> {
    Optional<Apelacion> findByExpediente(String expediente);
    List<Apelacion> findByEstado(EstadoApelacion estado);
    List<Apelacion> findByCiudadano_IdUsuario(Long idUsuario);
    boolean existsBySolicitud_IdSolicitud(Long idSolicitud);
}