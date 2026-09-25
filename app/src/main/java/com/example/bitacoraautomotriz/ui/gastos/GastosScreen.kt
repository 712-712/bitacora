
package com.example.bitacoraautomotriz.ui.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D

@Composable
fun GastosScreen(
    onNuevoGasto: () -> Unit,
    onVerGastos: () -> Unit,
    onBuscarGasto: () -> Unit,
    onRegresar: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B44))
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 48.dp,
                bottom = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "MÓDULO DE GASTOS",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        // NUEVO GASTO
        BotonModulo3D(
            texto = "NUEVO GASTO",
            colorClaro = Color(0xFFFF9999),
            colorMedio = Color(0xFFFF4141),
            colorOscuro = Color(0xFFB51F1F),
            onClick = onNuevoGasto
        )

        Spacer(modifier = Modifier.height(6.dp))

        // VER GASTOS
        BotonModulo3D(
            texto = "VER GASTOS",
            colorClaro = Color(0xFFFF9999),
            colorMedio = Color(0xFFFF4141),
            colorOscuro = Color(0xFFB51F1F),
            onClick = onVerGastos
        )

        Spacer(modifier = Modifier.height(6.dp))

        // BUSCAR GASTO
        BotonModulo3D(
            texto = "BUSCAR GASTO",
            colorClaro = Color(0xFFFF9999),
            colorMedio = Color(0xFFFF4141),
            colorOscuro = Color(0xFFB51F1F),
            onClick = onBuscarGasto
        )

        Spacer(modifier = Modifier.height(6.dp))

        // REGRESAR
        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Color(0xFFD5E1E6),
            colorMedio = Color(0xFF90A4AE),
            colorOscuro = Color(0xFF455A64),
            onClick = onRegresar
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )
    }
}

