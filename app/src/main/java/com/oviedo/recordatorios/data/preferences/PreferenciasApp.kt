package com.oviedo.recordatorios.data.preferences

/**
 * Configuración simple de la app, guardada en DataStore.
 * Los valores por defecto se usan cuando todavía no hay nada guardado,
 * cuando el valor guardado es desconocido o cuando falla la lectura.
 */
data class PreferenciasApp(
    val temaOscuro: Boolean = false,
    val mostrarCompletados: Boolean = true,
    val ordenListado: OrdenListado = OrdenListado.FECHA
)

enum class OrdenListado(val etiqueta: String) {
    FECHA("Fecha"),
    TITULO("Título"),
    PRIORIDAD("Prioridad");

    companion object {
        /** Un valor desconocido o ausente vuelve al orden predeterminado. */
        fun desdeTexto(texto: String?): OrdenListado =
            entries.firstOrNull { it.name == texto } ?: FECHA
    }
}
