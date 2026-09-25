package com.example.bitacoraautomotriz.ui.autos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun MisAutosScreen(
    nombreCliente: String,
    onAgregarAuto: () -> Unit,
    onRegresar: () -> Unit
) {
    var autos by remember { mutableStateOf(emptyList<Auto>()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(nombreCliente) {
        try {
            autos = AutoRepository.obtenerAutosPorCliente(nombreCliente.uppercase())
        } catch (e: Exception) {
            // Manejo de error silencioso
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AUTOS DE:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo
        )
        Text(
            text = nombreCliente.uppercase(),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (cargando) {
            Text(
                text = "CARGANDO AUTOS...",
                fontSize = 18.sp,
                color = Colores.TextoTarjeta
            )
        } else if (autos.isEmpty()) {
            Text(
                text = "ESTE CLIENTE AÚN NO TIENE AUTOS REGISTRADOS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TextoBoton
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(autos) { auto ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Colores.FondoTarjeta
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "PLACA:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Colores.TextoTarjeta.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = auto.placa,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Colores.TextoTarjeta
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "VEHÍCULO:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Colores.TextoTarjeta.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${auto.marca} ${auto.modelo} (${auto.anio})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Colores.TextoTarjeta
                                )
                            }

                            if (auto.color.isNotBlank() || auto.vin.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "DETALLES: ${auto.color} ${if (auto.vin.isNotBlank()) "- VIN: ${auto.vin}" else ""}",
                                    fontSize = 14.sp,
                                    color = Colores.TextoTarjeta.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        BotonModulo3D(
            texto = "AGREGAR NUEVO AUTO",
            icono = "➕",
            onClick = onAgregarAuto,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
