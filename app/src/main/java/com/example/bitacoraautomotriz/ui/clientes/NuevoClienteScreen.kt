package com.example.bitacoraautomotriz.ui.clientes

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
fun NuevoClienteScreen(
    onClienteCreado: (Cliente) -> Unit,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var clientesExistentes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            clientesExistentes = ClienteRepository.obtenerClientes(context)
        } catch (_: Exception) {
            clientesExistentes = emptyList()
        }
    }

    val nombreLimpio = nombre.trim()
    val telefonoLimpio = telefono.trim()

    val clienteCoincidencia = remember(nombreLimpio, telefonoLimpio, clientesExistentes) {
        if (nombreLimpio.isBlank() && telefonoLimpio.isBlank()) null
        else clientesExistentes.find {
            (nombreLimpio.isNotBlank() && it.nombre.trim().equals(nombreLimpio, ignoreCase = true)) ||
                    (telefonoLimpio.isNotBlank() && it.telefono.trim() == telefonoLimpio)
        }
    }

    val nombreFocus = remember { FocusRequester() }
    val telefonoFocus = remember { FocusRequester() }
    val correoFocus = remember { FocusRequester() }
    val direccionFocus = remember { FocusRequester() }

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

    fun guardar() {
        if (nombre.isBlank()) {
            mensaje = "EL NOMBRE ES REQUERIDO"
            nombreFocus.requestFocus()
            return
        }
        if (telefono.isBlank()) {
            mensaje = "EL TELÉFONO ES REQUERIDO"
            telefonoFocus.requestFocus()
            return
        }

        if (clienteCoincidencia != null) {
            mensaje = "ESTE CLIENTE YA EXISTE EN EL SISTEMA (ID: ${clienteCoincidencia.id})"
            return
        }

        guardando = true
        mensaje = ""

        scope.launch {
            try {
                val nuevoCliente = Cliente(
                    nombre = nombre.trim().uppercase(),
                    telefono = telefono.trim(),
                    correo = correo.trim().lowercase(),
                    direccion = direccion.trim().uppercase()
                )
                ClienteRepository.guardarCliente(nuevoCliente, context)
                focusManager.clearFocus()
                onClienteCreado(nuevoCliente)
            } catch (e: Exception) {
                mensaje = "ERROR AL GUARDAR: ${e.message}"
                guardando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "NUEVO CLIENTE",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )
        Spacer(modifier = Modifier.height(24.dp))

        // TARJETA DE ALERTA DE COINCIDENCIA DE CLIENTE REGISTRADO
        if (clienteCoincidencia != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB51F1F)),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚠️ CLIENTE YA REGISTRADO",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Este cliente ya existe en el sistema:\n• ID: ${clienteCoincidencia.id}\n• Nombre: ${clienteCoincidencia.nombre}\n• Teléfono: ${clienteCoincidencia.telefono}",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // NOMBRE
        Text(text = "NOMBRE:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it.uppercase(); mensaje = "" },
            textStyle = estiloTexto,
            colors = coloresCampo,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(nombreFocus),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { telefonoFocus.requestFocus() })
        )
        Spacer(modifier = Modifier.height(12.dp))

        // TELEFONO
        Text(text = "TELÉFONO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it.filter { char -> char.isDigit() || char == '-' || char == ' ' }; mensaje = "" },
            textStyle = estiloTexto,
            colors = coloresCampo,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(telefonoFocus),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { correoFocus.requestFocus() })
        )
        Spacer(modifier = Modifier.height(12.dp))

        // CORREO
        Text(text = "CORREO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it.lowercase(); mensaje = "" },
            textStyle = estiloTexto,
            colors = coloresCampo,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(correoFocus),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { direccionFocus.requestFocus() })
        )
        Spacer(modifier = Modifier.height(12.dp))

        // DIRECCION
        Text(text = "DIRECCIÓN:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it.uppercase(); mensaje = "" },
            textStyle = estiloTexto,
            colors = coloresCampo,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(72.dp).focusRequester(direccionFocus),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "⚠️ $mensaje", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
        }

        Spacer(modifier = Modifier.height(24.dp))

        BotonModulo3D(
            texto = if (guardando) "GUARDANDO..." else "GUARDAR CLIENTE",
            icono = "💾",
            colorClaro = Color(0xFF80D8FF),
            colorMedio = Color(0xFF00B8D4),
            colorOscuro = Color(0xFF006064),
            onClick = { guardar() },
            modifier = Modifier.fillMaxWidth().height(58.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = {
                focusManager.clearFocus()
                onRegresar()
            },
            modifier = Modifier.fillMaxWidth().height(58.dp),
            colorTexto = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
