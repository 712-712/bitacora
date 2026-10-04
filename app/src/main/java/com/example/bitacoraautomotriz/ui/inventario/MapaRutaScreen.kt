package com.example.bitacoraautomotriz.ui.inventario

import android.content.Intent
import android.location.Location
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
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

    val latCenter = lugarSeleccionado?.lat ?: if (latDestino != 0.0) latDestino else (ubicacionActual?.latitude ?: 19.4326)
    val lonCenter = lugarSeleccionado?.lon ?: if (lonDestino != 0.0) lonDestino else (ubicacionActual?.longitude ?: -99.1332)
    val nombreDestinoFinal = lugarSeleccionado?.nombre ?: nombreDestino.ifBlank { "REFACCIONARIA / TALLER CERCANO" }
    val direccionDestinoFinal = lugarSeleccionado?.direccion ?: direccionDestino

    fun abrirNavegacionGpsGoogleMaps() {
        try {
            val busquedaFinal = if (queryBusqueda.isBlank()) "refaccionaria taller" else "refaccionaria ${queryBusqueda.trim()}"
            val queryEncoded = URLEncoder.encode(busquedaFinal, StandardCharsets.UTF_8.toString())
            val gmmIntentUri = Uri.parse("geo:$latCenter,$lonCenter?q=$queryEncoded&z=14")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Abriendo mapa web...", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // ENCABEZADO
        Text(
            text = "MAPA INTERACTIVO DE BÚSQUEDA",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (queryBusqueda.isBlank()) "Refaccionarias y Talleres en 5 km" else "Buscando: \"$queryBusqueda\"",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        if (nombreDestinoFinal.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "📍 $nombreDestinoFinal${if (direccionDestinoFinal.isNotBlank()) " - $direccionDestinoFinal" else ""}",
                fontSize = 13.sp,
                color = Colores.EtiquetaCampo,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // VISTA NATIVA MAPA INTERNO DENTRO DE LA APP (OSMDROID)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Colores.FondoSecundario),
            contentAlignment = Alignment.Center
        ) {
            if (cargando) {
                CircularProgressIndicator(color = Color.White)
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", 0))
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(GeoPoint(latCenter, lonCenter))

                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // AGREGAR PIN DE MI UBICACIÓN EN AZUL
                            ubicacionActual?.let { loc ->
                                val miPin = Marker(this).apply {
                                    position = GeoPoint(loc.latitude, loc.longitude)
                                    title = "MI UBICACIÓN ACTUAL"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                overlays.add(miPin)
                            }

                            // AGREGAR PINES DE NEGOCIOS REALES ENCONTRADOS EN 5 KM
                            lugaresCercanos.forEach { lugar ->
                                val pinNegocio = Marker(this).apply {
                                    position = GeoPoint(lugar.lat, lugar.lon)
                                    title = lugar.nombre
                                    snippet = lugar.direccion
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    setOnMarkerClickListener { _, _ ->
                                        lugarSeleccionado = lugar
                                        showInfoWindow()
                                        true
                                    }
                                }
                                overlays.add(pinNegocio)
                            }

                            invalidate()
                        }
                    },
                    update = { mapView ->
                        mapView.controller.setCenter(GeoPoint(latCenter, lonCenter))
                        mapView.invalidate()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // LISTA DE OPCIONES ENCONTRADAS
        if (lugaresCercanos.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 120.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(lugaresCercanos) { lugar ->
                    val seleccionado = lugar == lugarSeleccionado
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (seleccionado) Color(0xFF006064) else Colores.FondoTarjeta
                        ),
                        onClick = { lugarSeleccionado = lugar }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = lugar.nombre,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "📍 ${lugar.direccion}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "%.1f km", lugar.distanciaMetros / 1000),
                                color = Color(0xFF7DFFB2),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // BOTÓN 1: NAVEGACIÓN GPS EN GOOGLE MAPS (OPCIONAL)
        BotonModulo3D(
            texto = "ABRIR NAVEGACIÓN EN GOOGLE MAPS",
            icono = "🗺️",
            colorClaro = Color(0xFF80D8FF),
            colorMedio = Color(0xFF00B8D4),
            colorOscuro = Color(0xFF006064),
            onClick = { abrirNavegacionGpsGoogleMaps() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            tamanioTexto = 14,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(10.dp))

        // BOTÓN 2: REGRESAR AL MENÚ PRINCIPAL DENTRO DE LA APP (GIGANTE Y ACCESIBLE)
        BotonModulo3D(
            texto = "REGRESAR AL MENÚ",
            icono = "🔙",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth().height(68.dp),
            tamanioTexto = 18,
            colorTexto = Color.White
        )
    }
}
