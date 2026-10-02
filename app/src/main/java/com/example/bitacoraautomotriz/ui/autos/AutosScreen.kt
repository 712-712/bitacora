package com.example.bitacoraautomotriz.ui.autos

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
fun AutosScreen(
    onNuevoAuto: () -> Unit,
    onVerAutos: () -> Unit,
    onBuscarAuto: () -> Unit,
    onEscanearVin: () -> Unit,
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
            text = "MÓDULO DE AUTOS",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(30.dp))

        // NUEVO AUTO
        BotonModulo3D(
            texto = "NUEVO AUTO",
            icono = "🚗",
            colorClaro = Color(0xFFB9F6CA),
            colorMedio = Color(0xFF00C853),
            colorOscuro = Color(0xFF00695C),
            onClick = onNuevoAuto,
            modifier = Modifier.fillMaxWidth(),
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BUSCAR AUTO
        BotonModulo3D(
            texto = "BUSCAR AUTO",
            icono = "🔍",
            colorClaro = Color(0xFFB9F6CA),
            colorMedio = Color(0xFF00C853),
            colorOscuro = Color(0xFF00695C),
            onClick = onBuscarAuto,
            modifier = Modifier.fillMaxWidth(),
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ESCANEAR VIN
        BotonModulo3D(
            texto = "ESCANEAR VIN",
            icono = "📷",
            colorClaro = Color(0xFFB9F6CA),
            colorMedio = Color(0xFF00C853),
            colorOscuro = Color(0xFF00695C),
            onClick = onEscanearVin,
            modifier = Modifier.fillMaxWidth(),
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        // REGRESAR
        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth(),
            colorTexto = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
