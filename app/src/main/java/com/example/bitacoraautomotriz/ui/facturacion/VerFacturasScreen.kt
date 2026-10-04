package com.example.bitacoraautomotriz.ui.facturacion

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Factura
import com.example.bitacoraautomotriz.repository.FacturaRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun VerFacturasScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    var facturas by remember { mutableStateOf<List<Factura>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = true) }

    LaunchedEffect(Unit) {
        try {
            facturas = FacturaRepository.obtenerFacturas(context)
        } catch (_: Exception) {
            facturas = emptyList()
        } finally {
            cargando = false
        }
    }

    val calHoy = remember { Calendar.getInstance() }
    val mesActual = calHoy.get(Calendar.MONTH)
    val anioActual = calHoy.get(Calendar.YEAR)
    val diasEnMes = calHoy.getActualMaximum(Calendar.DAY_OF_MONTH)
    val nombreMes = remember { SimpleDateFormat("MMMM", Locale.forLanguageTag("es-ES")).format(calHoy.time) }

    val facturasDelMes = facturas.filter { f ->
        try {
            val partes = f.fecha.split("/")
            if (partes.size == 3) {
                val m = partes[1].toIntOrNull()
                val y = partes[2].toIntOrNull()
                m == (mesActual + 1) && y == anioActual
            } else false
        } catch (_: Exception) { false }
    }

    val totalFacturadoMes = facturasDelMes.sumOf { if (it.total.isNaN() || it.total < 0) 0.0 else it.total }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "FACTURAS REGISTRADAS",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB51F1F)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL GENERAL FACTURADO",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${nombreMes.uppercase()} $anioActual",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8FFFFF)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Período del 01/${"%02d".format(mesActual + 1)}/$anioActual al $diasEnMes/${"%02d".format(mesActual + 1)}/$anioActual",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.US, "$ %,.2f", totalFacturadoMes),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (facturas.isEmpty()) {
                Text(
                    text = "NO HAY FACTURAS REGISTRADAS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                facturas.forEach { factura ->
                    val subtotal = if (factura.subtotal.isNaN() || factura.subtotal < 0) 0.0 else factura.subtotal
                    val iva = if (factura.iva.isNaN() || factura.iva < 0) 0.0 else factura.iva
                    val total = if (factura.total.isNaN() || factura.total < 0) 0.0 else factura.total

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
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
                                text = "FOLIO: #${factura.numero.ifBlank { "SIN NÚMERO" }}",
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
