package com.example.bitacoraautomotriz.ui.clientes

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.data.RecepcionVehiculo
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.repository.RecepcionRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import com.example.bitacoraautomotriz.utils.AudioUtils
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun VerReporteClienteScreen(
    ordenId: Int = 0,
    onRegresar: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var todasLasOrdenesFirebase by remember { mutableStateOf<List<OrdenServicio>>(emptyList()) }
    var orden by remember { mutableStateOf<OrdenServicio?>(null) }
    var autoDetalle by remember { mutableStateOf<Auto?>(null) }
    var recepcionDetalle by remember { mutableStateOf<RecepcionVehiculo?>(null) }
    var telefonoCliente by remember { mutableStateOf("") }
    var clienteId by remember { mutableStateOf(0) }
    var cargando by remember { mutableStateOf(true) }

    var estadoSeleccionado by remember { mutableStateOf("EN ESPERA") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                // ESCUCHAR DIRECTA Y PRIORITARIAMENTE DESDE FIREBASE REALTIME DATABASE EN TIEMPO REAL
                FirebaseSyncManager.escucharTodasLasOrdenesEnTiempoReal { ordenesFirebase ->
                    todasLasOrdenesFirebase = ordenesFirebase
                    if (ordenesFirebase.isNotEmpty()) {
                        val seleccionada = if (ordenId > 0) {
                            ordenesFirebase.find { it.id == ordenId } ?: ordenesFirebase.last()
                        } else {
                            ordenesFirebase.last()
                        }

                        orden = seleccionada
                        estadoSeleccionado = seleccionada.estado
                        cargando = false

                        try {
                            AudioUtils.reproducirSonidoMotorTresVeces(context)
                        } catch (_: Exception) {}

                        scope.launch {
                            try {
                                val clientes = ClienteRepository.obtenerClientes(context)
                                val c = clientes.find { it.nombre.trim().equals(seleccionada.cliente.trim(), ignoreCase = true) }
                                telefonoCliente = c?.telefono ?: "No disponible"
                                clienteId = c?.id ?: 0
                            } catch (_: Exception) {
                                telefonoCliente = "No disponible"
                                clienteId = 0
                            }

                            try {
                                val partes = seleccionada.auto.split("-")
                                val placaStr = partes.lastOrNull()?.trim() ?: ""
                                if (placaStr.isNotBlank()) {
                                    autoDetalle = AutoRepository.obtenerAutoPorPlaca(placaStr, context)
                                }
                            } catch (_: Exception) {
                                autoDetalle = null
                            }

                            try {
                                val recepciones = RecepcionRepository.obtenerRecepciones(context)
                                recepcionDetalle = recepciones.find { r -> r.cliente.equals(seleccionada.cliente, ignoreCase = true) } ?: recepciones.lastOrNull()
                            } catch (_: Exception) {
                                recepcionDetalle = null
                            }
                        }
                    } else {
                        // RESPALDO LOCAL ROOM
                        scope.launch {
                            try {
                                val listaLocal = OrdenServicioRepository.obtenerOrdenes(context)
                                if (listaLocal.isNotEmpty()) {
                                    val ultimaLocal = listaLocal.last()
                                    orden = ultimaLocal
                                    estadoSeleccionado = ultimaLocal.estado
                                }
                            } catch (_: Exception) {} finally {
                                cargando = false
                            }
                        }
                    }
                }
            } catch (_: Exception) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // SELECTOR DE COTIZACIONES SI EXISTEN VARIAS ÓRDENES EN FIREBASE
            if (todasLasOrdenesFirebase.size > 1) {
                Text(
                    text = "SELECCIONE SU VEHÍCULO O COTIZACIÓN:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    todasLasOrdenesFirebase.take(4).forEach { oItem ->
                        val esSeleccionada = orden?.id == oItem.id
                        Button(
                            onClick = {
                                orden = oItem
                                estadoSeleccionado = oItem.estado
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (esSeleccionada) Color(0xFF00C853) else Colores.FondoSecundario
                            )
                        ) {
                            Text(
                                text = "🚗 FOLIO #${oItem.id}: ${oItem.cliente} (${oItem.auto})",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

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

                // TARJETA COMPLETA CON EL REPORTE DEL CLIENTE Y NUM VIN + PLACAS
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

                        // PLACA Y VIN DEL VEHÍCULO
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "PLACA: ${autoDetalle?.placa?.uppercase() ?: "N/A"}   |   VIN: ${autoDetalle?.vin?.uppercase()?.ifBlank { "N/A" } ?: "N/A"}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7DFFB2)
                        )

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
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // DATOS INFORMATIVOS EN TEXTO PLANO DE SÓLO LECTURA PARA EL CLIENTE (BLOQUEADO CUALQUIER CAMBIO)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoSecundario),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "INFORMACIÓN DEL TALLER Y ESTIMACIÓN",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.TituloPrincipal
                        )

                        Text(text = "NOMBRE DEL TALLER / MECÁNICO:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = "TALLER MECÁNICO AUTORIZADO", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "DÍAS MÍNIMOS:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "1 DÍA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text(text = "DÍAS MÁXIMOS:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "3 DÍAS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // TARJETA COMPLEMENTARIA DE RECEPCIÓN Y PROTECCIÓN LEGAL DE INGRESO
                if (recepcionDetalle != null) {
                    val r = recepcionDetalle!!
                    Spacer(modifier = Modifier.height(20.dp))

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
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "📋 ACTA DE RECEPCIÓN Y PROTECCIÓN LEGAL N° ${String.format(Locale.US, "%05d", r.id)}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7DFFB2)
                            )
                            Text(text = "FECHA DE INGRESO: ${r.fechaHora}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = "KILOMETRAJE REGISTRADO: ${r.kilometraje} km", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF004D33))

                            Text(text = "• Fotos de Ángulos: REGISTRADAS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "• Firma Digital del Cliente: ACEPTADA EN PANTALLA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7DFFB2))
                            Text(text = "• Protegido por Huella SHA-256: ${r.hashIntegridadSha256.take(18)}...", fontSize = 13.sp, color = Color.LightGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ESTADO DE LA COTIZACIÓN:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // BOTONES DE RESPUESTA DEL CLIENTE (ACEPTADO O RECHAZADO)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. RECHAZADO
                    BotonModulo3D(
                        texto = "RECHAZADO",
                        colorClaro = if (estadoSeleccionado == "RECHAZADO") Color(0xFFEF9A9A) else Color(0xFF64B5F6),
                        colorMedio = if (estadoSeleccionado == "RECHAZADO") Color(0xFFE53935) else Color(0xFF1976D2),
                        colorOscuro = if (estadoSeleccionado == "RECHAZADO") Color(0xFFB71C1C) else Color(0xFF0D47A1),
                        onClick = {
                            estadoSeleccionado = "RECHAZADO"
                            scope.launch {
                                try {
                                    val ordenActualizada = o.copy(estado = "RECHAZADO")
                                    OrdenServicioRepository.actualizarEstadoYAvance(o.id, "RECHAZADO", 0, "", context)
                                    FirebaseSyncManager.subirOrdenAFirebase(ordenActualizada)
                                    Toast.makeText(context, "❌ Cotización marcada como RECHAZADA", Toast.LENGTH_SHORT).show()
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.weight(1f).height(58.dp),
                        tamanioTexto = 15,
                        colorTexto = Color.Black
                    )

                    // 2. ACEPTADO
                    BotonModulo3D(
                        texto = "ACEPTADO",
                        colorClaro = if (estadoSeleccionado == "ACEPTADO") Color(0xFF7DFFB2) else Color(0xFF64B5F6),
                        colorMedio = if (estadoSeleccionado == "ACEPTADO") Color(0xFF00D96B) else Color(0xFF1976D2),
                        colorOscuro = if (estadoSeleccionado == "ACEPTADO") Color(0xFF008844) else Color(0xFF0D47A1),
                        onClick = {
                            estadoSeleccionado = "ACEPTADO"
                            scope.launch {
                                try {
                                    val ordenActualizada = o.copy(estado = "ACEPTADO", porcentajeAvance = 100)
                                    OrdenServicioRepository.actualizarEstadoYAvance(o.id, "ACEPTADO", 100, "", context)
                                    FirebaseSyncManager.subirOrdenAFirebase(ordenActualizada)
                                    Toast.makeText(context, "✅ Cotización marcada como ACEPTADA", Toast.LENGTH_SHORT).show()
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier.weight(1f).height(58.dp),
                        tamanioTexto = 15,
                        colorTexto = Color.Black
                    )
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
