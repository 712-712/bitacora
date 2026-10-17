package com.example.bitacoraautomotriz.ui.ordenes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun BuscarOrdenScreen(
    onRegresar: () -> Unit,
    onEditarOrden: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var textoBusqueda by remember { mutableStateOf("") }
    var ordenes by remember { mutableStateOf<List<OrdenServicio>>(emptyList()) }
    var todosLosClientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var mostrarDialogoEliminar by remember { mutableStateOf<OrdenServicio?>(null) }

    LaunchedEffect(refreshTrigger) {
        cargando = true
        try {
            todosLosClientes = ClienteRepository.obtenerClientes(context)
            ordenes = OrdenServicioRepository.obtenerOrdenes(context).sortedByDescending { it.id }
        } catch (_: Exception) {
            ordenes = emptyList()
        } finally {
            cargando = false
        }
    }

    val ordenesFiltradas by remember(ordenes, textoBusqueda) {
        derivedStateOf {
            if (textoBusqueda.isBlank()) {
                ordenes
            } else {
                val qRaw = textoBusqueda.trim().lowercase(Locale.getDefault())
                val digitsOnly = textoBusqueda.replace(Regex("[^0-9]"), "")

                ordenes.filter { o ->
                    val idStr = o.id.toString()
                    val idFormatted = String.format(Locale.US, "%05d", o.id)

                    val matchId = (digitsOnly.isNotBlank() && idStr.contains(digitsOnly)) ||
                                  (digitsOnly.isNotBlank() && idFormatted.contains(digitsOnly)) ||
                                  idStr.contains(qRaw) ||
                                  idFormatted.contains(qRaw)

                    val matchTexto = o.cliente.lowercase(Locale.getDefault()).contains(qRaw) ||
                                     o.auto.lowercase(Locale.getDefault()).contains(qRaw) ||
                                     o.fallaReportada.lowercase(Locale.getDefault()).contains(qRaw) ||
                                     o.diagnostico.lowercase(Locale.getDefault()).contains(qRaw) ||
                                     o.estado.lowercase(Locale.getDefault()).contains(qRaw)

                    matchId || matchTexto
                }
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

    fun formatearFolio(id: Int): String = String.format(Locale.US, "%05d", id)

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
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "COTIZACIONES EN PROCESO",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CUADRO DE BÚSQUEDA INTEGRADO EN TIEMPO REAL
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = {
                    Text(
                        text = "BUSCAR COTIZACIÓN (FOLIO, CLIENTE, PLACA...)",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar Cotización",
                        tint = Color.White
                    )
                },
                trailingIcon = {
                    if (textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { textoBusqueda = "" }) {
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
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (cargando) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (ordenesFiltradas.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (textoBusqueda.isBlank()) "NO HAY COTIZACIONES REGISTRADAS" else "NO SE ENCONTRARON COTIZACIONES PARA \"$textoBusqueda\"",
                        color = Color(0xFFFF5252),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(ordenesFiltradas) { orden ->
                        val clienteId = todosLosClientes.find { it.nombre.orEmpty().trim().equals(orden.cliente.orEmpty().trim(), ignoreCase = true) }?.id ?: 0
                        val manoObra = if (orden.costoManoObra.isNaN() || orden.costoManoObra < 0) 0.0 else orden.costoManoObra
                        val refacciones = if (orden.costoRefacciones.isNaN() || orden.costoRefacciones < 0) 0.0 else orden.costoRefacciones
                        val iva = if (orden.iva.isNaN() || orden.iva < 0) 0.0 else orden.iva
                        val total = if (orden.total.isNaN() || orden.total < 0) 0.0 else orden.total

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "FOLIO: ${formatearFolio(orden.id)}   |   ID: $clienteId",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7DFFB2),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = orden.estado.orEmpty().uppercase(),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (orden.estado) {
                                            "TERMINADO", "ENTREGADO", "ACEPTADO" -> Color(0xFF7DFFB2)
                                            "RECHAZADO" -> Color(0xFFFF5252)
                                            else -> Color(0xFF90CAF9)
                                        },
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = orden.cliente.orEmpty().uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(text = orden.auto.orEmpty().uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                    Column {
                                        Text(text = "FECHA DE COTIZACIÓN", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = "📅 ${orden.fecha.orEmpty()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

                                        if (orden.fechaEntrega.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "📅 ${orden.fechaEntrega.orEmpty()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0xFF004D33))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(text = "Mano de Obra:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text(text = String.format(Locale.US, "$ %,.2f", manoObra), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(text = "Refacciones:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text(text = String.format(Locale.US, "$ %,.2f", refacciones), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(text = "I.V.A. 16%", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text(text = String.format(Locale.US, "$ %,.2f", iva), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "TOTAL", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text(text = String.format(Locale.US, "$ %,.2f", total), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    BotonModulo3D(
                                        texto = "🔧 DETALLE Y SEGUIMIENTO",
                                        colorClaro = Color(0xFFD7B899),
                                        colorMedio = Color(0xFF9B6B43),
                                        colorOscuro = Color(0xFF5D3A1A),
                                        onClick = { onEditarOrden(orden.id) },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        tamanioTexto = 15,
                                        colorTexto = Color.Black
                                    )

                                    BotonModulo3D(
                                        texto = "ELIMINAR",
                                        colorClaro = Color(0xFFEF9A9A),
                                        colorMedio = Color(0xFFE53935),
                                        colorOscuro = Color(0xFFB71C1C),
                                        onClick = { mostrarDialogoEliminar = orden },
                                        modifier = Modifier.fillMaxWidth().height(52.dp),
                                        tamanioTexto = 16,
                                        colorTexto = Color.Black
                                    )
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
                    onClick = onRegresar,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    tamanioTexto = 16,
                    colorTexto = Color.White
                )
            }
        }
    }

    mostrarDialogoEliminar?.let { orden ->
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = null },
            title = { Text("¿Eliminar cotización?") },
            text = { Text("Folio: ${formatearFolio(orden.id)}\nCliente: ${orden.cliente}\n\nEsta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            OrdenServicioRepository.eliminarOrden(orden, context)
                            Toast.makeText(context, "Cotización eliminada", Toast.LENGTH_SHORT).show()
                            refreshTrigger++
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        mostrarDialogoEliminar = null
                    }
                }) { Text("SÍ, ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = null }) { Text("CANCELAR") }
            }
        )
    }
}
