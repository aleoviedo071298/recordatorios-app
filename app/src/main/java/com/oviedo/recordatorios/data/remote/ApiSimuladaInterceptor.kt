package com.oviedo.recordatorios.data.remote

import android.util.Log
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

/**
 * Servicio remoto simulado (solo en debug). Guarda los registros en
 * memoria y responde como lo haría una API REST.
 *
 * [modo] permite forzar fallos para probar el manejo de errores:
 * NORMAL, ERROR_SERVIDOR (500), SOLICITUD_INVALIDA (400 en escrituras),
 * TIMEOUT o SIN_CONEXION. Se configura con la propiedad de Gradle
 * RECORDATORIOS_API_SIMULACION.
 */
class ApiSimuladaInterceptor(
    private val modo: String = "NORMAL"
) : Interceptor {

    private val gson = Gson()

    private val registros =
        ConcurrentHashMap<String, RecordatorioDto>()

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {

        val request = chain.request()
        val segmentos = request.url.pathSegments
        val metodo = request.method

        when (modo) {
            "TIMEOUT" -> {
                Log.d("SYNC_API", "Método=$metodo | Timeout simulado")
                throw SocketTimeoutException("Timeout simulado")
            }
            "SIN_CONEXION" -> {
                Log.d("SYNC_API", "Método=$metodo | Sin conexión simulada")
                throw UnknownHostException("Sin conexión simulada")
            }
            "ERROR_SERVIDOR" -> {
                Log.d("SYNC_API", "Método=$metodo | Código=500 (simulado)")
                return respuesta(request, 500, """{"error":"Error simulado"}""")
            }
            "SOLICITUD_INVALIDA" -> if (metodo != "GET") {
                Log.d("SYNC_API", "Método=$metodo | Código=400 (simulado)")
                return respuesta(request, 400, """{"error":"Solicitud inválida simulada"}""")
            }
        }

        var codigo = 200
        var resultado = "[]"

        when {
            metodo == "GET" &&
                segmentos == listOf("recordatorios") -> {

                resultado = gson.toJson(registros.values.toList())
            }

            metodo == "POST" &&
                segmentos == listOf("recordatorios") -> {

                val dto = leerDto(request.body)
                val id = dto?.id
                if (dto == null || id.isNullOrBlank() || dto.titulo.isNullOrBlank()) {
                    codigo = 400
                    resultado = """{"error":"Datos inválidos"}"""
                } else {
                    // Idempotente: si el UUID ya existe, no crea otro registro.
                    registros.putIfAbsent(id, dto)
                    resultado = gson.toJson(registros[id])
                    codigo = 201
                }
            }

            metodo == "PUT" &&
                segmentos.size == 2 &&
                segmentos[0] == "recordatorios" -> {

                val id = segmentos[1]
                val dto = leerDto(request.body)

                if (dto == null || dto.id != id || dto.titulo.isNullOrBlank()) {
                    codigo = 400
                    resultado = """{"error":"Datos inválidos"}"""
                } else if (!registros.containsKey(id)) {
                    codigo = 404
                    resultado = """{"error":"No encontrado"}"""
                } else {
                    registros[id] = dto
                    resultado = gson.toJson(dto)
                }
            }

            metodo == "DELETE" &&
                segmentos.size == 2 &&
                segmentos[0] == "recordatorios" -> {

                val id = segmentos[1]
                val eliminado = registros.remove(id)

                if (eliminado == null) {
                    codigo = 404
                    resultado = """{"error":"No encontrado"}"""
                } else {
                    resultado = gson.toJson(eliminado)
                }
            }

            else -> {
                codigo = 404
                resultado = """{"error":"Ruta no encontrada"}"""
            }
        }

        Log.d(
            "SYNC_API",
            "Método=$metodo | Código=$codigo | " +
                "Registros remotos=${registros.size}"
        )

        return respuesta(request, codigo, resultado)
    }

    private fun respuesta(
        request: Request,
        codigo: Int,
        cuerpo: String
    ): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(codigo)
        .message("API simulada")
        .body(cuerpo.toResponseBody("application/json".toMediaType()))
        .build()

    private fun leerDto(
        body: okhttp3.RequestBody?
    ): RecordatorioDto? {
        if (body == null) return null

        return try {
            val buffer = Buffer()
            body.writeTo(buffer)
            gson.fromJson(
                buffer.readUtf8(),
                RecordatorioDto::class.java
            )
        } catch (e: Exception) {
            null
        }
    }
}
