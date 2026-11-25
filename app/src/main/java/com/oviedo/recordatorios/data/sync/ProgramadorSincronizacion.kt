package com.oviedo.recordatorios.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

/**
 * Programa la sincronización con WorkManager.
 *
 * - Requiere red: sin conexión, el trabajo espera hasta que vuelva.
 * - Es un trabajo único: aunque varios eventos pidan sincronizar a la vez
 *   (crear, editar, recuperar la red), nunca corren dos en paralelo.
 * - Ante un fallo transitorio reintenta con backoff exponencial.
 */
object ProgramadorSincronizacion {

    private const val NOMBRE_TRABAJO = "sincronizacion_recordatorios"

    fun programar(context: Context) {
        val restricciones = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val solicitud = OneTimeWorkRequestBuilder<SincronizacionWorker>()
            .setConstraints(restricciones)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            NOMBRE_TRABAJO,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            solicitud
        )
    }

    /** true mientras la sincronización se está ejecutando. */
    fun observarEnCurso(context: Context): Flow<Boolean> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(NOMBRE_TRABAJO)
            .map { trabajos -> trabajos.any { it.state == WorkInfo.State.RUNNING } }
}
