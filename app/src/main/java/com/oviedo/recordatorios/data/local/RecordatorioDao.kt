package com.oviedo.recordatorios.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {

    // ---------- Escritura ----------

    @Insert
    suspend fun insertar(recordatorio: RecordatorioEntity): Long

    /** Inserta un registro descargado; si su idRemoto ya existe, no hace nada. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExiste(recordatorio: RecordatorioEntity): Long

    @Update
    suspend fun actualizar(recordatorio: RecordatorioEntity)

    @Delete
    suspend fun eliminar(recordatorio: RecordatorioEntity)

    // ---------- Consultas observadas por la interfaz ----------

    @Query("""
        SELECT * FROM recordatorios
        WHERE eliminado = 0
        ORDER BY fecha DESC
    """)
    fun obtenerTodos(): Flow<List<RecordatorioEntity>>

    @Query("""
        SELECT * FROM recordatorios
        WHERE id = :id AND eliminado = 0
    """)
    fun obtenerPorId(id: Int): Flow<RecordatorioEntity?>

    /** Filtro por estado (por ejemplo, solo los completados). */
    @Query("""
        SELECT * FROM recordatorios
        WHERE estado = :estado AND eliminado = 0
        ORDER BY fecha DESC
    """)
    fun filtrarPorEstado(estado: String): Flow<List<RecordatorioEntity>>

    /** Filtro que excluye un estado (se usa para ocultar los completados). */
    @Query("""
        SELECT * FROM recordatorios
        WHERE estado != :estado AND eliminado = 0
        ORDER BY fecha DESC
    """)
    fun obtenerExcluyendoEstado(estado: String): Flow<List<RecordatorioEntity>>

    @Query("SELECT COUNT(*) FROM recordatorios WHERE eliminado = 0")
    fun contarVisibles(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM recordatorios
        WHERE pendienteSincronizacion = 1 AND errorSincronizacion IS NULL
    """)
    fun observarCantidadPendientes(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM recordatorios
        WHERE errorSincronizacion IS NOT NULL
    """)
    fun observarCantidadConError(): Flow<Int>

    // ---------- Sincronización ----------

    /** Cambios para enviar. Los rechazados quedan fuera hasta un reintento manual. */
    @Query("""
        SELECT * FROM recordatorios
        WHERE pendienteSincronizacion = 1 AND errorSincronizacion IS NULL
        ORDER BY id
    """)
    suspend fun obtenerPendientes(): List<RecordatorioEntity>

    @Query("""
        SELECT COUNT(*) FROM recordatorios
        WHERE pendienteSincronizacion = 1 AND errorSincronizacion IS NULL
    """)
    suspend fun contarPendientes(): Int

    @Query("SELECT * FROM recordatorios WHERE idRemoto = :idRemoto")
    suspend fun obtenerPorIdRemoto(idRemoto: String): RecordatorioEntity?

    @Query("""
        UPDATE recordatorios
        SET pendienteSincronizacion = 0,
            errorSincronizacion = NULL
        WHERE id = :id
          AND versionLocal = :versionEnviada
          AND pendienteSincronizacion = 1
    """)
    suspend fun marcarSincronizado(id: Int, versionEnviada: Long): Int

    @Query("""
        UPDATE recordatorios
        SET errorSincronizacion = :mensaje
        WHERE id = :id AND versionLocal = :versionEnviada
    """)
    suspend fun marcarError(id: Int, versionEnviada: Long, mensaje: String): Int

    @Query("""
        UPDATE recordatorios
        SET errorSincronizacion = NULL
        WHERE errorSincronizacion IS NOT NULL
    """)
    suspend fun limpiarErrores()

    /** Borra físicamente un registro cuya eliminación ya confirmó el servicio. */
    @Query("""
        DELETE FROM recordatorios
        WHERE id = :id AND versionLocal = :versionEnviada AND eliminado = 1
    """)
    suspend fun eliminarConfirmado(id: Int, versionEnviada: Long): Int

    /** Aplica datos del servicio solo si no hay cambios locales sin enviar. */
    @Query("""
        UPDATE recordatorios
        SET titulo = :titulo,
            descripcion = :descripcion,
            categoria = :categoria,
            prioridad = :prioridad,
            estado = :estado,
            fecha = :fecha
        WHERE id = :id AND pendienteSincronizacion = 0
    """)
    suspend fun actualizarDesdeServidor(
        id: Int,
        titulo: String,
        descripcion: String?,
        categoria: String,
        prioridad: String,
        estado: String,
        fecha: Long
    ): Int

    @Query("UPDATE recordatorios SET idRemoto = :uuid WHERE id = :id")
    suspend fun asignarIdRemoto(id: Int, uuid: String)
}
