package com.directorio.funcionarios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.directorio.funcionarios.model.Funcionario;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    // Coincidencia parcial, sin distinguir mayúsculas/minúsculas
    List<Funcionario> findByNombreContainingIgnoreCase(String nombre);

    // Nombre completo del departamento, sin distinguir mayúsculas/minúsculas
    List<Funcionario> findByDepartamentoIgnoreCase(String departamento);
}
