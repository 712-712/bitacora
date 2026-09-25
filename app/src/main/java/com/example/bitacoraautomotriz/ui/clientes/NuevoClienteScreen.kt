package com.example.bitacoraautomotriz.ui.clientes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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

    val nombreFocus = remember { FocusRequester() }
    val telefonoFocus = remember { FocusRequester() }
    val correoFocus = remember { FocusRequester() }
    val direccionFocus = remember { FocusRequester() }

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    val estiloTextoFormulario = TextStyle(
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "NUEVO CLIENTE", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal)
        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "NOMBRE COMPLETO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp))
        OutlinedTextField(
            value = nombre, onValueChange = { nombre = it.uppercase() }, textStyle = estiloTextoFormulario, shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Colores.FondoSecundario, unfocusedContainerColor = Colores.FondoSecundario, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedIndicatorColor = Colores.BordeBoton, unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f), cursorColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(64.dp).focusRequester(nombreFocus),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { telefonoFocus.requestFocus() })
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "TELÉFONO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp))
        OutlinedTextField(
            value = telefono, onValueChange = { if (it.all { char -> char.isDigit() || char == '+' }) telefono = it }, textStyle = estiloTextoFormulario, shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Colores.FondoSecundario, unfocusedContainerColor = Colores.FondoSecundario, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedIndicatorColor = Colores.BordeBoton, unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f), cursorColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(64.dp).focusRequester(telefonoFocus),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { correoFocus.requestFocus() })
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "CORREO ELECTRÓNICO", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp))
        OutlinedTextField(
            value = correo, onValueChange = { correo = it.lowercase() }, textStyle = estiloTextoFormulario, shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Colores.FondoSecundario, unfocusedContainerColor = Colores.FondoSecundario, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedIndicatorColor = Colores.BordeBoton, unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f), cursorColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(64.dp).focusRequester(correoFocus),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { direccionFocus.requestFocus() })
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "DIRECCIÓN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal, modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp))
        OutlinedTextField(
            value = direccion, onValueChange = { direccion = it.uppercase() }, textStyle = estiloTextoFormulario, shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Colores.FondoSecundario, unfocusedContainerColor = Colores.FondoSecundario, focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedIndicatorColor = Colores.BordeBoton, unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f), cursorColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(100.dp).focusRequester(direccionFocus),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
        Spacer(modifier = Modifier.height(32.dp))

        BotonModulo3D(
            texto = "GUARDAR CLIENTE",
            colorClaro = Color(0xFF80D8FF),
            colorMedio = Color(0xFF00B8D4),
            colorOscuro = Color(0xFF006064),
            onClick = {
                if (nombre.isBlank()) {
                    Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@BotonModulo3D
                }
                scope.launch {
                    try {
                        // ✅ AQUÍ SE ASIGNA EL ID:
                        // Al poner id = 0, Room Database intercepta esto y le asigna
                        // AUTOMÁTICAMENTE el siguiente número consecutivo disponible (1, 2, 3...)
                        // en el momento exacto en que se guarda en la base de datos.
                        val nuevoCliente = Cliente(
                            id = 0,
                            nombre = nombre.trim(),
                            telefono = telefono.trim(),
                            direccion = direccion.trim(),
                            correo = correo.trim()
                        )

                        ClienteRepository.guardarCliente(nuevoCliente)

                        Toast.makeText(context, "✅ Cliente guardado con ID: ${nuevoCliente.id}", Toast.LENGTH_SHORT).show()
                        onClienteCreado(nuevoCliente)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        BotonModulo3D(texto = "REGRESAR", colorClaro = Color(0xFFD5E1E6), colorMedio = Color(0xFF90A4AE), colorOscuro = Color(0xFF455A64), onClick = onRegresar)
    }
}
