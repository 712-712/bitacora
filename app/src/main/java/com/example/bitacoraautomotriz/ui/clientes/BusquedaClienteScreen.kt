package com.example.bitacoraautomotriz.ui.clientes

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.R
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import com.example.bitacoraautomotriz.utils.WhatsAppUtils
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BusquedaClienteScreen(
    onClienteSeleccionado: (Int, String) -> Unit,
    onEditarCliente: (Int) -> Unit,
    onEliminarCliente: (Int) -> Unit,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var clientes by remember { mutableStateOf<List<Cliente>>(value = emptyList()) }
    var busqueda by remember { mutableStateOf(value = "") }
    var refreshTrigger by remember { mutableStateOf(value = 0) }
    var cargando by remember { mutableStateOf(value = true) }

    LaunchedEffect(refreshTrigger) {
        try {
            clientes = ClienteRepository.obtenerClientes(context).sortedByDescending { it.id }
        } catch (_: Exception) {
            clientes = emptyList()
        } finally {
            cargando = false
        }
    }

    val query = busqueda.uppercase().trim()
    val clientesFiltrados = clientes.filter { cliente ->
        cliente.nombre.orEmpty().uppercase().contains(query, ignoreCase = true) ||
                cliente.id.toString().contains(query, ignoreCase = true)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BUSCAR CLIENTE",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ESCRIBA EL NOMBRE O ID:",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.EtiquetaCampo,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it.uppercase() },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Colores.FondoSecundario,
                    unfocusedContainerColor = Colores.FondoSecundario,
                    disabledContainerColor = Colores.FondoSecundario,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Colores.BordeBoton,
                    unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (clientesFiltrados.isEmpty()) {
                Text(
                    text = if (clientes.isEmpty()) "NO HAY CLIENTES REGISTRADOS" else "NO SE ENCONTRARON CLIENTES",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo
                )
            } else {
                clientesFiltrados.forEach { cliente ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ID CLIENTE: ${cliente.id}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7DFFB2)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = cliente.nombre.orEmpty().uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "TELÉFONO DEL CLIENTE:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (cliente.telefono.isNotBlank()) {
                                    IconButton(
                                        onClick = { WhatsAppUtils.abrirWhatsApp(context, cliente.telefono) },
                                        modifier = Modifier.padding(end = 6.dp).size(36.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_whatsapp_logo),
                                            contentDescription = "Enviar WhatsApp",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "📞 ${cliente.telefono}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0033FF),
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${cliente.telefono}")
                                        }
                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                    }
                                )
                            }

                            if (cliente.correo.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "CORREO ELECTRÓNICO:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "✉️ ${cliente.correo.lowercase()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0033FF),
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:${cliente.correo}")
                                        }
                                        try { context.startActivity(intent) } catch (_: Exception) {}
                                    }
                                )
                            }

                            if (cliente.direccion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "DIRECCIÓN REGISTRADA:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "📍 ${cliente.direccion.uppercase()}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF004D33))

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                BotonModulo3D(
                                    texto = "EDITAR CLIENTE",
                                    icono = "✏️",
                                    colorClaro = Color(0xFF90CAF9),
                                    colorMedio = Color(0xFF1976D2),
                                    colorOscuro = Color(0xFF0D47A1),
                                    onClick = { onEditarCliente(cliente.id) },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    tamanioTexto = 15,
                                    colorTexto = Color.Black
                                )

                                BotonModulo3D(
                                    texto = "ELIMINAR CLIENTE",
                                    icono = "🗑️",
                                    colorClaro = Color(0xFFEF9A9A),
                                    colorMedio = Color(0xFFE53935),
                                    colorOscuro = Color(0xFFB71C1C),
                                    onClick = {
                                        scope.launch {
                                            val cantidadAutos = AutoRepository.contarAutosPorCliente(cliente.nombre, context)
                                            val mensajeAdvertencia = if (cantidadAutos > 0) {
                                                "¿Está seguro de eliminar al cliente \"${cliente.nombre}\"?\n\n⚠️ ADVERTENCIA: Esta acción eliminará TAMBIÉN:\n• $cantidadAutos auto(s) registrado(s)\n• Todos los registros de servicio asociados\n\nEsta acción NO se puede deshacer."
                                            } else {
                                                "¿Está seguro de eliminar al cliente \"${cliente.nombre}\"?\n\nEste cliente no tiene autos registrados.\n\nEsta acción NO se puede deshacer."
                                            }

                                            AlertDialog.Builder(context).apply {
                                                setTitle("⚠️ ELIMINACIÓN EN CASCADA")
                                                setMessage(mensajeAdvertencia)
                                                setPositiveButton("SÍ, ELIMINAR TODO") { _, _ ->
                                                    scope.launch {
                                                        try {
                                                            ClienteRepository.eliminarCliente(cliente.id, context)
                                                            Toast.makeText(context, "Cliente y sus datos eliminados", Toast.LENGTH_SHORT).show()
                                                            refreshTrigger++
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                                setNegativeButton("CANCELAR", null)
                                                show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    tamanioTexto = 15,
                                    colorTexto = Color.Black
                                )

                                BotonModulo3D(
                                    texto = "VER AUTOS CLIENTES",
                                    icono = "🚗",
                                    colorClaro = Color(0xFFB9F6CA),
                                    colorMedio = Color(0xFF00C853),
                                    colorOscuro = Color(0xFF00695C),
                                    onClick = {
                                        val nombreCodificado = URLEncoder.encode(cliente.nombre.orEmpty(), StandardCharsets.UTF_8.toString())
                                        onClienteSeleccionado(cliente.id, nombreCodificado)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    tamanioTexto = 15,
                                    colorTexto = Color.Black
                                )
                            }
                        }
                    }
                }
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
