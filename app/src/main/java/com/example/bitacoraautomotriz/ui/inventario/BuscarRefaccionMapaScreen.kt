package com.example.bitacoraautomotriz.ui.inventario

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// ======================================================
// MODELO
// ======================================================

data class LugarRepuesto(
    val nombre: String,
    val lat: Double,
    val lon: Double,
    val direccion: String,
    val distanciaMetros: Double
)

// ======================================================
// DISTANCIA
// ======================================================

fun distanciaEntreCoordenadas(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val resultados = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, resultados)
    return resultados[0].toDouble()
}

// ======================================================
// OBTENER UBICACIÓN ACTUAL
// ======================================================

fun obtenerUbicacionActual(
    context: Context,
    onUbicacion: (Location?) -> Unit
) {
    val tienePermiso =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    if (!tienePermiso) {
        onUbicacion(null)
        return
    }

    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    var mejorUbicacion: Location? = null
    val proveedores = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

    for (proveedor in proveedores) {
        try {
            val ubicacion = locationManager.getLastKnownLocation(proveedor)
            if (ubicacion != null) {
                if (mejorUbicacion == null || ubicacion.accuracy < mejorUbicacion.accuracy) {
                    mejorUbicacion = ubicacion
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (mejorUbicacion != null) {
        onUbicacion(mejorUbicacion)
        return
    }

    var ubicacionEntregada = false
    val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (ubicacionEntregada) return
            ubicacionEntregada = true
            try {
                locationManager.removeUpdates(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onUbicacion(location)
        }
    }

    try {
        var proveedorActivo = false
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            proveedorActivo = true
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, listener)
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            proveedorActivo = true
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 1f, listener)
        }

        if (!proveedorActivo) {
            onUbicacion(null)
            return
        }

        Thread {
            try {
                Thread.sleep(8000)
                if (!ubicacionEntregada) {
                    ubicacionEntregada = true
                    try {
                        locationManager.removeUpdates(listener)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    onUbicacion(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

    } catch (e: SecurityException) {
        e.printStackTrace()
        onUbicacion(null)
    }
}

// ======================================================
// BÚSQUEDA NOMINATIM Y OVERPASS (RESTRICTA A 5 KM)
// ======================================================

suspend fun consultarNominatim(
    queryTexto: String,
    latUsuario: Double,
    lonUsuario: Double
): List<LugarRepuesto> = withContext(Dispatchers.IO) {
    val resultado = mutableListOf<LugarRepuesto>()
    try {
        val q = if (queryTexto.isBlank()) "refaccionaria taller autopartes" else queryTexto.trim()
        val queryEncoded = URLEncoder.encode(q, "UTF-8")

        val viewboxDelta = 0.045
        val minLon = lonUsuario - viewboxDelta
        val maxLon = lonUsuario + viewboxDelta
        val minLat = latUsuario - viewboxDelta
        val maxLat = latUsuario + viewboxDelta

        val urlString = "https://nominatim.openstreetmap.org/search?q=$queryEncoded&format=json&addressdetails=1&limit=30&bounded=1&viewbox=$minLon,$maxLat,$maxLon,$minLat"
        val url = URL(urlString)
        val conexion = url.openConnection() as HttpURLConnection
        conexion.requestMethod = "GET"
        conexion.connectTimeout = 8000
        conexion.readTimeout = 10000
        conexion.setRequestProperty("User-Agent", "BitacoraAutomotriz/1.0 (Android App)")

        if (conexion.responseCode in 200..299) {
            val respuesta = conexion.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(respuesta)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val lat = obj.getDouble("lat")
                val lon = obj.getDouble("lon")
                val displayName = obj.optString("display_name", "")
                val addressObj = obj.optJSONObject("address")

                val name = obj.optString("name", "")
                val nombre = when {
                    name.isNotBlank() -> name.uppercase()
                    displayName.isNotBlank() -> displayName.split(",").firstOrNull()?.uppercase() ?: "REFACCIONARIA / TALLER"
                    else -> "REFACCIONARIA / TALLER"
                }

                val road = addressObj?.optString("road") ?: ""
                val houseNumber = addressObj?.optString("house_number") ?: ""
                val neighbourhood = addressObj?.optString("neighbourhood") ?: ""
                val suburb = addressObj?.optString("suburb") ?: neighbourhood
                val cityTown = addressObj?.optString("town") ?: ""
                val city = addressObj?.optString("city") ?: cityTown

                val direccion = when {
                    road.isNotBlank() && houseNumber.isNotBlank() && suburb.isNotBlank() -> "$road #$houseNumber, Col. $suburb"
                    road.isNotBlank() && houseNumber.isNotBlank() -> "$road #$houseNumber"
                    road.isNotBlank() && suburb.isNotBlank() -> "$road, Col. $suburb"
                    road.isNotBlank() -> road
                    city.isNotBlank() -> city
                    displayName.isNotBlank() -> displayName
                    else -> "UBICACIÓN REGISTRADA"
                }

                val distancia = distanciaEntreCoordenadas(latUsuario, lonUsuario, lat, lon)

                if (distancia <= 5000.0) {
                    resultado.add(
                        LugarRepuesto(
                            nombre = nombre,
                            lat = lat,
                            lon = lon,
                            direccion = direccion,
                            distanciaMetros = distancia
                        )
                    )
                }
            }
        }
        conexion.disconnect()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    resultado
}

suspend fun consultarOverpass(
    query: String,
    latUsuario: Double,
    lonUsuario: Double
): List<LugarRepuesto> =
    withContext(Dispatchers.IO) {
        val resultado = mutableListOf<LugarRepuesto>()
        try {
            val url = URL("https://overpass-api.de/api/interpreter")
            val conexion = url.openConnection() as HttpURLConnection
            conexion.requestMethod = "POST"
            conexion.doOutput = true
            conexion.connectTimeout = 8000
            conexion.readTimeout = 12000
            conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            OutputStreamWriter(conexion.outputStream).use { writer ->
                writer.write("data=" + URLEncoder.encode(query, "UTF-8"))
            }

            if (conexion.responseCode !in 200..299) {
                conexion.disconnect()
                return@withContext emptyList()
            }

            val respuesta = conexion.inputStream.bufferedReader().use { it.readText() }
            conexion.disconnect()

            val json = JSONObject(respuesta)
            val elementos = json.optJSONArray("elements") ?: return@withContext emptyList()

            for (i in 0 until elementos.length()) {
                val elem = elementos.getJSONObject(i)
                val tags = elem.optJSONObject("tags")
                val nombre = tags?.optString("name", "REFACCIONARIA / TALLER") ?: "REFACCIONARIA / TALLER"

                val coords = obtenerCoordenadasElemento(elem) ?: continue
                val lat = coords.first
                val lon = coords.second

                val calle = tags?.optString("addr:street", "") ?: ""
                val numero = tags?.optString("addr:housenumber", "") ?: ""
                val ciudad = tags?.optString("addr:city", "") ?: ""
                val direccion = when {
                    calle.isNotBlank() && numero.isNotBlank() && ciudad.isNotBlank() -> "$calle $numero, $ciudad"
                    calle.isNotBlank() && numero.isNotBlank() -> "$calle $numero"
                    calle.isNotBlank() -> calle
                    ciudad.isNotBlank() -> ciudad
                    else -> "DIRECCIÓN CERCANA"
                }

                val distancia = distanciaEntreCoordenadas(latUsuario, lonUsuario, lat, lon)

                if (distancia <= 5000.0) {
                    resultado.add(
                        LugarRepuesto(
                            nombre = nombre.uppercase(),
                            lat = lat,
                            lon = lon,
                            direccion = direccion,
                            distanciaMetros = distancia
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        resultado.distinctBy { "${it.nombre}_${it.lat}_${it.lon}" }.sortedBy { it.distanciaMetros }
    }

fun obtenerCoordenadasElemento(elemento: JSONObject): Pair<Double, Double>? {
    return try {
        if (elemento.has("lat") && elemento.has("lon")) {
            Pair(elemento.getDouble("lat"), elemento.getDouble("lon"))
        } else if (elemento.has("center")) {
            val center = elemento.getJSONObject("center")
            Pair(center.getDouble("lat"), center.getDouble("lon"))
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

suspend fun buscarRepuestosCercanos(
    lat: Double,
    lon: Double,
    textoBusqueda: String,
    radioMetros: Int = 5000
): List<LugarRepuesto> =
    withContext(Dispatchers.IO) {
        val textoLimpio = textoBusqueda.trim()

        val resultadosNominatim = consultarNominatim(textoLimpio, lat, lon)
            .filter { it.distanciaMetros <= radioMetros }

        if (resultadosNominatim.isNotEmpty()) {
            return@withContext resultadosNominatim.sortedBy { it.distanciaMetros }
        }

        val queryOverpass = """
        [out:json][timeout:12];
        (
          nwr["shop"="car_parts"](around:$radioMetros,$lat,$lon);
          nwr["shop"="car_repair"](around:$radioMetros,$lat,$lon);
          nwr["shop"="auto_parts"](around:$radioMetros,$lat,$lon);
          nwr["amenity"="car_repair"](around:$radioMetros,$lat,$lon);
        );
        out center;
        """.trimIndent()

        val resultadosOverpass = consultarOverpass(queryOverpass, lat, lon)
            .filter { it.distanciaMetros <= radioMetros }

        if (resultadosOverpass.isNotEmpty()) {
            val filtrados = if (textoLimpio.isBlank()) {
                resultadosOverpass
            } else {
                resultadosOverpass.filter {
                    it.nombre.contains(textoLimpio, ignoreCase = true) || it.direccion.contains(textoLimpio, ignoreCase = true)
                }
            }
            if (filtrados.isNotEmpty()) return@withContext filtrados.sortedBy { it.distanciaMetros }
            return@withContext resultadosOverpass.sortedBy { it.distanciaMetros }
        }

        emptyList()
    }

// ======================================================
// PANTALLA DE BÚSQUEDA
// ======================================================

@Composable
fun BuscarRefaccionMapaScreen(
    onBuscarResultados: (String) -> Unit = {},
    onRegresar: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var textoBusqueda by remember { mutableStateOf("") }

    // LIMPIEZA AUTOMÁTICA AL ABRIR O REGRESAR A LA PANTALLA
    LaunchedEffect(Unit) {
        textoBusqueda = ""
    }

    fun abrirGoogleMapsConCoordenadasReales() {
        focusManager.clearFocus()
        keyboardController?.hide()

        obtenerUbicacionActual(context) { loc ->
            val lat = loc?.latitude ?: 19.4326
            val lon = loc?.longitude ?: -99.1332
            val q = if (textoBusqueda.isBlank()) "refaccionaria taller autopartes" else "refaccionaria ${textoBusqueda.trim()}"
            val queryEncoded = URLEncoder.encode(q, StandardCharsets.UTF_8.toString())

            val mapIntentUri = Uri.parse("geo:$lat,$lon?q=$queryEncoded&z=14")
            val mapIntent = Intent(Intent.ACTION_VIEW, mapIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            try {
                context.startActivity(mapIntent)
            } catch (_: Exception) {
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$queryEncoded&center=$lat,$lon")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "BUSCAR REFACCIONES, REPUESTOS Y TALLERES",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Encuentre refaccionarias y talleres cercanos (Máximo 5 km)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        // CAMPO DE BÚSQUEDA CON BOTÓN ÍCONO LIMPIAR AL FINAL (X)
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it.uppercase() },
            label = {
                Text(
                    text = "¿QUÉ REFACCIÓN BUSCAS?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo
                )
            },
            placeholder = {
                Text(
                    text = "EJEMPLO: BUJÍAS, ACEITE, FRENOS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo.copy(alpha = 0.6f)
                )
            },
            trailingIcon = {
                if (textoBusqueda.isNotBlank()) {
                    IconButton(onClick = {
                        textoBusqueda = ""
                        focusManager.clearFocus()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar Búsqueda",
                            tint = Color.White
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { abrirGoogleMapsConCoordenadasReales() }
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Colores.FondoSecundario,
                unfocusedContainerColor = Colores.FondoSecundario,
                disabledContainerColor = Colores.FondoSecundario,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Colores.TituloPrincipal,
                unfocusedLabelColor = Colores.EtiquetaCampo,
                cursorColor = Color.White,
                focusedIndicatorColor = Colores.BordeBoton,
                unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BOTÓN ROSA / MORADO DE LIMPIAR BÚSQUEDA
        if (textoBusqueda.isNotBlank()) {
            BotonModulo3D(
                texto = "LIMPIAR BÚSQUEDA",
                icono = "🧹",
                colorClaro = Color(0xFFF3A7FF),
                colorMedio = Color(0xFFD83CFF),
                colorOscuro = Color(0xFF7B1599),
                colorTexto = Color.Black,
                onClick = {
                    textoBusqueda = ""
                    focusManager.clearFocus()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                tamanioTexto = 15
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // BOTÓN DIRECTO AL MAPA CON GPS DEL USUARIO Y ZOOM 5 KM
        BotonModulo3D(
            texto = "BUSCAR REPUESTO EN MAPA",
            icono = "🗺️",
            colorClaro = Color(0xFFF3A7FF),
            colorMedio = Color(0xFFD83CFF),
            colorOscuro = Color(0xFF7B1599),
            onClick = { abrirGoogleMapsConCoordenadasReales() },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.Black
        )

        Spacer(modifier = Modifier.height(28.dp))

        // TARJETA ROJA PEQUEÑA CON RECOMENDACIÓN DE LA IA EN EL MAPA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFB51F1F)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🤖 Si no encuentras lo que buscas, pregúntale a la IA dentro del mapa",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BOTÓN REGRESAR VISIBLE
        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth().height(60.dp),
            tamanioTexto = 17,
            colorTexto = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
