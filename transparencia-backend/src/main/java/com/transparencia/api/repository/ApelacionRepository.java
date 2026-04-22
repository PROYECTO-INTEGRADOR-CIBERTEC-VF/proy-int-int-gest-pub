package com.transparencia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.transparencia.api.model.entity.Apelacion;
import com.transparencia.api.model.entity.EstadoApelacion;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApelacionRepository extends JpaRepository<Apelacion, Long> {

    Optional<Apelacion> findByExpediente(String expediente);

    @Query("SELECT a FROM Apelacion a LEFT JOIN FETCH a.solicitud s LEFT JOIN FETCH s.entidad LEFT JOIN FETCH a.ciudadano WHERE a.expediente = :expediente")
    Optional<Apelacion> findByExpedienteWithDetails(@Param("expediente") String expediente);

    List<Apelacion> findByCiudadano_IdUsuarioOrderByFechaApelacionDesc(Long ciudadanoId);

    List<Apelacion> findByCiudadano_IdUsuario(Long ciudadanoId);


    List<Apelacion> findByEstadoOrderByFechaApelacionDesc(EstadoApelacion estado);

    List<Apelacion> findByEstado(EstadoApelacion estado);

    List<Apelacion> findByEstadoIn(List<EstadoApelacion> estados);

    long countByEstado(EstadoApelacion estado);

    boolean existsBySolicitud_IdSolicitud(Long solicitudId);

    @Query("SELECT a FROM Apelacion a LEFT JOIN FETCH a.solicitud LEFT JOIN FETCH a.ciudadano WHERE a.id = :id")
    Optional<Apelacion> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT a FROM Apelacion a LEFT JOIN FETCH a.solicitud s LEFT JOIN FETCH s.entidad WHERE a.ciudadano.idUsuario = :ciudadanoId ORDER BY a.fechaApelacion DESC")
    List<Apelacion> findByCiudadanoWithSolicitud(@Param("ciudadanoId") Long ciudadanoId);

    @Query("SELECT a FROM Apelacion a WHERE a.estado NOT IN :estadosFinales ORDER BY a.fechaApelacion DESC")
    List<Apelacion> findPendientes(@Param("estadosFinales") List<EstadoApelacion> estadosFinales);
}