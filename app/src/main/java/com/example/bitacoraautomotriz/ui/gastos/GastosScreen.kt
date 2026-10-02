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
import com.example.bitacoraautomotriz.ui.theme.Colores

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
            .background(Colores.FondoPantalla)
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
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(30.dp))

        // NUEVO GASTO
        BotonModulo3D(
            texto = "NUEVO GASTO",
            colorClaro = Color(0xFFFF9999),
            colorMedio = Color(0xFFFF4141),
            colorOscuro = Color(0xFFB51F1F),
            onClick = onNuevoGasto,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // VER GASTOS
        BotonModulo3D(
            texto = "VER GASTOS",
            colorClaro = Color(0xFFFF9999),
            colorMedio = Color(0xFFFF4141),
            colorOscuro = Color(0xFFB51F1F),
            onClick = onVerGastos,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BUSCAR GASTO
        BotonModulo3D(
            texto = "BUSCAR GASTO",
            colorClaro = Color(0xFFFF9999),
            colorMedio = Color(0xFFFF4141),
            colorOscuro = Color(0xFFB51F1F),
            onClick = onBuscarGasto,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // REGRESAR
        BotonModulo3D(
            texto = "REGRESAR",
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
