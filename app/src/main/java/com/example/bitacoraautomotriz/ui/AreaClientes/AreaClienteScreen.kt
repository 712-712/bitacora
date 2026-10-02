package com.example.bitacoraautomotriz.ui.areacliente

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
fun AreaClienteScreen(
    onDatosClienteAutos: () -> Unit,
    onDatosFacturacion: () -> Unit,
    onEstadoReparacion: () -> Unit,
    onCitaEntrega: () -> Unit,
    onHistorial: () -> Unit,
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
                top = 42.dp,
                bottom = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // =========================================
        // TÍTULO
        // =========================================
        Text(
            text = "ÁREA DEL CLIENTE",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Consulta y seguimiento de su vehículo",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo
        )

        Spacer(modifier = Modifier.height(28.dp))

        // =========================================
        // MIS AUTOS
        // =========================================
        BotonModulo3D(
            texto = "MIS AUTOS",
            icono = "🚗",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = onDatosClienteAutos,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================
        // MIS DATOS DE FACTURACIÓN
        // =========================================
        BotonModulo3D(
            texto = "MIS DATOS DE FACTURACIÓN",
            icono = "📄",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = onDatosFacturacion,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================
        // ESTADO DE MI REPARACIÓN
        // =========================================
        BotonModulo3D(
            texto = "ESTADO DE MI REPARACIÓN",
            icono = "🔧",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = onEstadoReparacion,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================
        // CITA DE INGRESO AL TALLER
        // =========================================
        BotonModulo3D(
            texto = "CITA DE INGRESO AL TALLER",
            icono = "📅",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = onCitaEntrega,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(14.dp))

        // =========================================
        // HISTORIAL
        // =========================================
        BotonModulo3D(
            texto = "HISTORIAL",
            icono = "📜",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            onClick = onHistorial,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        // =========================================
        // SALIR DE LA APP (Gris)
        // =========================================
        BotonModulo3D(
            texto = "SALIR DE LA APP",
            icono = "🚪",
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
