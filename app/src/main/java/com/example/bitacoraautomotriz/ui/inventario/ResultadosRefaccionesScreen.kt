package com.example.bitacoraautomotriz.ui.inventario

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ResultadosRefaccionesScreen(
    queryBusqueda: String,
    onVerEnMapa: (Double, Double, String, String) -> Unit,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var lugares by remember { mutableStateOf<List<LugarRepuesto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var mensaje by remember { mutableStateOf("Buscando refaccionarias y talleres cercanos...") }

    LaunchedEffect(queryBusqueda) {
        scope.launch {
            try {
                obtenerUbicacionActual(context) { ubicacion ->
                    val latFinal = if (ubicacion != null && (ubicacion.latitude != 0.0 || ubicacion.longitude != 0.0)) ubicacion.latitude else 19.4326
                    val lonFinal = if (ubicacion != null && (ubicacion.latitude != 0.0 || ubicacion.longitude != 0.0)) ubicacion.longitude else -99.1332

                    scope.launch {
                        try {
                            val resultados = buscarRepuestosCercanos(latFinal, lonFinal, queryBusqueda, 5000)
                            lugares = resultados
                            cargando = false
                            mensaje = if (lugares.isNotEmpty()) {
                                "Se encontraron ${lugares.size} negocios cercanos en un radio de 5 km."
                            } else {
                                "No se encontraron negocios cercanos dentro de 5 km."
                            }
                        } catch (e: Exception) {
                            cargando = false
                            mensaje = "Error al conectar con el servicio de ubicaciones."
                        }
                    }
                }
            } catch (e: Exception) {
                cargando = false
                mensaje = "No se pudo obtener la ubicación actual."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "RESULTADOS CERCANOS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (queryBusqueda.isBlank()) "Refaccionarias, Repuestos y Talleres" else "Búsqueda: \"$queryBusqueda\"",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = mensaje,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (lugares.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "NO SE ENCONTRARON NEGOCIOS CERCANOS PARA \"${queryBusqueda.uppercase()}\"",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    BotonModulo3D(
                        texto = "BUSCAR DIRECTO EN GOOGLE MAPS",
                        icono = "🗺️",
                        colorClaro = Color(0xFF80D8FF),
                        colorMedio = Color(0xFF00B8D4),
                        colorOscuro = Color(0xFF006064),
                        onClick = {
                            val uri = Uri.parse("https://www.google.com/maps/search/${Uri.encode(queryBusqueda.ifBlank { "refaccionaria taller autopartes" })}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        tamanioTexto = 15,
                        colorTexto = Color.Black
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lugares) { lugar ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onVerEnMapa(lugar.lat, lugar.lon, lugar.nombre, lugar.direccion)
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = lugar.nombre,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "📍 ${lugar.direccion}",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format(Locale.US, "📏 %.1f KM DE DISTANCIA (Toca para ver mapa)", lugar.distanciaMetros / 1000),
                                color = Color(0xFF7DFFB2),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(
            texto = "REGRESAR",
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
