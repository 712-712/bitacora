package com.example.bitacoraautomotriz.ui.clientes

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import com.example.bitacoraautomotriz.utils.AudioUtils

@Composable
fun EstadoReparacionScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    var ordenes by remember { mutableStateOf<List<OrdenServicio>>(emptyList()) }
    var ordenSeleccionada by remember { mutableStateOf<OrdenServicio?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val lista = OrdenServicioRepository.obtenerOrdenes(context)
            ordenes = lista
            if (lista.isNotEmpty()) {
                val ultima = lista.last()
                ordenSeleccionada = ultima
                try {
                    AudioUtils.reproducirSonidoMotorTresVeces(context)
                } catch (_: Exception) {}

                // ESCUCHAR EN TIEMPO REAL DESDE FIREBASE REALTIME DATABASE (<1 SEG SEGUNDO)
                FirebaseSyncManager.escucharOrdenEnTiempoReal(ultima.id) { ordenDescargada ->
                    ordenSeleccionada = ordenDescargada
                    try {
                        AudioUtils.reproducirSonidoMotorTresVeces(context)
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {
            ordenes = emptyList()
        } finally {
            cargando = false
        }
    }

    val ordenActual = ordenSeleccionada
    val avanceActual = ordenActual?.porcentajeAvance?.coerceIn(0, 100) ?: 50

    // COLOR CROMÁTICO SEGÚN EL PORCENTAJE (ROJO 0% ➡️ VERDE 100%)
    val colorCromaticoAvance = when {
        avanceActual <= 10 -> Color(0xFFD32F2F) // Rojo
        avanceActual <= 35 -> Color(0xFFF57C00) // Naranja
        avanceActual <= 65 -> Color(0xFFFFB300) // Amarillo
        avanceActual <= 89 -> Color(0xFF7CB342) // Verde Claro / Lima
        else -> Color(0xFF00C853)               // Verde Intenso 100%
    }

    val colorBarraAnimado by animateColorAsState(targetValue = colorCromaticoAvance, label = "ColorAvance")

    val etapaTexto = when {
        avanceActual == 0 -> "0% - EN ESPERA / DIAGNÓSTICO INICIAL"
        avanceActual <= 25 -> "25% - INICIO DE REPARACIÓN Y DESARME"
        avanceActual <= 50 -> "50% - MONTAJE DE REFACCIONES Y SERVICIO"
        avanceActual <= 75 -> "75% - FASE FINAL Y PRUEBAS DE MANEJO"
        else -> "100% - REPARACIÓN COMPLETADA"
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
            // TÍTULO DE LA PANTALLA
            Text(
                text = "ESTADO DE REPARACIÓN DE MI AUTO",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sincronizado en tiempo real con el taller",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.EtiquetaCampo,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                // TARJETA PRINCIPAL DEL ESTADO DE REPARACIÓN (SÓLO LECTURA PARA EL CLIENTE)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (ordenActual != null) {
                            Text(
                                text = "VEHÍCULO: ${ordenActual.auto.uppercase()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "CLIENTE: ${ordenActual.cliente.uppercase()}   |   FOLIO: #${ordenActual.id}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7DFFB2),
                                textAlign = TextAlign.Center
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF004D33))
                        } else {
                            Text(
                                text = "ESTADO DE SERVICIO DEL VEHÍCULO",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // PORCENTAJE CROMÁTICO
                        Text(
                            text = "$avanceActual%",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorBarraAnimado,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // BARRA DE PROGRESO CROMÁTICA (ROJO A VERDE)
                        LinearProgressIndicator(
                            progress = { avanceActual / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(11.dp)),
                            color = colorBarraAnimado,
                            trackColor = Color.Black.copy(alpha = 0.3f),
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // DESCRIPCIÓN DE LA ETAPA
                        Text(
                            text = etapaTexto,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        // SI LLEGA AL 100%, MUESTRA "LISTO PARA SU ENTREGA" Y BOTÓN VERDE "ENTERADO"
                        if (avanceActual >= 100) {
                            Spacer(modifier = Modifier.height(18.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF00C853))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✨ LISTO PARA SU ENTREGA ✨",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // BOTÓN COLOR VERDE "ENTERADO"
                            BotonModulo3D(
                                texto = "ENTERADO",
                                icono = "✅",
                                colorClaro = Color(0xFFB9F6CA),
                                colorMedio = Color(0xFF00C853),
                                colorOscuro = Color(0xFF00695C),
                                colorTexto = Color.Black,
                                onClick = {
                                    Toast.makeText(context, "✅ Notificación confirmada por el cliente", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp),
                                tamanioTexto = 16
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
                    colorTexto = Color.White,
                    onClick = onRegresar,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    tamanioTexto = 16
                )
            }
        }
    }
}
