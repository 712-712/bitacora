package com.example.bitacoraautomotriz.ui.reportes

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
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun ReportesScreen(
    onReporteClientes: () -> Unit,
    onReporteAutos: () -> Unit,
    onReporteOrdenes: () -> Unit,
    onReporteInventario: () -> Unit,
    onReporteGastos: () -> Unit,
    onReporteFacturacion: () -> Unit,
    onReporteRecepciones: () -> Unit = {},
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
            text = "MÓDULO DE REPORTES",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // BOTÓN VERDE DESTACADO DE REPORTE DE RECEPCIÓN E INSPECCIÓN DEL VEHÍCULO
        BotonModulo3D(
            texto = "REPORTE RECEPCIÓN E INSPECCIÓN DEL VEHÍCULO",
            colorClaro = Color(0xFFB9F6CA),
            colorMedio = Color(0xFF00C853),
            colorOscuro = Color(0xFF00695C),
            onClick = onReporteRecepciones,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        BotonModulo3D(
            texto = "REPORTE DE CLIENTES",
            colorClaro = Color(0xFFADB8FF),
            colorMedio = Color(0xFF5C70FF),
            colorOscuro = Color(0xFF29399E),
            onClick = onReporteClientes,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        BotonModulo3D(
            texto = "REPORTE DE AUTOS",
            colorClaro = Color(0xFFADB8FF),
            colorMedio = Color(0xFF5C70FF),
            colorOscuro = Color(0xFF29399E),
            onClick = onReporteAutos,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        BotonModulo3D(
            texto = "REPORTE DE ÓRDENES",
            colorClaro = Color(0xFFADB8FF),
            colorMedio = Color(0xFF5C70FF),
            colorOscuro = Color(0xFF29399E),
            onClick = onReporteOrdenes,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        BotonModulo3D(
            texto = "REPORTE DE INVENTARIO",
            colorClaro = Color(0xFFADB8FF),
            colorMedio = Color(0xFF5C70FF),
            colorOscuro = Color(0xFF29399E),
            onClick = onReporteInventario,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        BotonModulo3D(
            texto = "REPORTE DE GASTOS",
            colorClaro = Color(0xFFADB8FF),
            colorMedio = Color(0xFF5C70FF),
            colorOscuro = Color(0xFF29399E),
            onClick = onReporteGastos,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        BotonModulo3D(
            texto = "REPORTE DE FACTURACIÓN",
            colorClaro = Color(0xFFADB8FF),
            colorMedio = Color(0xFF5C70FF),
            colorOscuro = Color(0xFF29399E),
            onClick = onReporteFacturacion,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(18.dp))

        // REGRESAR
        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = onRegresar,
            colorTexto = Color.White
        )
    }
}
