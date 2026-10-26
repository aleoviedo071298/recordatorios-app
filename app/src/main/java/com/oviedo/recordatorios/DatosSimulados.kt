package com.oviedo.recordatorios

val recordatorios = listOf(
    Recordatorio(
        id = 1,
        titulo = "Estudiar Kotlin",
        descripcion = "Repasar sealed classes",
        categoria = Categoria.Estudio,
        prioridad = Prioridad.Alta,
        estado = EstadoRecordatorio.Pendiente
    ),
    Recordatorio(
        id = 2,
        titulo = "Comprar alimentos",
        descripcion = "Ir al supermercado",
        categoria = Categoria.Personal,
        prioridad = Prioridad.Media,
        estado = EstadoRecordatorio.Completado
    ),
    Recordatorio(
        id = 3,
        titulo = "Entregar informe",
        descripcion = "Finalizar el trabajo",
        categoria = Categoria.Trabajo,
        prioridad = Prioridad.Alta,
        estado = EstadoRecordatorio.Pendiente
    ),

    Recordatorio(
        id = 4,
        titulo = "Organizar escritorio",
        descripcion = null,
        categoria = Categoria.Personal,
        prioridad = Prioridad.Baja,
        estado = EstadoRecordatorio.Cancelado
    )
)

val categoriasUnicas = recordatorios
    .map { it.categoria }
    .toSet()

val coloresCategorias = mapOf(
    Categoria.Estudio to "Azul",
    Categoria.Trabajo to "Rojo",
    Categoria.Personal to "Verde"
)

val recordatoriosPendientes = recordatorios.filter {
    it.estado == EstadoRecordatorio.Pendiente
}

val recordatoriosAltaPrioridad = recordatorios.filter {
    it.prioridad == Prioridad.Alta
}