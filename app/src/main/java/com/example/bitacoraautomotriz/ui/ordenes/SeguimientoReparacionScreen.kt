package com.example.bitacoraautomotriz.ui.ordenes

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.R
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun SeguimientoReparacionScreen(
    ordenId: Int,
    onRegresar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val tallerFocusRequester = remember { FocusRequester() }
    val minDiasFocusRequester = remember { FocusRequester() }
    val maxDiasFocusRequester = remember { FocusRequester() }

    var orden by remember { mutableStateOf<OrdenServicio?>(null) }
    var autoDetalle by remember { mutableStateOf<Auto?>(null) }
    var telefonoCliente by remember { mutableStateOf("") }
    var clienteId by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }

    var estadoSeleccionado by remember { mutableStateOf("EN ESPERA") }
    var fechaIngreso by remember { mutableStateOf("") }
    var fechaEntrega by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    var nombreTallerMecanico by remember { mutableStateOf("") }
    var diasMinimos by remember { mutableStateOf("") }
    var diasMaximos by remember { mutableStateOf("") }

    val coloresCamposTexto = TextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoPantalla,
        unfocusedContainerColor = Colores.FondoPantalla,
        disabledContainerColor = Colores.FondoPantalla,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White,
        cursorColor = Color.White,
        selectionColors = TextSelectionColors(
            handleColor = Color.White,
            backgroundColor = Color(0xFF90CAF9).copy(alpha = 0.4f)
        )
    )

    LaunchedEffect(ordenId) {
        scope.launch {
            orden = OrdenServicioRepository.obtenerOrdenPorId(ordenId, context)
            orden?.let { o ->
                estadoSeleccionado = o.estado
                fechaEntrega = o.fechaEntrega

                try {
                    val clientes = ClienteRepository.obtenerClientes(context)
                    val clienteEncontrado = clientes.find { it.nombre.trim().equals(o.cliente.trim(), ignoreCase = true) }
                    telefonoCliente = clienteEncontrado?.telefono ?: "No disponible"
                    clienteId = clienteEncontrado?.id ?: 0
                } catch (_: Exception) {
                    telefonoCliente = "No disponible"
                    clienteId = 0
                }

                try {
                    val partes = o.auto.split("-")
                    val placaStr = partes.lastOrNull()?.trim() ?: ""
                    if (placaStr.isNotBlank()) {
                        autoDetalle = AutoRepository.obtenerAutoPorPlaca(placaStr, context)
                    }
                } catch (_: Exception) {
                    autoDetalle = null
                }
            }
            cargando = false
        }
    }

    fun mostrarCalendarioDoble() {
        val calendario = Calendar.getInstance()
        DatePickerDialog(
            context,
            R.style.CalendarioVerdeTheme,
            { _, year, month, dayOfMonth ->
                fechaIngreso = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)

                DatePickerDialog(
                    context,
                    R.style.CalendarioVerdeTheme,
                    { _, y2, m2, d2 ->
                        fechaEntrega = String.format(Locale.getDefault(), "%02d/%02d/%04d", d2, m2 + 1, y2)
                    },
                    year, month, dayOfMonth
                ).show()

            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun guardarCambios() {
        val o = orden ?: return
        guardando = true
        scope.launch {
            try {
                OrdenServicioRepository.actualizarEstadoYAvance(
                    o.id,
                    estadoSeleccionado,
                    if (estadoSeleccionado == "ACEPTADO") 100 else 0,
                    fechaEntrega,
                    context
                )
                Toast.makeText(context, "✅ Estado y fechas actualizados", Toast.LENGTH_SHORT).show()
                guardando = false
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                guardando = false
            }
        }
    }

    fun enviarWhatsApp() {
        val o = orden ?: return
        if (telefonoCliente == "No disponible" || telefonoCliente.isBlank()) {
            Toast.makeText(context, "No hay teléfono registrado", Toast.LENGTH_LONG).show()
            return
        }
        val telefonoLimpio = telefonoCliente.replace(Regex("[^0-9]"), "")

        val emisor = if (nombreTallerMecanico.isBlank()) "su mecánico / taller" else nombreTallerMecanico.trim().uppercase()
        val minD = if (diasMinimos.isBlank()) "1" else diasMinimos.trim()
        val maxD = if (diasMaximos.isBlank()) "3" else diasMaximos.trim()

        val leyendaInicial = """
HOLA, soy $emisor.

Normalmente este tipo de reparaciones terminadas, toman entre $minD y $maxD días, pero prefiero confirmarle el tiempo exacto de entrega mañana una vez que hayamos revisado a fondo su vehículo y verifiquemos la disponibilidad de las piezas. En cuanto tenga esta confirmación le enviaré el tiempo de entrega.
        """.trimIndent()

        val mensaje = """
$leyendaInicial

────────────────────
*COTIZACIÓN DE SERVICIO*
Folio: ${String.format(Locale.US, "%05d", o.id)}   |   ID: $clienteId
Cliente: ${o.cliente}
Auto: ${o.auto}
Placa: ${autoDetalle?.placa ?: "N/A"}   |   VIN: ${autoDetalle?.vin?.ifBlank { "N/A" } ?: "N/A"}

*Detalles:*
- Falla: ${o.fallaReportada}
- Diagnóstico: ${o.diagnostico}
- Trabajo: ${o.trabajoRealizado}

*COSTOS:*
- Mano de Obra: $ ${String.format(Locale.US, "%,.2f", o.costoManoObra)}
- Refacciones: $ ${String.format(Locale.US, "%,.2f", o.costoRefacciones)}
- I.V.A. (16%): $ ${String.format(Locale.US, "%,.2f", o.iva)}
*TOTAL: $ ${String.format(Locale.US, "%,.2f", o.total)}*

Fecha de cotización: ${o.fecha}

────────────────────
📱 *Por favor responde a este mensaje con:*
✅ "ACEPTO" para aprobar
❌ "RECHAZO" si no está de acuerdo
────────────────────
        """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/52$telefonoLimpio?text=${Uri.encode(mensaje)}")
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp)
        ) {
            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (orden == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No se encontró la cotización", color = Color.White, fontSize = 18.sp)
                }
            } else {
                val o = orden!!
                Text(
                    text = "ENVIAR REPORTE AL CLIENTE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal,
                    softWrap = false,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(
                            text = "FOLIO: ${String.format(Locale.US, "%05d", o.id)}   |   ID: $clienteId",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DFFB2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = o.cliente.uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = o.auto.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        // PLACA Y VIN DEL VEHÍCULO SOLICITADOS
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "PLACA: ${autoDetalle?.placa?.uppercase() ?: "N/A"}   |   VIN: ${autoDetalle?.vin?.uppercase()?.ifBlank { "N/A" } ?: "N/A"}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DFFB2)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "TELÉFONO DEL CLIENTE:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📞 $telefonoCliente",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0033FF),
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$telefonoCliente")
                                }
                                try { context.startActivity(intent) } catch (_: Exception) { }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Fecha de cotización: ${o.fecha}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFF004D33))
                        Text(text = "Falla Reportada:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = o.fallaReportada, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Diagnóstico:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = o.diagnostico, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Trabajo por Realizar:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = o.trabajoRealizado, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFF004D33))
                        Text(text = "COSTOS:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = "Mano de Obra: $ " + String.format(Locale.US, "%,.2f", o.costoManoObra), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Refacciones: $ " + String.format(Locale.US, "%,.2f", o.costoRefacciones), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "I.V.A. (16%): $ " + String.format(Locale.US, "%,.2f", o.iva), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "TOTAL: $ " + String.format(Locale.US, "%,.2f", o.total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CAMPOS PERSONALIZADOS PARA EL REPORTE DE WHATSAPP
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoSecundario)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "DATOS PARA MENSAJE WHATSAPP",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.TituloPrincipal
                        )

                        Text(text = "NOMBRE DEL TALLER O MECÁNICO", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                        OutlinedTextField(
                            value = nombreTallerMecanico,
                            onValueChange = { nombreTallerMecanico = it.uppercase() },
                            placeholder = { Text("EJ: TALLER LOS PINOS / JUAN PÉREZ", color = Color.Gray, fontSize = 15.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { minDiasFocusRequester.requestFocus() }),
                            shape = RoundedCornerShape(10.dp),
                            colors = coloresCamposTexto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(tallerFocusRequester)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "DÍAS MÍNIMOS", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = diasMinimos,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) diasMinimos = it },
                                    placeholder = { Text("Ej: 1", color = Color.Gray, fontSize = 15.sp) },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { maxDiasFocusRequester.requestFocus() }),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = coloresCamposTexto,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(minDiasFocusRequester)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "DÍAS MÁXIMOS", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = diasMaximos,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) diasMaximos = it },
                                    placeholder = { Text("Ej: 3", color = Color.Gray, fontSize = 15.sp) },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = coloresCamposTexto,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(maxDiasFocusRequester)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ESTADO DE LA COTIZACIÓN:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // 1. EN ESPERA
                    BotonModulo3D(
                        texto = "EN ESPERA",
                        colorClaro = if (estadoSeleccionado == "EN ESPERA") Color(0xFF90CAF9) else Color(0xFF64B5F6),
                        colorMedio = if (estadoSeleccionado == "EN ESPERA") Color(0xFF1976D2) else Color(0xFF1976D2),
                        colorOscuro = if (estadoSeleccionado == "EN ESPERA") Color(0xFF0D47A1) else Color(0xFF0D47A1),
                        onClick = { estadoSeleccionado = "EN ESPERA" },
                        modifier = Modifier.weight(1f).height(58.dp),
                        tamanioTexto = 15,
                        colorTexto = Color.Black
                    )
                    // 2. RECHAZADO
                    BotonModulo3D(
                        texto = "RECHAZADO",
                        colorClaro = if (estadoSeleccionado == "RECHAZADO") Color(0xFFEF9A9A) else Color(0xFF64B5F6),
                        colorMedio = if (estadoSeleccionado == "RECHAZADO") Color(0xFFE53935) else Color(0xFF1976D2),
                        colorOscuro = if (estadoSeleccionado == "RECHAZADO") Color(0xFFB71C1C) else Color(0xFF0D47A1),
                        onClick = { estadoSeleccionado = "RECHAZADO" },
                        modifier = Modifier.weight(1f).height(58.dp),
                        tamanioTexto = 15,
                        colorTexto = Color.Black
                    )
                    // 3. ACEPTADO
                    BotonModulo3D(
                        texto = "ACEPTADO",
                        colorClaro = if (estadoSeleccionado == "ACEPTADO") Color(0xFF7DFFB2) else Color(0xFF64B5F6),
                        colorMedio = if (estadoSeleccionado == "ACEPTADO") Color(0xFF00D96B) else Color(0xFF1976D2),
                        colorOscuro = if (estadoSeleccionado == "ACEPTADO") Color(0xFF008844) else Color(0xFF0D47A1),
                        onClick = { estadoSeleccionado = "ACEPTADO" },
                        modifier = Modifier.weight(1f).height(58.dp),
                        tamanioTexto = 15,
                        colorTexto = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (estadoSeleccionado == "ACEPTADO") {
                    Text(
                        text = "PROGRAMAR TRABAJO:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7DFFB2),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    BotonModulo3D(
                        texto = if (fechaIngreso.isEmpty()) "📅 SELECCIONAR INGRESO Y ENTREGA DEL VEHÍCULO" else " INGRESO: $fechaIngreso | ENTREGA: $fechaEntrega",
                        colorClaro = Color(0xFF90CAF9), colorMedio = Color(0xFF1976D2), colorOscuro = Color(0xFF0D47A1),
                        onClick = { mostrarCalendarioDoble() },
                        modifier = Modifier.fillMaxWidth(),
                        colorTexto = Color.Black
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                BotonModulo3D(
                    texto = if (guardando) "GUARDANDO..." else "💾 GUARDAR CAMBIOS",
                    colorClaro = Color(0xFFD7B899), colorMedio = Color(0xFF9B6B43), colorOscuro = Color(0xFF5D3A1A),
                    onClick = { guardarCambios() },
                    colorTexto = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                BotonModulo3D(
                    texto = "📱 ENVIAR AL CLIENTE",
                    colorClaro = Color(0xFFD7B899), colorMedio = Color(0xFF9B6B43), colorOscuro = Color(0xFF5D3A1A),
                    onClick = { enviarWhatsApp() },
                    colorTexto = Color.Black
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // BOTÓN REGRESAR FIJO E INMÓVIL AL FONDO DE LA PANTALLA (DETRÁS DEL TECLADO)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Colores.FondoPantalla
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                BotonModulo3D(
                    texto = "REGRESAR",
                    icono = "🔙",
                    colorClaro = Colores.RegresarClaro,
                    colorMedio = Colores.RegresarMedio,
                    colorOscuro = Colores.RegresarOscuro,
                    onClick = onRegresar,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colorTexto = Color.White
                )
            }
        }
    }
}
