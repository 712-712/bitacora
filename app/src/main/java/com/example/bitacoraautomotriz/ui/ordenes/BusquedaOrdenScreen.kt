package com.example.bitacoraautomotriz.ui.ordenes

import android.widget.Toast
import androidx.compose.foundation.background
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

    var textoBusqueda by remember { mutableStateOf(value = "") }
    var ordenes by remember { mutableStateOf<List<OrdenServicio>>(value = emptyList()) }
    var todosLosClientes by remember { mutableStateOf<List<Cliente>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = false) }
    var mostrarDialogoEliminar by remember { mutableStateOf<OrdenServicio?>(value = null) }

    fun buscar() {
        cargando = true
        scope.launch {
            try {
                val resultado = if (textoBusqueda.isNotBlank()) {
                    OrdenServicioRepository.buscarOrdenesPorTexto("%${textoBusqueda.trim().uppercase()}%", context)
                } else {
                    OrdenServicioRepository.obtenerOrdenes(context)
                }
                ordenes = resultado
            } catch (_: Exception) {
                ordenes = emptyList()
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            todosLosClientes = ClienteRepository.obtenerClientes(context)
        } catch (_: Exception) {
            todosLosClientes = emptyList()
        }
        buscar()
    }

    fun formatearFolio(id: Int): String = String.format(Locale.US, "%05d", id)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Text(
            text = "COTIZACIONES EN PROCESO",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "BUSCAR POR CLIENTE, AUTO O PLACA",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.EtiquetaCampo,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it.uppercase() },
            textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
            placeholder = { Text("Escriba aquí...", fontSize = 18.sp, color = Color.Gray) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus(); buscar() }),
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

        BotonModulo3D(
            texto = "BUSCAR",
            icono = "🔍",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = { focusManager.clearFocus(); buscar() },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (ordenes.isEmpty() && textoBusqueda.isNotBlank()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("NO SE ENCONTRARON COTIZACIONES", color = Color(0xFFFF5252), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(ordenes) { orden ->
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

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                BotonModulo3D(
                                    texto = "ENVIAR REPORTE AL CLIENTE",
                                    colorClaro = Color(0xFFD7B899),
                                    colorMedio = Color(0xFF9B6B43),
                                    colorOscuro = Color(0xFF5D3A1A),
                                    onClick = { onEditarOrden(orden.id) },
                                    modifier = Modifier.fillMaxWidth().height(58.dp),
                                    tamanioTexto = 14,
                                    colorTexto = Color.Black
                                )

                                BotonModulo3D(
                                    texto = "ELIMINAR",
                                    colorClaro = Color(0xFFEF9A9A),
                                    colorMedio = Color(0xFFE53935),
                                    colorOscuro = Color(0xFFB71C1C),
                                    onClick = { mostrarDialogoEliminar = orden },
                                    modifier = Modifier.fillMaxWidth().height(58.dp),
                                    tamanioTexto = 16,
                                    colorTexto = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            colorTexto = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
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
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        mostrarDialogoEliminar = null
                        buscar()
                    }
                }) { Text("SÍ, ELIMINAR", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = null }) { Text("CANCELAR") }
            }
        )
    }
}
