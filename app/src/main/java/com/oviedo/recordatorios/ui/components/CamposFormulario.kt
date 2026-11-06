package com.oviedo.recordatorios.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Campo de texto controlado: el valor y el error provienen del estado,
 * y cada cambio se informa mediante [onValorChange].
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    mensajeError: String? = null,
    ayuda: String? = null,
    minLineas: Int = 1
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        isError = mensajeError != null,
        minLines = minLineas,
        supportingText = {
            when {
                mensajeError != null -> Text("Error: $mensajeError")
                ayuda != null -> Text(ayuda)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                if (mensajeError != null) error(mensajeError)
            }
    )
}

/**
 * Grupo de opciones controlado (RadioButton). La opción elegida
 * proviene del estado y la selección se informa mediante [onSeleccion].
 */
@Composable
fun <T> SelectorOpciones(
    titulo: String,
    opciones: List<T>,
    seleccionada: T?,
    nombreOpcion: (T) -> String,
    onSeleccion: (T) -> Unit,
    modifier: Modifier = Modifier,
    mensajeError: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                if (mensajeError != null) error(mensajeError)
            },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium
        )
        Column(modifier = Modifier.selectableGroup()) {
            opciones.forEach { opcion ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = opcion == seleccionada,
                            onClick = { onSeleccion(opcion) },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = opcion == seleccionada,
                        onClick = null
                    )
                    Text(
                        text = nombreOpcion(opcion),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        if (mensajeError != null) {
            Text(
                text = "Error: $mensajeError",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
