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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun AreaClienteScreen(
    onDatosClienteAutos: () -> Unit,
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
        // DATOS CLIENTE Y AUTOS
        // =========================================
        BotonModulo3D(
            texto = "DATOS CLIENTE Y AUTOS",
            icono = "👤",
            onClick = onDatosClienteAutos,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(7.dp))

        // =========================================
        // ESTADO DE REPARACIÓN
        // =========================================
        BotonModulo3D(
            texto = "ESTADO DE REPARACIÓN",
            icono = "🔧",
            onClick = onEstadoReparacion,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(7.dp))

        // =========================================
        // CITA DE ENTREGA
        // =========================================
        BotonModulo3D(
            texto = "CITA DE ENTREGA",
            icono = "📅",
            onClick = onCitaEntrega,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(7.dp))

        // =========================================
        // HISTORIAL
        // =========================================
        BotonModulo3D(
            texto = "HISTORIAL",
            icono = "📜",
            onClick = onHistorial,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // =========================================
        // REGRESAR
        // =========================================
        BotonModulo3D(
            texto = "SALIR DE LA APP",
            icono = "🚪",
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
