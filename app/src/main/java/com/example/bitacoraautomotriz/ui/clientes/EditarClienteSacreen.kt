package com.example.bitacoraautomotriz.ui.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
                        correo = correo.trim(),
                        direccion = direccion.trim().uppercase(),
                    )
                )
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
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
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it.uppercase() },
                placeholder = {
                    Text(
                        "Nombre",
                        color = Colores.EtiquetaCampo.copy(alpha = 0.6f),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                textStyle = estiloCampo,
                colors = coloresCampo,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 2. TELÉFONO
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                placeholder = {
                    Text(
                        "Teléfono",
                        color = Colores.EtiquetaCampo.copy(alpha = 0.6f),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                textStyle = estiloCampo,
                colors = coloresCampo,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 3. CORREO
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                placeholder = {
                    Text(
                        "Correo",
                        color = Colores.EtiquetaCampo.copy(alpha = 0.6f),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                textStyle = estiloCampo,
                colors = coloresCampo,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 4. DIRECCIÓN
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it.uppercase() },
                placeholder = {
                    Text(
                        "Dirección",
                        color = Colores.EtiquetaCampo.copy(alpha = 0.6f),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                textStyle = estiloCampo,
                colors = coloresCampo,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    error!!,
                    color = Colores.TextoBoton,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            BotonModulo3D(
                texto = if (guardando) "GUARDANDO..." else "GUARDAR CAMBIOS",
                icono = "💾",
                onClick = { if (!guardando) guardar() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
