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
import com.example.bitacoraautomotriz.data.Gasto
import com.example.bitacoraautomotriz.repository.GastoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ReporteGastosScreen(
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    var gastos by remember { mutableStateOf<List<Gasto>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = true) }

    LaunchedEffect(Unit) {
        try {
            gastos = GastoRepository.obtenerGastos(context)
        } catch (_: Exception) {
            gastos = emptyList()
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

    // FILTRAR GASTOS DEL MES VIGENTE
    val gastosDelMes = gastos.filter { g ->
        try {
            val partes = g.fecha.split("/")
            if (partes.size == 3) {
                val m = partes[1].toIntOrNull()
                val y = partes[2].toIntOrNull()
                m == (mesActual + 1) && y == anioActual
            } else false
        } catch (_: Exception) { false }
    }

    val totalGastosMes = gastosDelMes.sumOf { if (it.monto.isNaN() || it.monto < 0) 0.0 else it.monto }

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
            text = "REPORTE DE GASTOS",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TARJETA ROJA CON EL TOTAL GENERAL DE GASTOS DEL MES VIGENTE
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
                    text = "TOTAL GENERAL DE GASTOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${nombreMes.uppercase()} $anioActual",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9999)
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
                    text = String.format(Locale.US, "$ %,.2f", totalGastosMes),
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
        } else if (gastos.isEmpty()) {
            Text(
                text = "NO HAY GASTOS REGISTRADOS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        } else {
            Row {
                Text(text = "Total de gastos registrados: ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = "${gastos.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
            }

            Spacer(modifier = Modifier.height(16.dp))

            gastos.forEach { gasto ->
                val total = if (gasto.monto.isNaN() || gasto.monto < 0) 0.0 else gasto.monto
                val precioBase = total / 1.16
                val iva = total - precioBase

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
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
                        Row {
                            Text(text = "CONCEPTO: ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = gasto.concepto.orEmpty().uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row {
                            Text(text = "CATEGORÍA: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = gasto.categoria.orEmpty().uppercase(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row {
                            Text(text = "FECHA: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = gasto.fecha.orEmpty(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        if (gasto.descripcion.orEmpty().isNotBlank()) {
                            Row {
                                Text(text = "DESCRIPCIÓN: ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = gasto.descripcion.orEmpty().uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF004D33))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "PRECIO / BASE:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", precioBase), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "I.V.A. (16%):", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", iva), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "TOTAL DEL GASTO:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(text = String.format(Locale.US, "$ %,.2f", total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
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
