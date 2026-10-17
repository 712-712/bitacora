package com.example.bitacoraautomotriz.ui.clientes

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.R
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.data.RecepcionVehiculo
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.repository.RecepcionRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun VerReporteClienteScreen(
    ordenId: Int = 0,
    esModoCliente: Boolean = false,
    onRegresar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val esCliente = remember(esModoCliente) {
        esModoCliente || context.packageName.lowercase().contains("cliente")
    }

    val prefs = remember { context.getSharedPreferences("info_taller_reporte_prefs", Context.MODE_PRIVATE) }

    var nombreTaller by remember {
        mutableStateOf(prefs.getString("nombre_taller", "TALLER MECÁNICO AUTORIZADO") ?: "TALLER MECÁNICO AUTORIZADO")
    }
    var diasMinimos by remember {
        mutableStateOf(prefs.getString("dias_minimos", "1 DÍA") ?: "1 DÍA")
    }
    var diasMaximos by remember {
        mutableStateOf(prefs.getString("dias_maximos", "5 DÍAS") ?: "5 DÍAS")
    }
    var editandoInformacion by remember { mutableStateOf(false) }

    var avisoEditado by remember { mutableStateOf(prefs.getString("aviso_editado", "") ?: "") }

    fun guardarDatosTallerYAviso() {
        prefs.edit()
            .putString("nombre_taller", nombreTaller)
            .putString("dias_minimos", diasMinimos)
            .putString("dias_maximos", diasMaximos)
            .putString("aviso_editado", avisoEditado)
            .apply()
    }

    var todasLasOrdenes by remember { mutableStateOf<List<OrdenServicio>>(emptyList()) }
    var orden by remember { mutableStateOf<OrdenServicio?>(null) }
    var autoDetalle by remember { mutableStateOf<Auto?>(null) }
    var recepcionDetalle by remember { mutableStateOf<RecepcionVehiculo?>(null) }
    var telefonoCliente by remember { mutableStateOf("") }
    var clienteId by remember { mutableIntStateOf(0) }
    var cargando by remember { mutableStateOf(true) }
    var mostrarDialogoEliminar by remember { mutableStateOf<OrdenServicio?>(null) }

    var estadoSeleccionado by remember { mutableStateOf("EN ESPERA") }
    var fechaIngreso by remember { mutableStateOf("") }
    var fechaEntrega by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    fun cargarDetallesParaOrden(o: OrdenServicio) {
        orden = o
        estadoSeleccionado = o.estado
        fechaEntrega = o.fechaEntrega
        scope.launch {
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

            try {
                val recepciones = RecepcionRepository.obtenerRecepciones(context)
                recepcionDetalle = recepciones.find { r -> r.cliente.equals(o.cliente, ignoreCase = true) } ?: recepciones.lastOrNull()
            } catch (_: Exception) {
                recepcionDetalle = null
            }

            try {
                // Escuchar Recepción y Check-In legal en tiempo real desde Firebase
                FirebaseSyncManager.escucharRecepcionesEnTiempoReal { recepcionesFirebase ->
                    if (recepcionesFirebase.isNotEmpty()) {
                        val rFirebase = recepcionesFirebase.find { r -> r.cliente.trim().equals(o.cliente.trim(), ignoreCase = true) } ?: recepcionesFirebase.lastOrNull()
                        if (rFirebase != null) {
                            recepcionDetalle = rFirebase
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(ordenId) {
        scope.launch {
            cargando = true
            try {
                // 1. Cargar desde Room Local
                val ordenesLocales = OrdenServicioRepository.obtenerOrdenes(context)
                if (ordenesLocales.isNotEmpty()) {
                    todasLasOrdenes = ordenesLocales.sortedByDescending { it.id }
                    val inicial = if (ordenId > 0) {
                        ordenesLocales.find { it.id == ordenId } ?: ordenesLocales.last()
                    } else {
                        ordenesLocales.last()
                    }
                    cargarDetallesParaOrden(inicial)
                    cargando = false
                }
            } catch (_: Exception) {}

            try {
                // 2. Escuchar en tiempo real desde Firebase Database
                FirebaseSyncManager.escucharTodasLasOrdenesEnTiempoReal { ordenesFirebase ->
                    if (ordenesFirebase.isNotEmpty()) {
                        todasLasOrdenes = ordenesFirebase.sortedByDescending { it.id }
                        val seleccionada = if (ordenId > 0) {
                            ordenesFirebase.find { it.id == ordenId } ?: ordenesFirebase.last()
                        } else {
                            ordenesFirebase.last()
                        }
                        cargarDetallesParaOrden(seleccionada)
                    }
                    cargando = false
                }
            } catch (_: Exception) {
                cargando = false
            }
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

    fun guardarCambiosCompleto(navegarAlRegresar: Boolean = true) {
        guardando = true
        scope.launch {
            try {
                guardarDatosTallerYAviso()
                val o = orden
                if (o != null) {
                    val ordenActualizada = o.copy(
                        estado = estadoSeleccionado,
                        fechaEntrega = if (fechaEntrega.isNotBlank()) fechaEntrega else o.fechaEntrega
                    )
                    OrdenServicioRepository.actualizarEstadoYAvance(
                        o.id,
                        estadoSeleccionado,
                        o.porcentajeAvance,
                        ordenActualizada.fechaEntrega,
                        context
                    )
                    FirebaseSyncManager.subirOrdenAFirebase(ordenActualizada)
                }
                Toast.makeText(context, "✅ Información guardada y transmitida a la Consola del Cliente", Toast.LENGTH_SHORT).show()
                guardando = false
                editandoInformacion = false
                if (navegarAlRegresar) {
                    onRegresar()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                guardando = false
            }
        }
    }

    val nombreClienteText = orden?.cliente?.uppercase() ?: "CLIENTE"
    val autoModelText = remember(orden, autoDetalle) {
        val autoNombre = orden?.auto?.uppercase() ?: "VEHÍCULO"
        val placaStr = autoDetalle?.placa?.uppercase() ?: ""
        if (placaStr.isNotBlank() && !autoNombre.contains(placaStr)) {
            "$autoNombre ($placaStr)"
        } else {
            autoNombre
        }
    }

    val plantillaAviso = remember(nombreClienteText, autoModelText, diasMinimos, diasMaximos) {
        val minStr = diasMinimos.replace(Regex("[^0-9]"), "").ifBlank { "1" }
        val maxStr = diasMaximos.replace(Regex("[^0-9]"), "").ifBlank { "5" }
        "Estimado/a $nombreClienteText, ya tenemos el diagnóstico de su $autoModelText. La reparación toma de $minStr a $maxStr días hábiles sujeto a disponibilidad de repuestos. Le informaremos sobre cada avance. ¡Saludos cordiales!"
    }

    val avisoFinal = if (avisoEditado.isNotBlank()) avisoEditado else plantillaAviso

    val coloresCamposTexto = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        disabledContainerColor = Colores.FondoSecundario,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White,
        focusedBorderColor = Colores.BordeBoton,
        unfocusedBorderColor = Colores.BordeBoton.copy(alpha = 0.5f),
        cursorColor = Color.White,
        selectionColors = TextSelectionColors(
            handleColor = Color.White,
            backgroundColor = Color(0xFF00DDEB).copy(alpha = 0.4f)
        )
    )

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
            // TÍTULO ACTUALIZADO ALINEADO EN DOS LÍNEAS CENTRADAS
            Text(
                text = "REPORTE DE COTIZACIÓN Y\nSERVICIO AL CLIENTE",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SELECTOR DE VEHÍCULOS / COTIZACIONES EN PROCESO (LISTA COMPLETA)
            if (todasLasOrdenes.isNotEmpty()) {
                Text(
                    text = "SELECCIONE SU VEHÍCULO O COTIZACIÓN:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todasLasOrdenes.forEach { oItem ->
                        val esSeleccionada = orden?.id == oItem.id
                        Button(
                            onClick = {
                                cargarDetallesParaOrden(oItem)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (esSeleccionada) Color(0xFF00C853) else Colores.FondoSecundario
                            )
                        ) {
                            Text(
                                text = "🚗 FOLIO #${String.format(Locale.US, "%05d", oItem.id)}: ${oItem.cliente.uppercase()} (${oItem.auto.uppercase()})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // SI HAY UNA ORDEN SELECCIONADA: MOSTRAR BOTONES DE ESTADO Y BOTÓN ROJO ELIMINAR FOLIO JUSTO DEBAJO DEL SELECTOR
            if (orden != null) {
                val o = orden!!

                Text(
                    text = "ESTADO DE LA COTIZACIÓN:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // BOTONES DE ESTADO REUBICADOS (EN ESPERA, RECHAZADO, ACEPTADO)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // 1. EN ESPERA
                    BotonModulo3D(
                        texto = "EN ESPERA",
                        colorClaro = if (estadoSeleccionado == "EN ESPERA") Color(0xFF90CAF9) else Color(0xFF64B5F6),
                        colorMedio = if (estadoSeleccionado == "EN ESPERA") Color(0xFF1976D2) else Color(0xFF1976D2),
                        colorOscuro = if (estadoSeleccionado == "EN ESPERA") Color(0xFF0D47A1) else Color(0xFF0D47A1),
                        onClick = { estadoSeleccionado = "EN ESPERA" },
                        modifier = Modifier.weight(1f).height(50.dp),
                        tamanioTexto = 13,
                        colorTexto = Color.Black
                    )
                    // 2. RECHAZADO
                    BotonModulo3D(
                        texto = "RECHAZADO",
                        colorClaro = if (estadoSeleccionado == "RECHAZADO") Color(0xFFEF9A9A) else Color(0xFF64B5F6),
                        colorMedio = if (estadoSeleccionado == "RECHAZADO") Color(0xFFE53935) else Color(0xFF1976D2),
                        colorOscuro = if (estadoSeleccionado == "RECHAZADO") Color(0xFFB71C1C) else Color(0xFF0D47A1),
                        onClick = { estadoSeleccionado = "RECHAZADO" },
                        modifier = Modifier.weight(1f).height(50.dp),
                        tamanioTexto = 13,
                        colorTexto = Color.Black
                    )
                    // 3. ACEPTADO
                    BotonModulo3D(
                        texto = "ACEPTADO",
                        colorClaro = if (estadoSeleccionado == "ACEPTADO") Color(0xFF7DFFB2) else Color(0xFF64B5F6),
                        colorMedio = if (estadoSeleccionado == "ACEPTADO") Color(0xFF00D96B) else Color(0xFF1976D2),
                        colorOscuro = if (estadoSeleccionado == "ACEPTADO") Color(0xFF008844) else Color(0xFF0D47A1),
                        onClick = { estadoSeleccionado = "ACEPTADO" },
                        modifier = Modifier.weight(1f).height(50.dp),
                        tamanioTexto = 13,
                        colorTexto = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BOTÓN ROJO: ELIMINAR FOLIO (SÓLO PARA MODO TALLER)
                if (!esCliente) {
                    BotonModulo3D(
                        texto = "ELIMINAR FOLIO",
                        icono = "🗑️",
                        colorClaro = Color(0xFFEF9A9A),
                        colorMedio = Color(0xFFE53935),
                        colorOscuro = Color(0xFFB71C1C),
                        colorTexto = Color.Black,
                        onClick = { mostrarDialogoEliminar = o },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        tamanioTexto = 15
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

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

                Spacer(modifier = Modifier.height(20.dp))

                // TARJETA UNIFICADA EN COLOR VERDE: INFORMACIÓN DEL TALLER, ESTIMACIÓN Y AVISO AL CLIENTE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "INFORMACIÓN DEL TALLER Y ESTIMACIÓN",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "NOMBRE DEL TALLER / MECÁNICO:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        OutlinedTextField(
                            value = nombreTaller,
                            onValueChange = { nombreTaller = it.uppercase() },
                            enabled = editandoInformacion,
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = coloresCamposTexto,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DÍAS MÍNIMOS:",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                OutlinedTextField(
                                    value = diasMinimos,
                                    onValueChange = {
                                        diasMinimos = it.uppercase()
                                        avisoEditado = ""
                                    },
                                    enabled = editandoInformacion,
                                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Characters,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = coloresCamposTexto,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DÍAS MÁXIMOS:",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                OutlinedTextField(
                                    value = diasMaximos,
                                    onValueChange = {
                                        diasMaximos = it.uppercase()
                                        avisoEditado = ""
                                    },
                                    enabled = editandoInformacion,
                                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Characters,
                                        imeAction = ImeAction.Done
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = coloresCamposTexto,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF004D33))

                        Text(
                            text = "📢 AVISO DE DIAGNÓSTICO Y TIEMPO DE SERVICIO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        OutlinedTextField(
                            value = avisoFinal,
                            onValueChange = {
                                avisoEditado = it.uppercase()
                            },
                            enabled = editandoInformacion,
                            textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                            maxLines = 10,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = coloresCamposTexto,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // UN SÓLO PAR DE BOTONES DE EDICIÓN Y GUARDADO PARA TODA LA TARJETA
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (esCliente) {
                                // SÓLO PARA APP CLIENTE: BOTÓN ELIMINAR (ROJO)
                                BotonModulo3D(
                                    texto = "ELIMINAR",
                                    icono = "🗑️",
                                    colorClaro = Color(0xFFEF9A9A),
                                    colorMedio = Color(0xFFE53935),
                                    colorOscuro = Color(0xFFB71C1C),
                                    colorTexto = Color.Black,
                                    onClick = {
                                        if (orden != null) {
                                            mostrarDialogoEliminar = orden
                                        } else {
                                            Toast.makeText(context, "No hay folio seleccionado para eliminar", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    tamanioTexto = 14
                                )
                            } else {
                                // PARA APP TALLER: BOTÓN EDITAR (AZUL)
                                BotonModulo3D(
                                    texto = "EDITAR",
                                    icono = "✏️",
                                    colorClaro = Color(0xFF80D8FF),
                                    colorMedio = Color(0xFF00B8D4),
                                    colorOscuro = Color(0xFF006064),
                                    colorTexto = Color.Black,
                                    onClick = {
                                        editandoInformacion = true
                                        Toast.makeText(context, "✏️ Edición de información y aviso habilitada", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    tamanioTexto = 14
                                )
                            }

                            // BOTÓN GUARDAR (VERDE): GUARDA, TRANSMITE Y REGRESA A LA CONSOLA DE AVISOS
                            BotonModulo3D(
                                texto = if (guardando) "GUARDANDO..." else "GUARDAR",
                                icono = "💾",
                                colorClaro = Color(0xFFB9F6CA),
                                colorMedio = Color(0xFF00C853),
                                colorOscuro = Color(0xFF00695C),
                                colorTexto = Color.Black,
                                onClick = {
                                    guardarCambiosCompleto(navegarAlRegresar = true)
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                tamanioTexto = 14
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                        colorClaro = Color(0xFF90CAF9), colorMedio = Color(0xFF1976D2), colorOscuro = Color(0xFF004D33),
                        onClick = { mostrarCalendarioDoble() },
                        modifier = Modifier.fillMaxWidth(),
                        colorTexto = Color.Black
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // BOTÓN REGRESAR FIJO E INMÓVIL AL FONDO DE LA PANTALLA
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

    // DIÁLOGO DE CONFIRMACIÓN PARA ELIMINAR EL FOLIO DE COTIZACIÓN
    mostrarDialogoEliminar?.let { ordenAEliminar ->
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = null },
            title = { Text("¿Eliminar Folio #${String.format(Locale.US, "%05d", ordenAEliminar.id)}?") },
            text = {
                Text("Cliente: ${ordenAEliminar.cliente}\nVehículo: ${ordenAEliminar.auto}\n\nEsta acción eliminará el informe completo de la pantalla y de la nube. No se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                OrdenServicioRepository.eliminarOrden(ordenAEliminar, context)
                                Toast.makeText(context, "✅ Folio #${String.format(Locale.US, "%05d", ordenAEliminar.id)} eliminado exitosamente", Toast.LENGTH_SHORT).show()
                                val listaNueva = todasLasOrdenes.filter { it.id != ordenAEliminar.id }
                                todasLasOrdenes = listaNueva
                                if (listaNueva.isNotEmpty()) {
                                    cargarDetallesParaOrden(listaNueva.first())
                                } else {
                                    orden = null
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                            mostrarDialogoEliminar = null
                        }
                    }
                ) {
                    Text("SÍ, ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = null }) {
                    Text("CANCELAR")
                }
            }
        )
    }
}
