package com.example.bitacoraautomotriz.ui.ordenes

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.RecepcionVehiculo
import com.example.bitacoraautomotriz.repository.RecepcionRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun VerRecepcionesScreen(
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var recepciones by remember { mutableStateOf<List<RecepcionVehiculo>>(emptyList()) }
    var busqueda by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(refreshTrigger) {
        try {
            recepciones = RecepcionRepository.obtenerRecepciones(context)
        } catch (_: Exception) {
            recepciones = emptyList()
        } finally {
            cargando = false
        }
    }

    val query = busqueda.uppercase().trim()
    val recepcionesFiltradas = recepciones.filter { r ->
        r.cliente.uppercase().contains(query, ignoreCase = true) ||
                r.placa.uppercase().contains(query, ignoreCase = true) ||
                r.auto.uppercase().contains(query, ignoreCase = true) ||
                r.vin.uppercase().contains(query, ignoreCase = true) ||
                r.id.toString().contains(query, ignoreCase = true)
    }

    fun reenviarWhatsApp(r: RecepcionVehiculo) {
        val textoMensaje = """
📋 *ACTA DE INGRESO Y RECEPCIÓN DE VEHÍCULO*
Folio: ${String.format(Locale.US, "%05d", r.id)}
Fecha / Hora: ${r.fechaHora}

────────────────────
Cliente: ${r.cliente}
Vehículo: ${r.auto}
Placa: ${r.placa}   |   VIN: ${r.vin.ifBlank { "N/A" }}
Kilometraje: ${r.kilometraje} km

*Estado de Evidencia Legal:*
• Evidencia fotográfica de ángulos: OK con Marca de Agua
• Video de inspección: ${if (r.videoPath.isNotBlank()) "REGISTRADO CON SELLO LEGAL Y METADATOS EXIF" else "N/A"}
• Firma digital en pantalla: ${if (r.firmaPath.isNotBlank()) "ACEPTADA" else "N/A"}
• Huella SHA-256: ${r.hashIntegridadSha256}
• Retención seleccionada: ${r.tiempoRetencion}

Agradecemos su confianza.
────────────────────
        """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/?text=${Uri.encode(textoMensaje)}")
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
                text = "HISTORIAL DE RECEPCIONES",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Actas de ingreso, evidencia fotográfica y firmas digitales",
                fontSize = 15.sp,
                color = Colores.EtiquetaCampo,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // BUSCADOR DE RECEPCIONES
            Text(
                text = "BUSCAR POR CLIENTE, PLACA, AUTO O FOLIO:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp)
            )

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it.uppercase() },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                placeholder = { Text("Buscar...", color = Color.Gray) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Colores.FondoSecundario,
                    unfocusedContainerColor = Colores.FondoSecundario,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Colores.BordeBoton,
                    unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (recepcionesFiltradas.isEmpty()) {
                Text(
                    text = if (recepciones.isEmpty()) "NO HAY RECEPCIONES REGISTRADAS" else "NO SE ENCONTRARON REGISTROS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TextoGlobal
                )
            } else {
                recepcionesFiltradas.forEach { r ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ACTA DE RECEPCIÓN N° ${String.format(Locale.US, "%05d", r.id)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7DFFB2)
                            )
                            Text(text = "CLIENTE: ${r.cliente}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "VEHÍCULO: ${r.auto}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "PLACA: ${r.placa}   |   VIN: ${r.vin.ifBlank { "N/A" }}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7DFFB2))
                            Text(text = "KILOMETRAJE: ${r.kilometraje} km", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "FECHA / HORA: ${r.fechaHora}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF004D33))

                            Text(
                                text = "• Fotos de Ángulos: REGISTRADAS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (r.videoPath.isNotBlank()) {
                                Text(
                                    text = "• Video de Inspección: REGISTRADO",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7DFFB2)
                                )
                            }
                            if (r.firmaPath.isNotBlank()) {
                                Text(
                                    text = "• Firma Digital del Cliente: ACEPTADA",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7DFFB2)
                                )
                            }

                            Text(
                                text = "• Creador Hash SHA-256: ${r.hashIntegridadSha256.take(20)}...",
                                fontSize = 13.sp,
                                color = Color.LightGray
                            )
                            Text(
                                text = "• Tiempo de Retención: ${r.tiempoRetencion}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Yellow
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BotonModulo3D(
                                    texto = "📱 WHATSAPP",
                                    colorClaro = Color(0xFFD7B899),
                                    colorMedio = Color(0xFF9B6B43),
                                    colorOscuro = Color(0xFF5D3A1A),
                                    onClick = { reenviarWhatsApp(r) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    tamanioTexto = 14,
                                    colorTexto = Color.Black
                                )

                                BotonModulo3D(
                                    texto = "ELIMINAR",
                                    colorClaro = Color(0xFFEF9A9A),
                                    colorMedio = Color(0xFFE53935),
                                    colorOscuro = Color(0xFFB71C1C),
                                    onClick = {
                                        AlertDialog.Builder(context).apply {
                                            setTitle("⚠️ ELIMINAR ACTA DE RECEPCIÓN")
                                            setMessage("¿Está seguro de eliminar el acta N° ${r.id} del cliente ${r.cliente}?")
                                            setPositiveButton("ELIMINAR") { _, _ ->
                                                scope.launch {
                                                    try {
                                                        RecepcionRepository.eliminarRecepcion(r, context)
                                                        refreshTrigger++
                                                        Toast.makeText(context, "Acta eliminada correctamente", Toast.LENGTH_SHORT).show()
                                                    } catch (_: Exception) {}
                                                }
                                            }
                                            setNegativeButton("CANCELAR", null)
                                            show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    tamanioTexto = 14,
                                    colorTexto = Color.Black
                                )
                            }
                        }
                    }
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
}
