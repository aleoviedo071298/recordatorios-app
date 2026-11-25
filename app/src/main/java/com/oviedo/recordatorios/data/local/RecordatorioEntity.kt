package com.oviedo.recordatorios.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Esquema de la tabla de Room. Además de los datos del recordatorio,
 * guarda la información necesaria para la sincronización:
 *
 * - idRemoto: UUID estable que identifica el registro en el servicio.
 *   Es único, así que el mismo registro nunca se guarda dos veces.
 * - pendienteSincronizacion: hay cambios locales sin confirmar.
 * - eliminado: borrado lógico, pendiente de informar al servicio.
 * - versionLocal: aumenta con cada cambio local; evita marcar como
 *   sincronizada una versión que cambió durante el envío.
 * - errorSincronizacion: motivo de un rechazo permanente del servicio.
 */
@Entity(
    tableName = "recordatorios",
    indices = [Index(value = ["idRemoto"], unique = true)]
)
data class RecordatorioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String?,
    val categoria: String,
    val prioridad: String,
    val estado: String,
    val fecha: Long,
    val pendienteSincronizacion: Boolean = true,
    val idRemoto: String? = null,
    @ColumnInfo(defaultValue = "0")
    val eliminado: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val versionLocal: Long = 0L,
    val errorSincronizacion: String? = null
)
