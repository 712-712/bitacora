package com.example.bitacoraautomotriz.ui.ordenes

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
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

    var orden by remember { mutableStateOf<OrdenServicio?>(null) }
    var telefonoCliente by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }

    var estadoSeleccionado by remember { mutableStateOf("EN ESPERA") }
    var fechaIngreso by remember { mutableStateOf("") }
    var fechaEntrega by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    // ✅ Control para saber si el usuario ya tocó un botón de estado (para pintarlos de azul al inicio)
    var seleccionHecha by remember { mutableStateOf(false) }

    LaunchedEffect(ordenId) {
        scope.launch {
            orden = OrdenServicioRepository.obtenerOrdenPorId(ordenId)
            orden?.let { o ->
                estadoSeleccionado = o.estado
                fechaEntrega = o.fechaEntrega
                seleccionHecha = true // Si ya tiene un estado de la BD, marcamos que ya se seleccionó

                try {
                    val clientes = ClienteRepository.obtenerClientes()
                    val clienteEncontrado = clientes.find { it.nombre.trim().equals(o.cliente.trim(), ignoreCase = true) }
                    telefonoCliente = clienteEncontrado?.telefono ?: "No disponible"
                } catch (e: Exception) {
                    telefonoCliente = "No disponible"
                }
            }
            cargando = false
        }
    }

    // ✅ CALENDARIO UNIFICADO (Abre dos veces seguidas: Ingreso y luego Entrega)
    fun mostrarCalendarioDoble() {
        val calendario = Calendar.getInstance()
        // 1. Selector de Fecha de INGRESO
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            fechaIngreso = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)

            // 2. Selector de Fecha de ENTREGA (se abre automáticamente después de elegir Ingreso)
            DatePickerDialog(context, { _, y2, m2, d2 ->
                fechaEntrega = String.format(Locale.getDefault(), "%02d/%02d/%04d", d2, m2 + 1, y2)
            }, year, month, dayOfMonth).show()

        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
    }

    fun guardarCambios() {
        val o = orden ?: return
        guardando = true
        scope.launch {
            try {
                OrdenServicioRepository.actualizarEstadoYAvance(o.id, estadoSeleccionado, if (estadoSeleccionado == "ACEPTADO") 100 else 0, fechaEntrega)
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
        val mensaje = """
*COTIZACIÓN DE SERVICIO*
Folio: ${String.format("%05d", o.id)}
Cliente: ${o.cliente}
Auto: ${o.auto}

*Detalles:*
- Falla: ${o.fallaReportada}
- Diagnóstico: ${o.diagnostico}
- Trabajo: ${o.trabajoRealizado}

*COSTOS:*
- Mano de Obra: ${'$'}${String.format(Locale.US, "%.2f", o.costoManoObra)}
- Refacciones: ${'$'}${String.format(Locale.US, "%.2f", o.costoRefacciones)}
- I.V.A. (16%): ${'$'}${String.format(Locale.US, "%.2f", o.iva)}
*TOTAL: ${'$'}${String.format(Locale.US, "%.2f", o.total)}*

Fecha de cotización: ${o.fecha}

────────────────────
📱 *Por favor responde a este mensaje con:*
✅ "ACEPTO" para aprobar
❌ "RECHAZO" si no está de acuerdo
────────────────────
        """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/52$telefonoLimpio?text=${Uri.encode(mensaje)}")
        try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_LONG).show() }
    }

    // Colores base
    val azulClaro = Color(0xFF90CAF9); val azulMedio = Color(0xFF1976D2); val azulOscuro = Color(0xFF0D47A1)
    val rojoClaro = Color(0xFFEF9A9A); val rojoMedio = Color(0xFFE53935); val rojoOscuro = Color(0xFFB71C1C)
    val verdeClaro = Color(0xFF7DFFB2); val verdeMedio = Color(0xFF00D96B); val verdeOscuro = Color(0xFF008844)
    val grisClaro = Color(0xFFB0BEC5); val grisMedio = Color(0xFF78909C); val grisOscuro = Color(0xFF455A64)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF001B44)).padding(24.dp).verticalScroll(rememberScrollState())) {
        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
        } else if (orden == null) {
            Text(text = "No se encontró la cotización", color = Color.White, fontSize = 18.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            val o = orden!!
            Text(text = "ENVIAR REPORTE AL CLIENTE", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, softWrap = false, maxLines = 1, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(text = "FOLIO: ${String.format("%05d", o.id)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = o.cliente, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B44))
                    Text(text = o.auto, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17202A))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "TELÉFONO DEL CLIENTE:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF607D8B))
                    Text(text = "📞 $telefonoCliente", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B44))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Fecha de cotización: ${o.fecha}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(text = "Falla Reportada:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = o.fallaReportada, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Diagnóstico:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = o.diagnostico, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Trabajo por Realizar:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = o.trabajoRealizado, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(text = "COSTOS:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = "Mano de Obra: $ ${String.format(Locale.US, "%.2f", o.costoManoObra)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = "Refacciones: $ ${String.format(Locale.US, "%.2f", o.costoRefacciones)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = "I.V.A. (16%): $ ${String.format(Locale.US, "%.2f", o.iva)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(text = "TOTAL: $ ${String.format(Locale.US, "%.2f", o.total)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            // ✅ BOTÓN DE 3 PARTES (Todos AZULES al inicio)
            Text(text = "ESTADO DE LA COTIZACIÓN:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90CAF9), modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // 1. EN ESPERA
                BotonModulo3D(
                    texto = "EN ESPERA",
                    colorClaro = if (estadoSeleccionado == "EN ESPERA") Color(0xFF90CAF9) else Color(0xFF64B5F6),
                    colorMedio = if (estadoSeleccionado == "EN ESPERA") Color(0xFF1976D2) else Color(0xFF1976D2),
                    colorOscuro = if (estadoSeleccionado == "EN ESPERA") Color(0xFF0D47A1) else Color(0xFF0D47A1),
                    onClick = { estadoSeleccionado = "EN ESPERA" },
                    modifier = Modifier.weight(1f).height(50.dp),
                    tamanioTexto = 13
                )
                // 2. RECHAZADO
                BotonModulo3D(
                    texto = "RECHAZADO",
                    colorClaro = if (estadoSeleccionado == "RECHAZADO") Color(0xFFEF9A9A) else Color(0xFF64B5F6),
                    colorMedio = if (estadoSeleccionado == "RECHAZADO") Color(0xFFE53935) else Color(0xFF1976D2),
                    colorOscuro = if (estadoSeleccionado == "RECHAZADO") Color(0xFFB71C1C) else Color(0xFF0D47A1),
                    onClick = { estadoSeleccionado = "RECHAZADO" },
                    modifier = Modifier.weight(1f).height(50.dp),
                    tamanioTexto = 13
                )
                // 3. ACEPTADO
                BotonModulo3D(
                    texto = "ACEPTADO",
                    colorClaro = if (estadoSeleccionado == "ACEPTADO") Color(0xFF7DFFB2) else Color(0xFF64B5F6),
                    colorMedio = if (estadoSeleccionado == "ACEPTADO") Color(0xFF00D96B) else Color(0xFF1976D2),
                    colorOscuro = if (estadoSeleccionado == "ACEPTADO") Color(0xFF008844) else Color(0xFF0D47A1),
                    onClick = { estadoSeleccionado = "ACEPTADO" },
                    modifier = Modifier.weight(1f).height(50.dp),
                    tamanioTexto = 13
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ CALENDARIO UNIFICADO (Solo aparece si está ACEPTADO)
            if (estadoSeleccionado == "ACEPTADO") {
                Text(text = "PROGRAMAR TRABAJO:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7DFFB2), modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(12.dp))

                BotonModulo3D(
                    texto = if (fechaIngreso.isEmpty()) "📅 SELECCIONAR INGRESO Y ENTREGA" else " INGRESO: $fechaIngreso | ENTREGA: $fechaEntrega",
                    colorClaro = Color(0xFF90CAF9), colorMedio = Color(0xFF1976D2), colorOscuro = Color(0xFF0D47A1),
                    onClick = { mostrarCalendarioDoble() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            BotonModulo3D(
                texto = if (guardando) "GUARDANDO..." else "💾 GUARDAR CAMBIOS",
                colorClaro = Color(0xFFD7B899), colorMedio = Color(0xFF9B6B43), colorOscuro = Color(0xFF5D3A1A),
                onClick = { guardarCambios() }
            )
            Spacer(modifier = Modifier.height(16.dp))

            BotonModulo3D(
                texto = "📱 ENVIAR AL CLIENTE",
                colorClaro = Color(0xFFD7B899), colorMedio = Color(0xFF9B6B43), colorOscuro = Color(0xFF5D3A1A),
                onClick = { enviarWhatsApp() }
            )
            Spacer(modifier = Modifier.height(16.dp))

            BotonModulo3D(
                texto = "REGRESAR",
                colorClaro = Color(0xFFD5E1E6), colorMedio = Color(0xFF90A4AE), colorOscuro = Color(0xFF455A64),
                onClick = onRegresar
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
