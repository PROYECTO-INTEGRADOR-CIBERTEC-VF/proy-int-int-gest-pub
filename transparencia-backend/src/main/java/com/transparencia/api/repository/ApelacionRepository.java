package com.transparencia.api.repository;

import com.transparencia.api.model.entity.Apelacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApelacionRepository extends JpaRepository<Apelacion, Long> {
    
  
    boolean existsBySolicitud_IdSolicitud(Long idSolicitud);
    
}