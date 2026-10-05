package com.example.bitacoraautomotriz.ui.clientes

import android.content.Intent
import android.net.Uri
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
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun VerReporteClienteScreen(
    ordenId: Int = 0,
    onRegresar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var orden by remember { mutableStateOf<OrdenServicio?>(null) }
    var telefonoCliente by remember { mutableStateOf("") }
    var clienteId by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(ordenId) {
        scope.launch {
            try {
                val lista = OrdenServicioRepository.obtenerOrdenes(context)
                val ordenEncontrada = if (ordenId > 0) {
                    lista.find { it.id == ordenId } ?: lista.lastOrNull()
                } else {
                    lista.lastOrNull()
                }

                orden = ordenEncontrada
                ordenEncontrada?.let { o ->
                    // ESCUCHAR CAMBIOS EN TIEMPO REAL DESDE FIREBASE
                    FirebaseSyncManager.escucharOrdenEnTiempoReal(o.id) { ordenDescargada ->
                        orden = ordenDescargada
                    }

                    try {
                        val clientes = ClienteRepository.obtenerClientes(context)
                        val c = clientes.find { it.nombre.trim().equals(o.cliente.trim(), ignoreCase = true) }
                        telefonoCliente = c?.telefono ?: "No disponible"
                        clienteId = c?.id ?: 0
                    } catch (_: Exception) {
                        telefonoCliente = "No disponible"
                        clienteId = 0
                    }
                }
            } catch (_: Exception) {
                orden = null
            } finally {
                cargando = false
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
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "REPORTE DE SERVICIO AL CLIENTE",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(20.dp))

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
                    Text(text = "NO TIENE REPORTES DE SERVICIO ACTIVOS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                val o = orden!!
                val colorEstado = when (o.estado.uppercase()) {
                    "ACEPTADO", "TERMINADO", "ENTREGADO" -> Color(0xFF7DFFB2)
                    "RECHAZADO" -> Color(0xFFFF5252)
                    else -> Color(0xFF90CAF9)
                }

                // TARJETA COMPLETA CON EL REPORTE DEL CLIENTE (SIN BOTONES ADMINISTRATIVOS)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "FOLIO: ${String.format(Locale.US, "%05d", o.id)}   |   ID: $clienteId",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DFFB2)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = o.cliente.uppercase(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = o.auto.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (telefonoCliente.isNotBlank() && telefonoCliente != "No disponible") {
                            Text(text = "TELÉFONO REGISTRADO:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(
                                text = "📞 $telefonoCliente",
                                fontSize = 18.sp,
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
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Text(text = "FECHA DE COTIZACIÓN: ${o.fecha}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF004D33))

                        Text(text = "FALLA REPORTADA:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = o.fallaReportada, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "DIAGNÓSTICO:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = o.diagnostico, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "TRABAJO POR REALIZAR:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = o.trabajoRealizado, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF004D33))

                        Text(text = "COSTOS DEL SERVICIO:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Mano de Obra:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", o.costoManoObra), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Refacciones:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", o.costoRefacciones), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "I.V.A. (16%):", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", o.iva), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "TOTAL:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", o.total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF004D33))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "ESTADO DE LA COTIZACIÓN:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = o.estado.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorEstado)
                        }

                        if (o.fechaEntrega.isNotBlank()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "FECHA DE ENTREGA:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = o.fechaEntrega, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
