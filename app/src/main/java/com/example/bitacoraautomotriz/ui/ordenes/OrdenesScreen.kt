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

@Composable
fun OrdenesScreen(
    onNuevaOrden: () -> Unit,
    onBuscarOrden: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B44))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ✅ 1. TÍTULO EN UNA SOLA LÍNEA
        Text(
            text = "MÓDULO DE COTIZACIONES",
            fontSize = 24.sp, // ✅ Reducido para que quepa en una línea
            fontWeight = FontWeight.Bold,
            color = Color.White,
            softWrap = false, // ✅ Fuerza una sola línea
            maxLines = 1, // ✅ Máximo 1 línea
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(48.dp))
        // ==========================================
// BOTÓN 1: NUEVA ORDEN
// ==========================================
        BotonModulo3D(
            texto = "NUEVA ORDEN DE COTIZACION",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = { onNuevaOrden() }, // ✅ Cambia el comentario por esto
            tamanioTexto = 20
        )

        Spacer(modifier = Modifier.height(16.dp))

// ==========================================
// BOTÓN 2: SEGUIMIENTO / BUSCAR
// ==========================================
        BotonModulo3D(
            texto = "SEGUIMIENTO DE COTIZACIONES",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = { onBuscarOrden() },
            tamanioTexto = 19  // ✅ Reducido para que quepa en UNA línea
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ✅ BOTÓN REGRESAR (Gris)
        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Color(0xFFD5E1E6),
            colorMedio = Color(0xFF90A4AE),
            colorOscuro = Color(0xFF455A64),
            onClick = onRegresar
        )
    }
}
