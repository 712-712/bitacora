package com.example.bitacoraautomotriz.ui.inventario

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// =====================================
// MODELO DE DATOS
// =====================================

data class LugarRepuestoTexto(
    val nombre: String,
    val lat: Double,
    val lon: Double,
    val direccion: String,
    val distanciaMetros: Double
)

// =====================================
// FUNCIÓN QUE CONSULTA OVERPASS API (GRATIS, SIN API KEY)
// =====================================

suspend fun buscarRepuestosCercanosTexto(
    lat: Double,
    lon: Double,
    radioMetros: Int = 5000
): List<LugarRepuestoTexto> = withContext(Dispatchers.IO) {

    val query = """
        [out:json][timeout:25];
        (
          node["shop"="car_parts"](around:$radioMetros,$lat,$lon);
          node["shop"="car_repair"](around:$radioMetros,$lat,$lon);
          way["shop"="car_parts"](around:$radioMetros,$lat,$lon);
          way["shop"="car_repair"](around:$radioMetros,$lat,$lon);
        );
        out center;
    """.trimIndent()

    val resultado = mutableListOf<LugarRepuestoTexto>()

    try {
        val url = URL("https://overpass-api.de/api/interpreter")
        val conexion = url.openConnection() as HttpURLConnection

        conexion.requestMethod = "POST"
        conexion.doOutput = true
        conexion.setRequestProperty(
            "Content-Type",
            "application/x-www-form-urlencoded"
        )

        OutputStreamWriter(conexion.outputStream).use { writer ->
            writer.write("data=" + java.net.URLEncoder.encode(query, "UTF-8"))
        }

        val respuesta = conexion.inputStream.bufferedReader().use { it.readText() }

        val json = JSONObject(respuesta)
        val elementos: JSONArray = json.getJSONArray("elements")

        for (i in 0 until elementos.length()) {
            val elemento = elementos.getJSONObject(i)

            val latItem: Double
            val lonItem: Double

            if (elemento.has("center")) {
                val center = elemento.getJSONObject("center")
                latItem = center.getDouble("lat")
                lonItem = center.getDouble("lon")
            } else {
                latItem = elemento.optDouble("lat")
                lonItem = elemento.optDouble("lon")
            }

            val tags = elemento.optJSONObject("tags")
            val nombre = tags?.optString("name", "Refaccionaria / Taller sin nombre")
                ?: "Refaccionaria / Taller sin nombre"

            val calle = tags?.optString("addr:street", "") ?: ""
            val numero = tags?.optString("addr:housenumber", "") ?: ""
            val direccion = if (calle.isNotEmpty()) "$calle $numero".trim() else "Dirección no disponible"

            val distancia = distanciaEntreCoordenadasTexto(lat, lon, latItem, lonItem)

            resultado.add(
                LugarRepuestoTexto(
                    nombre = nombre,
                    lat = latItem,
                    lon = lonItem,
                    direccion = direccion,
                    distanciaMetros = distancia
                )
            )
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }

    resultado.sortedBy { it.distanciaMetros }
}

fun distanciaEntreCoordenadasTexto(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val resultados = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, resultados)
    return resultados[0].toDouble()
}

fun obtenerUbicacionActualTexto(context: Context, onUbicacion: (Location?) -> Unit) {

    val tienePermiso = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!tienePermiso) {
        onUbicacion(null)
        return
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val proveedores = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER
    )

    var mejorUbicacion: Location? = null

    for (proveedor in proveedores) {
        try {
            val ubicacion = locationManager.getLastKnownLocation(proveedor)
            if (ubicacion != null) {
                if (mejorUbicacion == null || ubicacion.accuracy < mejorUbicacion!!.accuracy) {
                    mejorUbicacion = ubicacion
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    onUbicacion(mejorUbicacion)
}

// =====================================
// PANTALLA: BÚSQUEDA DE REPUESTOS POR TEXTO/NOMBRE
// (con lista de refaccionarias/talleres cercanos)
// =====================================

@Composable
fun BuscarRepuestosScreen(
    onRegresar: () -> Unit = {}
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var textoBusqueda by remember { mutableStateOf("") }

    var permisoConcedido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var ubicacionUsuario by remember { mutableStateOf<Location?>(null) }
    var lugares by remember { mutableStateOf<List<LugarRepuestoTexto>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf("") }

    val lanzadorPermiso = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permisoConcedido = concedido
        if (!concedido) {
            mensajeError = "SE NECESITA EL PERMISO DE UBICACIÓN PARA BUSCAR REPUESTOS CERCANOS"
        }
    }

    LaunchedEffect(Unit) {
        if (!permisoConcedido) {
            lanzadorPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(permisoConcedido) {
        if (permisoConcedido) {
            cargando = true
            obtenerUbicacionActualTexto(context) { ubicacion ->
                ubicacionUsuario = ubicacion

                if (ubicacion != null) {
                    scope.launch {
                        lugares = buscarRepuestosCercanosTexto(ubicacion.latitude, ubicacion.longitude)
                        cargando = false
                    }
                } else {
                    mensajeError = "NO SE PUDO OBTENER TU UBICACIÓN. ACTIVA EL GPS E INTENTA DE NUEVO"
                    cargando = false
                }
            }
        }
    }

    // Filtra los resultados según lo que el usuario escriba
    val lugaresFiltrados = if (textoBusqueda.isBlank()) {
        lugares
    } else {
        lugares.filter {
            it.nombre.contains(textoBusqueda, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(onClick = onRegresar) {
                Text(text = "← Regresar")
            }
        }

        Text(
            text = "BUSCAR REPUESTO POR NOMBRE",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            label = { Text("Nombre del repuesto o negocio") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (cargando) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (lugaresFiltrados.isEmpty()) {
            Text(
                text = "NO SE ENCONTRARON RESULTADOS",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(lugaresFiltrados) { lugar ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = lugar.nombre,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(text = lugar.direccion)
                            Text(
                                text = "${(lugar.distanciaMetros / 1000).let { "%.1f".format(it) }} km de distancia"
                            )
                        }
                    }
                }
            }
        }
    }
}
