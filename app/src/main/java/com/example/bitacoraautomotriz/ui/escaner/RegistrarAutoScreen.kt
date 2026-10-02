package com.example.bitacoraautomotriz.ui.escaner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.ui.autos.SelectorClientesDialog
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

@Composable
fun RegistrarAutoScreen(
    vin: String,
    onGuardado: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // ✅ FocusRequesters para navegación fluida entre campos
    val marcaFocus = remember { FocusRequester() }
    val modeloFocus = remember { FocusRequester() }
    val anioFocus = remember { FocusRequester() }
    val placaFocus = remember { FocusRequester() }
    val colorFocus = remember { FocusRequester() }
    val kilometrajeFocus = remember { FocusRequester() }

    var cliente by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("") }

    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var mostrarSelectorCliente by remember { mutableStateOf(false) }
    var listaClientes by remember { mutableStateOf(emptyList<Cliente>()) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }

    LaunchedEffect(Unit) {
        listaClientes = ClienteRepository.obtenerClientes()
    }

    fun guardar() {
        val nombreClienteFinal = clienteSeleccionado?.nombre ?: cliente.trim()

        if (nombreClienteFinal.isBlank() || marca.isBlank() || modelo.isBlank() || placa.isBlank()) {
            error = "CLIENTE, MARCA, MODELO Y PLACA SON REQUERIDOS"
            return
        }
        val anioInt = anio.toIntOrNull()
        val kmInt = kilometraje.toIntOrNull() ?: 0
        if (anioInt == null) {
            error = "EL AÑO DEBE SER UN NÚMERO VÁLIDO"
            return
        }

        guardando = true
        error = null

        scope.launch {
            try {
                AutoRepository.guardarAuto(
                    Auto(
                        cliente = nombreClienteFinal.uppercase(),
                        marca = marca.trim().uppercase(),
                        modelo = modelo.trim().uppercase(),
                        anio = anioInt,
                        placa = placa.trim().uppercase(),
                        vin = vin,
                        color = color.trim().uppercase(),
                        kilometraje = kmInt,
                    )
                )
                onGuardado()
            } catch (e: Exception) {
                error = "ERROR AL GUARDAR: ${e.message}"
            } finally {
                guardando = false
            }
        }
    }

    val estiloCampo = TextStyle(
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )

    val coloresCampo = TextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        cursorColor = Color.White,
        focusedIndicatorColor = Colores.BordeBoton,
        unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f)
    )

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Colores.FondoPantalla) // ✅ 1. Fondo correcto
                .padding(padding)
                .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(), // ✅ Scroll y teclado
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "REGISTRAR AUTO",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "VIN ESCANEADO: $vin",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(24.dp))

            // CLIENTE
            Text(
                text = "CLIENTE:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal, // ✅ 3. Etiqueta arriba en azul
                modifier = Modifier.align(Alignment.Start)
            )
            BotonModulo3D(
                texto = if (clienteSeleccionado != null) "CLIENTE: ${clienteSeleccionado!!.nombre}" else "SELECCIONAR CLIENTE",
                colorClaro = Color(0xFF80D8FF),
                colorMedio = Color(0xFF00B8D4),
                colorOscuro = Color(0xFF006064),
                onClick = { mostrarSelectorCliente = true },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // MARCA
            Text(text = "MARCA:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = marca,
                onValueChange = { marca = it.uppercase() },
                textStyle = estiloCampo,
                colors = coloresCampo, // ✅ 2. Fondo de campo correcto
                shape = RoundedCornerShape(12.dp),
                singleLine = true, // ✅ 4. Evita saltos de línea
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { modeloFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth().focusRequester(marcaFocus)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // MODELO
            Text(text = "MODELO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = modelo,
                onValueChange = { modelo = it.uppercase() },
                textStyle = estiloCampo,
                colors = coloresCampo,
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { anioFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth().focusRequester(modeloFocus)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // AÑO
            Text(text = "AÑO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = anio,
                onValueChange = { anio = it.filter { char -> char.isDigit() } }, // Solo dígitos
                textStyle = estiloCampo,
                colors = coloresCampo,
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), // ✅ 5. Teclado numérico
                keyboardActions = KeyboardActions(onNext = { placaFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth().focusRequester(anioFocus)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // PLACA
            Text(text = "PLACA:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = placa,
                onValueChange = { placa = it.uppercase() },
                textStyle = estiloCampo,
                colors = coloresCampo,
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { colorFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth().focusRequester(placaFocus)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // COLOR
            Text(text = "COLOR:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = color,
                onValueChange = { color = it.uppercase() },
                textStyle = estiloCampo,
                colors = coloresCampo,
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { kilometrajeFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth().focusRequester(colorFocus)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // KILOMETRAJE
            Text(text = "KILOMETRAJE:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start))
            OutlinedTextField(
                value = kilometraje,
                onValueChange = { kilometraje = it.filter { char -> char.isDigit() } }, // Solo dígitos
                textStyle = estiloCampo,
                colors = coloresCampo,
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), // ✅ 5. Teclado numérico y "Listo"
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth().focusRequester(kilometrajeFocus)
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "⚠️ $error", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ 6. Botones con fillMaxWidth y clearFocus para garantizar que funcionen
            BotonModulo3D(
                texto = if (guardando) "GUARDANDO..." else "GUARDAR AUTO",
                colorClaro = Color(0xFFB9F6CA),
                colorMedio = Color(0xFF00C853),
                colorOscuro = Color(0xFF00695C),
                onClick = {
                    focusManager.clearFocus()
                    if (!guardando) guardar()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            BotonModulo3D(
                texto = "CANCELAR",
                colorClaro = Color(0xFFD5E1E6),
                colorMedio = Color(0xFF90A4AE),
                colorOscuro = Color(0xFF455A64),
                onClick = {
                    focusManager.clearFocus()
                    onGuardado()
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Espacio final para garantizar que el scroll tenga recorrido
            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    if (mostrarSelectorCliente) {
        SelectorClientesDialog(
            clientes = listaClientes,
            onClienteSeleccionado = { clienteElegido ->
                clienteSeleccionado = clienteElegido
                cliente = clienteElegido.nombre
                mostrarSelectorCliente = false
            },
            onCerrar = { mostrarSelectorCliente = false }
        )
    }
}
