package com.example.bitacoraautomotriz.ui.dashboard

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
fun DashboardScreen(
    onClientesClick: () -> Unit,
    onAreaClienteClick: () -> Unit,
    onOrdenesClick: () -> Unit,
    onInventarioClick: () -> Unit,
    onGastosClick: () -> Unit,
    onFacturacionClick: () -> Unit,
    onReportesClick: () -> Unit,
    onConfiguracionClick: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // ✅ TÍTULO PRINCIPAL AHORA USA EL AZUL GLOBAL
        Text(
            text = "BITÁCORA AUTOMOTRIZ",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Colores.TituloPrincipal // <-- AQUÍ ESTABA EL ERROR, YA CORREGIDO
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Administración de Taller",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal // También unificado
        )

        Spacer(modifier = Modifier.height(32.dp))

        BotonModulo3D(texto = "CLIENTES", colorClaro = Color(0xFF80D8FF), colorMedio = Color(0xFF00B8D4), colorOscuro = Color(0xFF006064), onClick = onClientesClick)
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(texto = "COTIZACIONES DE SERVICIOS", colorClaro = Color(0xFFD7B899), colorMedio = Color(0xFF9B6B43), colorOscuro = Color(0xFF5D3A1A), onClick = onOrdenesClick)
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(texto = "INVENTARIO DEL TALLER", colorClaro = Color(0xFFF3A7FF), colorMedio = Color(0xFFD83CFF), colorOscuro = Color(0xFF7B1599), onClick = onInventarioClick)
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(texto = "GASTOS DEL TALLER", colorClaro = Color(0xFFFF9999), colorMedio = Color(0xFFFF4141), colorOscuro = Color(0xFFB51F1F), onClick = onGastosClick)
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(texto = "FACTURACIÓN", colorClaro = Color(0xFF8FFFFF), colorMedio = Color(0xFF00DDEB), colorOscuro = Color(0xFF007F88), onClick = onFacturacionClick)
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(texto = "REPORTES", colorClaro = Color(0xFFADB8FF), colorMedio = Color(0xFF5C70FF), colorOscuro = Color(0xFF29399E), onClick = onReportesClick)
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(texto = "CONFIGURACIÓN", colorClaro = Color(0xFFFFF59D), colorMedio = Color(0xFFFFEB3B), colorOscuro = Color(0xFFFBC02D), onClick = onConfiguracionClick)
        Spacer(modifier = Modifier.height(24.dp))

        BotonModulo3D(texto = "ÁREA DEL CLIENTE", colorClaro = Color(0xFFFF5252), colorMedio = Color(0xFFD50000), colorOscuro = Color(0xFF8B0000), onClick = onAreaClienteClick)
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(texto = "SALIR DE LA APP", colorClaro = Color(0xFFB0BEC5), colorMedio = Color(0xFF607D8B), colorOscuro = Color(0xFF263238), onClick = onRegresar)
        Spacer(modifier = Modifier.height(16.dp))
    }
}
