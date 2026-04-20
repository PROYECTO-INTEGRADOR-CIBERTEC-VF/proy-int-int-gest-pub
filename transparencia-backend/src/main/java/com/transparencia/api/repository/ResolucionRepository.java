package com.transparencia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.transparencia.api.model.entity.Resolucion;

import java.util.List;

@Repository
public interface ResolucionRepository extends JpaRepository<Resolucion, Long> {

    List<Resolucion> findByApelacion_IdApelacion(Long apelacionId);

    List<Resolucion> findByMiembroTTAIP_IdUsuario(Long miembroId);

    List<Resolucion> findByTipoResolucion(Resolucion.TipoResolucion tipo);
}
