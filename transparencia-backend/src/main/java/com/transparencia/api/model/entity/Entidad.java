package com.transparencia.api.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "entidades")
public class Entidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entidad")
    private Long idEntidad;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 50)
    private String acronimo;

    public Long getId() {
        return this.idEntidad;
    }
}
