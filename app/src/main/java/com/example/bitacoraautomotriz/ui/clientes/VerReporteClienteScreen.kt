package com.example.bitacoraautomotriz.ui.clientes

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
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun VerReporteClienteScreen(
    ordenId: Int = 0,
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
            try {
                val lista = OrdenServicioRepository.obtenerOrdenes(context)
                val ordenEncontrada = if (ordenId > 0) {
                    lista.find { it.id == ordenId } ?: lista.lastOrNull()
                } else {
                    lista.lastOrNull()
                }

                orden = ordenEncontrada
                ordenEncontrada?.let { o ->
                    estadoSeleccionado = o.estado
                    // ESCUCHAR CAMBIOS EN TIEMPO REAL DESDE FIREBASE
                    FirebaseSyncManager.escucharOrdenEnTiempoReal(o.id) { ordenDescargada ->
                        orden = ordenDescargada
                        estadoSeleccionado = ordenDescargada.estado
                    }

                    try {
                        val clientes = ClienteRepository.obtenerClientes(context)
                        val c = clientes.find { it.nombre.trim().equals(o.cliente.trim(), ignoreCase = true) }
                        telefonoCliente = c?.telefono ?: "No disponible"
                        clienteId = c?.id ?: 0
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
            } catch (_: Exception) {
                orden = null
            } finally {
                cargando = false
            }
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
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "REPORTE DE SERVICIO AL CLIENTE",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                    Text(text = "NO TIENE REPORTES DE SERVICIO ACTIVOS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                val o = orden!!

                // TARJETA COMPLETA CON EL REPORTE DEL CLIENTE Y NUM VIN + PLACAS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "FOLIO: ${String.format(Locale.US, "%05d", o.id)}   |   ID: $clienteId",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DFFB2)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = o.cliente.uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = o.auto.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        // PLACA Y VIN DEL VEHÍCULO
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "PLACA: ${autoDetalle?.placa?.uppercase() ?: "N/A"}   |   VIN: ${autoDetalle?.vin?.uppercase()?.ifBlank { "N/A" } ?: "N/A"}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DFFB2)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (telefonoCliente.isNotBlank() && telefonoCliente != "No disponible") {
                            Text(text = "TELÉFONO REGISTRADO:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(
                                text = "📞 $telefonoCliente",
                                fontSize = 18.sp,
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
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(text = "FECHA DE COTIZACIÓN: ${o.fecha}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF004D33))

                        Text(text = "FALLA REPORTADA:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = o.fallaReportada, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "DIAGNÓSTICO:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = o.diagnostico, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "TRABAJO POR REALIZAR:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = o.trabajoRealizado, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF004D33))

                        Text(text = "COSTOS DEL SERVICIO:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Mano de Obra:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", o.costoManoObra), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Refacciones:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", o.costoRefacciones), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "I.V.A. (16%):", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", o.iva), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "TOTAL:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", o.total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CAMPOS DE DATOS PARA MENSAJE WHATSAPP
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

                // BOTONES DE ESTADO DE COTIZACIÓN (RECHAZADO Y ACEPTADO) SIN EL BOTÓN "EN ESPERA" SOLICITADO
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. RECHAZADO
                    BotonModulo3D(
                        texto = "RECHAZADO",
                        colorClaro = if (estadoSeleccionado == "RECHAZADO") Color(0xFFEF9A9A) else Color(0xFF64B5F6),
                        colorMedio = if (estadoSeleccionado == "RECHAZADO") Color(0xFFE53935) else Color(0xFF1976D2),
                        colorOscuro = if (estadoSeleccionado == "RECHAZADO") Color(0xFFB71C1C) else Color(0xFF0D47A1),
                        onClick = {
                            estadoSeleccionado = "RECHAZADO"
                            scope.launch {
                                try {
                                    OrdenServicioRepository.actualizarEstadoYAvance(o.id, "RECHAZADO", 0, "", context)
                                    Toast.makeText(context, "❌ Cotización marcada como RECHAZADA", Toast.LENGTH_SHORT).show()
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.weight(1f).height(58.dp),
                        tamanioTexto = 15,
                        colorTexto = Color.Black
                    )

                    // 2. ACEPTADO
                    BotonModulo3D(
                        texto = "ACEPTADO",
                        colorClaro = if (estadoSeleccionado == "ACEPTADO") Color(0xFF7DFFB2) else Color(0xFF64B5F6),
                        colorMedio = if (estadoSeleccionado == "ACEPTADO") Color(0xFF00D96B) else Color(0xFF1976D2),
                        colorOscuro = if (estadoSeleccionado == "ACEPTADO") Color(0xFF008844) else Color(0xFF0D47A1),
                        onClick = {
                            estadoSeleccionado = "ACEPTADO"
                            scope.launch {
                                try {
                                    OrdenServicioRepository.actualizarEstadoYAvance(o.id, "ACEPTADO", 100, "", context)
                                    Toast.makeText(context, "✅ Cotización marcada como ACEPTADA", Toast.LENGTH_SHORT).show()
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.weight(1f).height(58.dp),
                        tamanioTexto = 15,
                        colorTexto = Color.Black
                    )
                }
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
                    tamanioTexto = 16,
                    colorTexto = Color.White
                )
            }
        }
    }
}
