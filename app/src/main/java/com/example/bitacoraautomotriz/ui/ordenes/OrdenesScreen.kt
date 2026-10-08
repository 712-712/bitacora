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
    onRecepcionVehiculo: () -> Unit = {},
    onVerRecepciones: () -> Unit = {},
    onProgramarAlerta: () -> Unit = {},
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
            color = Colores.TituloPrincipal,
            softWrap = false,
            maxLines = 1,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))

        BotonModulo3D(
            texto = "NUEVA ORDEN DE COTIZACIÓN",
            icono = "📄",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = { onNuevaOrden() },
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        // BOTÓN VERDE RECEPCIÓN Y PROTECCIÓN LEGAL DEL VEHÍCULO
        BotonModulo3D(
            texto = "RECEPCIÓN Y CHECK-IN VEHÍCULO",
            icono = "📷",
            colorClaro = Color(0xFFB9F6CA),
            colorMedio = Color(0xFF00C853),
            colorOscuro = Color(0xFF00695C),
            onClick = { onRecepcionVehiculo() },
            tamanioTexto = 15,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        // BOTÓN VERDE HISTORIAL DE RECEPCIONES Y PROTECCIÓN LEGAL
        BotonModulo3D(
            texto = "HISTORIAL DE RECEPCIONES Y CHECK-IN",
            icono = "📜",
            colorClaro = Color(0xFFB9F6CA),
            colorMedio = Color(0xFF00C853),
            colorOscuro = Color(0xFF00695C),
            onClick = { onVerRecepciones() },
            tamanioTexto = 14,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        BotonModulo3D(
            texto = "SEGUIMIENTO DE COTIZACIONES",
            icono = "🔍",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = { onBuscarOrden() },
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        BotonModulo3D(
            texto = "PROGRAMAR ALERTA DE REVISIÓN",
            icono = "🔔",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = { onProgramarAlerta() },
            tamanioTexto = 15,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = onRegresar,
            colorTexto = Color.White
        )
    }
}
