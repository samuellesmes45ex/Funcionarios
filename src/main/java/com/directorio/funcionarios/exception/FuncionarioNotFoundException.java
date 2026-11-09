package com.directorio.funcionarios.exception;

public class FuncionarioNotFoundException extends RuntimeException {

    public FuncionarioNotFoundException(Long id) {
        super("No existe un funcionario con ID " + id);
    }
}
