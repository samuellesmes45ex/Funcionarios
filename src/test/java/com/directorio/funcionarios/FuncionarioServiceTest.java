package com.directorio.funcionarios;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.directorio.funcionarios.exception.FuncionarioNotFoundException;
import com.directorio.funcionarios.exception.SolicitudInvalidaException;
import com.directorio.funcionarios.model.Funcionario;
import com.directorio.funcionarios.repository.FuncionarioRepository;
import com.directorio.funcionarios.service.FuncionarioService;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository repository;

    @InjectMocks
    private FuncionarioService service;

    private Funcionario sample;

    @BeforeEach
    void setUp() {
        sample = new Funcionario(1L, "Ana Perez", "Analista", "Contabilidad", "1234567");
    }

    @Test
    @DisplayName("Listar sin filtros devuelve todos")
    void listarSinFiltrosDevuelveTodos() {
        when(repository.findAll()).thenReturn(List.of(sample));

        List<Funcionario> resultado = service.listar(null, null);

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Filtros con solo espacios se tratan como ausentes")
    void listarFiltrosConEspaciosSeTratanComoAusentes() {
        when(repository.findAll()).thenReturn(List.of(sample));

        List<Funcionario> resultado = service.listar("   ", "   ");

        assertEquals(1, resultado.size());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Búsqueda por nombre busca coincidencia parcial sin distinguir mayúsculas")
    void listarPorNombre() {
        when(repository.findByNombreContainingIgnoreCase("ana")).thenReturn(List.of(sample));

        List<Funcionario> resultado = service.listar("  ana  ", null);

        assertEquals(1, resultado.size());
        verify(repository).findByNombreContainingIgnoreCase("ana");
    }

    @Test
    @DisplayName("Filtro por departamento busca por área completa")
    void listarPorDepartamento() {
        when(repository.findByDepartamentoIgnoreCase("Contabilidad")).thenReturn(List.of(sample));

        List<Funcionario> resultado = service.listar(null, "Contabilidad");

        assertEquals(1, resultado.size());
        verify(repository).findByDepartamentoIgnoreCase("Contabilidad");
    }

    @Test
    @DisplayName("Si llegan nombre y departamento, se aplica el filtro de nombre")
    void listarPrioridadNombreSobreDepartamento() {
        when(repository.findByNombreContainingIgnoreCase("ana")).thenReturn(List.of(sample));

        List<Funcionario> resultado = service.listar("ana", "Contabilidad");

        assertEquals(1, resultado.size());
        verify(repository).findByNombreContainingIgnoreCase("ana");
        verify(repository, never()).findByDepartamentoIgnoreCase(any());
    }

    @Test
    @DisplayName("Consultar por ID inexistente lanza FuncionarioNotFoundException")
    void consultarPorIdNoExiste() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(FuncionarioNotFoundException.class, () -> service.consultarPorId(99L));
    }

    @Test
    @DisplayName("Crear funcionario con ID no nulo lanza SolicitudInvalidaException")
    void crearConIdLanzaExcepcion() {
        Funcionario fConId = new Funcionario(10L, "Carlos", "Dev", "TI", null);

        assertThrows(SolicitudInvalidaException.class, () -> service.crear(fConId));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Crear funcionario limpia espacios en blanco y guarda")
    void crearLimpiaEspacios() {
        Funcionario nuevo = new Funcionario(null, "  Carlos  ", "  Dev  ", "  TI  ", "  5551234  ");
        when(repository.save(any(Funcionario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Funcionario guardado = service.crear(nuevo);

        assertEquals("Carlos", guardado.getNombre());
        assertEquals("Dev", guardado.getCargo());
        assertEquals("TI", guardado.getDepartamento());
        assertEquals("5551234", guardado.getTelefono());
    }

    @Test
    @DisplayName("Actualizar funcionario conserva su ID y actualiza campos")
    void actualizarExitoso() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample));
        when(repository.save(any(Funcionario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Funcionario datos = new Funcionario(null, "Ana Actualizada", "Senior", "TI", "");
        Funcionario actualizado = service.actualizar(1L, datos);

        assertEquals(1L, actualizado.getId());
        assertEquals("Ana Actualizada", actualizado.getNombre());
        assertEquals("Senior", actualizado.getCargo());
        assertEquals("TI", actualizado.getDepartamento());
        assertEquals("", actualizado.getTelefono());
    }

    @Test
    @DisplayName("Eliminar funcionario existente lo borra del repositorio")
    void eliminarExitoso() {
        when(repository.findById(1L)).thenReturn(Optional.of(sample));

        service.eliminar(1L);

        verify(repository).delete(sample);
    }
}
