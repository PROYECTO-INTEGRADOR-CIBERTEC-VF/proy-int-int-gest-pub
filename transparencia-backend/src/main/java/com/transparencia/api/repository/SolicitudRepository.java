package com.transparencia.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.transparencia.api.model.entity.Solicitud;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findByExpediente(String expediente);

    List<Solicitud> findByCiudadano_IdUsuarioOrderByFechaPresentacionDesc(Long ciudadanoId);

    List<Solicitud> findByEntidad_IdEntidadOrderByFechaPresentacionDesc(Long entidadId);

    List<Solicitud> findByEstado(Solicitud.EstadoSolicitud estado);

    Page<Solicitud> findByEstado(Solicitud.EstadoSolicitud estado, Pageable pageable);

    long countByEstado(Solicitud.EstadoSolicitud estado);

    long countByEntidad_IdEntidad(Long entidadId);

    @Query("SELECT s FROM Solicitud s WHERE s.fechaLimite < :hoy AND s.estado NOT IN :estadosFinales")
    List<Solicitud> findVencidas(@Param("hoy") LocalDate hoy, @Param("estadosFinales") List<Solicitud.EstadoSolicitud> estadosFinales);

    @Query("SELECT s FROM Solicitud s LEFT JOIN FETCH s.respuesta LEFT JOIN FETCH s.documentos WHERE s.idSolicitud = :id")
    Optional<Solicitud> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT s FROM Solicitud s LEFT JOIN FETCH s.respuesta WHERE s.ciudadano.idUsuario = :ciudadanoId ORDER BY s.fechaPresentacion DESC")
    List<Solicitud> findByCiudadanoWithRespuesta(@Param("ciudadanoId") Long ciudadanoId);

    @Query("SELECT COUNT(s) FROM Solicitud s WHERE s.entidad.idEntidad = :entidadId AND s.estado = :estado")
    long countByEntidadAndEstado(@Param("entidadId") Long entidadId, @Param("estado") Solicitud.EstadoSolicitud estado);

    @Query("SELECT MAX(CAST(SUBSTRING(s.expediente, 11, 5) AS int)) FROM Solicitud s WHERE s.expediente LIKE :prefix")
    Integer findMaxExpedienteNumber(@Param("prefix") String prefix);
}
