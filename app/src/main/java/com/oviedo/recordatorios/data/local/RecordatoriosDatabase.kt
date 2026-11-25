package com.oviedo.recordatorios.data.local

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oviedo.recordatorios.data.mapper.toEntity
import com.oviedo.recordatorios.recordatorios
import java.util.UUID

@Database(
    entities = [RecordatorioEntity::class],
    version = 4,
    exportSchema = false
)
abstract class RecordatoriosDatabase : RoomDatabase() {
    abstract fun recordatorioDao(): RecordatorioDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE recordatorios ADD COLUMN idRemoto TEXT")
        db.execSQL(
            "ALTER TABLE recordatorios ADD COLUMN eliminado INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE recordatorios ADD COLUMN versionLocal INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE recordatorios ADD COLUMN errorSincronizacion TEXT")
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_recordatorios_idRemoto " +
                "ON recordatorios(idRemoto)"
        )
    }
}

/**
 * Carga los recordatorios de ejemplo una sola vez, cuando se crea la base.
 * Si la persona después los elimina todos, no vuelven a aparecer.
 */
object DatosInicialesCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val ahora = System.currentTimeMillis()

        recordatorios.forEachIndexed { indice, recordatorio ->
            val entidad = recordatorio.toEntity()
            val valores = ContentValues().apply {
                put("titulo", entidad.titulo)
                put("descripcion", entidad.descripcion)
                put("categoria", entidad.categoria)
                put("prioridad", entidad.prioridad)
                put("estado", entidad.estado)
                put("fecha", ahora - indice * 60_000L)
                put("pendienteSincronizacion", 1)
                put("idRemoto", UUID.randomUUID().toString())
                put("eliminado", 0)
                put("versionLocal", 1L)
            }
            db.insert("recordatorios", SQLiteDatabase.CONFLICT_ABORT, valores)
        }
    }
}
