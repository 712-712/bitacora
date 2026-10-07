package com.example.bitacoraautomotriz.ui.autos

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

@Composable
fun NuevoAutoScreen(
    nombreCliente: String = "",
    vinInicial: String = "",
    onEscanearVin: () -> Unit,
    onGuardar: () -> Unit,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf(vinInicial) }
    var color by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    LaunchedEffect(vinInicial) {
        if (vinInicial.isNotBlank()) {
            vin = vinInicial
        }
    }

    var autoExistente by remember { mutableStateOf<Auto?>(null) }

    val vinFocus = remember { FocusRequester() }
    val marcaFocus = remember { FocusRequester() }
    val modeloFocus = remember { FocusRequester() }
    val anioFocus = remember { FocusRequester() }
    val placaFocus = remember { FocusRequester() }
    val colorFocus = remember { FocusRequester() }
    val kmFocus = remember { FocusRequester() }

    val estiloTexto = TextStyle(
        color = Colores.TextoTarjeta,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )

    val coloresCampo = TextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        focusedTextColor = Colores.TextoTarjeta,
        unfocusedTextColor = Colores.TextoTarjeta,
        focusedIndicatorColor = Colores.BordeBoton,
        unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
        cursorColor = Colores.TextoBoton
    )

    fun guardarAuto() {
        if (autoExistente != null) {
            mensaje = "ESTA PLACA YA ESTÁ REGISTRADA EN EL SISTEMA."
            return
        }

        if (marca.isBlank()) { mensaje = "INGRESE LA MARCA"; marcaFocus.requestFocus(); return }
        if (modelo.isBlank()) { mensaje = "INGRESE EL MODELO"; modeloFocus.requestFocus(); return }
        if (anio.isBlank()) { mensaje = "INGRESE EL AÑO"; anioFocus.requestFocus(); return }
        if (placa.isBlank()) { mensaje = "INGRESE LA PLACA"; placaFocus.requestFocus(); return }

        val anioNumero = anio.toIntOrNull()
        if (anioNumero == null || anioNumero < 1900 || anioNumero > 2100) {
            mensaje = "AÑO NO VÁLIDO (1900-2100)"; anioFocus.requestFocus(); return
        }

        val kilometrajeNumero = if (kilometraje.isBlank()) 0 else kilometraje.toIntOrNull() ?: 0

        val nuevoAuto = Auto(
            cliente = nombreCliente.uppercase(),
            marca = marca.trim().uppercase(),
            modelo = modelo.trim().uppercase(),
            anio = anioNumero,
            placa = placa.trim().uppercase(),
            kilometraje = kilometrajeNumero,
            vin = vin.trim().uppercase(),
            color = color.trim().uppercase()
        )

        scope.launch {
            try {
                AutoRepository.guardarAuto(nuevoAuto, context)
                focusManager.clearFocus()
                keyboardController?.hide()
                onGuardar()
            } catch (e: Exception) {
                mensaje = "ERROR: ${e.message}"
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
                text = "NUEVO AUTO",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            if (nombreCliente.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CLIENTE:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal
                )
                Text(
                    text = nombreCliente.uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. BOTÓN DE ESCÁNER VIN AL INICIO DEL FORMULARIO
            BotonModulo3D(
                texto = "📷 ESCANEAR CÓDIGO VIN",
                colorClaro = Color(0xFFD1C4E9),
                colorMedio = Color(0xFF7E57C2),
                colorOscuro = Color(0xFF4527A0),
                onClick = onEscanearVin,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 2. CAMPO VIN DEBAJO DEL BOTÓN ESCÁNER
            Text("VIN:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = vin,
                onValueChange = { vin = it.uppercase(); mensaje = "" },
                textStyle = estiloTexto,
                colors = coloresCampo,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(vinFocus),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { marcaFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 3. MARCA
            Text("MARCA:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = marca,
                onValueChange = { marca = it.uppercase(); mensaje = "" },
                textStyle = estiloTexto,
                colors = coloresCampo,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(marcaFocus),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { modeloFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 4. MODELO
            Text("MODELO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = modelo,
                onValueChange = { modelo = it.uppercase(); mensaje = "" },
                textStyle = estiloTexto,
                colors = coloresCampo,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(modeloFocus),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { anioFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 5. AÑO
            Text("AÑO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = anio,
                onValueChange = { anio = it.filter { char -> char.isDigit() }; mensaje = "" },
                textStyle = estiloTexto,
                colors = coloresCampo,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(anioFocus),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { placaFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 6. PLACA
            Text("PLACA:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = placa,
                onValueChange = { nuevaPlaca ->
                    placa = nuevaPlaca.uppercase()
                    mensaje = ""
                    if (nuevaPlaca.length >= 4) {
                        scope.launch {
                            autoExistente = AutoRepository.obtenerAutoPorPlaca(nuevaPlaca.uppercase())
                        }
                    } else {
                        autoExistente = null
                    }
                },
                textStyle = estiloTexto,
                colors = coloresCampo.copy(
                    focusedIndicatorColor = if (autoExistente != null) Colores.TextoBoton else Colores.BordeBoton
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(placaFocus),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { colorFocus.requestFocus() })
            )

            if (autoExistente != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "⚠️ YA EXISTE UN AUTO CON ESTA PLACA", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Colores.TextoBoton)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // 7. COLOR
            Text("COLOR:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = color,
                onValueChange = { color = it.uppercase(); mensaje = "" },
                textStyle = estiloTexto,
                colors = coloresCampo,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(colorFocus),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { kmFocus.requestFocus() })
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 8. KILOMETRAJE
            Text("KILOMETRAJE:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = kilometraje,
                onValueChange = { kilometraje = it.filter { char -> char.isDigit() }; mensaje = "" },
                textStyle = estiloTexto,
                colors = coloresCampo,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(kmFocus),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboardController?.hide() })
            )

            if (mensaje.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = mensaje, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Colores.TextoBoton)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (autoExistente != null) {
                BotonModulo3D(texto = "PLACA YA REGISTRADA", icono = "⚠️", onClick = { }, modifier = Modifier.fillMaxWidth())
            } else {
                BotonModulo3D(
                    texto = "GUARDAR AUTO",
                    icono = "💾",
                    colorClaro = Color(0xFFB9F6CA),
                    colorMedio = Color(0xFF00C853),
                    colorOscuro = Color(0xFF00695C),
                    onClick = { guardarAuto() },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colorTexto = Color.Black
                )
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
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onRegresar()
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colorTexto = Color.White
                )
            }
        }
    }
}
