package com.example.bitacoraautomotriz.ui.ordenes

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun EnviarInformesClienteScreen(
    ordenIdInicial: Int = 0,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var ordenes by remember { mutableStateOf<List<OrdenServicio>>(emptyList()) }
    var ordenSeleccionada by remember { mutableStateOf<OrdenServicio?>(null) }
    var todosLosClientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var busquedaCliente by remember { mutableStateOf("") }

    var autosDelCliente by remember { mutableStateOf<List<Auto>>(emptyList()) }
    var autoSeleccionado by remember { mutableStateOf<Auto?>(null) }

    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                todosLosClientes = ClienteRepository.obtenerClientes(context)
                ordenes = OrdenServicioRepository.obtenerOrdenes(context)
                if (ordenIdInicial > 0) {
                    val encontrada = ordenes.find { it.id == ordenIdInicial } ?: ordenes.lastOrNull()
                    ordenSeleccionada = encontrada
                    encontrada?.let { o ->
                        clienteSeleccionado = todosLosClientes.find { c -> c.nombre.equals(o.cliente, ignoreCase = true) }
                    }
                }
            } catch (_: Exception) {
                todosLosClientes = emptyList()
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(clienteSeleccionado) {
        if (clienteSeleccionado != null) {
            try {
                val listaAutos = AutoRepository.obtenerAutosPorCliente(clienteSeleccionado!!.nombre, context)
                autosDelCliente = listaAutos
                if (listaAutos.size == 1) {
                    autoSeleccionado = listaAutos.first()
                }
            } catch (_: Exception) {
                autosDelCliente = emptyList()
            }
        } else {
            autosDelCliente = emptyList()
        }
    }

    val queryCliente = busquedaCliente.uppercase().trim()
    val clientesFiltrados = todosLosClientes.filter { c ->
        c.nombre.uppercase().contains(queryCliente, ignoreCase = true) ||
                c.id.toString().contains(queryCliente, ignoreCase = true)
    }

    fun enviarWhatsAppGeneral(textoMensaje: String) {
        if (clienteSeleccionado == null) {
            Toast.makeText(context, "Seleccione un cliente primero", Toast.LENGTH_SHORT).show()
            return
        }
        val c = clienteSeleccionado!!
        val telLimpio = c.telefono.replace(Regex("[^0-9]"), "")

        if (telLimpio.isBlank()) {
            Toast.makeText(context, "El cliente no tiene teléfono registrado", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/52$telLimpio?text=${Uri.encode(textoMensaje)}")
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "CENTRO DE NOTIFICACIONES Y AVISOS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Envíos directos formateados por WhatsApp y Firebase al cliente",
                fontSize = 14.sp,
                color = Colores.EtiquetaCampo,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // CARD 1: SELECCIÓN DE CLIENTE Y AUTO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. RECEPTOR DE LA NOTIFICACIÓN", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    if (clienteSeleccionado == null) {
                        OutlinedTextField(
                            value = busquedaCliente,
                            onValueChange = { busquedaCliente = it.uppercase() },
                            placeholder = { Text("Buscar cliente por nombre o ID...", color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (clientesFiltrados.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                clientesFiltrados.take(4).forEach { cliente ->
                                    Button(
                                        onClick = { clienteSeleccionado = cliente },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Colores.FondoSecundario)
                                    ) {
                                        Text(text = "${cliente.nombre} (${cliente.telefono})", color = Color.White)
                                    }
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(text = "CLIENTE: ${clienteSeleccionado!!.nombre.uppercase()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "TELÉFONO: ${clienteSeleccionado!!.telefono}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7DFFB2))
                            Spacer(modifier = Modifier.height(2.dp))
                            TextButton(
                                onClick = { clienteSeleccionado = null; autoSeleccionado = null; ordenSeleccionada = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("CAMBIAR DE CLIENTE", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        if (autoSeleccionado == null) {
                            if (autosDelCliente.isNotEmpty()) {
                                Text("SELECCIONE EL AUTO DEL CLIENTE:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    autosDelCliente.forEach { auto ->
                                        Button(
                                            onClick = { autoSeleccionado = auto },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Colores.FondoSecundario)
                                        ) {
                                            Text(text = "🚗 ${auto.marca} ${auto.modelo} (${auto.placa})", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "AUTO: ${autoSeleccionado!!.marca.uppercase()} ${autoSeleccionado!!.modelo.uppercase()} (${autoSeleccionado!!.placa.uppercase()})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7DFFB2))
                                Spacer(modifier = Modifier.height(2.dp))
                                TextButton(
                                    onClick = { autoSeleccionado = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("CAMBIAR DE AUTO", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CARD 2: CONSOLA DE NOTIFICACIONES RÁPIDAS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("2. CONSOLA DE AVISOS Y NOTIFICACIONES", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    val cNombre = clienteSeleccionado?.nombre?.uppercase() ?: "ESTIMADO CLIENTE"
                    val aNombre = if (autoSeleccionado != null) "${autoSeleccionado!!.marca} ${autoSeleccionado!!.modelo} (${autoSeleccionado!!.placa})" else "su vehículo"

                    // 1. AVISO DE COTIZACIÓN
                    BotonModulo3D(
                        texto = "📄 1. ENVIAR COTIZACIÓN DE SERVICIO",
                        colorClaro = Color(0xFFD7B899), colorMedio = Color(0xFF9B6B43), colorOscuro = Color(0xFF5D3A1A),
                        colorTexto = Color.Black,
                        onClick = {
                            val o = ordenSeleccionada
                            val txt = """
HOLA $cNombre.
*COTIZACIÓN DE SERVICIO DE TALLER MECÁNICO*
Vehículo: $aNombre

- Falla: ${o?.fallaReportada ?: "Revisión general"}
- Diagnóstico: ${o?.diagnostico ?: "Diagnóstico completado"}
- Trabajo por realizar: ${o?.trabajoRealizado ?: "Mantenimiento y reparación"}

*COSTOS:*
- Total Cotizado: $ ${String.format(Locale.US, "%,.2f", o?.total ?: 0.0)}

📱 *Por favor responda este mensaje con:*
✅ "ACEPTO" para aprobar la reparación
❌ "RECHAZO" en caso de no autorizar
                            """.trimIndent()
                            enviarWhatsAppGeneral(txt)
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        tamanioTexto = 14
                    )

                    // 2. AVISO DE RECEPCIÓN Y CHECK-IN
                    BotonModulo3D(
                        texto = "📷 2. ENVIAR ACTA DE RECEPCIÓN Y CHECK-IN",
                        colorClaro = Color(0xFFB9F6CA), colorMedio = Color(0xFF00C853), colorOscuro = Color(0xFF00695C),
                        colorTexto = Color.Black,
                        onClick = {
                            val txt = """
📋 *ACTA DE INGRESO Y RECEPCIÓN DE VEHÍCULO*
Cliente: $cNombre
Vehículo: $aNombre

• Registro de 8 ángulos fotográficos: OK
• Firma digital del cliente en pantalla: ACEPTADA
• Estado de recepción registrado con huella SHA-256 de protección.

Su vehículo ha ingresado a nuestras instalaciones con éxito.
                            """.trimIndent()
                            enviarWhatsAppGeneral(txt)
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        tamanioTexto = 14
                    )

                    // 3. AVISO DE AVANCE DE REPARACIÓN
                    BotonModulo3D(
                        texto = "🔧 3. ENVIAR AVANCE DE REPARACIÓN (0% - 100%)",
                        colorClaro = Color(0xFF90CAF9), colorMedio = Color(0xFF1976D2), colorOscuro = Color(0xFF0D47A1),
                        colorTexto = Color.Black,
                        onClick = {
                            val o = ordenSeleccionada
                            val pct = o?.porcentajeAvance ?: 50
                            val txt = """
🔧 *REPORTE DE AVANCE DE REPARACIÓN EN TIEMPO REAL*
Cliente: $cNombre
Vehículo: $aNombre

*Avance Actual del Servicio: $pct%*
• Estado: ${if (pct >= 100) "REPARACIÓN COMPLETADA" else "En proceso de servicio y montaje"}
• Fecha estimada de entrega: ${o?.fechaEntrega?.ifBlank { "Confirmada por taller" } ?: "Próximamente"}

Puede consultar el avance visual en tiempo real desde la App Cliente.
                            """.trimIndent()
                            enviarWhatsAppGeneral(txt)
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        tamanioTexto = 14
                    )

                    // 4. AVISO DE AUTO LISTO Y ESTÉTICA COMPLETADA
                    BotonModulo3D(
                        texto = "✨ 4. NOTIFICAR 'AUTO LISTO Y LAVADO'",
                        colorClaro = Color(0xFFB9F6CA), colorMedio = Color(0xFF00C853), colorOscuro = Color(0xFF00695C),
                        colorTexto = Color.Black,
                        onClick = {
                            val txt = """
✨ *¡SU VEHÍCULO ESTÁ LISTO PARA SU ENTREGA!* ✨
Cliente: $cNombre
Vehículo: $aNombre

Informamos que los trabajos de reparación, pruebas de manejo y lavado/estética han sido completados con éxito al 100%.

Puede pasar a recoger su vehículo a nuestras instalaciones. ¡Agradecemos su preferencia!
                            """.trimIndent()
                            enviarWhatsAppGeneral(txt)
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        tamanioTexto = 14
                    )

                    // 5. AVISO DE ALERTA DE MANTENIMIENTO PREVENTIVO
                    BotonModulo3D(
                        texto = "🔔 5. ENVIAR ALERTA DE MANTENIMIENTO PREVENTIVO",
                        colorClaro = Color(0xFFFFF59D), colorMedio = Color(0xFFFFEB3B), colorOscuro = Color(0xFFFBC02D),
                        colorTexto = Color.Black,
                        onClick = {
                            val txt = """
🔔 *RECORDATORIO DE MANTENIMIENTO PREVENTIVO*
Estimado $cNombre, le recordamos que se aproxima la fecha recomendada para el próximo servicio preventivo de $aNombre (Cambio de aceite, filtro, frenos y niveles).

Le sugerimos agendar su cita de ingreso para mantener su vehículo en óptimas condiciones.
                            """.trimIndent()
                            enviarWhatsAppGeneral(txt)
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        tamanioTexto = 14
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
}
