package com.example.bitacoraautomotriz.ui.autos

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Modifier.scrollAlEnfocar(
    scrollState: ScrollState,
    scope: CoroutineScope
): Modifier {
    return this.onFocusChanged { focusState ->
        if (focusState.isFocused) {
            scope.launch {
                delay(150)
                val nuevoValor = (scrollState.value + 280).coerceAtMost(scrollState.maxValue)
                scrollState.animateScrollTo(nuevoValor)
            }
        }
    }
}

@Composable
fun EditarAutoScreen(
    autoId: Int,
    onGuardado: () -> Unit,
    onRegresar: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    val clienteFocus = remember { FocusRequester() }
    val marcaFocus = remember { FocusRequester() }
    val modeloFocus = remember { FocusRequester() }
    val anioFocus = remember { FocusRequester() }
    val placaFocus = remember { FocusRequester() }
    val vinFocus = remember { FocusRequester() }
    val colorFocus = remember { FocusRequester() }
    val kilometrajeFocus = remember { FocusRequester() }

    var cargando by remember { mutableStateOf(true) }
    var autoOriginal by remember { mutableStateOf<Auto?>(null) }

    var cliente by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("") }

    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(autoId) {
        val auto = AutoRepository.obtenerAutoPorId(autoId)
        autoOriginal = auto
        if (auto != null) {
            cliente = auto.cliente
            marca = auto.marca
            modelo = auto.modelo
            anio = auto.anio.toString()
            placa = auto.placa
            vin = auto.vin
            color = auto.color
            kilometraje = auto.kilometraje.toString()
        }
        cargando = false
    }

    fun guardar() {
        val original = autoOriginal ?: return

        if (cliente.isBlank() || marca.isBlank() || modelo.isBlank() || placa.isBlank()) {
            error = "Cliente, marca, modelo y placa son requeridos"
            return
        }
        val anioInt = anio.toIntOrNull()
        val kmInt = kilometraje.toIntOrNull()
        if (anioInt == null) {
            error = "El año debe ser un número válido"
            return
        }
        if (kmInt == null) {
            error = "El kilometraje debe ser un número válido"
            return
        }

        guardando = true
        error = null

        scope.launch {
            try {
                AutoRepository.actualizarAuto(
                    original.copy(
                        cliente = cliente.trim(),
                        marca = marca.trim(),
                        modelo = modelo.trim(),
                        anio = anioInt,
                        placa = placa.trim(),
                        vin = vin.trim(),
                        color = color.trim(),
                        kilometraje = kmInt,
                    )
                )
                focusManager.clearFocus()
                keyboardController?.hide()
                onGuardado()
            } catch (e: Exception) {
                error = "Error al guardar: ${e.message}"
            } finally {
                guardando = false
            }
        }
    }

    val estiloCampo = TextStyle(
        color = Colores.TextoTarjeta,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )

    val coloresCampo = TextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        focusedTextColor = Colores.TextoTarjeta,
        unfocusedTextColor = Colores.TextoTarjeta,
        cursorColor = Colores.TextoBoton,
        focusedIndicatorColor = Colores.BordeBoton,
        unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f)
    )

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
                .verticalScroll(scrollState)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EDITAR AUTO",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (cargando) {
                CircularProgressIndicator(color = Colores.TituloPrincipal)
            } else if (autoOriginal == null) {
                Text("No se encontró el auto.", color = Colores.TextoBoton, fontSize = 18.sp)
            } else {

                // 1. CLIENTE
                Text("CLIENTE:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = cliente,
                    onValueChange = { cliente = it.uppercase() },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { marcaFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(clienteFocus).scrollAlEnfocar(scrollState, scope)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 2. MARCA
                Text("MARCA:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = marca,
                    onValueChange = { marca = it.uppercase() },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { modeloFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(marcaFocus).scrollAlEnfocar(scrollState, scope)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 3. MODELO
                Text("MODELO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = modelo,
                    onValueChange = { modelo = it.uppercase() },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { anioFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(modeloFocus).scrollAlEnfocar(scrollState, scope)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 4. AÑO
                Text("AÑO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = anio,
                    onValueChange = { anio = it.filter { char -> char.isDigit() } },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { placaFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(anioFocus).scrollAlEnfocar(scrollState, scope)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 5. PLACA
                Text("PLACA:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = placa,
                    onValueChange = { placa = it.uppercase() },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { vinFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(placaFocus).scrollAlEnfocar(scrollState, scope)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 6. VIN
                Text("VIN:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = vin,
                    onValueChange = { vin = it.uppercase() },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { colorFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(vinFocus).scrollAlEnfocar(scrollState, scope)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 7. COLOR
                Text("COLOR:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it.uppercase() },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { kilometrajeFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(colorFocus).scrollAlEnfocar(scrollState, scope)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 8. KILOMETRAJE
                Text("KILOMETRAJE:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = kilometraje,
                    onValueChange = { kilometraje = it.filter { char -> char.isDigit() } },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboardController?.hide() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(kilometrajeFocus).scrollAlEnfocar(scrollState, scope)
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("⚠️ $error", color = Colores.TextoBoton, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                BotonModulo3D(
                    texto = if (guardando) "GUARDANDO..." else "GUARDAR CAMBIOS",
                    icono = "💾",
                    colorClaro = Color(0xFFB9F6CA),
                    colorMedio = Color(0xFF00C853),
                    colorOscuro = Color(0xFF00695C),
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (!guardando) guardar()
                    },
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
