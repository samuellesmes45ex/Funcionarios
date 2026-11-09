package com.directorio.funcionarios;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.directorio.funcionarios.controller.FuncionarioController;
import com.directorio.funcionarios.exception.FuncionarioNotFoundException;
import com.directorio.funcionarios.exception.SolicitudInvalidaException;
import com.directorio.funcionarios.model.Funcionario;
import com.directorio.funcionarios.service.FuncionarioService;

@WebMvcTest(FuncionarioController.class)
class FuncionarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FuncionarioService service;

    @Test
    @DisplayName("GET /api/v1/funcionarios devuelve 200 con lista si hay registros")
    void listarConRegistrosDevuelve200() throws Exception {
        Funcionario f = new Funcionario(1L, "Ana Perez", "Analista", "Contabilidad", "1234567");
        when(service.listar(null, null)).thenReturn(List.of(f));

        mockMvc.perform(get("/api/v1/funcionarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Ana Perez"));
    }

    @Test
    @DisplayName("GET /api/v1/funcionarios devuelve 204 sin cuerpo si el directorio está vacío")
    void listarVacioDevuelve204() throws Exception {
        when(service.listar(null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/funcionarios"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/funcionarios/{id} devuelve 200 con funcionario si existe")
    void consultarPorIdExiste() throws Exception {
        Funcionario f = new Funcionario(1L, "Ana Perez", "Analista", "Contabilidad", null);
        when(service.consultarPorId(1L)).thenReturn(f);

        mockMvc.perform(get("/api/v1/funcionarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Perez"));
    }

    @Test
    @DisplayName("GET /api/v1/funcionarios/{id} devuelve 404 si ID no existe")
    void consultarPorIdNoExisteDevuelve404() throws Exception {
        when(service.consultarPorId(99L)).thenThrow(new FuncionarioNotFoundException(99L));

        mockMvc.perform(get("/api/v1/funcionarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe un funcionario con ID 99"));
    }

    @Test
    @DisplayName("GET /api/v1/funcionarios/{id} devuelve 400 si ID no es numérico")
    void consultarPorIdInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/v1/funcionarios/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/funcionarios devuelve 201 con el recurso creado")
    void registrarExitoso() throws Exception {
        Funcionario creado = new Funcionario(1L, "Carlos", "Dev", "TI", null);
        when(service.crear(any(Funcionario.class))).thenReturn(creado);

        String json = "{\"nombre\":\"Carlos\",\"cargo\":\"Dev\",\"departamento\":\"TI\"}";

        mockMvc.perform(post("/api/v1/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos"));
    }

    @Test
    @DisplayName("POST /api/v1/funcionarios responde 400 si falta campo obligatorio o está en blanco")
    void registrarCamposInvalidosDevuelve400() throws Exception {
        String json = "{\"nombre\":\"   \",\"cargo\":\"\",\"departamento\":null}";

        mockMvc.perform(post("/api/v1/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/funcionarios responde 400 si se envía ID no nulo")
    void registrarConIdDevuelve400() throws Exception {
        when(service.crear(any(Funcionario.class)))
                .thenThrow(new SolicitudInvalidaException("No se debe enviar un ID al registrar un funcionario"));

        String json = "{\"id\":10,\"nombre\":\"Carlos\",\"cargo\":\"Dev\",\"departamento\":\"TI\"}";

        mockMvc.perform(post("/api/v1/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("No se debe enviar un ID al registrar un funcionario"));
    }

    @Test
    @DisplayName("POST /api/v1/funcionarios responde 400 si el teléfono contiene letras")
    void registrarConTelefonoConLetrasDevuelve400() throws Exception {
        String json = "{\"nombre\":\"Carlos\",\"cargo\":\"Dev\",\"departamento\":\"TI\",\"telefono\":\"123abc\"}";

        mockMvc.perform(post("/api/v1/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje").value("El teléfono solo debe contener números"));
    }

    @Test
    @DisplayName("PUT /api/v1/funcionarios/{id} devuelve 200 con funcionario actualizado")
    void actualizarExitosoDevuelve200() throws Exception {
        Funcionario actualizado = new Funcionario(1L, "Carlos Editado", "Senior", "TI", "5559999");
        when(service.actualizar(eq(1L), any(Funcionario.class))).thenReturn(actualizado);

        String json = "{\"nombre\":\"Carlos Editado\",\"cargo\":\"Senior\",\"departamento\":\"TI\",\"telefono\":\"5559999\"}";

        mockMvc.perform(put("/api/v1/funcionarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos Editado"));
    }

    @Test
    @DisplayName("PUT /api/v1/funcionarios/{id} devuelve 404 si ID no existe")
    void actualizarInexistenteDevuelve404() throws Exception {
        when(service.actualizar(eq(99L), any(Funcionario.class)))
                .thenThrow(new FuncionarioNotFoundException(99L));

        String json = "{\"nombre\":\"Carlos\",\"cargo\":\"Senior\",\"departamento\":\"TI\"}";

        mockMvc.perform(put("/api/v1/funcionarios/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /api/v1/funcionarios/{id} devuelve 204 al eliminar")
    void eliminarExitosoDevuelve204() throws Exception {
        doNothing().when(service).eliminar(1L);

        mockMvc.perform(delete("/api/v1/funcionarios/1"))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @DisplayName("DELETE /api/v1/funcionarios/{id} devuelve 404 si ID no existe")
    void eliminarInexistenteDevuelve404() throws Exception {
        doThrow(new FuncionarioNotFoundException(99L)).when(service).eliminar(99L);

        mockMvc.perform(delete("/api/v1/funcionarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET con ?nombre=ana devuelve 200 con coincidencias")
    void buscarPorNombreDevuelve200() throws Exception {
        Funcionario f = new Funcionario(1L, "Ana Perez", "Analista", "Contabilidad", null);
        when(service.listar("ana", null)).thenReturn(List.of(f));

        mockMvc.perform(get("/api/v1/funcionarios").param("nombre", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana Perez"));
    }

    @Test
    @DisplayName("GET con ?nombre=desconocido devuelve 204 si no hay coincidencias")
    void buscarPorNombreSinCoincidenciasDevuelve204() throws Exception {
        when(service.listar("desconocido", null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/funcionarios").param("nombre", "desconocido"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET con ?departamento=Contabilidad devuelve 200")
    void filtrarPorDepartamentoDevuelve200() throws Exception {
        Funcionario f = new Funcionario(1L, "Ana Perez", "Analista", "Contabilidad", null);
        when(service.listar(null, "Contabilidad")).thenReturn(List.of(f));

        mockMvc.perform(get("/api/v1/funcionarios").param("departamento", "Contabilidad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departamento").value("Contabilidad"));
    }
}
