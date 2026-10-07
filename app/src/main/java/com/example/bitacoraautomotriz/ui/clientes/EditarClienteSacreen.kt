package com.example.bitacoraautomotriz.ui.clientes

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
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

@Composable
fun EditarClienteScreen(
    clienteId: Int,
    onGuardado: () -> Unit,
    onRegresar: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val nombreFocus = remember { FocusRequester() }
    val telefonoFocus = remember { FocusRequester() }
    val correoFocus = remember { FocusRequester() }
    val direccionFocus = remember { FocusRequester() }

    var cargando by remember { mutableStateOf(true) }
    var clienteOriginal by remember { mutableStateOf<Cliente?>(null) }

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(clienteId) {
        val cliente = ClienteRepository.obtenerClientePorId(clienteId)
        clienteOriginal = cliente
        if (cliente != null) {
            nombre = cliente.nombre
            telefono = cliente.telefono
            correo = cliente.correo
            direccion = cliente.direccion
        }
        cargando = false
    }

    fun guardar() {
        val original = clienteOriginal ?: return

        if (nombre.isBlank() || telefono.isBlank()) {
            error = "NOMBRE Y TELÉFONO SON REQUERIDOS"
            return
        }

        guardando = true
        error = null

        scope.launch {
            try {
                ClienteRepository.actualizarCliente(
                    original.copy(
                        nombre = nombre.trim().uppercase(),
                        telefono = telefono.trim(),
                        correo = correo.trim().lowercase(),
                        direccion = direccion.trim().uppercase(),
                    )
                )
                focusManager.clearFocus()
                keyboardController?.hide()
                onGuardado()
            } catch (e: Exception) {
                error = "ERROR AL GUARDAR LOS CAMBIOS"
            } finally {
                guardando = false
            }
        }
    }

    val estiloCampo = TextStyle(
        color = Colores.TextoTarjeta,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )

    val coloresCampo = TextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        disabledContainerColor = Colores.FondoSecundario,
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
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "EDITAR CLIENTE",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (cargando) {
                CircularProgressIndicator(color = Colores.TituloPrincipal)
            } else if (clienteOriginal == null) {
                Text(
                    "NO SE ENCONTRÓ EL CLIENTE",
                    color = Colores.TextoBoton,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                // 1. NOMBRE
                Text("NOMBRE:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it.uppercase() },
                    placeholder = { Text("Nombre", color = Colores.EtiquetaCampo.copy(alpha = 0.6f), fontSize = 19.sp, fontWeight = FontWeight.Bold) },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { telefonoFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(nombreFocus)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 2. TELÉFONO
                Text("TELÉFONO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it.filter { char -> char.isDigit() || char == '-' || char == ' ' } },
                    placeholder = { Text("Teléfono", color = Colores.EtiquetaCampo.copy(alpha = 0.6f), fontSize = 19.sp, fontWeight = FontWeight.Bold) },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { correoFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(telefonoFocus)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 3. CORREO
                Text("CORREO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it.lowercase() },
                    placeholder = { Text("Correo", color = Colores.EtiquetaCampo.copy(alpha = 0.6f), fontSize = 19.sp, fontWeight = FontWeight.Bold) },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { direccionFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(correoFocus)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 4. DIRECCIÓN
                Text("DIRECCIÓN:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { direccion = it.uppercase() },
                    placeholder = { Text("Dirección", color = Colores.EtiquetaCampo.copy(alpha = 0.6f), fontSize = 19.sp, fontWeight = FontWeight.Bold) },
                    textStyle = estiloCampo,
                    colors = coloresCampo,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboardController?.hide() }),
                    modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(direccionFocus)
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(error!!, color = Colores.TextoBoton, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                BotonModulo3D(
                    texto = if (guardando) "GUARDANDO..." else "GUARDAR CAMBIOS",
                    icono = "💾",
                    colorClaro = Color(0xFF80D8FF),
                    colorMedio = Color(0xFF00B8D4),
                    colorOscuro = Color(0xFF006064),
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        if (!guardando) guardar()
                    },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colorTexto = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
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
