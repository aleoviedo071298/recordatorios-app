package com.oviedo.recordatorios.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oviedo.recordatorios.Categoria
import com.oviedo.recordatorios.EstadoRecordatorio
import com.oviedo.recordatorios.Prioridad
import com.oviedo.recordatorios.Recordatorio
import com.oviedo.recordatorios.data.EstadoSincronizacionGeneral
import com.oviedo.recordatorios.data.RecordatorioRepository
import com.oviedo.recordatorios.ui.state.DetalleUiState
import com.oviedo.recordatorios.ui.state.EstadoEnvio
import com.oviedo.recordatorios.ui.state.FormularioUiState
import com.oviedo.recordatorios.ui.state.ListadoUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de las pantallas de listado, detalle y formulario.
 * Depende solo de la interfaz RecordatorioRepository: no conoce Room,
 * DataStore ni Retrofit.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RecordatoriosViewModel(
    private val repositorio: RecordatorioRepository
) : ViewModel() {

    // ---------- Listado ----------

    /** Cada incremento vuelve a suscribirse al Flow (botón Reintentar). */
    private val recargaListado = MutableStateFlow(0)

    val listadoState: StateFlow<ListadoUiState> = recargaListado
        .flatMapLatest {
            repositorio.observarListado()
                .map<_, ListadoUiState> { resultado ->
                    if (resultado.recordatorios.isNotEmpty()) {
                        ListadoUiState.Contenido(resultado.recordatorios)
                    } else {
                        ListadoUiState.Vacio(hayOcultos = resultado.totalGuardados > 0)
                    }
                }
                .onStart { emit(ListadoUiState.Cargando) }
                .catch { e ->
                    emit(ListadoUiState.Error(e.message ?: "No se pudieron cargar los recordatorios."))
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListadoUiState.Cargando)

    val estadoSincronizacion: StateFlow<EstadoSincronizacionGeneral> = repositorio
        .observarEstadoSincronizacion()
        .catch { emit(EstadoSincronizacionGeneral()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoSincronizacionGeneral())

    fun cargarRecordatorios() {
        recargaListado.update { it + 1 }
    }

    fun reintentarSincronizacion() {
        viewModelScope.launch {
            try {
                repositorio.reintentarSincronizacion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeOperacion.value = e.message ?: "No se pudo programar la sincronización."
            }
        }
    }

    // ---------- Detalle ----------

    private data class SolicitudDetalle(val id: Int, val intento: Int)

    private val solicitudDetalle = MutableStateFlow<SolicitudDetalle?>(null)

    /** Evita mostrar "no encontrado" en el instante en que se elimina el registro. */
    @Volatile
    private var eliminacionEnCurso = false

    val detalleState: StateFlow<DetalleUiState> = solicitudDetalle
        .filterNotNull()
        .flatMapLatest { solicitud ->
            repositorio.observarPorId(solicitud.id)
                .filter { it != null || !eliminacionEnCurso }
                .map<Recordatorio?, DetalleUiState> { recordatorio ->
                    if (recordatorio != null) {
                        DetalleUiState.Contenido(recordatorio)
                    } else {
                        DetalleUiState.Error("No se encontró el recordatorio solicitado.")
                    }
                }
                .onStart { emit(DetalleUiState.Cargando) }
                .catch { e ->
                    emit(DetalleUiState.Error(e.message ?: "No se pudo cargar el recordatorio."))
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState.Cargando)

    private val _operacionEnCurso = MutableStateFlow(false)
    val operacionEnCurso: StateFlow<Boolean> = _operacionEnCurso.asStateFlow()

    /** Error de una acción (completar, eliminar) sin perder el contenido en pantalla. */
    private val _mensajeOperacion = MutableStateFlow<String?>(null)
    val mensajeOperacion: StateFlow<String?> = _mensajeOperacion.asStateFlow()

    fun cargarDetalle(id: Int) {
        solicitudDetalle.update { actual ->
            SolicitudDetalle(id = id, intento = (actual?.intento ?: 0) + 1)
        }
    }

    /** Alterna entre Pendiente y Completado. */
    fun alternarCompletado(id: Int) {
        ejecutarOperacion {
            val recordatorio = repositorio.obtenerPorId(id)
                ?: throw IllegalArgumentException("Recordatorio no encontrado")

            val nuevoEstado = if (recordatorio.estado == EstadoRecordatorio.Completado) {
                EstadoRecordatorio.Pendiente
            } else {
                EstadoRecordatorio.Completado
            }
            repositorio.actualizarRecordatorio(recordatorio.copy(estado = nuevoEstado))
        }
    }

    fun eliminarRecordatorio(id: Int, onEliminado: () -> Unit) {
        ejecutarOperacion {
            eliminacionEnCurso = true
            try {
                repositorio.eliminarRecordatorio(id)
            } catch (e: Exception) {
                eliminacionEnCurso = false
                throw e
            }
            onEliminado()
        }
    }

    fun descartarMensaje() {
        _mensajeOperacion.value = null
    }

    private fun ejecutarOperacion(accion: suspend () -> Unit) {
        if (_operacionEnCurso.value) return
        _operacionEnCurso.value = true
        _mensajeOperacion.value = null

        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mensajeOperacion.value = e.message ?: "No se pudo completar la operación."
            } finally {
                _operacionEnCurso.value = false
            }
        }
    }

    // ---------- Formulario (crear y editar) ----------

    private val _formularioState = MutableStateFlow(FormularioUiState())
    val formularioState: StateFlow<FormularioUiState> = _formularioState.asStateFlow()

    /** Carga los datos actuales para editar. Se ignora si ya están cargados. */
    fun cargarParaEditar(id: Int) {
        if (_formularioState.value.idEdicion == id) return

        viewModelScope.launch {
            try {
                val recordatorio = repositorio.obtenerPorId(id)
                    ?: throw IllegalArgumentException("No se encontró el recordatorio.")

                _formularioState.value = FormularioUiState(
                    titulo = recordatorio.titulo,
                    descripcion = recordatorio.descripcion.orEmpty(),
                    categoria = recordatorio.categoria,
                    prioridad = recordatorio.prioridad,
                    idEdicion = id
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _formularioState.update {
                    it.copy(
                        idEdicion = id,
                        estadoEnvio = EstadoEnvio.Error(e.message ?: "No se pudo cargar el recordatorio.")
                    )
                }
            }
        }
    }

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
                val titulo = datos.titulo.trim()
                val descripcion = datos.descripcion.trim().ifBlank { null }
                val categoria = requireNotNull(datos.categoria)
                val prioridad = requireNotNull(datos.prioridad)

                val idEdicion = datos.idEdicion
                if (idEdicion != null) {
                    val actual = repositorio.obtenerPorId(idEdicion)
                        ?: throw IllegalArgumentException("El recordatorio ya no existe.")
                    repositorio.actualizarRecordatorio(
                        actual.copy(
                            titulo = titulo,
                            descripcion = descripcion,
                            categoria = categoria,
                            prioridad = prioridad
                        )
                    )
                } else {
                    // id = 0: Room asigna el identificador definitivo.
                    repositorio.crearRecordatorio(
                        Recordatorio(
                            id = 0,
                            titulo = titulo,
                            descripcion = descripcion,
                            categoria = categoria,
                            prioridad = prioridad,
                            estado = EstadoRecordatorio.Pendiente
                        )
                    )
                }

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
}
