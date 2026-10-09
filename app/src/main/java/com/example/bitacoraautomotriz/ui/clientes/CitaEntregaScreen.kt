package com.example.bitacoraautomotriz.ui.clientes

import android.widget.Toast
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitaEntregaScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    var mostrarCalendario by remember { mutableStateOf(value = false) }
    var mostrarHorarios by remember { mutableStateOf(value = false) }
    var fechaSeleccionada by remember { mutableStateOf(value = "No seleccionada") }
    var horarioSeleccionado by remember { mutableStateOf(value = "No seleccionado") }
    var citaConfirmada by remember { mutableStateOf(value = false) }

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
            text = "CITA DE INGRESO AL TALLER",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Programe la recepción de su vehículo",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo
        )

        Spacer(modifier = Modifier.height(30.dp))

        // FECHA
        Text(
            text = "FECHA DE INGRESO",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TextoTarjeta
        )

        Spacer(modifier = Modifier.height(10.dp))

        // SELECCIONAR FECHA (CAFÉ)
        BotonModulo3D(
            texto = if (fechaSeleccionada == "No seleccionada") {
                "SELECCIONAR FECHA"
            } else {
                fechaSeleccionada
            },
            icono = "📅",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            colorTexto = Color.Black,
            onClick = { mostrarCalendario = true },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(25.dp))

        // HORARIO
        Text(
            text = "HORARIO DE INGRESO",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TextoTarjeta
        )

        Spacer(modifier = Modifier.height(10.dp))

        // SELECCIONAR HORARIO (CAFÉ)
        BotonModulo3D(
            texto = if (horarioSeleccionado == "No seleccionado") {
                "SELECCIONAR HORARIO"
            } else {
                horarioSeleccionado
            },
            icono = "🕐",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            colorTexto = Color.Black,
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

        // CONFIRMAR CITA (CAFÉ)
        BotonModulo3D(
            texto = "CONFIRMAR CITA",
            icono = "✔️",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            colorTexto = Color.Black,
            onClick = {
                if ((fechaSeleccionada != "No seleccionada") && (horarioSeleccionado != "No seleccionado")) {
                    val citaMap = mapOf(
                        "tipo" to "CITA_INGRESO",
                        "fecha" to fechaSeleccionada,
                        "horario" to horarioSeleccionado,
                        "timestamp" to System.currentTimeMillis()
                    )
                    FirebaseSyncManager.publicarRespuestaClienteConPush("cliente", citaMap)
                    citaConfirmada = true
                    Toast.makeText(context, "✅ Cita transmitida en tiempo real al taller vía Firebase", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Seleccione fecha y horario primero", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // REGRESAR (GRIS)
        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            colorTexto = Color.White,
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
                    Text(text = "ACEPTAR", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) {
                    Text("CANCELAR", color = Color.White)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF006C4C),
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = Color.White,
                    subheadContentColor = Color.White,
                    yearContentColor = Color.White,
                    currentYearContentColor = Color.White,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = Color(0xFF004D33),
                    dayContentColor = Color.White,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = Color(0xFF004D33),
                    todayContentColor = Color(0xFFFFD700),
                    todayDateBorderColor = Color(0xFFFFD700)
                )
            )
        }
    }

    // SELECCIÓN DE HORARIO (CAFÉ)
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9B6B43)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Text(
                                text = horario,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
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
                    text = "Su cita de ingreso ha sido programada y enviada al taller:\n\n" +
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
