package com.oviedo.recordatorios

import com.oviedo.recordatorios.data.local.RecordatorioEntity
import com.oviedo.recordatorios.data.mapper.toDomain
import com.oviedo.recordatorios.data.mapper.toDto
import com.oviedo.recordatorios.data.mapper.toEntityOrNull
import com.oviedo.recordatorios.data.preferences.OrdenListado
import com.oviedo.recordatorios.data.remote.RecordatorioDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class MapeosTest {

    private val dtoValido = RecordatorioDto(
        id = "uuid-1",
        titulo = "  Estudiar Room  ",
        descripcion = "   ",
        categoria = "Estudio",
        prioridad = "Alta",
        estado = "Pendiente",
        fecha = 1_700_000_000_000L
    )

    @Test
    fun dtoValido_seConvierteEnEntidadSincronizada() {
        val entidad = dtoValido.toEntityOrNull()!!

        assertEquals("uuid-1", entidad.idRemoto)
        assertEquals("Estudiar Room", entidad.titulo)
        assertNull(entidad.descripcion)
        assertFalse(entidad.pendienteSincronizacion)
    }

    @Test
    fun dtoSinIdOSinTitulo_seDescarta() {
        assertNull(dtoValido.copy(id = null).toEntityOrNull())
        assertNull(dtoValido.copy(id = " ").toEntityOrNull())
        assertNull(dtoValido.copy(titulo = "").toEntityOrNull())
    }

    @Test
    fun valoresDesconocidosDelServidor_usanLosPredeterminados() {
        val entidad = dtoValido.copy(
            categoria = "Otra",
            prioridad = "Urgentisima",
            estado = null
        ).toEntityOrNull()!!

        assertEquals("Personal", entidad.categoria)
        assertEquals("Media", entidad.prioridad)
        assertEquals("Pendiente", entidad.estado)
    }

    @Test
    fun estadoDeSincronizacion_seDerivaDeLaEntidad() {
        val base = RecordatorioEntity(
            id = 1, titulo = "Prueba", descripcion = null, categoria = "Trabajo",
            prioridad = "Baja", estado = "Pendiente", fecha = 1L
        )

        assertEquals(
            EstadoSincronizacion.Pendiente,
            base.copy(pendienteSincronizacion = true).toDomain().estadoSincronizacion
        )
        assertEquals(
            EstadoSincronizacion.Sincronizado,
            base.copy(pendienteSincronizacion = false).toDomain().estadoSincronizacion
        )
        assertEquals(
            EstadoSincronizacion.Error("HTTP 400"),
            base.copy(errorSincronizacion = "HTTP 400").toDomain().estadoSincronizacion
        )
    }

    @Test
    fun entidadADto_usaElUuidComoIdentificador() {
        val entidad = dtoValido.toEntityOrNull()!!
        assertEquals("uuid-1", entidad.toDto("uuid-1").id)
    }

    @Test
    fun ordenDesconocido_vuelveAFecha() {
        assertEquals(OrdenListado.FECHA, OrdenListado.desdeTexto("ALFABETICO_INVERSO"))
        assertEquals(OrdenListado.FECHA, OrdenListado.desdeTexto(null))
        assertEquals(OrdenListado.TITULO, OrdenListado.desdeTexto("TITULO"))
    }
}
