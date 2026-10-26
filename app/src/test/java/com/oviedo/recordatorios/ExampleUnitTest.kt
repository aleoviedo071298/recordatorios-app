package com.oviedo.recordatorios

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun tituloVacioNoPermitido() {
        val resultado = runCatching {
            Recordatorio(
                id = 1,
                titulo = "",
                descripcion = null,
                categoria = Categoria.Estudio,
                prioridad = Prioridad.Alta,
                estado = EstadoRecordatorio.Pendiente
            )
        }

        assertEquals(
            IllegalArgumentException::class.java,
            resultado.exceptionOrNull()?.javaClass
        )
    }
}