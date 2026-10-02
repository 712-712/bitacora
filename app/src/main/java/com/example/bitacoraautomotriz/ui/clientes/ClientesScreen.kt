package com.example.bitacoraautomotriz.ui.clientes

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
fun ClientesScreen(
    onNuevoCliente: () -> Unit,
    onVerClientes: () -> Unit,
    onBuscarCliente: () -> Unit,
    onVerAutos: () -> Unit,
    onAgregarAuto: () -> Unit,
    onRegresar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "MÓDULO DE CLIENTES",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )
        Spacer(modifier = Modifier.height(32.dp))

        BotonModulo3D(
            texto = "NUEVO CLIENTE",
            colorClaro = Color(0xFF80D8FF),
            colorMedio = Color(0xFF00B8D4),
            colorOscuro = Color(0xFF006064),
            onClick = onNuevoCliente,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(
            texto = "VER MIS CLIENTES",
            colorClaro = Color(0xFF80D8FF),
            colorMedio = Color(0xFF00B8D4),
            colorOscuro = Color(0xFF006064),
            onClick = onVerClientes,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(
            texto = "BUSCAR CLIENTE",
            colorClaro = Color(0xFF80D8FF),
            colorMedio = Color(0xFF00B8D4),
            colorOscuro = Color(0xFF006064),
            onClick = onBuscarCliente,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Botones de Autos dentro del módulo de Clientes (Color Verde)
        BotonModulo3D(
            texto = "VER AUTOS CLIENTES",
            colorClaro = Color(0xFFB9F6CA),
            colorMedio = Color(0xFF00C853),
            colorOscuro = Color(0xFF00695C),
            onClick = onVerAutos,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(
            texto = "AGREGAR AUTO NUEVO",
            colorClaro = Color(0xFFB9F6CA),
            colorMedio = Color(0xFF00C853),
            colorOscuro = Color(0xFF00695C),
            onClick = onAgregarAuto,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth(),
            colorTexto = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}
