package com.example.bitacoraautomotriz.ui.escaner

// Escáner del Número de Identificación Vehicular (VIN) usando CameraX + ML Kit.
// Busca el auto en Room a través de AutoRepository.obtenerAutoPorVin(vin).
//
// Requiere en app/build.gradle.kts:
//   implementation("androidx.camera:camera-camera2:1.3.4")
//   implementation("androidx.camera:camera-lifecycle:1.3.4")
//   implementation("androidx.camera:camera-view:1.3.4")
//   implementation("com.google.mlkit:barcode-scanning:17.3.0")
//
// Nota: algunos autos de fabricación nacional usan códigos de barras cortos
// (ej. 8 caracteres) en vez del VIN completo de 17. Por eso no se valida
// longitud: se acepta y busca tal cual lo que la cámara detecte.

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

// Tiempo mínimo antes de aceptar el mismo código otra vez (evita disparos duplicados).
private const val MS_ANTES_DE_REPETIR_CODIGO = 3000L

@Composable
fun BarcodeScannerScreen(
    onAutoEncontrado: (vin: String) -> Unit,
    onAutoNoEncontrado: (vin: String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> tienePermiso = concedido }

    LaunchedEffect(Unit) {
        if (!tienePermiso) permisoLauncher.launch(Manifest.permission.CAMERA)
    }

    var isProcessing by remember { mutableStateOf(false) }
    var vinDetectado by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var errorMensaje by remember { mutableStateOf<String?>(null) }
    var autoEncontrado by remember { mutableStateOf<Boolean?>(null) }
    var flashActivo by remember { mutableStateOf(false) }
    var mostrarEntradaManual by remember { mutableStateOf(false) }
    var vinManual by remember { mutableStateOf("") }

    var ultimoCodigoLeido by remember { mutableStateOf<String?>(null) }
    var ultimaLecturaTimestamp by remember { mutableStateOf(0L) }

    fun buscarPorVin(vin: String) {
        cargando = true
        errorMensaje = null
        scope.launch {
            try {
                val auto = AutoRepository.obtenerAutoPorVin(vin)
                autoEncontrado = auto != null
            } catch (e: Exception) {
                errorMensaje = "No se pudo consultar la base de datos. Intenta de nuevo."
            } finally {
                cargando = false
                vinDetectado = vin
            }
        }
    }

    fun manejarCodigoDetectado(codigo: String) {
        val ahora = System.currentTimeMillis()
        // Ignora si es el mismo código leído hace muy poco (evita disparos duplicados).
        if (codigo == ultimoCodigoLeido && ahora - ultimaLecturaTimestamp < MS_ANTES_DE_REPETIR_CODIGO) {
            return
        }
        ultimoCodigoLeido = codigo
        ultimaLecturaTimestamp = ahora

        isProcessing = true
        buscarPorVin(codigo)
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (tienePermiso) {
                CameraPreviewVin(
                    isProcessing = isProcessing,
                    flashActivo = flashActivo,
                    onVinDetected = { codigo -> manejarCodigoDetectado(codigo) },
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(width = 280.dp, height = 100.dp)
                        .border(3.dp, Color.Green),
                )
                Text(
                    "Alinea el código VIN del cristal dentro del recuadro",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp),
                )

                // Controles inferiores: linterna y entrada manual
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    IconButton(
                        onClick = { flashActivo = !flashActivo },
                        modifier = Modifier.align(Alignment.CenterStart),
                    ) {
                        Icon(
                            imageVector = if (flashActivo) FlashOnIcon else FlashOffIcon,
                            contentDescription = "Linterna",
                            tint = Color.White,
                        )
                    }

                    OutlinedButton(
                        onClick = { mostrarEntradaManual = true },
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        Icon(KeyboardIcon, contentDescription = null)
                        Text(" Escribir VIN")
                    }
                }
            } else {
                Text(
                    "Se necesita permiso de cámara para escanear el VIN.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            }

            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        // Diálogo de resultado (auto encontrado / no encontrado / error)
        if (vinDetectado != null && !cargando) {
            val vin = vinDetectado!!
            AlertDialog(
                onDismissRequest = {
                    vinDetectado = null
                    isProcessing = false
                },
                title = { Text("Código detectado") },
                text = {
                    Column {
                        Text(vin)
                        Text(
                            when {
                                errorMensaje != null -> errorMensaje!!
                                autoEncontrado == true -> "✅ Auto encontrado en la bitácora."
                                else -> "⚠️ Este código no está registrado. ¿Deseas dar de alta el auto?"
                            }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        vinDetectado = null
                        isProcessing = false
                        when {
                            errorMensaje != null -> buscarPorVin(vin) // reintentar
                            autoEncontrado == true -> onAutoEncontrado(vin)
                            else -> onAutoNoEncontrado(vin)
                        }
                    }) {
                        Text(
                            when {
                                errorMensaje != null -> "Reintentar"
                                autoEncontrado == true -> "Ver Auto"
                                else -> "Registrar Auto"
                            }
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        vinDetectado = null
                        isProcessing = false
                    }) { Text("Cancelar") }
                },
            )
        }

        // Diálogo de entrada manual del VIN
        if (mostrarEntradaManual) {
            AlertDialog(
                onDismissRequest = { mostrarEntradaManual = false },
                title = { Text("Escribir VIN manualmente") },
                text = {
                    OutlinedTextField(
                        value = vinManual,
                        onValueChange = { vinManual = it },
                        label = { Text("VIN o código") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val codigo = vinManual.trim().uppercase()
                        mostrarEntradaManual = false
                        vinManual = ""
                        if (codigo.isNotEmpty()) {
                            isProcessing = true
                            buscarPorVin(codigo)
                        }
                    }) { Text("Buscar") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { mostrarEntradaManual = false }) {
                        Text("Cancelar")
                    }
                },
            )
        }
    }
}

@Composable
private fun CameraPreviewVin(
    isProcessing: Boolean,
    flashActivo: Boolean,
    onVinDetected: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )
    }
    var camera by remember { mutableStateOf<Camera?>(null) }

    // Activa/desactiva la linterna cuando cambia flashActivo
    LaunchedEffect(flashActivo, camera) {
        camera?.cameraControl?.enableTorch(flashActivo)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (isProcessing) {
                        imageProxy.close()
                        return@setAnalyzer
                    }
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees,
                        )
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                val valor = barcodes.firstOrNull()?.rawValue
                                if (valor != null) {
                                    onVinDetected(valor.trim().uppercase())
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
    )
}

private val FlashOnIcon: ImageVector
    get() = ImageVector.Builder(
        name = "FlashOn",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(7f, 2f)
            verticalLineToRelative(11f)
            horizontalLineToRelative(3f)
            verticalLineToRelative(9f)
            lineToRelative(7f, -12f)
            horizontalLineToRelative(-4f)
            lineToRelative(4f, -8f)
            close()
        }
    }.build()

private val FlashOffIcon: ImageVector
    get() = ImageVector.Builder(
        name = "FlashOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(3.27f, 1.44f)
            lineTo(2f, 2.72f)
            lineToRelative(5f, 5f)
            verticalLineTo(13f)
            horizontalLineToRelative(3f)
            verticalLineToRelative(9f)
            lineToRelative(3.58f, -6.14f)
            lineToRelative(5.7f, 5.7f)
            lineToRelative(1.27f, -1.27f)
            lineTo(3.27f, 1.44f)
            close()
            moveTo(17f, 10f)
            horizontalLineToRelative(-4f)
            lineToRelative(4f, -8f)
            horizontalLineTo(7f)
            verticalLineToRelative(1.17f)
            lineTo(14.83f, 11f)
            horizontalLineTo(17f)
            close()
        }
    }.build()

private val KeyboardIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Keyboard",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(20f, 5f)
            horizontalLineTo(4f)
            curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f)
            lineTo(2f, 17f)
            curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
            horizontalLineToRelative(16f)
            curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
            verticalLineTo(7f)
            curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f)
            close()
            moveTo(11f, 8f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(-2f)
            verticalLineTo(8f)
            close()
            moveTo(11f, 11f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(-2f)
            close()
            moveTo(8f, 8f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            horizontalLineTo(8f)
            verticalLineTo(8f)
            close()
            moveTo(8f, 11f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            horizontalLineTo(8f)
            verticalLineToRelative(-2f)
            close()
            moveTo(7f, 13f)
            horizontalLineTo(5f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            close()
            moveTo(7f, 10f)
            horizontalLineTo(5f)
            verticalLineTo(8f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            close()
            moveTo(16f, 17f)
            horizontalLineTo(8f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(8f)
            verticalLineToRelative(2f)
            close()
            moveTo(16f, 13f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            close()
            moveTo(16f, 10f)
            horizontalLineToRelative(-2f)
            verticalLineTo(8f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            close()
            moveTo(19f, 13f)
            horizontalLineToRelative(-2f)
            verticalLineToRelative(-2f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            close()
            moveTo(19f, 10f)
            horizontalLineToRelative(-2f)
            verticalLineTo(8f)
            horizontalLineToRelative(2f)
            verticalLineToRelative(2f)
            close()
        }
    }.build()
