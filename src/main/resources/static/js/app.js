// URL base de la API REST (misma aplicación, no requiere CORS)
const API_URL = "/api/v1/funcionarios";

// ---------- Referencias al DOM ----------
const formFuncionario = document.getElementById("form-funcionario");
const campoId = document.getElementById("id");
const campoNombre = document.getElementById("nombre");
const campoCargo = document.getElementById("cargo");
const campoDepartamento = document.getElementById("departamento");
const campoTelefono = document.getElementById("telefono");
const btnGuardar = document.getElementById("btn-guardar");
const btnLimpiar = document.getElementById("btn-limpiar");
const btnListado = document.getElementById("btn-listado");
const tituloFormulario = document.getElementById("titulo-formulario");
const tablaCuerpo = document.getElementById("tabla-cuerpo");
const contador = document.getElementById("contador");
const cajaMensaje = document.getElementById("mensaje");

const formBuscarId = document.getElementById("form-buscar-id");
const formBuscarNombre = document.getElementById("form-buscar-nombre");
const formBuscarDepartamento = document.getElementById("form-buscar-departamento");
const campoBuscarId = document.getElementById("buscar-id");
const campoBuscarNombre = document.getElementById("buscar-nombre");
const campoBuscarDepartamento = document.getElementById("buscar-departamento");

// ---------- Mensajes ----------
function mostrarMensaje(tipo, texto) {
    cajaMensaje.textContent = texto;
    cajaMensaje.className = "mensaje " + tipo;
}

function ocultarMensaje() {
    cajaMensaje.textContent = "";
    cajaMensaje.className = "mensaje oculto";
}

// ---------- Comunicación con la API ----------
// Devuelve el JSON de la respuesta, o null si es 204 (sin cuerpo).
// Si la API responde con error, lanza un Error con el mensaje recibido.
async function peticion(url, opciones) {
    let respuesta;
    try {
        respuesta = await fetch(url, opciones);
    } catch (e) {
        throw new Error("No se pudo conectar con el servidor. Verifica que la aplicación esté en ejecución.");
    }

    if (respuesta.status === 204) {
        return null;
    }

    let cuerpo = null;
    try {
        cuerpo = await respuesta.json();
    } catch (e) {
        cuerpo = null;
    }

    if (!respuesta.ok) {
        const mensaje = cuerpo && cuerpo.mensaje ? cuerpo.mensaje : "Error inesperado (código " + respuesta.status + ")";
        throw new Error(mensaje);
    }
    return cuerpo;
}

// ---------- Tabla ----------
function crearCelda(texto) {
    const td = document.createElement("td");
    td.textContent = texto;
    return td;
}

function dibujarTabla(lista) {
    tablaCuerpo.innerHTML = "";
    contador.textContent = lista.length > 0 ? "(" + lista.length + ")" : "";

    lista.forEach(function (f) {
        const fila = document.createElement("tr");
        fila.appendChild(crearCelda(f.id));
        fila.appendChild(crearCelda(f.nombre));
        fila.appendChild(crearCelda(f.cargo));
        fila.appendChild(crearCelda(f.departamento));
        fila.appendChild(crearCelda(f.telefono ? f.telefono : ""));

        const celdaAcciones = document.createElement("td");
        celdaAcciones.className = "celda-acciones";

        const btnEditar = document.createElement("button");
        btnEditar.type = "button";
        btnEditar.className = "btn btn-secundario btn-pequeno";
        btnEditar.textContent = "Editar";
        btnEditar.setAttribute("aria-label", "Editar a " + f.nombre);
        btnEditar.addEventListener("click", function () {
            editarFuncionario(f.id);
        });

        const btnEliminar = document.createElement("button");
        btnEliminar.type = "button";
        btnEliminar.className = "btn btn-peligro btn-pequeno";
        btnEliminar.textContent = "Eliminar";
        btnEliminar.setAttribute("aria-label", "Eliminar a " + f.nombre);
        btnEliminar.addEventListener("click", function () {
            eliminarFuncionario(f.id, f.nombre);
        });

        celdaAcciones.appendChild(btnEditar);
        celdaAcciones.appendChild(btnEliminar);
        fila.appendChild(celdaAcciones);
        tablaCuerpo.appendChild(fila);
    });
}

// ---------- Listado y búsquedas ----------
async function cargarListado(mostrarAviso) {
    try {
        const lista = await peticion(API_URL);
        if (lista === null) {
            dibujarTabla([]);
            if (mostrarAviso) {
                mostrarMensaje("aviso", "El directorio está vacío. Registra el primer funcionario.");
            }
            return;
        }
        dibujarTabla(lista);
        if (mostrarAviso) {
            mostrarMensaje("exito", "Listado completo cargado.");
        }
    } catch (e) {
        dibujarTabla([]);
        mostrarMensaje("error", e.message);
    }
}

async function buscarPor(parametro, valor, descripcion) {
    if (valor.trim() === "") {
        mostrarMensaje("aviso", "Escribe un valor para " + descripcion + ".");
        return;
    }
    try {
        const url = API_URL + "?" + parametro + "=" + encodeURIComponent(valor.trim());
        const lista = await peticion(url);
        if (lista === null) {
            dibujarTabla([]);
            mostrarMensaje("aviso", "No se encontraron funcionarios " + descripcion + " \"" + valor.trim() + "\".");
            return;
        }
        dibujarTabla(lista);
        mostrarMensaje("exito", "Se encontraron " + lista.length + " resultado(s).");
    } catch (e) {
        mostrarMensaje("error", e.message);
    }
}

async function consultarPorId(valor) {
    const texto = valor.trim();
    if (!/^\d+$/.test(texto)) {
        mostrarMensaje("aviso", "Escribe un ID numérico válido.");
        return;
    }
    try {
        const f = await peticion(API_URL + "/" + texto);
        dibujarTabla([f]);
        mostrarMensaje("exito", "Funcionario con ID " + texto + " encontrado.");
    } catch (e) {
        dibujarTabla([]);
        mostrarMensaje("aviso", e.message);
    }
}

// ---------- Formulario ----------
function limpiarFormulario() {
    formFuncionario.reset();
    campoId.value = "";
    tituloFormulario.textContent = "Registrar funcionario";
    btnGuardar.textContent = "Guardar";
    [campoNombre, campoCargo, campoDepartamento, campoTelefono].forEach(function (c) {
        c.classList.remove("invalido");
    });
}

function validarFormulario() {
    let valido = true;
    let mensajeError = "";

    [campoNombre, campoCargo, campoDepartamento].forEach(function (campo) {
        if (campo.value.trim() === "") {
            campo.classList.add("invalido");
            valido = false;
            mensajeError = "Nombre, cargo y departamento son obligatorios y no pueden estar vacíos.";
        } else {
            campo.classList.remove("invalido");
        }
    });

    const tel = campoTelefono.value.trim();
    if (tel !== "" && !/^\d+$/.test(tel)) {
        campoTelefono.classList.add("invalido");
        valido = false;
        mensajeError = mensajeError || "El teléfono solo debe contener números.";
    } else {
        campoTelefono.classList.remove("invalido");
    }

    if (!valido && mensajeError) {
        mostrarMensaje("error", mensajeError);
    }

    return valido;
}

async function editarFuncionario(id) {
    try {
        const f = await peticion(API_URL + "/" + id);
        campoId.value = f.id;
        campoNombre.value = f.nombre;
        campoCargo.value = f.cargo;
        campoDepartamento.value = f.departamento;
        campoTelefono.value = f.telefono ? f.telefono : "";
        tituloFormulario.textContent = "Editar funcionario";
        btnGuardar.textContent = "Guardar cambios";
        mostrarMensaje("aviso", "Editando a " + f.nombre + ". Modifica los datos y guarda los cambios.");
        formFuncionario.scrollIntoView({ behavior: "smooth", block: "start" });
        campoNombre.focus();
    } catch (e) {
        mostrarMensaje("error", e.message);
        cargarListado(false);
    }
}

async function guardarFuncionario(evento) {
    evento.preventDefault();

    if (!validarFormulario()) {
        return;
    }

    const datos = {
        nombre: campoNombre.value.trim(),
        cargo: campoCargo.value.trim(),
        departamento: campoDepartamento.value.trim(),
        telefono: campoTelefono.value.trim()
    };

    const editando = campoId.value !== "";
    const url = editando ? API_URL + "/" + campoId.value : API_URL;
    const metodo = editando ? "PUT" : "POST";

    try {
        await peticion(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(datos)
        });
        limpiarFormulario();
        await cargarListado(false);
        mostrarMensaje("exito", editando
            ? "Funcionario actualizado correctamente."
            : "Funcionario registrado correctamente.");
    } catch (e) {
        mostrarMensaje("error", e.message);
    }
}

async function eliminarFuncionario(id, nombre) {
    const confirmado = window.confirm("¿Seguro que deseas eliminar a " + nombre + "? Esta acción no se puede deshacer.");
    if (!confirmado) {
        mostrarMensaje("aviso", "Eliminación cancelada. El registro se conservó.");
        return;
    }
    try {
        await peticion(API_URL + "/" + id, { method: "DELETE" });
        if (campoId.value === String(id)) {
            limpiarFormulario();
        }
        await cargarListado(false);
        mostrarMensaje("exito", "Funcionario eliminado correctamente.");
    } catch (e) {
        mostrarMensaje("error", e.message);
        cargarListado(false);
    }
}

// ---------- Eventos ----------
formFuncionario.addEventListener("submit", guardarFuncionario);

// Restringir el campo de teléfono para que solo acepte números (impide letras y símbolos)
campoTelefono.addEventListener("input", function () {
    this.value = this.value.replace(/\D/g, "");
});

btnLimpiar.addEventListener("click", function () {
    limpiarFormulario();
    ocultarMensaje();
});

btnListado.addEventListener("click", function () {
    campoBuscarId.value = "";
    campoBuscarNombre.value = "";
    campoBuscarDepartamento.value = "";
    cargarListado(true);
});

formBuscarId.addEventListener("submit", function (e) {
    e.preventDefault();
    consultarPorId(campoBuscarId.value);
});

formBuscarNombre.addEventListener("submit", function (e) {
    e.preventDefault();
    buscarPor("nombre", campoBuscarNombre.value, "con el nombre");
});

formBuscarDepartamento.addEventListener("submit", function (e) {
    e.preventDefault();
    buscarPor("departamento", campoBuscarDepartamento.value, "en el departamento");
});

// Al abrir la página se muestra el listado completo
cargarListado(false);
