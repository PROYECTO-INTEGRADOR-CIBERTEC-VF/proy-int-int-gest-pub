package com.transparencia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.transparencia.api.model.entity.Ciudadano;

import java.util.Optional;

@Repository
public interface CiudadanoRepository extends JpaRepository<Ciudadano, Long> {
    Optional<Ciudadano> findByDni(String dni);
    Optional<Ciudadano> findByEmail(String email);
}
