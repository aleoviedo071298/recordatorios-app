package com.oviedo.recordatorios.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oviedo.recordatorios.Recordatorio
import com.oviedo.recordatorios.nombreCategoria
import com.oviedo.recordatorios.nombreEstado
import com.oviedo.recordatorios.nombrePrioridad

@Composable
fun TarjetaRecordatorio(
    recordatorio: Recordatorio,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = recordatorio.titulo,
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "Categoría: ${nombreCategoria(recordatorio.categoria)}")
            Text(text = "Prioridad: ${nombrePrioridad(recordatorio.prioridad)}")
            Text(text = "Estado: ${nombreEstado(recordatorio.estado)}")
        }
    }
}
