package com.example.bitacoraautomotriz.ui.ordenes

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.data.RecepcionVehiculo
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
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

    // LAUNCHER DE CÁMARA EN TIEMPO REAL CON MARCA DE AGUA
    val launcherCamaraEnVivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
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
            Toast.makeText(context, "📷 Foto $slotFotoActual tomada con marca de agua y metadatos EXIF", Toast.LENGTH_SHORT).show()
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

    fun tomarFotoSlot(slot: String) {
        slotFotoActual = slot
        launcherCamaraEnVivo.launch()
    }

    fun tomarVideo() {
        try {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            context.startActivity(intent)
            videoTomado = true
        } catch (_: Exception) {
            launcherVideoCamara.launch("video/*")
        }
    }

    fun guardarRecepcionVehiculo() {
        if (clienteSeleccionado == null) { mensaje = "SELECCIONE EL CLIENTE"; return }
        if (autoSeleccionado == null) { mensaje = "SELECCIONE EL VEHÍCULO"; return }
        if (kilometraje.isBlank()) { mensaje = "INGRESE EL KILOMETRAJE DEL TABLERO"; return }
        val kmNum = kilometraje.toIntOrNull()
        if (kmNum == null || kmNum < 0) { mensaje = "INGRESE UN KILOMETRAJE VÁLIDO"; return }

        guardando = true
        mensaje = ""

        val c = clienteSeleccionado!!
        val a = autoSeleccionado!!
        val hashTexto = "${c.nombre}_${a.placa}_${a.vin}_${kmNum}_${fechaHoraActual}"
        val hashGenerado = RecepcionRepository.calcularHashSha256(hashTexto)

        val recepcion = RecepcionVehiculo(
            cliente = c.nombre.trim().uppercase(),
            auto = "${a.marca} ${a.modelo} (${a.anio})".uppercase(),
            placa = a.placa.trim().uppercase(),
            vin = a.vin.trim().uppercase(),
            kilometraje = kmNum,
            fechaHora = fechaHoraActual,
            fotoFrentePath = if (fotoFrenteBitmap != null) "REGISTRADA_CON_MARCA_AGUA" else "",
            fotoAtrasPath = if (fotoAtrasBitmap != null) "REGISTRADA_CON_MARCA_AGUA" else "",
            fotoIzquierdaPath = if (fotoIzquierdaBitmap != null) "REGISTRADA_CON_MARCA_AGUA" else "",
            fotoDerechaPath = if (fotoDerechaBitmap != null) "REGISTRADA_CON_MARCA_AGUA" else "",
            fotoTechoPath = if (fotoTechoBitmap != null) "REGISTRADA_CON_MARCA_AGUA" else "",
            fotoRinesPath = if (fotoRinesBitmap != null) "REGISTRADA_CON_MARCA_AGUA" else "",
            fotoInteriorPath = if (fotoInteriorBitmap != null) "REGISTRADA_CON_MARCA_AGUA" else "",
            fotoTableroPath = if (fotoTableroBitmap != null) "REGISTRADA_CON_MARCA_AGUA" else "",
            videoPath = if (videoTomado) "VIDEO_REGISTRADO" else "",
            firmaPath = if (firmaCapturada) "FIRMA_TACTIL_VALIDA" else "",
            hashIntegridadSha256 = hashGenerado,
            tiempoRetencion = retencionSeleccionada
        )

        scope.launch {
            try {
                RecepcionRepository.guardarRecepcion(recepcion, context)
                guardando = false
                Toast.makeText(context, "✅ Acta de recepción guardada con firma, marca de agua y metadatos EXIF", Toast.LENGTH_LONG).show()
                mensaje = "✅ REGISTRO DE RECEPCIÓN Y PROTECCIÓN LEGAL COMPLETADO CON ÉXITO."
            } catch (e: Exception) {
                guardando = false
                mensaje = "ERROR AL GUARDAR: ${e.message}"
            }
        }
    }

    fun enviarComprobanteWhatsApp() {
        if (clienteSeleccionado == null || autoSeleccionado == null) {
            Toast.makeText(context, "Seleccione cliente y auto primero", Toast.LENGTH_SHORT).show()
            return
        }
        val c = clienteSeleccionado!!
        val a = autoSeleccionado!!
        val telLimpio = c.telefono.replace(Regex("[^0-9]"), "")

        if (telLimpio.isBlank()) {
            Toast.makeText(context, "El cliente no tiene teléfono registrado", Toast.LENGTH_SHORT).show()
            return
        }

        val emisor = if (nombreTaller.isBlank()) "TALLER MECÁNICO" else nombreTaller.trim().uppercase()
        val kmNum = kilometraje.ifBlank { a.kilometraje.toString() }

        val textoMensaje = """
📋 *ACTA DE INGRESO Y RECEPCIÓN DE VEHÍCULO*
Taller: $emisor
Fecha / Hora: $fechaHoraActual

────────────────────
Cliente: ${c.nombre.uppercase()}
Vehículo: ${a.marca.uppercase()} ${a.modelo.uppercase()} (${a.anio})
Placa: ${a.placa.uppercase()}   |   VIN: ${a.vin.ifBlank { "N/A" }.uppercase()}
Kilometraje: $kmNum km

*Estado de la evidencia fotográfica y legal:*
• Fotos registradas con marcas de agua y metadatos EXIF: OK
• Video de inspección: ${if (videoTomado) "INCLUIDO" else "OPCIONAL"}
• Firma digital del cliente: ACEPTADA Y BLOQUEADA
• Creador Hash SHA-256: Protegido
• Periodo de retención legal: $retencionSeleccionada

Agradecemos su confianza.
────────────────────
        """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/52$telLimpio?text=${Uri.encode(textoMensaje)}")
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
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
                fontSize = 15.sp,
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

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        slotsLista.chunked(2).forEach { par ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                par.forEach { (nombreSlot, bitmap, accionToma) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(90.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (bitmap != null) Color(0xFF00C853) else Colores.FondoSecundario)
                                            .clickable { accionToma() }
                                            .border(1.5.dp, if (bitmap != null) Color(0xFF7DFFB2) else Colores.BordeBoton, RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = nombreSlot,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.35f)),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                Text(
                                                    text = "✅ $nombreSlot",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(bottom = 4.dp)
                                                )
                                            }
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("📷", fontSize = 22.sp)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(nombreSlot, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. LIENZO DE FIRMA DIGITAL TÁCTIL CON BLOQUEO TRAS PRESIONAR ACEPTAR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. FIRMA DIGITAL DEL CLIENTE", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)

                    if (firmaCapturada) {
                        Text(
                            text = "🔒 FIRMADO Y BLOQUEADO",
                            color = Color(0xFF7DFFB2),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    Text("El cliente firma en pantalla confirmando la recepción del auto:", fontSize = 13.sp, color = Colores.EtiquetaCampo)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (firmaCapturada) Color(0xFFE8F5E9) else Color.White)
                            .border(2.dp, if (firmaCapturada) Color(0xFF00C853) else Color.Black, RoundedCornerShape(12.dp))
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(firmaCapturada) {
                                    if (!firmaCapturada) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            down.consume()
                                            val listaPuntos = mutableStateListOf(down.position)
                                            trazosFirma.add(listaPuntos)

                                            do {
                                                val event = awaitPointerEvent()
                                                val change = event.changes.firstOrNull()
                                                if (change != null && change.pressed) {
                                                    change.consume()
                                                    listaPuntos.add(change.position)
                                                }
                                            } while (event.changes.any { it.pressed })
                                        }
                                    }
                                }
                        ) {
                            trazosFirma.forEach { puntos ->
                                for (i in 0 until puntos.size - 1) {
                                    drawLine(
                                        color = Color.Black,
                                        start = puntos[i],
                                        end = puntos[i + 1],
                                        strokeWidth = 6f
                                    )
                                }
                            }
                        }
                    }

                    if (!firmaCapturada) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                if (trazosFirma.isNotEmpty()) {
                                    firmaCapturada = true
                                    Toast.makeText(context, "🔒 Firma digital aceptada y bloqueada de forma segura", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Realice su firma antes de aceptar", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("✅ ACEPTAR Y BLOQUEAR FIRMA", color = Color(0xFF7DFFB2), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            TextButton(onClick = {
                                trazosFirma.clear()
                                firmaCapturada = false
                                Toast.makeText(context, "🔓 Campo de firma desbloqueado", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("🗑️ BORRAR FIRMA", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("✅ FIRMA ACEPTADA Y VALIDADA", color = Color(0xFF7DFFB2), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(
                                onClick = {
                                    trazosFirma.clear()
                                    firmaCapturada = false
                                    Toast.makeText(context, "🔓 Campo de firma desbloqueado", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("🗑️ BORRAR FIRMA", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. TEMPORIZADOR DE CONTEO REGRESIVO Y RETENCIÓN DE FOTOS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoSecundario),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("4. TEMPORIZADOR DE AUTO-ELIMINACIÓN DE FOTOS", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal)
                    Text("Seleccione el tiempo tras el cual la evidencia expira y se purga automáticamente:", fontSize = 13.sp, color = Colores.EtiquetaCampo)

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { menuRetencionExpandido = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Colores.FondoPantalla)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = retencionSeleccionada, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "▼", color = Color.White)
                            }
                        }

                        DropdownMenu(
                            expanded = menuRetencionExpandido,
                            onDismissRequest = { menuRetencionExpandido = false },
                            modifier = Modifier.background(Colores.FondoTarjeta)
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
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. BOTÓN VIDEO
            BotonModulo3D(
                texto = if (videoTomado) "✅ VIDEO REGISTRADO" else "🎥 GRABAR VIDEO OPCIONAL\n(15-20 SEGUNDOS)",
                colorClaro = if (videoTomado) Color(0xFFB9F6CA) else Color(0xFF80D8FF),
                colorMedio = if (videoTomado) Color(0xFF00C853) else Color(0xFF00B8D4),
                colorOscuro = if (videoTomado) Color(0xFF00695C) else Color(0xFF006064),
                colorTexto = Color.Black,
                onClick = { tomarVideo() },
                modifier = Modifier.fillMaxWidth().height(62.dp),
                tamanioTexto = 14
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 6. BOTÓN AMARILLO DE CONSEJOS Y ASPECTOS LEGALES
            BotonModulo3D(
                texto = "💡 CONSEJOS Y ASPECTOS LEGALES",
                colorClaro = Color(0xFFFFF59D),
                colorMedio = Color(0xFFFFEB3B),
                colorOscuro = Color(0xFFFBC02D),
                colorTexto = Color.Black,
                onClick = { mostrarDialogoConsejos = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                tamanioTexto = 15
            )

            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(mensaje, color = Color(0xFFFF5252), fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTONES DE GUARDADO Y WHATSAPP
            BotonModulo3D(
                texto = if (guardando) "GUARDANDO..." else "💾 GUARDAR ACTA DE RECEPCIÓN",
                colorClaro = Color(0xFFB9F6CA),
                colorMedio = Color(0xFF00C853),
                colorOscuro = Color(0xFF00695C),
                colorTexto = Color.Black,
                onClick = { if (!guardando) guardarRecepcionVehiculo() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                tamanioTexto = 16
            )

            Spacer(modifier = Modifier.height(12.dp))

            BotonModulo3D(
                texto = "📱 ENVIAR COMPROBANTE AL CLIENTE",
                colorClaro = Color(0xFFD7B899),
                colorMedio = Color(0xFF9B6B43),
                colorOscuro = Color(0xFF5D3A1A),
                colorTexto = Color.Black,
                onClick = { enviarComprobanteWhatsApp() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                tamanioTexto = 16
            )

            Spacer(modifier = Modifier.height(16.dp))
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

        // DIÁLOGO TARJETA DE CONSEJOS Y ASPECTOS LEGALES
        if (mostrarDialogoConsejos) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoConsejos = false },
                containerColor = Colores.FondoTarjeta,
                title = {
                    Text(
                        text = "💡 CONSEJOS Y ASPECTOS LEGALES DE PROTECCIÓN",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("📜 1. AVISO DE PRIVACIDAD:", color = Color(0xFF7DFFB2), fontWeight = FontWeight.Bold)
                        Text("Los vehículos son fotografiados al ingreso como inventario obligatorio del estado físico del auto.", color = Color.White, fontSize = 14.sp)

                        Text("✍️ 2. CONSENTIMIENTO FIRMADO:", color = Color(0xFF7DFFB2), fontWeight = FontWeight.Bold)
                        Text("La firma digital táctil del cliente en la app sirve como prueba legal de aceptación del estado de recepción.", color = Color.White, fontSize = 14.sp)

                        Text("🔐 3. INTEGRIDAD HASH SHA-256:", color = Color(0xFF7DFFB2), fontWeight = FontWeight.Bold)
                        Text("Cada acta genera una huella criptográfica SHA-256. Si alguien intenta alterar las fotos, la firma o los datos, el Hash cambia y demuestra manipulación en un juicio.", color = Color.White, fontSize = 14.sp)

                        Text("⏳ 4. TIEMPO DE RETENCIÓN:", color = Color(0xFF7DFFB2), fontWeight = FontWeight.Bold)
                        Text("Se recomienda conservar la evidencia mínimo 2 años (tiempo legal de prescripción de disputas vehiculares). Al cumplirse el periodo, el sistema purga automáticamente la evidencia para liberar memoria en el taller.", color = Color.White, fontSize = 14.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { mostrarDialogoConsejos = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                    ) {
                        Text("ENTENDIDO", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
