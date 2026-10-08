package com.example.bitacoraautomotriz.ui.facturacion

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Factura
import com.example.bitacoraautomotriz.repository.FacturaRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import java.util.Locale

@Composable
fun BusquedaFacturaScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var textoBusqueda by remember { mutableStateOf(value = "") }
    var todasLasFacturas by remember { mutableStateOf<List<Factura>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = true) }

    LaunchedEffect(Unit) {
        try {
            todasLasFacturas = FacturaRepository.obtenerFacturas(context)
        } catch (_: Exception) {
            todasLasFacturas = emptyList()
        } finally {
            cargando = false
        }
    }

    val resultados = if (textoBusqueda.isBlank()) {
        todasLasFacturas
    } else {
        val query = textoBusqueda.trim()
        todasLasFacturas.filter { factura ->
            factura.numero.contains(query, ignoreCase = true) ||
                    factura.cliente.contains(query, ignoreCase = true)
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
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "BUSCAR FACTURA",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "NÚMERO DE FOLIO O CLIENTE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                placeholder = { Text("Escriba para buscar...", fontSize = 18.sp, color = Colores.EtiquetaCampo.copy(alpha = 0.6f)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus(); keyboardController?.hide() },
                    onDone = { focusManager.clearFocus(); keyboardController?.hide() }
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Colores.FondoSecundario,
                    unfocusedContainerColor = Colores.FondoSecundario,
                    disabledContainerColor = Colores.FondoSecundario,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Colores.BordeBoton,
                    unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(70.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (resultados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (todasLasFacturas.isEmpty())
                            "NO HAY FACTURAS REGISTRADAS"
                        else
                            "NO SE ENCONTRARON COINCIDENCIAS",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(resultados) { factura ->
                        val subtotal = if (factura.subtotal.isNaN() || factura.subtotal < 0) 0.0 else factura.subtotal
                        val iva = if (factura.iva.isNaN() || factura.iva < 0) 0.0 else factura.iva
                        val total = if (factura.total.isNaN() || factura.total < 0) 0.0 else factura.total

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
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "FOLIO: # ${factura.numero.ifBlank { "SIN NÚMERO" }}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Text(
                                    text = "CLIENTE: ${factura.cliente.ifBlank { "SIN CLIENTE" }}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Text(
                                    text = "FECHA: ${factura.fecha.ifBlank { "N/A" }}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF004D33))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "SUBTOTAL:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = String.format(Locale.US, "$ %,.2f", subtotal), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "I.V.A. (16%):", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = String.format(Locale.US, "$ %,.2f", iva), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "TOTAL:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = String.format(Locale.US, "$ %,.2f", total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                                }
                            }
                        }
                    }
                }
            }
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
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onRegresar()
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    tamanioTexto = 16,
                    colorTexto = Color.White
                )
            }
        }
    }
}
