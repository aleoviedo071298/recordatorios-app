package com.oviedo.recordatorios.data.remote

import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T : Any> ejecutarApi(
    solicitud: suspend () -> Response<T>
): ResultadoApi<T> {

    return try {
        val respuesta = solicitud()

        if (respuesta.isSuccessful) {
            val cuerpo = respuesta.body()

            if (cuerpo != null) {
                ResultadoApi.Exito(cuerpo)
            } else {
                ResultadoApi.ErrorDesconocido(
                    "El servidor devolvió una respuesta vacía"
                )
            }

        } else {
            ResultadoApi.ErrorHttp(
                codigo = respuesta.code(),
                mensaje = when (respuesta.code()) {
                    in 400..499 -> "Solicitud rechazada por el servidor"
                    in 500..599 -> "Error interno del servidor"
                    else -> "Error HTTP ${respuesta.code()}"
                }
            )
        }

    } catch (e: CancellationException) {
        throw e

    } catch (e: SocketTimeoutException) {
        ResultadoApi.TiempoAgotado()

    } catch (e: IOException) {
        ResultadoApi.SinConexion()

    } catch (e: Exception) {
        ResultadoApi.ErrorDesconocido(
            e.message ?: "Error desconocido"
        )
    }
}
