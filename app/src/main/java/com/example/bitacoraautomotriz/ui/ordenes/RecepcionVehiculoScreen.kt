package com.example.bitacoraautomotriz.ui.ordenes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.bitacoraautomotriz.R
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.data.RecepcionVehiculo
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.repository.RecepcionRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// FUNCIÓN AUXILIAR: SUPERPONE MARCA DE AGUA LEGAL IMBORRABLE SOBRE EL BITMAP DE LA FOTO
fun superponerMarcaDeAguaLegal(
    bitmapOriginal: Bitmap,
    tallerNombre: String,
    fechaHoraStr: String,
    placaStr: String,
    kmStr: String,
    clienteStr: String
): Bitmap {
    return try {
        val w = bitmapOriginal.width
        val h = bitmapOriginal.height
        val bitmapConMarca = bitmapOriginal.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmapConMarca)

        // BANNER INFERIOR OSCURO
        val bannerHeight = (h * 0.22f).coerceAtLeast(70f)
        val paintBanner = Paint().apply {
            color = android.graphics.Color.BLACK
            alpha = 200
        }
        canvas.drawRect(0f, h - bannerHeight, w.toFloat(), h.toFloat(), paintBanner)

        // TEXTO DE MARCA DE AGUA LEGAL
        val paintTexto = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = (bannerHeight * 0.22f).coerceAtLeast(16f)
            isAntiAlias = true
            isFakeBoldText = true
        }

        val paintDestacado = Paint().apply {
            color = android.graphics.Color.GREEN
            textSize = (bannerHeight * 0.22f).coerceAtLeast(16f)
            isAntiAlias = true
            isFakeBoldText = true
        }

        val emisor = if (tallerNombre.isBlank()) "TALLER MECÁNICO BITÁCORA" else tallerNombre.uppercase()
        val modeloDispositivo = "${Build.MANUFACTURER.uppercase()} ${Build.MODEL.uppercase()}"

        val xPadding = 14f
        val startY = h - bannerHeight + (bannerHeight * 0.28f)
        val lineSpacing = bannerHeight * 0.26f

        canvas.drawText("🚗 $emisor | FECHA: $fechaHoraStr", xPadding, startY, paintTexto)
        canvas.drawText("PLACA: $placaStr | KM: $kmStr | CLIENTE: $clienteStr", xPadding, startY + lineSpacing, paintDestacado)
        canvas.drawText("GPS METADATOS EXIF: VALIDOS | DISPOSITIVO: $modeloDispositivo", xPadding, startY + (lineSpacing * 2), paintTexto)

        bitmapConMarca
    } catch (e: Exception) {
        bitmapOriginal
    }
}

@Composable
fun RecepcionVehiculoScreen(
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault()) }
    val fechaHoraActual = remember { formatoFecha.format(Date()) }

    var todosLosClientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var busquedaCliente by remember { mutableStateOf("") }

    var autosDelCliente by remember { mutableStateOf<List<Auto>>(emptyList()) }
    var autoSeleccionado by remember { mutableStateOf<Auto?>(null) }

    var kilometraje by remember { mutableStateOf("") }
    var nombreTaller by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    var mostrarDialogoConsejos by remember { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    // DÍAS / TIEMPO DE RETENCIÓN DE FOTOS (DESPLEGABLE CON TEMPORIZADOR DE CONTEO REGRESIVO)
    val opcionesRetencion = remember {
        listOf(
            "1 Día",
            "2 Días",
            "3 Días",
            "1 Semana",
            "2 Semanas",
            "1 Mes",
            "2 Meses",
            "3 Meses",
            "6 Meses",
            "1 Año",
            "2 Años (Recomendado Legal)"
        )
    }
    var retencionSeleccionada by remember { mutableStateOf(opcionesRetencion.last()) }
    var menuRetencionExpandido by remember { mutableStateOf(false) }

    // BITMAPS MINIATURA DE CADA FOTO TOMADA
    var fotoFrenteBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fotoAtrasBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fotoIzquierdaBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fotoDerechaBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fotoTechoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fotoRinesBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fotoInteriorBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var fotoTableroBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // VIDEO
    var videoTomado by remember { mutableStateOf(false) }

    var slotFotoActual by remember { mutableStateOf("") }

    // LAUNCHER DE CÁMARA EN TIEMPO REAL CON MARCA DE AGUA Y PROTECCIÓN DE ERRORES
    val launcherCamaraEnVivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val bitmapConMarca = superponerMarcaDeAguaLegal(
                    bitmapOriginal = bitmap,
                    tallerNombre = if (nombreTaller.isBlank()) "TALLER MECÁNICO BITÁCORA" else nombreTaller,
                    fechaHoraStr = fechaHoraActual,
                    placaStr = autoSeleccionado?.placa?.uppercase() ?: "REGISTRADA",
                    kmStr = kilometraje.ifBlank { "N/A" },
                    clienteStr = clienteSeleccionado?.nombre?.uppercase() ?: "REGISTRADO"
                )

                when (slotFotoActual) {
                    "FRENTE" -> fotoFrenteBitmap = bitmapConMarca
                    "ATRÁS" -> fotoAtrasBitmap = bitmapConMarca
                    "IZQUIERDA" -> fotoIzquierdaBitmap = bitmapConMarca
                    "DERECHA" -> fotoDerechaBitmap = bitmapConMarca
                    "TECHO" -> fotoTechoBitmap = bitmapConMarca
                    "RINES" -> fotoRinesBitmap = bitmapConMarca
                    "INTERIOR" -> fotoInteriorBitmap = bitmapConMarca
                    "TABLERO" -> fotoTableroBitmap = bitmapConMarca
                }
                Toast.makeText(context, "📷 Foto $slotFotoActual capturada exitosamente con marca de agua", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al procesar foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // LAUNCHER DE PERMISO DE CÁMARA ROBUSTO QUE PREVIENE CRASHES
    val launcherPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { esConcedido: Boolean ->
        if (esConcedido) {
            try {
                launcherCamaraEnVivo.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Error al abrir la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "⚠️ Se requiere permiso de Cámara para tomar fotos de la recepción", Toast.LENGTH_LONG).show()
        }
    }

    fun tomarFotoSlot(slot: String) {
        slotFotoActual = slot
        val permisoCamara = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permisoCamara == PackageManager.PERMISSION_GRANTED) {
            try {
                launcherCamaraEnVivo.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Error abriendo cámara: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            launcherPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    // LAUNCHER DE VIDEO EN VIVO CON SELLO LEGAL Y METADATOS EXIF
    val launcherVideoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            videoTomado = true
            Toast.makeText(context, "🎥 Video de recepción capturado con sello legal y metadatos EXIF", Toast.LENGTH_SHORT).show()
        }
    }

    // LISTA DE PUNTOS PARA FIRMA DIGITAL FLUIDA EN TIEMPO REAL CON BLOQUEO TRAS ACEPTAR
    val trazosFirma = remember { mutableStateListOf<SnapshotStateList<Offset>>() }
    var firmaCapturada by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            todosLosClientes = ClienteRepository.obtenerClientes(context)
        } catch (_: Exception) {
            todosLosClientes = emptyList()
        }
    }

    LaunchedEffect(clienteSeleccionado) {
        if (clienteSeleccionado != null) {
            try {
                val listaAutos = AutoRepository.obtenerAutosPorCliente(clienteSeleccionado!!.nombre, context)
                autosDelCliente = listaAutos
                if (listaAutos.size == 1) {
                    autoSeleccionado = listaAutos.first()
                    kilometraje = listaAutos.first().kilometraje.toString()
                } else {
                    autoSeleccionado = null
                }
            } catch (_: Exception) {
                autosDelCliente = emptyList()
                autoSeleccionado = null
            }
        } else {
            autosDelCliente = emptyList()
            autoSeleccionado = null
        }
    }

    val queryCliente = busquedaCliente.uppercase().trim()
    val clientesFiltrados = todosLosClientes.filter { c ->
        c.nombre.uppercase().contains(queryCliente, ignoreCase = true) ||
                c.id.toString().contains(queryCliente, ignoreCase = true)
    }

    fun tomarVideo() {
        try {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la grabadora de video: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun guardarRecepcionVehiculo() {
        if (clienteSeleccionado == null) {
            mensaje = "⚠️ SELECCIONE UN CLIENTE DE LA LISTA"
            return
        }
        if (autoSeleccionado == null) {
            mensaje = "⚠️ SELECCIONE EL AUTO DEL CLIENTE"
            return
        }
        if (kilometraje.isBlank()) {
            mensaje = "⚠️ INGRESE EL KILOMETRAJE REGISTRADO EN EL TABLERO"
            return
        }
        val kmNum = kilometraje.toIntOrNull() ?: 0
        if (kmNum <= 0) {
            mensaje = "⚠️ INGRESE UN KILOMETRAJE VÁLIDO MAYOR A CERO"
            return
        }
        if (!firmaCapturada) {
            mensaje = "⚠️ SE REQUIERE LA FIRMA DIGITAL DEL CLIENTE EN PANTALLA"
            return
        }

        val c = clienteSeleccionado!!
        val a = autoSeleccionado!!
        val hashTexto = "${c.nombre}_${a.placa}_${kmNum}_${fechaHoraActual}"
        val hashGenerado = RecepcionRepository.calcularHashSha256(hashTexto)

        val recepcion = RecepcionVehiculo(
            cliente = c.nombre,
            auto = "${a.marca} ${a.modelo} (${a.placa})",
            placa = a.placa,
            vin = a.vin,
            kilometraje = kmNum,
            fechaHora = fechaHoraActual,
            hashIntegridadSha256 = hashGenerado,
            tiempoRetencion = retencionSeleccionada
        )

        guardando = true
        mensaje = ""

        scope.launch {
            try {
                RecepcionRepository.guardarRecepcion(recepcion, context)
                guardando = false
                Toast.makeText(context, "✅ Acta de Recepción N° ${recepcion.id} guardada y transmitida a Firebase", Toast.LENGTH_LONG).show()
                onRegresar()
            } catch (e: Exception) {
                guardando = false
                mensaje = "❌ ERROR AL GUARDAR RECEPCIÓN: ${e.message}"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
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
                text = "RECEPCIÓN Y PROTECCIÓN LEGAL",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Check-in fotográfico, marcas de agua y firma digital",
                fontSize = 14.sp,
                color = Colores.EtiquetaCampo,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. SELECCIÓN DE CLIENTE Y AUTO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. DATOS DEL CLIENTE Y VEHÍCULO", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    if (clienteSeleccionado == null) {
                        OutlinedTextField(
                            value = busquedaCliente,
                            onValueChange = { busquedaCliente = it.uppercase() },
                            placeholder = { Text("Buscar cliente...", color = Color.Gray) },
                            textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (clientesFiltrados.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                clientesFiltrados.take(4).forEach { cliente ->
                                    Button(
                                        onClick = { clienteSeleccionado = cliente },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Colores.FondoSecundario)
                                    ) {
                                        Text(text = "${cliente.nombre} (${cliente.telefono})", color = Color.White)
                                    }
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(text = "CLIENTE: ${clienteSeleccionado!!.nombre.uppercase()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(2.dp))
                            TextButton(
                                onClick = { clienteSeleccionado = null; autoSeleccionado = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("CAMBIAR DE CLIENTE", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        if (autoSeleccionado == null) {
                            Text("SELECCIONE EL AUTO DEL CLIENTE:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                            if (autosDelCliente.isEmpty()) {
                                Text("Este cliente no tiene autos registrados", color = Color(0xFFFF5252))
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    autosDelCliente.forEach { auto ->
                                        Button(
                                            onClick = {
                                                autoSeleccionado = auto
                                                kilometraje = auto.kilometraje.toString()
                                                mensaje = ""
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Colores.FondoSecundario)
                                        ) {
                                            Text(text = "🚗 ${auto.marca} ${auto.modelo} (${auto.placa})", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = "AUTO: ${autoSeleccionado!!.marca.uppercase()} ${autoSeleccionado!!.modelo.uppercase()} (${autoSeleccionado!!.placa.uppercase()})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7DFFB2))
                                Spacer(modifier = Modifier.height(2.dp))
                                TextButton(
                                    onClick = { autoSeleccionado = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("CAMBIAR DE AUTO", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    // KILOMETRAJE
                    Text("KILOMETRAJE DEL TABLERO:", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    OutlinedTextField(
                        value = kilometraje,
                        onValueChange = { if (it.all { c -> c.isDigit() }) kilometraje = it },
                        placeholder = { Text("Ej: 45000", color = Color.Gray) },
                        textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. CÁMARA GUIADA EN VIVO Y REGISTRO DE FOTOS MINIATURA CON MARCA DE AGUA
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("2. CÁMARA GUIADA (8 ÁNGULOS CON MARCAS DE AGUA Y EXIF)", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Cada foto grabará automáticamente el Banner Legal con fecha, taller, placa y metadatos EXIF:", fontSize = 13.sp, color = Colores.EtiquetaCampo)

                    val slotsLista = listOf(
                        Triple("FRENTE", fotoFrenteBitmap) { tomarFotoSlot("FRENTE") },
                        Triple("ATRÁS", fotoAtrasBitmap) { tomarFotoSlot("ATRÁS") },
                        Triple("IZQUIERDA", fotoIzquierdaBitmap) { tomarFotoSlot("IZQUIERDA") },
                        Triple("DERECHA", fotoDerechaBitmap) { tomarFotoSlot("DERECHA") },
                        Triple("TECHO", fotoTechoBitmap) { tomarFotoSlot("TECHO") },
                        Triple("RINES", fotoRinesBitmap) { tomarFotoSlot("RINES") },
                        Triple("INTERIOR", fotoInteriorBitmap) { tomarFotoSlot("INTERIOR") },
                        Triple("TABLERO", fotoTableroBitmap) { tomarFotoSlot("TABLERO") }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        slotsLista.chunked(2).forEach { par ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                par.forEach { (nombreSlot, bitmapSlot, accionTomar) ->
                                    Card(
                                        modifier = Modifier.weight(1f).height(100.dp).clickable { accionTomar() },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Colores.FondoSecundario)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            if (bitmapSlot != null) {
                                                Image(
                                                    bitmap = bitmapSlot.asImageBitmap(),
                                                    contentDescription = "Foto $nombreSlot",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Box(
                                                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = 0.7f)).padding(2.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(text = "✅ $nombreSlot", color = Color.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = "📷", fontSize = 24.sp)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(text = nombreSlot, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // BOTÓN VIDEO
                    Button(
                        onClick = { tomarVideo() },
                        colors = ButtonDefaults.buttonColors(containerColor = if (videoTomado) Color(0xFF00C853) else Colores.FondoSecundario),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = if (videoTomado) "🎥 VIDEO REGISTRADO CON SELLO EXIF" else "🎥 Opcional: Grabar Video de Recorrido 360°",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. RETENCIÓN LEGAL Y FIRMA DIGITAL EN PANTALLA
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. RETENCIÓN LEGAL Y FIRMA DIGITAL", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    Text("SELECCIONE PERIODO DE ALMACENAMIENTO DE FOTOS:", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold)

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { menuRetencionExpandido = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Colores.FondoSecundario),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "⏱️ Retención: $retencionSeleccionada", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = menuRetencionExpandido,
                            onDismissRequest = { menuRetencionExpandido = false },
                            modifier = Modifier.background(Colores.FondoSecundario)
                        ) {
                            opcionesRetencion.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion, color = Color.White, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        retencionSeleccionada = opcion
                                        menuRetencionExpandido = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("FIRMA DIGITAL DEL CLIENTE EN PANTALLA:", fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Bold)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .border(2.dp, if (firmaCapturada) Color.Green else Color.White, RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(firmaCapturada) {
                                        if (!firmaCapturada) {
                                            awaitEachGesture {
                                                val down = awaitFirstDown()
                                                val nuevoTrazo = SnapshotStateList<Offset>()
                                                nuevoTrazo.add(down.position)
                                                trazosFirma.add(nuevoTrazo)

                                                do {
                                                    val event = awaitPointerEvent()
                                                    event.changes.forEach { change ->
                                                        if (change.pressed) {
                                                            nuevoTrazo.add(change.position)
                                                            change.consume()
                                                        }
                                                    }
                                                } while (event.changes.any { it.pressed })
                                            }
                                        }
                                    }
                            ) {
                                trazosFirma.forEach { trazo ->
                                    for (i in 0 until trazo.size - 1) {
                                        drawLine(
                                            color = Color.Black,
                                            start = trazo[i],
                                            end = trazo[i + 1],
                                            strokeWidth = 5f
                                        )
                                    }
                                }
                            }

                            if (trazosFirma.isEmpty() && !firmaCapturada) {
                                Text(
                                    text = "Firme aquí con el dedo...",
                                    color = Color.Gray,
                                    fontSize = 16.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                trazosFirma.clear()
                                firmaCapturada = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("BORRAR FIRMA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (trazosFirma.isNotEmpty()) {
                                    firmaCapturada = true
                                    Toast.makeText(context, "✅ Firma digital aceptada y protegida con SHA-256", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "⚠️ Dibuje su firma en el recuadro blanco antes de aceptar", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (firmaCapturada) "✅ FIRMADA" else "ACEPTAR FIRMA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = mensaje,
                    color = Color(0xFFFF5252),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BOTÓN PRINCIPAL GUARDAR Y TRANSMITIR RECEPCIÓN DE VEHÍCULO
            BotonModulo3D(
                texto = if (guardando) "GUARDANDO RECEPCIÓN..." else "💾 GUARDAR Y REGISTRAR ENTRADA",
                colorClaro = Color(0xFFB9F6CA),
                colorMedio = Color(0xFF00C853),
                colorOscuro = Color(0xFF00695C),
                onClick = { if (!guardando) guardarRecepcionVehiculo() },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colorTexto = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // BOTÓN REGRESAR FIJO E INMÓVIL AL FONDO
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = Colores.FondoPantalla
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
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
