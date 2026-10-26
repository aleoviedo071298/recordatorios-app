package com.oviedo.recordatorios

import android.R.id


data class Recordatorio (
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val categoria: Categoria,
    val prioridad: Prioridad,
    val estado: EstadoRecordatorio,
    val ubicacion: String? = null,
    val fechaAviso: String? = null
) {
    init {
        require(id > 0){
            "El ID debe ser mayor a 0"
        }
        require(titulo.isNotBlank()){
            "El titulo no puede estar en blanco"
        }
    }
}

sealed class Prioridad {
    data object Alta : Prioridad()
    data object Media : Prioridad()
    data object Baja : Prioridad()
}

sealed class Categoria {
    data object Estudio : Categoria()
    data object Trabajo : Categoria()
    data object Personal : Categoria()
}

sealed class EstadoRecordatorio {
    data object Pendiente : EstadoRecordatorio()
    data object Completado : EstadoRecordatorio()
    data object Cancelado : EstadoRecordatorio()
}