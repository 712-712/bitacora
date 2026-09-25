package com.example.bitacoraautomotriz.ui.facturacion

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
fun FacturacionScreen(
    onNuevaFactura: () -> Unit,
    onVerFacturas: () -> Unit,
    onBuscarFactura: () -> Unit,
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
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "MÓDULO DE FACTURACIÓN",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // NUEVA FACTURA
        BotonModulo3D(
            texto = "NUEVA FACTURA",
            colorClaro = Color(0xFF8FFFFF),
            colorMedio = Color(0xFF00DDEB),
            colorOscuro = Color(0xFF007F88),
            onClick = onNuevaFactura
        )

        // VER FACTURAS
        BotonModulo3D(
            texto = "VER FACTURAS",
            colorClaro = Color(0xFF8FFFFF),
            colorMedio = Color(0xFF00DDEB),
            colorOscuro = Color(0xFF007F88),
            onClick = onVerFacturas
        )

        // BUSCAR FACTURA
        BotonModulo3D(
            texto = "BUSCAR FACTURA",
            colorClaro = Color(0xFF8FFFFF),
            colorMedio = Color(0xFF00DDEB),
            colorOscuro = Color(0xFF007F88),
            onClick = onBuscarFactura
        )

        // REGRESAR
        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Color(0xFFD5E1E6),
            colorMedio = Color(0xFF90A4AE),
            colorOscuro = Color(0xFF455A64),
            onClick = onRegresar
        )
    }
}

