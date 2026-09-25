
package com.example.bitacoraautomotriz.ui.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Gasto
import com.example.bitacoraautomotriz.repository.GastoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import java.util.Locale

@Composable
fun VerGastosScreen(
    onRegresar: () -> Unit
) {

    var gastos by remember {
        mutableStateOf(emptyList<Gasto>())
    }

    LaunchedEffect(Unit) {
        gastos = GastoRepository.obtenerGastos()
    }

    val totalGastos = gastos.sumOf {
        it.monto
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B44))
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 48.dp,
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        // =========================================
        // TÍTULO
        // =========================================

        Text(
            text = "GASTOS REGISTRADOS",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (gastos.isEmpty()) {

            Text(
                text = "No hay gastos registrados.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

        } else {

            // =========================================
            // LISTA DE GASTOS
            // =========================================

            gastos.forEach { gasto ->

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        text = "Concepto: ${gasto.concepto}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = "Categoría: ${gasto.categoria}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = "Fecha: ${gasto.fecha}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // =========================================
                    // MONTO DEL GASTO
                    // =========================================

                    Text(
                        text = String.format(
                            Locale.US,
                            "Monto: $%,.2f",
                            gasto.monto
                        ),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = "Descripción: ${gasto.descripcion}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
                )
            }

            // =========================================
            // TOTAL GENERAL
            // =========================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFFF4141),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "TOTAL DE GASTOS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = String.format(
                        Locale.US,
                        "$%,.2f",
                        totalGastos
                    ),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        // =========================================
        // REGRESAR
        // =========================================

        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Color(0xFFD5E1E6),
            colorMedio = Color(0xFF90A4AE),
            colorOscuro = Color(0xFF455A64),
            onClick = onRegresar
        )
    }
}

