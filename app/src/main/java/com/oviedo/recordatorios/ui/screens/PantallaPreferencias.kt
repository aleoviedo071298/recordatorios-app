package com.oviedo.recordatorios.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oviedo.recordatorios.BuildConfig
import com.oviedo.recordatorios.data.preferences.OrdenListado
import com.oviedo.recordatorios.data.preferences.PreferenciasApp
import com.oviedo.recordatorios.ui.viewmodel.PreferenciasViewModel
import com.oviedo.recordatorios.ui.viewmodel.PreferenciasViewModelFactory

@Composable
fun PantallaPreferencias(
    viewModelFactory: PreferenciasViewModelFactory,
    onVolver: () -> Unit
) {
    val preferenciasViewModel: PreferenciasViewModel = viewModel(factory = viewModelFactory)
    val preferencias by preferenciasViewModel.preferencias.collectAsStateWithLifecycle()

    ContenidoPreferencias(
        preferencias = preferencias,
        onTemaOscuroChange = preferenciasViewModel::cambiarTemaOscuro,
        onMostrarCompletadosChange = preferenciasViewModel::cambiarMostrarCompletados,
        onOrdenChange = preferenciasViewModel::cambiarOrdenListado,
        onProbarValorInvalido = preferenciasViewModel::guardarOrdenInvalidoParaPrueba,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContenidoPreferencias(
    preferencias: PreferenciasApp,
    onTemaOscuroChange: (Boolean) -> Unit,
    onMostrarCompletadosChange: (Boolean) -> Unit,
    onOrdenChange: (OrdenListado) -> Unit,
    onProbarValorInvalido: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Preferencias") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al listado"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilaInterruptor(
                texto = "Tema oscuro",
                activado = preferencias.temaOscuro,
                onCambio = onTemaOscuroChange
            )

            HorizontalDivider()

            FilaInterruptor(
                texto = "Mostrar completados",
                activado = preferencias.mostrarCompletados,
                onCambio = onMostrarCompletadosChange
            )

            HorizontalDivider()

            Text(
                text = "Ordenar recordatorios por",
                style = MaterialTheme.typography.titleMedium
            )

            Column(modifier = Modifier.selectableGroup()) {
                OrdenListado.entries.forEach { opcion ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = preferencias.ordenListado == opcion,
                                onClick = { onOrdenChange(opcion) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = preferencias.ordenListado == opcion,
                            onClick = null
                        )
                        Text(
                            text = opcion.etiqueta,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Text(
                text = "Las preferencias se guardan en DataStore y se conservan al cerrar la app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Herramienta de prueba (solo debug): guarda un orden inexistente
            // para comprobar que la app vuelve al valor predeterminado.
            if (BuildConfig.DEBUG) {
                HorizontalDivider()
                OutlinedButton(
                    onClick = onProbarValorInvalido,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Prueba: guardar un orden inválido")
                }
            }
        }
    }
}

@Composable
private fun FilaInterruptor(
    texto: String,
    activado: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(texto)
        Switch(checked = activado, onCheckedChange = onCambio)
    }
}
