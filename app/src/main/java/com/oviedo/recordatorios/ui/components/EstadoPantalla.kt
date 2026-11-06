package com.oviedo.recordatorios.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Indicador reutilizable para el estado de carga.
 */
@Composable
fun IndicadorCarga(
    modifier: Modifier = Modifier,
    mensaje: String = "Cargando..."
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.semantics { contentDescription = mensaje }
            )
            Text(text = mensaje)
        }
    }
}

/**
 * Mensaje reutilizable para comunicar estados vacíos o de error,
 * con una acción opcional (crear, reintentar, volver).
 */
@Composable
fun MensajeEstado(
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    esError: Boolean = false,
    textoAccion: String? = null,
    onAccion: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.semantics {
                if (esError) liveRegion = LiveRegionMode.Assertive
            }
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = if (esError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center
            )
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            if (textoAccion != null) {
                Button(onClick = onAccion) {
                    Text(textoAccion)
                }
            }
        }
    }
}
