package com.example.bitacoraautomotriz.ui.escaner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.ui.autos.SelectorClientesDialog
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import kotlinx.coroutines.launch

@Composable
fun RegistrarAutoScreen(
    vin: String,
    onGuardado: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var cliente by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("") }

    var guardando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // ♿ Estados para el Selector de Clientes
    var mostrarSelectorCliente by remember { mutableStateOf(false) }
    var listaClientes by remember { mutableStateOf(emptyList<Cliente>()) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }

    // Cargar la lista de clientes al abrir la pantalla
    LaunchedEffect(Unit) {
        listaClientes = ClienteRepository.obtenerClientes()
    }

    fun guardar() {
        // Usamos el nombre del cliente seleccionado, o el escrito manualmente si no hay selección
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

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "VIN ESCANEADO: $vin",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF00AEEF)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ♿ BOTÓN PARA ABRIR EL SELECTOR DE CLIENTES
            BotonModulo3D(
                texto = if (clienteSeleccionado != null) "CLIENTE: ${clienteSeleccionado!!.nombre}" else "SELECCIONAR CLIENTE",
                colorClaro = Color(0xFF80D8FF),
                colorMedio = Color(0xFF00B8D4),
                colorOscuro = Color(0xFF006064),
                onClick = { mostrarSelectorCliente = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = marca,
                onValueChange = { marca = it.uppercase() },
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = modelo,
                onValueChange = { modelo = it.uppercase() },
                label = { Text("Modelo") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = anio,
                onValueChange = { anio = it },
                label = { Text("Año") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = placa,
                onValueChange = { placa = it.uppercase() },
                label = { Text("Placa") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = color,
                onValueChange = { color = it.uppercase() },
                label = { Text("Color") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = kilometraje,
                onValueChange = { kilometraje = it },
                label = { Text("Kilometraje") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (error != null) {
                Text(
                    text = error!!,
                    modifier = Modifier.padding(top = 12.dp),
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ♿ BOTÓN DE GUARDAR CON ESTILO 3D
            BotonModulo3D(
                texto = if (guardando) "GUARDANDO..." else "GUARDAR AUTO",
                colorClaro = Color(0xFFB9F6CA),
                colorMedio = Color(0xFF00C853),
                colorOscuro = Color(0xFF00695C),
                onClick = { if (!guardando) guardar() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BotonModulo3D(
                texto = "CANCELAR",
                colorClaro = Color(0xFFD5E1E6),
                colorMedio = Color(0xFF90A4AE),
                colorOscuro = Color(0xFF455A64),
                onClick = onGuardado
            )
        }
    }

    // ♿ AQUÍ SE MUESTRA EL DIÁLOGO CUANDO EL USUARIO TOCA "SELECCIONAR CLIENTE"
    if (mostrarSelectorCliente) {
        SelectorClientesDialog(
            clientes = listaClientes,
            onClienteSeleccionado = { clienteElegido ->
                clienteSeleccionado = clienteElegido
                cliente = clienteElegido.nombre // Mantenemos compatibilidad con tu base de datos
                mostrarSelectorCliente = false
            },
            onCerrar = {
                mostrarSelectorCliente = false
            }
        )
    }
}
