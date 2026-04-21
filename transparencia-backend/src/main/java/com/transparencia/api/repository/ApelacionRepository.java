package com.transparencia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.transparencia.api.model.entity.Apelacion;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApelacionRepository extends JpaRepository<Apelacion, Long> {

    Optional<Apelacion> findByExpediente(String expediente);

    List<Apelacion> findByCiudadano_IdUsuarioOrderByFechaApelacionDesc(Long ciudadanoId);

    List<Apelacion> findByEstadoOrderByFechaApelacionDesc(Apelacion.EstadoApelacion estado);

    long countByEstado(Apelacion.EstadoApelacion estado);

    @Query("SELECT a FROM Apelacion a LEFT JOIN FETCH a.solicitud s LEFT JOIN FETCH s.entidad LEFT JOIN FETCH a.ciudadano WHERE a.idApelacion = :id")
    Optional<Apelacion> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT a FROM Apelacion a WHERE a.estado NOT IN :estadosFinales ORDER BY a.fechaApelacion DESC")
    List<Apelacion> findPendientes(@Param("estadosFinales") List<Apelacion.EstadoApelacion> estadosFinales);
}
