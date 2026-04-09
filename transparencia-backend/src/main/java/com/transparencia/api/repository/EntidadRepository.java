package com.transparencia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.transparencia.api.model.entity.Entidad;

@Repository
public interface EntidadRepository extends JpaRepository<Entidad, Long> {
}
