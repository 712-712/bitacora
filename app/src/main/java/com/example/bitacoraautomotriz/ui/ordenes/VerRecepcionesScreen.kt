package com.example.bitacoraautomotriz.ui.ordenes

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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.RecepcionVehiculo
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.repository.RecepcionRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import java.util.Locale

@Composable
fun VerRecepcionesScreen(
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var busqueda by remember { mutableStateOf("") }
    var recepciones by remember { mutableStateOf<List<RecepcionVehiculo>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
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

    fun retransmitirFirebase(r: RecepcionVehiculo) {
        val mapRecepcion = mapOf(
            "id" to r.id,
            "cliente" to r.cliente,
            "auto" to r.auto,
            "placa" to r.placa,
            "kilometraje" to r.kilometraje,
            "fechaHora" to r.fechaHora,
            "hashIntegridadSha256" to r.hashIntegridadSha256,
            "tiempoRetencion" to r.tiempoRetencion
        )
        FirebaseSyncManager.publicarInformeTallerConPush(r.cliente, mapRecepcion)
        Toast.makeText(context, "⚡ Acta N° ${r.id} transmitida en tiempo real a Firebase", Toast.LENGTH_SHORT).show()
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
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consulta de actas de recepción y marcas de agua legales",
                fontSize = 14.sp,
                color = Colores.EtiquetaCampo
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it.uppercase() },
                placeholder = { Text("Buscar por Cliente, Placa o Folio...", color = Color.Gray) },
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (recepcionesFiltradas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (recepciones.isEmpty()) "NO HAY RECEPCIONES REGISTRADAS" else "NO SE ENCONTRARON COINCIDENCIAS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    recepcionesFiltradas.forEach { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "📋 ACTA N° ${String.format(Locale.US, "%05d", r.id)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7DFFB2)
                                )
                                Text(text = "CLIENTE: ${r.cliente}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "VEHÍCULO: ${r.auto}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "PLACA: ${r.placa}   |   VIN: ${r.vin.ifBlank { "N/A" }}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7DFFB2))
                                Text(text = "FECHA / HORA: ${r.fechaHora}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "KILOMETRAJE: ${r.kilometraje} km", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)

                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF004D33))

                                Text(text = "• Fotos de Ángulos: REGISTRADAS CON MARCA DE AGUA EXIF", fontSize = 13.sp, color = Color.White)
                                Text(text = "• Firma Digital Táctil: FIRMADA Y ACEPTADA EN PANTALLA", fontSize = 13.sp, color = Color(0xFF7DFFB2))
                                Text(text = "• Huella Criptográfica SHA-256: ${r.hashIntegridadSha256}", fontSize = 12.sp, color = Color.LightGray)
                                Text(text = "• Periodo de Retención Expiración: ${r.tiempoRetencion}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)

                                Spacer(modifier = Modifier.height(8.dp))

                                BotonModulo3D(
                                    texto = "⚡ TRANSMITIR A FIREBASE",
                                    colorClaro = Color(0xFFB9F6CA), colorMedio = Color(0xFF00C853), colorOscuro = Color(0xFF00695C),
                                    colorTexto = Color.Black,
                                    onClick = { retransmitirFirebase(r) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    tamanioTexto = 14
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // BOTÓN REGRESAR FIJO E INMÓVIL AL FONDO
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = Colores.FondoPantalla
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
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
