package com.transparencia.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.transparencia.api.model.entity.MiembroTTAIP;

import java.util.List;

@Repository
public interface MiembroTTAIPRepository extends JpaRepository<MiembroTTAIP, Long> {
    List<MiembroTTAIP> findByActivoTrue();
}
