package com.oviedo.recordatorios.data.remote

sealed interface ResultadoApi<out T> {

    data class Exito<T>(
        val datos: T
    ) : ResultadoApi<T>

    data class ErrorHttp(
        val codigo: Int,
        val mensaje: String
    ) : ResultadoApi<Nothing>

    data class SinConexion(
        val mensaje: String = "Sin conexión al servidor"
    ) : ResultadoApi<Nothing>

    data class TiempoAgotado(
        val mensaje: String = "Se agotó el tiempo de espera"
    ) : ResultadoApi<Nothing>

    data class ErrorDesconocido(
        val mensaje: String
    ) : ResultadoApi<Nothing>
}
