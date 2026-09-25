package com.example.bitacoraautomotriz.ui.inventario

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
import com.example.bitacoraautomotriz.data.Repuesto
import com.example.bitacoraautomotriz.repository.RepuestoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D

@Composable
fun VerInventarioScreen(
    onRegresar: () -> Unit
) {

    var repuestos by remember {
        mutableStateOf(emptyList<Repuesto>())
    }

    LaunchedEffect(Unit) {
        repuestos = RepuestoRepository.obtenerRepuestos()
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
            text = "INVENTARIO",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        if (repuestos.isEmpty()) {

            Text(
                text = "No hay repuestos registrados.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

        } else {

            // =========================================
            // SUBTÍTULO
            // =========================================

            Text(
                text = "REPUESTOS REGISTRADOS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            // =========================================
            // LISTA DE REPUESTOS
            // =========================================

            repuestos.forEach { repuesto ->

                // =========================================
                // CÁLCULO DEL VALOR TOTAL
                // =========================================

                val totalRepuesto =
                    repuesto.cantidad * repuesto.precio

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

                    // =========================================
                    // NOMBRE
                    // =========================================

                    Text(
                        text = "Nombre: ${repuesto.nombre}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // =========================================
                    // MARCA
                    // =========================================

                    Text(
                        text = "Marca: ${repuesto.marca}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // =========================================
                    // CATEGORÍA
                    // =========================================

                    Text(
                        text = "Categoría: ${repuesto.categoria}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // =========================================
                    // CANTIDAD
                    // =========================================

                    Text(
                        text = "Cantidad: ${repuesto.cantidad}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // =========================================
                    // PRECIO UNITARIO
                    // =========================================

                    Text(
                        text = "Precio unitario: $ %.2f"
                            .format(repuesto.precio),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // =========================================
                    // TOTAL
                    // =========================================

                    Text(
                        text = "Total: $ %.2f"
                            .format(totalRepuesto),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001B44)
                    )
                }

                Spacer(
                    modifier = Modifier.height(12.dp)
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
