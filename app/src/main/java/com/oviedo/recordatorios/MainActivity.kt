
package com.oviedo.recordatorios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.oviedo.recordatorios.ui.theme.RecordatoriosAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RecordatoriosAppTheme {
                PantallaRecordatorios()
            }
        }
    }
}

@Composable
fun PantallaRecordatorios() {

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingInterno ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .padding(16.dp)
        ) {

            Text(
                text = "Mis Recordatorios",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "${recordatorios.size} recordatorios registrados",
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(recordatorios) { recordatorio ->
                    TarjetaRecordatorio(recordatorio)
                }
            }
        }
    }
}

@Composable
fun TarjetaRecordatorio(recordatorio: Recordatorio) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = recordatorio.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Categoría: ${nombreCategoria(recordatorio.categoria)}"
            )

            Text(
                text = "Prioridad: ${nombrePrioridad(recordatorio.prioridad)}"
            )

            Text(
                text = "Estado: ${nombreEstado(recordatorio.estado)}"
            )
        }
    }
}

fun nombreCategoria(categoria: Categoria): String =
    when (categoria) {
        Categoria.Estudio -> "Estudio"
        Categoria.Trabajo -> "Trabajo"
        Categoria.Personal -> "Personal"
    }

fun nombrePrioridad(prioridad: Prioridad): String =
    when (prioridad) {
        Prioridad.Alta -> "Alta"
        Prioridad.Media -> "Media"
        Prioridad.Baja -> "Baja"
    }

fun nombreEstado(estado: EstadoRecordatorio): String =
    when (estado) {
        EstadoRecordatorio.Pendiente -> "Pendiente"
        EstadoRecordatorio.Completado -> "Completado"
        EstadoRecordatorio.Cancelado -> "Cancelado"
    }