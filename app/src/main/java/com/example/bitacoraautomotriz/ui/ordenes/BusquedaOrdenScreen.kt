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
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
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
    var cargando by remember { mutableStateOf(false) }
    var mostrarDialogoEliminar by remember { mutableStateOf<OrdenServicio?>(null) }

    fun buscar() {
        cargando = true
        scope.launch {
            val resultado = if (textoBusqueda.isNotBlank()) {
                OrdenServicioRepository.buscarOrdenesPorTexto("%${textoBusqueda.uppercase()}%")
            } else {
                OrdenServicioRepository.obtenerOrdenes()
            }
            ordenes = resultado
            cargando = false
        }
    }

    LaunchedEffect(Unit) { buscar() }

    fun formatearFolio(id: Int): String = String.format("%05d", id)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B44))
            .padding(24.dp)
    ) {
        Text(
            text = "COTIZACIÓN DE SERVICIO",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "BUSCAR POR CLIENTE, AUTO O PLACA",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90CAF9),
                softWrap = false,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it.uppercase() },
            textStyle = TextStyle(color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold),
            placeholder = { Text("Escriba aquí...", fontSize = 18.sp, color = Color.Gray) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus(); buscar() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black, unfocusedTextColor = Color.Black,
                focusedIndicatorColor = Color(0xFF00AEEF), unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth().height(70.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(
            texto = "BUSCAR",
            colorClaro = Color(0xFF7DFFB2), colorMedio = Color(0xFF00D96B), colorOscuro = Color(0xFF008844),
            onClick = { focusManager.clearFocus(); buscar() }
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("Buscando...", color = Color.White, fontSize = 18.sp)
            }
        } else if (ordenes.isEmpty() && textoBusqueda.isNotBlank()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("NO SE ENCONTRARON COTIZACIONES", color = Color(0xFFFF5252), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(ordenes) { orden ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(text = "FOLIO: ${formatearFolio(orden.id)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = orden.cliente, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B44))
                            Text(text = orden.auto, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17202A))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column {
                                    Text(text = "FECHA DE COTIZACIÓN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "📅 ${orden.fecha}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                                    if (orden.fechaEntrega.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "📅 ${orden.fechaEntrega}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }
                                Text(
                                    text = orden.estado,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (orden.estado == "TERMINADO" || orden.estado == "ENTREGADO") Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE0E0E0))

                            // ✅ SECCIÓN DE COSTOS CORREGIDA (Todas las llaves en su lugar)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(text = "Mano de Obra:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF607D8B))
                                    Text(text = "$ ${String.format(Locale.US, "%.2f", orden.costoManoObra)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17202A))
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(text = "Refacciones:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF607D8B))
                                    Text(text = "$ ${String.format(Locale.US, "%.2f", orden.costoRefacciones)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17202A))
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(text = "I.V.A. 16%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF607D8B))
                                    Text(text = "$ ${String.format(Locale.US, "%.2f", orden.iva)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF17202A))
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "TOTAL", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001B44))
                                    Text(text = "$ ${String.format(Locale.US, "%.2f", orden.total)}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                BotonModulo3D(
                                    texto = "VER COTIZACIÓN",
                                    colorClaro = Color(0xFFD7B899),
                                    colorMedio = Color(0xFF9B6B43),
                                    colorOscuro = Color(0xFF5D3A1A),
                                    onClick = { onEditarOrden(orden.id) },
                                    modifier = Modifier.fillMaxWidth().height(60.dp)
                                )

                                BotonModulo3D(
                                    texto = "ELIMINAR",
                                    colorClaro = Color(0xFFEF9A9A),
                                    colorMedio = Color(0xFFE53935),
                                    colorOscuro = Color(0xFFB71C1C),
                                    onClick = { mostrarDialogoEliminar = orden },
                                    modifier = Modifier.fillMaxWidth().height(60.dp)
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
            colorClaro = Color(0xFFD5E1E6), colorMedio = Color(0xFF90A4AE), colorOscuro = Color(0xFF455A64),
            onClick = onRegresar
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
                        OrdenServicioRepository.eliminarOrden(orden)
                        Toast.makeText(context, "Cotización eliminada", Toast.LENGTH_SHORT).show()
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
