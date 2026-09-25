package com.example.bitacoraautomotriz.ui.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaEntregaScreen(
    onRegresar: () -> Unit
) {
    var mostrarCalendario by remember { mutableStateOf(false) }
    var mostrarHorarios by remember { mutableStateOf(false) }
    var fechaSeleccionada by remember { mutableStateOf("No seleccionada") }
    var horarioSeleccionado by remember { mutableStateOf("No seleccionado") }
    var citaConfirmada by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 40.dp,
                bottom = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // TÍTULO
        Text(
            text = "CITA DE ENTREGA",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Programe la entrega de su vehículo",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo
        )

        Spacer(modifier = Modifier.height(30.dp))

        // FECHA
        Text(
            text = "FECHA DE ENTREGA",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TextoTarjeta
        )

        Spacer(modifier = Modifier.height(10.dp))

        BotonModulo3D(
            texto = if (fechaSeleccionada == "No seleccionada") {
                "SELECCIONAR FECHA"
            } else {
                fechaSeleccionada
            },
            icono = "📅",
            onClick = { mostrarCalendario = true },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(25.dp))

        // HORARIO
        Text(
            text = "HORARIO DE ENTREGA",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TextoTarjeta
        )

        Spacer(modifier = Modifier.height(10.dp))

        BotonModulo3D(
            texto = if (horarioSeleccionado == "No seleccionado") {
                "SELECCIONAR HORARIO"
            } else {
                horarioSeleccionado
            },
            icono = "🕐",
            onClick = { mostrarHorarios = true },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(30.dp))

        // RESUMEN
        Text(
            text = "RESUMEN DE LA CITA",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Fecha: $fechaSeleccionada",
            fontSize = 18.sp,
            color = Colores.TextoTarjeta
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Horario: $horarioSeleccionado",
            fontSize = 18.sp,
            color = Colores.TextoTarjeta
        )

        Spacer(modifier = Modifier.height(28.dp))

        // CONFIRMAR CITA
        Button(
            onClick = {
                if (fechaSeleccionada != "No seleccionada" && horarioSeleccionado != "No seleccionado") {
                    citaConfirmada = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Text(
                text = "CONFIRMAR CITA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // REGRESAR
        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // CALENDARIO
    if (mostrarCalendario) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            fechaSeleccionada = formato.format(Date(millis))
                        }
                        mostrarCalendario = false
                    }
                ) {
                    Text(text = "ACEPTAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("CANCELAR")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // SELECCIÓN DE HORARIO
    if (mostrarHorarios) {
        AlertDialog(
            onDismissRequest = { mostrarHorarios = false },
            title = {
                Text(text = "SELECCIONE UN HORARIO", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    val horarios = listOf(
                        "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
                        "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"
                    )
                    horarios.forEach { horario ->
                        Button(
                            onClick = {
                                horarioSeleccionado = horario
                                mostrarHorarios = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Text(text = horario, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // CITA CONFIRMADA
    if (citaConfirmada) {
        AlertDialog(
            onDismissRequest = { citaConfirmada = false },
            title = {
                Text(text = "CITA CONFIRMADA", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Su cita de entrega ha sido programada para:\n\n" +
                            "Fecha: $fechaSeleccionada\n" +
                            "Horario: $horarioSeleccionado",
                    fontSize = 17.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { citaConfirmada = false }) {
                    Text(text = "ACEPTAR", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
