package com.example.bitacoraautomotriz.ui.reportes

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun ReporteAutosScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    var autos by remember { mutableStateOf<List<Auto>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = true) }

    LaunchedEffect(Unit) {
        try {
            autos = AutoRepository.obtenerAutos(context)
        } catch (_: Exception) {
            autos = emptyList()
        } finally {
            cargando = false
        }
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "REPORTE DE AUTOS",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (autos.isEmpty()) {
                Text(
                    text = "NO HAY AUTOS REGISTRADOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Row {
                    Text(text = "Total de autos: ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "${autos.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                }

                Spacer(modifier = Modifier.height(16.dp))

                autos.forEach { auto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row {
                                Text(text = "VEHÍCULO: ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "${auto.marca.uppercase()} ${auto.modelo.uppercase()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Row {
                                Text(text = "AÑO: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "${auto.anio}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Row {
                                Text(text = "PLACA: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = auto.placa.uppercase(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Row {
                                Text(text = "CLIENTE: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = auto.cliente.uppercase(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            if (auto.vin.isNotBlank()) {
                                Row {
                                    Text(text = "VIN: ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = auto.vin.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
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
