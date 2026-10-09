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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.R
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@Composable
fun SeguimientoReparacionScreen(
    ordenId: Int,
    onCentroNotificaciones: (Int) -> Unit = {},
    onRegresar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var orden by remember { mutableStateOf<OrdenServicio?>(null) }
    var autoDetalle by remember { mutableStateOf<Auto?>(null) }
    var telefonoCliente by remember { mutableStateOf("") }
    var clienteId by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }

    var estadoSeleccionado by remember { mutableStateOf("EN ESPERA") }
    var porcentajeAvanceTaller by remember { mutableStateOf(0) }
    var fechaIngreso by remember { mutableStateOf("") }
    var fechaEntrega by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    LaunchedEffect(ordenId) {
        scope.launch {
            orden = OrdenServicioRepository.obtenerOrdenPorId(ordenId, context)
            orden?.let { o ->
                estadoSeleccionado = o.estado
                porcentajeAvanceTaller = o.porcentajeAvance
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
                val ordenActualizada = o.copy(
                    estado = estadoSeleccionado,
                    porcentajeAvance = porcentajeAvanceTaller,
                    fechaEntrega = fechaEntrega
                )
                OrdenServicioRepository.actualizarEstadoYAvance(
                    o.id,
                    estadoSeleccionado,
                    porcentajeAvanceTaller,
                    fechaEntrega,
                    context
                )
                FirebaseSyncManager.subirOrdenAFirebase(ordenActualizada)
                Toast.makeText(context, "✅ Avance y datos transmitidos a la App Cliente en Firebase", Toast.LENGTH_SHORT).show()
                guardando = false
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                guardando = false
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
                    text = "SEGUIMIENTO DE REPARACIÓN (TALLER)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal,
                    softWrap = false,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(20.dp))

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

                        // PLACA Y VIN DEL VEHÍCULO
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

                Spacer(modifier = Modifier.height(20.dp))

                // 🎛️ BOTONES PROMINENTES DE PORCENTAJE DE AVANCE (TALLER SELECCIONA Y TRANSMITE A FIREBASE)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoSecundario),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ACTUALIZAR AVANCE DE REPARACIÓN (TRANSMISIÓN DIRECTA A FIREBASE)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.TituloPrincipal
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val pasos = listOf(0, 25, 50, 75, 100)
                            pasos.forEach { paso ->
                                val esActivo = porcentajeAvanceTaller == paso
                                BotonModulo3D(
                                    texto = "$paso%",
                                    colorClaro = if (esActivo) Color(0xFFB9F6CA) else Color(0xFFD5E1E6),
                                    colorMedio = if (esActivo) Color(0xFF00C853) else Color(0xFF90A4AE),
                                    colorOscuro = if (esActivo) Color(0xFF00695C) else Color(0xFF455A64),
                                    onClick = {
                                        porcentajeAvanceTaller = paso
                                        scope.launch {
                                            val ordenActualizada = o.copy(porcentajeAvance = paso)
                                            OrdenServicioRepository.actualizarEstadoYAvance(o.id, o.estado, paso, o.fechaEntrega, context)
                                            FirebaseSyncManager.subirOrdenAFirebase(ordenActualizada)
                                            Toast.makeText(context, "⚡ Avance del $paso% transmitido a la App Cliente en Firebase", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    tamanioTexto = 14,
                                    colorTexto = if (esActivo) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // BOTÓN VERDE CENTRO DE NOTIFICACIONES Y AVISOS AL CLIENTE
                BotonModulo3D(
                    texto = "CENTRO DE NOTIFICACIONES AL CLIENTE",
                    icono = "📱",
                    colorClaro = Color(0xFFB9F6CA),
                    colorMedio = Color(0xFF00C853),
                    colorOscuro = Color(0xFF00695C),
                    onClick = { onCentroNotificaciones(o.id) },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    tamanioTexto = 15,
                    colorTexto = Color.Black
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        colorClaro = Color(0xFF90CAF9), colorMedio = Color(0xFF1976D2), colorOscuro = Color(0xFF0D47A1),
                        onClick = { mostrarCalendarioDoble() },
                        modifier = Modifier.fillMaxWidth(),
                        colorTexto = Color.Black
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                BotonModulo3D(
                    texto = if (guardando) "TRANSMITIENDO A FIREBASE..." else "⚡ TRANSMITIR CAMBIOS A FIREBASE",
                    colorClaro = Color(0xFFB9F6CA), colorMedio = Color(0xFF00C853), colorOscuro = Color(0xFF00695C),
                    onClick = { guardarCambios() },
                    colorTexto = Color.Black,
                    modifier = Modifier.fillMaxWidth().height(58.dp)
                )
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
                    colorTexto = Color.White
                )
            }
        }
    }
}
