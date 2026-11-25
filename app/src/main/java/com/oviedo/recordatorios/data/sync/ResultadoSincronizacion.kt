package com.oviedo.recordatorios.data.sync

enum class ResultadoSincronizacion {
    /** Se procesaron todos los pendientes y se descargaron los datos remotos. */
    COMPLETADA,

    /** Falló la red, hubo timeout o error del servidor: se vuelve a intentar más tarde. */
    REINTENTAR
}
