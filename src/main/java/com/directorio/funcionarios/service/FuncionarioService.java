package com.directorio.funcionarios.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.directorio.funcionarios.exception.FuncionarioNotFoundException;
import com.directorio.funcionarios.exception.SolicitudInvalidaException;
import com.directorio.funcionarios.model.Funcionario;
import com.directorio.funcionarios.repository.FuncionarioRepository;

@Service
public class FuncionarioService {

    private final FuncionarioRepository repository;

    public FuncionarioService(FuncionarioRepository repository) {
        this.repository = repository;
    }

    /**
     * Lista funcionarios. Prioridad: nombre > departamento > todos.
     * Los filtros vacíos o con solo espacios se tratan como ausentes.
     */
    public List<Funcionario> listar(String nombre, String departamento) {
        if (tieneTexto(nombre)) {
            return repository.findByNombreContainingIgnoreCase(nombre.trim());
        }
        if (tieneTexto(departamento)) {
            return repository.findByDepartamentoIgnoreCase(departamento.trim());
        }
        return repository.findAll();
    }

    public Funcionario consultarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new FuncionarioNotFoundException(id));
    }

    public Funcionario crear(Funcionario funcionario) {
        if (funcionario.getId() != null) {
            throw new SolicitudInvalidaException("No se debe enviar un ID al registrar un funcionario");
        }
        limpiarTextos(funcionario);
        return repository.save(funcionario);
    }

    public Funcionario actualizar(Long id, Funcionario datos) {
        Funcionario existente = consultarPorId(id);
        limpiarTextos(datos);
        existente.setNombre(datos.getNombre());
        existente.setCargo(datos.getCargo());
        existente.setDepartamento(datos.getDepartamento());
        existente.setTelefono(datos.getTelefono());
        return repository.save(existente);
    }

    public void eliminar(Long id) {
        Funcionario existente = consultarPorId(id);
        repository.delete(existente);
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private void limpiarTextos(Funcionario f) {
        f.setNombre(f.getNombre().trim());
        f.setCargo(f.getCargo().trim());
        f.setDepartamento(f.getDepartamento().trim());
        if (f.getTelefono() != null) {
            f.setTelefono(f.getTelefono().trim());
        }
    }
}
