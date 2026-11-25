package com.oviedo.recordatorios.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.oviedo.recordatorios.RecordatoriosApplication
import com.oviedo.recordatorios.data.SincronizadorRecordatorios
import com.oviedo.recordatorios.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException

class SincronizacionWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as RecordatoriosApplication
        val dao = app.database.recordatorioDao()
        val sincronizador = SincronizadorRecordatorios(dao = dao, api = RetrofitClient.api)

        return try {
            when (sincronizador.sincronizar()) {
                // Si durante el envío hubo cambios nuevos, se vuelve a intentar.
                ResultadoSincronizacion.COMPLETADA ->
                    if (dao.contarPendientes() == 0) Result.success() else Result.retry()

                ResultadoSincronizacion.REINTENTAR -> Result.retry()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
