package com.oviedo.recordatorios.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.oviedo.recordatorios.EstadoSincronizacion
import com.oviedo.recordatorios.data.EstadoSincronizacionGeneral

/**
 * Indica si un recordatorio está pendiente, sincronizado o con error.
 * Usa ícono y texto, no solo color.
 */
@Composable
fun EtiquetaSincronizacion(
    estado: EstadoSincronizacion,
    modifier: Modifier = Modifier
) {
    val (icono, texto, color) = when (estado) {
        EstadoSincronizacion.Pendiente -> Triple(
            Icons.Filled.CloudUpload,
            "Pendiente de sincronizar",
            MaterialTheme.colorScheme.tertiary
        )
        EstadoSincronizacion.Sincronizado -> Triple(
            Icons.Filled.CloudDone,
            "Sincronizado",
            MaterialTheme.colorScheme.primary
        )
        is EstadoSincronizacion.Error -> Triple(
            Icons.Filled.ErrorOutline,
            "Error de sincronización",
            MaterialTheme.colorScheme.error
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text = texto, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

/**
 * Resumen del estado de sincronización para el listado. No se muestra
 * cuando todo está sincronizado.
 */
@Composable
fun BannerSincronizacion(
    estado: EstadoSincronizacionGeneral,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (estado.pendientes == 0 && estado.conError == 0 && !estado.enCurso) return

    val hayError = estado.conError > 0
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        colors = CardDefaults.cardColors(
            containerColor = if (hayError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (estado.enCurso) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = if (hayError) Icons.Filled.ErrorOutline else Icons.Filled.CloudOff,
                    contentDescription = null
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                when {
                    estado.enCurso -> Text("Sincronizando con el servidor...")
                    estado.pendientes > 0 -> Text(
                        "${estado.pendientes} cambio(s) pendiente(s). Se enviarán cuando haya conexión."
                    )
                }
                if (hayError) {
                    Text(
                        text = "${estado.conError} recordatorio(s) rechazado(s) por el servidor.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!estado.enCurso) {
                TextButton(onClick = onReintentar) {
                    Text(if (hayError) "Reintentar" else "Sincronizar")
                }
            }
        }
    }
}
