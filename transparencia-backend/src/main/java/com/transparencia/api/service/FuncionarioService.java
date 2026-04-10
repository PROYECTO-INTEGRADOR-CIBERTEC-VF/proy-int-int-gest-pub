package com.transparencia.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.transparencia.api.model.entity.Funcionario;
import com.transparencia.api.repository.FuncionarioRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Funcionario> obtenerFuncionarioPorId(Long id) {
        return funcionarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Funcionario> obtenerFuncionariosPorEntidad(Long entidadId) {
        return funcionarioRepository.findByEntidad_IdEntidad(entidadId);
    }
}
