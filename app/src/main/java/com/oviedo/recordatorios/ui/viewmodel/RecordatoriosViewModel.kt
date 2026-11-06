package com.oviedo.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oviedo.recordatorios.Categoria
import com.oviedo.recordatorios.EstadoRecordatorio
import com.oviedo.recordatorios.Prioridad
import com.oviedo.recordatorios.Recordatorio
import com.oviedo.recordatorios.data.RecordatorioRepository
import com.oviedo.recordatorios.ui.state.DetalleUiState
import com.oviedo.recordatorios.ui.state.EstadoEnvio
import com.oviedo.recordatorios.ui.state.FormularioUiState
import com.oviedo.recordatorios.ui.state.ListadoUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecordatoriosViewModel(
    private val repositorio: RecordatorioRepository
) : ViewModel() {

    private val _listadoState =
        MutableStateFlow<ListadoUiState>(ListadoUiState.Cargando)
    val listadoState: StateFlow<ListadoUiState> = _listadoState.asStateFlow()

    private val _detalleState =
        MutableStateFlow<DetalleUiState>(DetalleUiState.Cargando)
    val detalleState: StateFlow<DetalleUiState> = _detalleState.asStateFlow()

    private val _formularioState = MutableStateFlow(FormularioUiState())
    val formularioState: StateFlow<FormularioUiState> =
        _formularioState.asStateFlow()

    // ---------- Listado ----------

    fun cargarRecordatorios() {
        viewModelScope.launch {
            _listadoState.value = ListadoUiState.Cargando
            try {
                delay(RETARDO_CARGA_MS) // simula una consulta asincrónica
                val datos = repositorio.obtenerRecordatorios()
                _listadoState.value = if (datos.isEmpty()) {
                    ListadoUiState.Vacio
                } else {
                    ListadoUiState.Contenido(datos)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _listadoState.value = ListadoUiState.Error(
                    e.message ?: "No se pudieron cargar los recordatorios."
                )
            }
        }
    }

    // ---------- Detalle ----------

    fun cargarDetalle(id: Int) {
        viewModelScope.launch {
            _detalleState.value = DetalleUiState.Cargando
            try {
                delay(RETARDO_CARGA_MS)
                val recordatorio = repositorio.obtenerPorId(id)
                _detalleState.value = if (recordatorio != null) {
                    DetalleUiState.Contenido(recordatorio)
                } else {
                    DetalleUiState.Error("No se encontró el recordatorio solicitado.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detalleState.value = DetalleUiState.Error(
                    e.message ?: "No se pudo cargar el recordatorio."
                )
            }
        }
    }

    // ---------- Formulario: eventos de la interfaz ----------

    fun actualizarTitulo(nuevoTitulo: String) {
        _formularioState.update { it.copy(titulo = nuevoTitulo, interactuado = true) }
    }

    fun actualizarDescripcion(nuevaDescripcion: String) {
        _formularioState.update { it.copy(descripcion = nuevaDescripcion, interactuado = true) }
    }

    fun actualizarCategoria(nuevaCategoria: Categoria) {
        _formularioState.update { it.copy(categoria = nuevaCategoria, interactuado = true) }
    }

    fun actualizarPrioridad(nuevaPrioridad: Prioridad) {
        _formularioState.update { it.copy(prioridad = nuevaPrioridad, interactuado = true) }
    }

    fun guardar() {
        val datos = _formularioState.value
        if (!datos.puedeGuardar) return

        _formularioState.update { it.copy(estadoEnvio = EstadoEnvio.Guardando) }

        viewModelScope.launch {
            try {
                delay(RETARDO_GUARDADO_MS) // simula una operación remota

                val existentes = repositorio.obtenerRecordatorios()
                val siguienteId = (existentes.maxOfOrNull { it.id } ?: 0) + 1

                val nuevoRecordatorio = Recordatorio(
                    id = siguienteId,
                    titulo = datos.titulo.trim(),
                    descripcion = datos.descripcion.trim().ifBlank { null },
                    categoria = requireNotNull(datos.categoria),
                    prioridad = requireNotNull(datos.prioridad),
                    estado = EstadoRecordatorio.Pendiente
                )
                repositorio.guardarRecordatorio(nuevoRecordatorio)

                _formularioState.update { it.copy(estadoEnvio = EstadoEnvio.Exito) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formularioState.update {
                    it.copy(
                        estadoEnvio = EstadoEnvio.Error(
                            e.message ?: "No se pudo guardar el recordatorio."
                        )
                    )
                }
            }
        }
    }

    private companion object {
        const val RETARDO_CARGA_MS = 800L
        const val RETARDO_GUARDADO_MS = 1000L
    }
}
