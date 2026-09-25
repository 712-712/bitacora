package com.example.bitacoraautomotriz.ui.inventario

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

// ======================================================
// COLORES
// ======================================================

val AzulPrincipalMapa = Color(0xFF1565C0)

val VerdeClaroMapa = Color(0xFF66BB6A)
val VerdeMedioMapa = Color(0xFF43A047)
val VerdeOscuroMapa = Color(0xFF2E7D32)

val GrisClaroMapa = Color(0xFFBDBDBD)
val GrisMedioMapa = Color(0xFF757575)
val GrisOscuroMapa = Color(0xFF424242)

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

    Location.distanceBetween(
        lat1,
        lon1,
        lat2,
        lon2,
        resultados
    )

    return resultados[0].toDouble()
}

// ======================================================
// OBTENER UBICACIÓN
// ======================================================

fun obtenerUbicacionActual(
    context: Context,
    onUbicacion: (Location?) -> Unit
) {

    val tienePermiso =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    if (!tienePermiso) {
        onUbicacion(null)
        return
    }

    val locationManager =
        context.getSystemService(
            Context.LOCATION_SERVICE
        ) as LocationManager

    // ----------------------------------------------
    // PRIMERO BUSCAMOS UNA UBICACIÓN YA CONOCIDA
    // ----------------------------------------------

    var mejorUbicacion: Location? = null

    val proveedores = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER
    )

    for (proveedor in proveedores) {

        try {

            val ubicacion =
                locationManager.getLastKnownLocation(
                    proveedor
                )

            if (ubicacion != null) {

                if (
                    mejorUbicacion == null ||
                    ubicacion.accuracy <
                    mejorUbicacion!!.accuracy
                ) {
                    mejorUbicacion = ubicacion
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ----------------------------------------------
    // SI YA TENEMOS UBICACIÓN, LA USAMOS
    // ----------------------------------------------

    if (mejorUbicacion != null) {

        onUbicacion(mejorUbicacion)

        return
    }

    // ----------------------------------------------
    // SI NO TENEMOS UBICACIÓN, PEDIMOS UNA NUEVA
    // ----------------------------------------------

    var ubicacionEntregada = false

    val listener =
        object : LocationListener {

            override fun onLocationChanged(
                location: Location
            ) {

                if (ubicacionEntregada) {
                    return
                }

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

        // GPS

        if (
            locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )
        ) {

            proveedorActivo = true

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                listener
            )
        }

        // RED

        if (
            locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        ) {

            proveedorActivo = true

            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                1f,
                listener
            )
        }

        // ------------------------------------------
        // SI NO HAY NINGÚN PROVEEDOR ACTIVO
        // ------------------------------------------

        if (!proveedorActivo) {

            onUbicacion(null)

            return
        }

        // ------------------------------------------
        // ESPERAMOS MÁXIMO 8 SEGUNDOS
        // ------------------------------------------

        Thread {

            try {

                Thread.sleep(8000)

                if (!ubicacionEntregada) {

                    ubicacionEntregada = true

                    try {
                        locationManager.removeUpdates(
                            listener
                        )
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
// ARCHIVO TEMPORAL PARA FOTOS
// ======================================================

fun crearArchivoImagenTemporal(context: Context): Uri {

    val carpeta = File(context.cacheDir, "fotos")

    if (!carpeta.exists()) {
        carpeta.mkdirs()
    }

    val archivo = File(
        carpeta,
        "foto_${System.currentTimeMillis()}.jpg"
    )

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
}

// ======================================================
// BUSCAR REFACCIONES
// ======================================================

suspend fun buscarRepuestosCercanos(
    lat: Double,
    lon: Double,
    textoBusqueda: String,
    radioMetros: Int = 5000
): List<LugarRepuesto> =
    withContext(Dispatchers.IO) {

        val texto =
            textoBusqueda
                .trim()
                .replace("\"", "")
                .replace("\\", "")

        val query =
            if (texto.isBlank()) {

                """
                [out:json][timeout:30];

                (
                  nwr["shop"="car_parts"]
                  (around:$radioMetros,$lat,$lon);

                  nwr["shop"="car_repair"]
                  (around:$radioMetros,$lat,$lon);
                );

                out center;
                """.trimIndent()

            } else {

                """
                [out:json][timeout:30];

                (
                  nwr["shop"="car_parts"]
                  ["name"~"$texto",i]
                  (around:$radioMetros,$lat,$lon);

                  nwr["shop"="car_parts"]
                  ["description"~"$texto",i]
                  (around:$radioMetros,$lat,$lon);

                  nwr["shop"="car_repair"]
                  ["name"~"$texto",i]
                  (around:$radioMetros,$lat,$lon);

                  nwr["shop"="car_repair"]
                  ["description"~"$texto",i]
                  (around:$radioMetros,$lat,$lon);
                );

                out center;
                """.trimIndent()
            }

        consultarOverpass(
            query = query,
            latUsuario = lat,
            lonUsuario = lon
        )
    }

// ======================================================
// BUSCAR TODOS LOS NEGOCIOS
// ======================================================

suspend fun buscarTodosLosNegociosCercanos(
    lat: Double,
    lon: Double,
    radioMetros: Int = 5000
): List<LugarRepuesto> =
    withContext(Dispatchers.IO) {

        val query = """

            [out:json][timeout:30];

            (
              nwr["shop"="car_parts"]
              (around:$radioMetros,$lat,$lon);

              nwr["shop"="car_repair"]
              (around:$radioMetros,$lat,$lon);
            );

            out center;

        """.trimIndent()

        consultarOverpass(
            query = query,
            latUsuario = lat,
            lonUsuario = lon
        )
    }

// ======================================================
// CONSULTA OVERPASS
// ======================================================

suspend fun consultarOverpass(
    query: String,
    latUsuario: Double,
    lonUsuario: Double
): List<LugarRepuesto> =
    withContext(Dispatchers.IO) {

        val resultado =
            mutableListOf<LugarRepuesto>()

        try {

            val url =
                URL(
                    "https://overpass-api.de/api/interpreter"
                )

            val conexion =
                url.openConnection()
                        as HttpURLConnection

            conexion.requestMethod = "POST"

            conexion.doOutput = true

            conexion.connectTimeout = 15000
            conexion.readTimeout = 30000

            conexion.setRequestProperty(
                "Content-Type",
                "application/x-www-form-urlencoded"
            )

            OutputStreamWriter(
                conexion.outputStream
            ).use { writer ->

                writer.write(
                    "data=" +
                            URLEncoder.encode(
                                query,
                                "UTF-8"
                            )
                )
            }

            val codigoRespuesta =
                conexion.responseCode

            if (
                codigoRespuesta !in 200..299
            ) {

                conexion.disconnect()

                return@withContext emptyList()
            }

            val respuesta =
                conexion.inputStream
                    .bufferedReader()
                    .use {
                        it.readText()
                    }

            conexion.disconnect()

            val json =
                JSONObject(respuesta)

            val elementos =
                json.optJSONArray(
                    "elements"
                )
                    ?: return@withContext emptyList()

            for (
            i in 0 until elementos.length()
            ) {

                val elemento =
                    elementos.getJSONObject(i)

                val coordenadas =
                    obtenerCoordenadasElemento(
                        elemento
                    )
                        ?: continue

                val lat =
                    coordenadas.first

                val lon =
                    coordenadas.second

                val tags =
                    elemento.optJSONObject(
                        "tags"
                    )

                val nombre =
                    tags?.optString(
                        "name",
                        ""
                    ) ?: ""

                val nombreFinal =
                    if (
                        nombre.isBlank()
                    ) {
                        "REFACCIONARIA / TALLER"
                    } else {
                        nombre
                    }

                val calle =
                    tags?.optString(
                        "addr:street",
                        ""
                    ) ?: ""

                val numero =
                    tags?.optString(
                        "addr:housenumber",
                        ""
                    ) ?: ""

                val ciudad =
                    tags?.optString(
                        "addr:city",
                        ""
                    ) ?: ""

                val direccion =
                    when {

                        calle.isNotBlank() &&
                                numero.isNotBlank() &&
                                ciudad.isNotBlank() ->

                            "$calle $numero, $ciudad"

                        calle.isNotBlank() &&
                                numero.isNotBlank() ->

                            "$calle $numero"

                        calle.isNotBlank() ->
                            calle

                        ciudad.isNotBlank() ->
                            ciudad

                        else ->
                            "DIRECCIÓN NO DISPONIBLE"
                    }

                val distancia =
                    distanciaEntreCoordenadas(
                        latUsuario,
                        lonUsuario,
                        lat,
                        lon
                    )

                resultado.add(
                    LugarRepuesto(
                        nombre =
                            nombreFinal,
                        lat =
                            lat,
                        lon =
                            lon,
                        direccion =
                            direccion,
                        distanciaMetros =
                            distancia
                    )
                )
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }

        resultado
            .distinctBy {
                "${it.nombre}_${it.lat}_${it.lon}"
            }
            .sortedBy {
                it.distanciaMetros
            }
    }

// ======================================================
// COORDENADAS
// ======================================================

fun obtenerCoordenadasElemento(
    elemento: JSONObject
): Pair<Double, Double>? {

    return try {

        if (
            elemento.has("lat") &&
            elemento.has("lon")
        ) {

            Pair(
                elemento.getDouble("lat"),
                elemento.getDouble("lon")
            )

        } else if (
            elemento.has("center")
        ) {

            val center =
                elemento.getJSONObject(
                    "center"
                )

            Pair(
                center.getDouble("lat"),
                center.getDouble("lon")
            )

        } else {

            null
        }

    } catch (e: Exception) {

        null
    }
}

// ======================================================
// PANTALLA
// ======================================================

@Composable
fun BuscarRefaccionMapaScreen(
    onRegresar: () -> Unit = {}
) {

    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    var permisoConcedido by remember {

        mutableStateOf(

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    var ubicacionUsuario by remember {
        mutableStateOf<Location?>(null)
    }

    var lugares by remember {
        mutableStateOf<List<LugarRepuesto>>(
            emptyList()
        )
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    var textoBusqueda by remember {
        mutableStateOf("")
    }

    var fotoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var fotoBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    val lanzadorPermiso =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { concedido ->

            permisoConcedido =
                concedido

            if (concedido) {

                mensaje =
                    "UBICACIÓN ACTIVADA. PRESIONA BUSCAR."

            } else {

                mensaje =
                    "SE NECESITA EL PERMISO DE UBICACIÓN."
            }
        }

    val lanzadorCamara =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.TakePicture()
        ) { exito ->

            if (exito && fotoUri != null) {

                try {

                    val stream =
                        context.contentResolver
                            .openInputStream(fotoUri!!)

                    fotoBitmap =
                        BitmapFactory.decodeStream(stream)

                    stream?.close()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    val lanzadorPermisoCamara =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestPermission()
        ) { concedido ->

            if (concedido) {

                val uri =
                    crearArchivoImagenTemporal(context)

                fotoUri = uri

                lanzadorCamara.launch(uri)
            }
        }

    // ==================================================
    // PERMISO AL ENTRAR
    // ==================================================

    LaunchedEffect(Unit) {

        if (!permisoConcedido) {

            lanzadorPermiso.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        } else {

            obtenerUbicacionActual(
                context
            ) { ubicacion ->

                ubicacionUsuario =
                    ubicacion

                if (
                    ubicacion == null
                ) {

                    mensaje =
                        "PRESIONA BUSCAR PARA OBTENER TU UBICACIÓN."
                }
            }
        }
    }

    // ==================================================
    // PANTALLA
    // ==================================================

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    AzulPrincipalMapa
                )
                .padding(12.dp),

        verticalArrangement =
            Arrangement.Top
    ) {

        // ----------------------------------------------
        // TÍTULO
        // ----------------------------------------------

        Text(

            text =
                "REFACCIONARIAS Y TALLERES CERCA DE TI",

            fontSize =
                22.sp,

            fontWeight =
                FontWeight.Bold,

            color =
                Color.White,

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        // ----------------------------------------------
        // CAMPO DE BÚSQUEDA
        // ----------------------------------------------

        OutlinedTextField(

            value =
                textoBusqueda,

            onValueChange = {
                textoBusqueda = it
            },

            label = {

                Text(

                    text =
                        "¿QUÉ REFACCIÓN BUSCAS?",

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            },

            placeholder = {

                Text(

                    text =
                        "Ejemplo: BUJÍAS, ACEITE, FILTROS",

                    fontSize =
                        15.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            },

            singleLine =
                true,

            shape =
                RoundedCornerShape(12.dp),

            textStyle =
                TextStyle(

                    fontSize =
                        18.sp,

                    fontWeight =
                        FontWeight.Bold
                ),

            colors =
                TextFieldDefaults.colors(

                    focusedContainerColor =
                        Color.White,

                    unfocusedContainerColor =
                        Color.White,

                    disabledContainerColor =
                        Color.White,

                    focusedTextColor =
                        Color.Black,

                    unfocusedTextColor =
                        Color.Black,

                    focusedLabelColor =
                        Color.Black,

                    unfocusedLabelColor =
                        Color.Black,

                    cursorColor =
                        Color.Black,

                    focusedIndicatorColor =
                        Color.Black,

                    unfocusedIndicatorColor =
                        Color.Black
                ),

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        // ----------------------------------------------
        // BOTÓN BUSCAR
        // ----------------------------------------------

        BotonModulo3D(

            texto =
                "🔎  BUSCAR REFACCIÓN CERCA DE MÍ",

            colorClaro =
                VerdeClaroMapa,

            colorMedio =
                VerdeMedioMapa,

            colorOscuro =
                VerdeOscuroMapa,

            onClick = {

                // MOSTRAR INMEDIATAMENTE QUE EL BOTÓN FUNCIONÓ

                cargando =
                    true

                lugares =
                    emptyList()

                mensaje =
                    "OBTENIENDO TU UBICACIÓN..."

                // --------------------------------------
                // PERMISO
                // --------------------------------------

                if (!permisoConcedido) {

                    cargando =
                        false

                    mensaje =
                        "ACTIVANDO PERMISO DE UBICACIÓN..."

                    lanzadorPermiso.launch(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )

                    return@BotonModulo3D
                }

                // --------------------------------------
                // OBTENER UBICACIÓN
                // --------------------------------------

                obtenerUbicacionActual(
                    context
                ) { ubicacion ->

                    if (
                        ubicacion == null
                    ) {

                        cargando =
                            false

                        mensaje =
                            "NO SE PUDO OBTENER TU UBICACIÓN. VERIFICA LA UBICACIÓN DEL EMULADOR."

                        return@obtenerUbicacionActual
                    }

                    ubicacionUsuario =
                        ubicacion

                    mensaje =
                        "BUSCANDO REFACCIONES CERCA DE TI..."

                    // ----------------------------------
                    // BUSCAR EN INTERNET
                    // ----------------------------------

                    scope.launch {

                        try {

                            val texto =
                                textoBusqueda.trim()

                            var resultados =
                                buscarRepuestosCercanos(

                                    lat =
                                        ubicacion.latitude,

                                    lon =
                                        ubicacion.longitude,

                                    textoBusqueda =
                                        texto,

                                    radioMetros =
                                        5000
                                )

                            // --------------------------------
                            // SI NO HAY COINCIDENCIAS,
                            // MOSTRAR NEGOCIOS CERCANOS
                            // --------------------------------

                            if (
                                resultados.isEmpty()
                            ) {

                                mensaje =
                                    "BUSCANDO NEGOCIOS CERCANOS..."

                                resultados =
                                    buscarTodosLosNegociosCercanos(

                                        lat =
                                            ubicacion.latitude,

                                        lon =
                                            ubicacion.longitude,

                                        radioMetros =
                                            5000
                                    )

                                if (
                                    texto.isNotBlank() &&
                                    resultados.isNotEmpty()
                                ) {

                                    mensaje =
                                        "NO SE PUDO CONFIRMAR \"$texto\". SE MUESTRAN NEGOCIOS CERCANOS."
                                }
                            }

                            lugares =
                                resultados

                            // --------------------------------
                            // MENSAJES FINALES
                            // --------------------------------

                            if (
                                resultados.isEmpty()
                            ) {

                                mensaje =
                                    "NO SE ENCONTRARON REFACCIONARIAS O TALLERES EN UN RADIO DE 5 KM."

                            } else if (
                                texto.isBlank()
                            ) {

                                mensaje =
                                    "SE ENCONTRARON ${resultados.size} NEGOCIOS CERCANOS."

                            } else if (
                                !mensaje.contains(
                                    "NO SE PUDO CONFIRMAR"
                                )
                            ) {

                                mensaje =
                                    "SE ENCONTRARON ${resultados.size} NEGOCIOS CERCANOS."
                            }

                            cargando =
                                false

                        } catch (e: Exception) {

                            e.printStackTrace()

                            lugares =
                                emptyList()

                            cargando =
                                false

                            mensaje =
                                "NO SE PUDO REALIZAR LA BÚSQUEDA. VERIFICA TU CONEXIÓN A INTERNET."
                        }
                    }
                }
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        // =================================================
        // ESPACIO ANTES DEL MAPA (bajado un poco más)
        // =================================================

        Spacer(
            modifier =
                Modifier.height(80.dp)
        )

        // =================================================
        // MAPA
        // =================================================

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(top = 8.dp)
        ) {

            AndroidView(

                modifier =
                    Modifier.fillMaxSize(),

                factory = { ctx ->

                    val config =
                        Configuration.getInstance()

                    config.userAgentValue =
                        "BitacoraAutomotriz-App/1.0"

                    config.osmdroidBasePath =
                        ctx.cacheDir

                    config.osmdroidTileCache =
                        java.io.File(
                            ctx.cacheDir,
                            "osmdroid_tiles"
                        )

                    MapView(ctx).apply {

                        setTileSource(
                            TileSourceFactory.MAPNIK
                        )

                        setMultiTouchControls(
                            true
                        )

                        controller.setZoom(
                            14.0
                        )
                    }
                },

                update = { mapView ->

                    mapView.overlays.clear()

                    val centro =
                        ubicacionUsuario?.let {

                            GeoPoint(
                                it.latitude,
                                it.longitude
                            )

                        } ?: GeoPoint(
                            19.4326,
                            -99.1332
                        )

                    mapView.controller.setCenter(
                        centro
                    )

                    // --------------------------------
                    // MARCADOR DEL USUARIO
                    // --------------------------------

                    if (
                        ubicacionUsuario != null
                    ) {

                        val marcadorYo =
                            Marker(mapView)

                        marcadorYo.position =
                            centro

                        marcadorYo.title =
                            "TÚ ESTÁS AQUÍ"

                        mapView.overlays.add(
                            marcadorYo
                        )
                    }

                    // --------------------------------
                    // MARCADORES DE NEGOCIOS
                    // --------------------------------

                    lugares.forEach { lugar ->

                        val marcador =
                            Marker(mapView)

                        marcador.position =
                            GeoPoint(
                                lugar.lat,
                                lugar.lon
                            )

                        marcador.title =
                            lugar.nombre

                        marcador.snippet =
                            lugar.direccion

                        mapView.overlays.add(
                            marcador
                        )
                    }

                    mapView.invalidate()
                }
            )
        }

        // =================================================
        // FOTO DEL AUTO O REFACCIÓN
        // =================================================

        BotonModulo3D(

            texto =
                "📷  TOMAR FOTO DEL AUTO O REFACCIÓN",

            colorClaro =
                VerdeClaroMapa,

            colorMedio =
                VerdeMedioMapa,

            colorOscuro =
                VerdeOscuroMapa,

            onClick = {

                val tienePermisoCamara =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                if (tienePermisoCamara) {

                    val uri =
                        crearArchivoImagenTemporal(context)

                    fotoUri = uri

                    lanzadorCamara.launch(uri)

                } else {

                    lanzadorPermisoCamara.launch(
                        Manifest.permission.CAMERA
                    )
                }
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        if (fotoBitmap != null) {

            Image(
                bitmap =
                    fotoBitmap!!.asImageBitmap(),

                contentDescription =
                    "Foto tomada",

                contentScale =
                    ContentScale.Crop,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
            )
        }

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        // =================================================
        // RESULTADOS / MENSAJE
        // =================================================

        if (cargando) {

            Box(

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),

                contentAlignment =
                    Alignment.Center
            ) {

                Column(

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator(
                        color =
                            Color.White
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(

                        text =
                            mensaje,

                        color =
                            Color.White,

                        fontSize =
                            18.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

        } else {

            if (
                mensaje.isNotEmpty()
            ) {

                Text(

                    text =
                        mensaje,

                    color =
                        Color.White,

                    fontSize =
                        17.sp,

                    fontWeight =
                        FontWeight.Bold,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 4.dp
                            )
                )

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )
            }

            if (
                lugares.isNotEmpty()
            ) {

                LazyColumn(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                ) {

                    items(
                        items =
                            lugares
                    ) { lugar ->

                        Card(

                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 4.dp
                                    ),

                            shape =
                                RoundedCornerShape(
                                    12.dp
                                ),

                            colors =
                                CardDefaults
                                    .cardColors(
                                        containerColor =
                                            Color.White
                                    )
                        ) {

                            Column(

                                modifier =
                                    Modifier.padding(
                                        14.dp
                                    )
                            ) {

                                Text(

                                    text =
                                        lugar.nombre,

                                    color =
                                        Color.Black,

                                    fontSize =
                                        19.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(

                                    text =
                                        lugar.direccion,

                                    color =
                                        Color.Black,

                                    fontSize =
                                        16.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(

                                    text =
                                        String.format(

                                            Locale.US,

                                            "%.1f KM DE DISTANCIA",

                                            lugar.distanciaMetros /
                                                    1000
                                        ),

                                    color =
                                        AzulPrincipalMapa,

                                    fontSize =
                                        16.sp,

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }
                }

            } else {

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        // =================================================
        // REGRESAR
        // =================================================

        BotonModulo3D(

            texto =
                "←  REGRESAR",

            colorClaro =
                GrisClaroMapa,

            colorMedio =
                GrisMedioMapa,

            colorOscuro =
                GrisOscuroMapa,

            onClick =
                onRegresar,

            modifier =
                Modifier.fillMaxWidth()
        )
    }
}
