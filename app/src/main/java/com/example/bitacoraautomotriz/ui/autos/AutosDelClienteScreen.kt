package com.example.bitacoraautomotriz.ui.autos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun AutosDelClienteScreen(
    clienteId: Int,
    nombreCliente: String,
    onAgregarAuto: () -> Unit,
    onEditarAuto: (Int) -> Unit,
    onRegresar: () -> Unit
) {
    var autos by remember { mutableStateOf(emptyList<Auto>()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        autos = AutoRepository.obtenerAutosPorCliente(nombreCliente).sortedByDescending { it.id }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AUTOS DEL CLIENTE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.EtiquetaCampo
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = nombreCliente.uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )
            Spacer(modifier = Modifier.height(20.dp))

            BotonModulo3D(
                texto = "AGREGAR AUTO NUEVO",
                icono = "🚗",
                colorClaro = Color(0xFFB9F6CA),
                colorMedio = Color(0xFF00C853),
                colorOscuro = Color(0xFF00695C),
                onClick = onAgregarAuto,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colorTexto = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (autos.isEmpty()) {
                Text(
                    text = "ESTE CLIENTE AÚN NO TIENE AUTOS REGISTRADOS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TextoGlobal
                )
            } else {
                autos.forEach { auto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ID AUTO: ${auto.id}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7DFFB2)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${auto.marca.orEmpty()} ${auto.modelo.orEmpty()} (${auto.anio})".uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = "PLACA: ${auto.placa.orEmpty().uppercase()}   |   VIN: ${auto.vin.orEmpty().ifBlank { "N/A" }.uppercase()}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7DFFB2)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "KILOMETRAJE REGISTRADO:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "${auto.kilometraje} km",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF004D33))

                            BotonModulo3D(
                                texto = "EDITAR AUTO",
                                icono = "✏️",
                                colorClaro = Color(0xFF90CAF9),
                                colorMedio = Color(0xFF1976D2),
                                colorOscuro = Color(0xFF0D47A1),
                                onClick = { onEditarAuto(auto.id) },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                tamanioTexto = 16,
                                colorTexto = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // BOTÓN REGRESAR FIJO E INMÓVIL AL FONDO DE LA PANTALLA
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Colores.FondoPantalla
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                BotonModulo3D(
                    texto = "REGRESAR",
                    icono = "🔙",
                    colorClaro = Colores.RegresarClaro,
                    colorMedio = Colores.RegresarMedio,
                    colorOscuro = Colores.RegresarOscuro,
                    onClick = onRegresar,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    tamanioTexto = 16,
                    colorTexto = Color.White
                )
            }
        }
    }
}
