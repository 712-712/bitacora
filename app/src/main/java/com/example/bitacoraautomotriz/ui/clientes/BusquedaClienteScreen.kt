package com.example.bitacoraautomotriz.ui.clientes

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
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
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                modifier = Modifier.fillMaxWidth().height(70.dp)
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "NOMBRE:", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                Text(text = cliente.nombre.orEmpty().uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "ID:", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                Text(text = "${cliente.id}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "TELÉFONO:", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (cliente.telefono.isNotBlank()) {
                                        IconButton(
                                            onClick = { WhatsAppUtils.abrirWhatsApp(context, cliente.telefono) },
                                            modifier = Modifier.padding(end = 4.dp).size(36.dp)
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
                                        text = cliente.telefono.orEmpty(),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(text = "CORREO:", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color.Black)

                            if (cliente.correo.isNotBlank()) {
                                ClickableText(
                                    text = AnnotatedString(" ${cliente.correo}"),
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:${cliente.correo}")
                                            putExtra(Intent.EXTRA_SUBJECT, "Contacto desde Taller Bitácora Automotriz")
                                        }
                                        try { context.startActivity(intent) } catch (_: Exception) { Toast.makeText(context, "No hay app de correo instalada", Toast.LENGTH_LONG).show() }
                                    },
                                    style = TextStyle(color = Color(0xFF0033FF), fontSize = 17.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                                )
                            } else {
                                Text(text = "Sin correo registrado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "DIRECCIÓN:", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color.Black)
                                Text(text = if (cliente.direccion.isNotBlank()) cliente.direccion.uppercase() else "SIN DIRECCIÓN", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                BotonModulo3D(
                                    texto = "EDITAR CLIENTE",
                                    icono = "✏️",
                                    colorClaro = Color(0xFF80D8FF),
                                    colorMedio = Color(0xFF00B8D4),
                                    colorOscuro = Color(0xFF006064),
                                    onClick = { onEditarCliente(cliente.id) },
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
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
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
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
                                    modifier = Modifier.fillMaxWidth().height(54.dp),
                                    tamanioTexto = 15,
                                    colorTexto = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // BOTÓN REGRESAR FIJO E INMÓVIL AL FONDO
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Colores.FondoPantalla)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            BotonModulo3D(
                texto = "REGRESAR",
                icono = "🔙",
                colorClaro = Colores.RegresarClaro,
                colorMedio = Colores.RegresarMedio,
                colorOscuro = Colores.RegresarOscuro,
                onClick = onRegresar,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colorTexto = Color.White
            )
        }
    }
}
