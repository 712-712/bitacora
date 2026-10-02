package com.example.bitacoraautomotriz.ui.ordenes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun OrdenesScreen(
    onNuevaOrden: () -> Unit,
    onBuscarOrden: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MÓDULO DE COTIZACIONES",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal, // ✅ Azul de títulos
            softWrap = false,
            maxLines = 1,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(48.dp))

        BotonModulo3D(
            texto = "NUEVA ORDEN DE COTIZACION",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = { onNuevaOrden() },
            tamanioTexto = 18,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(
            texto = "SEGUIMIENTO DE COTIZACIONES",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = { onBuscarOrden() },
            tamanioTexto = 17,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Color(0xFFD5E1E6),
            colorMedio = Color(0xFF90A4AE),
            colorOscuro = Color(0xFF455A64),
            onClick = onRegresar,
            colorTexto = Color.Black
        )
    }
}
