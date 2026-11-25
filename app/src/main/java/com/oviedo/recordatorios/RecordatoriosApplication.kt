package com.oviedo.recordatorios

import android.app.Application
import androidx.room.Room
import com.oviedo.recordatorios.data.RecordatorioRepository
import com.oviedo.recordatorios.data.RepositorioRoom
import com.oviedo.recordatorios.data.local.DatosInicialesCallback
import com.oviedo.recordatorios.data.local.MIGRATION_1_2
import com.oviedo.recordatorios.data.local.MIGRATION_2_3
import com.oviedo.recordatorios.data.local.MIGRATION_3_4
import com.oviedo.recordatorios.data.local.RecordatoriosDatabase
import com.oviedo.recordatorios.data.preferences.PreferenciasRepository
import com.oviedo.recordatorios.data.sync.ProgramadorSincronizacion

/**
 * Contenedor de dependencias: crea una única instancia de la base de
 * datos, de DataStore (a través de PreferenciasRepository) y del repositorio.
 */
class RecordatoriosApplication : Application() {

    val database: RecordatoriosDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            RecordatoriosDatabase::class.java,
            "recordatorios_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .addCallback(DatosInicialesCallback)
            .build()
    }

    val preferenciasRepository: PreferenciasRepository by lazy {
        PreferenciasRepository(applicationContext)
    }

    val repositorio: RecordatorioRepository by lazy {
        RepositorioRoom(
            dao = database.recordatorioDao(),
            preferencias = preferenciasRepository,
            context = applicationContext
        )
    }

    override fun onCreate() {
        super.onCreate()
        // Al abrir la app se envían los pendientes y se descargan los datos remotos.
        ProgramadorSincronizacion.programar(this)
    }
}
