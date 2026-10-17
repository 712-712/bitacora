package com.example.bitacoraautomotriz.ui.ordenes

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextAlign
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
            val listaLocal = RecepcionRepository.obtenerRecepciones(context)
            if (listaLocal.isNotEmpty()) {
                recepciones = listaLocal.sortedByDescending { it.id }
                cargando = false
            }
        } catch (_: Exception) {}

        try {
            FirebaseSyncManager.escucharRecepcionesEnTiempoReal { recepcionesFirebase ->
                if (recepcionesFirebase.isNotEmpty()) {
                    recepciones = recepcionesFirebase.sortedByDescending { it.id }
                }
                cargando = false
            }
        } catch (_: Exception) {
            cargando = false
        }
    }

    val query = busqueda.uppercase().trim()
    val recepcionesFiltradas = remember(recepciones, query) {
        if (query.isBlank()) {
            recepciones
        } else {
            recepciones.filter { r ->
                r.cliente.uppercase().contains(query, ignoreCase = true) ||
                r.placa.uppercase().contains(query, ignoreCase = true) ||
                r.auto.uppercase().contains(query, ignoreCase = true) ||
                r.vin.uppercase().contains(query, ignoreCase = true) ||
                r.id.toString().contains(query, ignoreCase = true)
            }
        }
    }

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
                text = "ACTAS DE RECEPCIONES E\nINSPECCIONES VEHICULARES",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Consulta de actas de recepción y marcas de agua legales",
                fontSize = 14.sp,
                color = Colores.EtiquetaCampo,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CUADRO DE BÚSQUEDA INTEGRADO EN TIEMPO REAL CON BOTÓN LIMPIAR
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = {
                    Text(
                        text = "BUSCAR (CLIENTE, PLACA O FOLIO...)",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar Recepción",
                        tint = Color.White
                    )
                },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { busqueda = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar Búsqueda",
                                tint = Color.White
                            )
                        }
                    }
                },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Search
                ),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                                    text = "📋 ACTA DE RECEPCIÓN Y PROTECCIÓN LEGAL N° ${String.format(Locale.US, "%05d", r.id)}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7DFFB2)
                                )
                                Text(text = "CLIENTE: ${r.cliente.uppercase()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "VEHÍCULO: ${r.auto.uppercase()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = "PLACA: ${r.placa.uppercase()}   |   VIN: ${r.vin.uppercase().ifBlank { "N/A" }}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7DFFB2))
                                Text(text = "FECHA / HORA: ${r.fechaHora}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "KILOMETRAJE: ${r.kilometraje} km", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)

                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF004D33))

                                Text(text = "• Fotos de Ángulos: REGISTRADAS CON MARCA DE AGUA EXIF", fontSize = 13.sp, color = Color.White)
                                Text(text = "• Firma Digital Táctil: FIRMADA Y ACEPTADA EN PANTALLA", fontSize = 13.sp, color = Color(0xFF7DFFB2))
                                Text(text = "• Huella Criptográfica SHA-256: ${r.hashIntegridadSha256}", fontSize = 12.sp, color = Color.LightGray)
                                Text(text = "• Periodo de Retención Expiración: ${r.tiempoRetencion}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
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
