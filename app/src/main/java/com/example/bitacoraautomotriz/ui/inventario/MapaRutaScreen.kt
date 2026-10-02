package com.example.bitacoraautomotriz.ui.inventario

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MapaRutaScreen(
    queryBusqueda: String = "refaccionaria taller",
    latDestino: Double = 0.0,
    lonDestino: Double = 0.0,
    nombreDestino: String = "",
    direccionDestino: String = "",
    onRegresar: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var ubicacionActual by remember { mutableStateOf<Location?>(null) }
    var lugaresCercanos by remember { mutableStateOf<List<LugarRepuesto>>(emptyList()) }
    var lugarSeleccionado by remember { mutableStateOf<LugarRepuesto?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(queryBusqueda) {
        obtenerUbicacionActual(context) { loc ->
            ubicacionActual = loc
            val latFinal = loc?.latitude ?: 19.4326
            val lonFinal = loc?.longitude ?: -99.1332

            scope.launch {
                try {
                    val resultados = buscarRepuestosCercanos(latFinal, lonFinal, queryBusqueda, 5000)
                    lugaresCercanos = resultados
                    if (resultados.isNotEmpty()) {
                        lugarSeleccionado = resultados.first()
                    }
                } catch (_: Exception) {
                    lugaresCercanos = emptyList()
                } finally {
                    cargando = false
                }
            }
        }
    }

    val latDestinoFinal = lugarSeleccionado?.lat ?: if (latDestino != 0.0) latDestino else (ubicacionActual?.latitude ?: 19.4326)
    val lonDestinoFinal = lugarSeleccionado?.lon ?: if (lonDestino != 0.0) lonDestino else (ubicacionActual?.longitude ?: -99.1332)
    val nombreDestinoFinal = lugarSeleccionado?.nombre ?: nombreDestino.ifBlank { "REFACCIONARIA / TALLER CERCANO" }
    val direccionDestinoFinal = lugarSeleccionado?.direccion ?: direccionDestino

    fun abrirGoogleMapsNavegacion() {
        try {
            val gmmIntentUri = Uri.parse("google.navigation:q=$latDestinoFinal,$lonDestinoFinal")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            val urlWeb = "https://www.google.com/maps/search/?api=1&query=${Uri.encode(queryBusqueda)}"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlWeb))
            context.startActivity(webIntent)
        }
    }

    val urlMapaActual = remember(latDestinoFinal, lonDestinoFinal) {
        "https://maps.google.com/maps?q=$latDestinoFinal,$lonDestinoFinal&z=15&output=embed"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "MAPA DE BÚSQUEDA CERCANA",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (queryBusqueda.isBlank()) "Refaccionarias y Talleres en 5 km" else "Buscando: \"$queryBusqueda\"",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        if (nombreDestinoFinal.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "📍 $nombreDestinoFinal${if (direccionDestinoFinal.isNotBlank()) " - $direccionDestinoFinal" else ""}",
                fontSize = 14.sp,
                color = Colores.EtiquetaCampo,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // BOTÓN ACCIÓN GOOGLE MAPS
        BotonModulo3D(
            texto = "ABRIR NAVEGACIÓN EN GOOGLE MAPS",
            icono = "🗺️",
            colorClaro = Color(0xFF80D8FF),
            colorMedio = Color(0xFF00B8D4),
            colorOscuro = Color(0xFF006064),
            onClick = { abrirGoogleMapsNavegacion() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            tamanioTexto = 15,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        // VISTA MAPA INTERACTIVO GOOGLE MAPS (CARGA ESTABLE)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Colores.FondoSecundario),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = WebViewClient()
                        loadUrl(urlMapaActual)
                    }
                },
                update = { webView ->
                    if (webView.url != urlMapaActual) {
                        webView.loadUrl(urlMapaActual)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // LISTA DE NEGOCIOS REALES ENCONTRADOS DENTRO DE 5 KM
        if (cargando) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
        } else if (lugaresCercanos.isNotEmpty()) {
            Text(
                text = "NEGOCIOS ENCONTRADOS EN 5 KM:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.EtiquetaCampo,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 140.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lugaresCercanos) { lugar ->
                    val seleccionado = lugar == lugarSeleccionado
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (seleccionado) Color(0xFF006064) else Colores.FondoTarjeta
                        ),
                        onClick = { lugarSeleccionado = lugar }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lugar.nombre,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "📍 ${lugar.direccion}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "%.1f km", lugar.distanciaMetros / 1000),
                                color = Color(0xFF7DFFB2),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            tamanioTexto = 15,
            colorTexto = Color.White
        )
    }
}
