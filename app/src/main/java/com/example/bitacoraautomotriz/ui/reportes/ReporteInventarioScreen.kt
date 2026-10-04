package com.example.bitacoraautomotriz.ui.reportes

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
import com.example.bitacoraautomotriz.data.Repuesto
import com.example.bitacoraautomotriz.repository.RepuestoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ReporteInventarioScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    var repuestos by remember { mutableStateOf<List<Repuesto>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = true) }

    LaunchedEffect(Unit) {
        try {
            repuestos = RepuestoRepository.obtenerRepuestos(context)
        } catch (_: Exception) {
            repuestos = emptyList()
        } finally {
            cargando = false
        }
    }

    // OBTENER MES Y AÑO VIGENTE DEL CALENDARIO CON DÍAS EXACTOS (01 AL 28, 29, 30 O 31)
    val calHoy = remember { Calendar.getInstance() }
    val mesActual = calHoy.get(Calendar.MONTH) // 0..11
    val anioActual = calHoy.get(Calendar.YEAR)
    val diasEnMes = calHoy.getActualMaximum(Calendar.DAY_OF_MONTH) // 28, 29, 30 o 31
    val nombreMes = remember { SimpleDateFormat("MMMM", Locale.forLanguageTag("es-ES")).format(calHoy.time) }

    val totalInversionInventario = repuestos.sumOf {
        val qty = it.cantidad.coerceAtLeast(0)
        val prc = if (it.precio.isNaN() || it.precio < 0) 0.0 else it.precio
        qty * prc
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 24.dp,
                bottom = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "REPORTE DE INVENTARIO",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TARJETA ROJA CON LA INVERSIÓN TOTAL EN INVENTARIO
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
                    text = "INVERSIÓN TOTAL EN INVENTARIO",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${nombreMes.uppercase()} $anioActual",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF3A7FF)
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
                    text = String.format(Locale.US, "$ %,.2f", totalInversionInventario),
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
        } else if (repuestos.isEmpty()) {
            Text(
                text = "NO HAY REPUESTOS REGISTRADOS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        } else {
            Row {
                Text(text = "Total de repuestos: ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "${repuestos.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
            }

            Spacer(modifier = Modifier.height(16.dp))

            repuestos.forEach { repuesto ->
                val cantidad = repuesto.cantidad.coerceAtLeast(0)
                val precio = if (repuesto.precio.isNaN() || repuesto.precio < 0) 0.0 else repuesto.precio
                val total = cantidad * precio

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row {
                            Text(text = "NOMBRE: ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = repuesto.nombre.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row {
                            Text(text = "MARCA: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = repuesto.marca.uppercase(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row {
                            Text(text = "CATEGORÍA: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = repuesto.categoria.uppercase(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row {
                            Text(text = "CANTIDAD: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = "$cantidad", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                        }

                        Row {
                            Text(text = "PRECIO UNITARIO: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(
                                text = String.format(Locale.US, "$ %,.2f", precio),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252)
                            )
                        }

                        Row {
                            Text(text = "TOTAL: ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(
                                text = String.format(Locale.US, "$ %,.2f", total),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

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

        Spacer(modifier = Modifier.height(12.dp))
    }
}
