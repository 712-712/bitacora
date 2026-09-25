package com.example.bitacoraautomotriz.ui.clientes

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

    var clientes by remember { mutableStateOf(emptyList<Cliente>()) }
    var busqueda by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        clientes = ClienteRepository.obtenerClientes()
    }

    // ✅ BÚSQUEDA POR NOMBRE O POR ID
    val clientesFiltrados = clientes.filter {
        it.nombre.contains(busqueda.uppercase(), ignoreCase = true) ||
                it.id.toString().contains(busqueda, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "BUSCAR CLIENTE", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Colores.TituloPrincipal)
        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "ESCRIBA EL NOMBRE O ID:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo, modifier = Modifier.align(Alignment.Start))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it.uppercase() },
            // ✅ CORREGIDO: Texto blanco para que se lea sobre el fondo oscuro del campo
            textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Colores.FondoSecundario, unfocusedContainerColor = Colores.FondoSecundario,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedIndicatorColor = Colores.BordeBoton, unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
                cursorColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().height(72.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (clientesFiltrados.isEmpty()) {
            Text(text = "NO SE ENCONTRARON CLIENTES", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
        } else {
            clientesFiltrados.forEach { cliente ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                        // ✅ TEXTOS DENTRO DE LA TARJETA CAMBIADOS A NEGRO NEGRITA
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "NOMBRE:", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            Text(text = cliente.nombre.uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "TELÉFONO:", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.Black)

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                                    text = "${cliente.telefono} | ID: ${cliente.id}",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "CORREO:", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black)

                        if (cliente.correo.isNotBlank()) {
                            ClickableText(
                                text = AnnotatedString(" ${cliente.correo}"),
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:${cliente.correo}")
                                        putExtra(Intent.EXTRA_SUBJECT, "Contacto desde Taller Bitácora Automotriz")
                                    }
                                    try { context.startActivity(intent) } catch (e: Exception) { Toast.makeText(context, "No hay app de correo instalada", Toast.LENGTH_LONG).show() }
                                },
                                style = TextStyle(color = Color(0xFF0033FF), fontSize = 18.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
                            )
                        } else {
                            Text(text = "Sin correo registrado", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "DIRECCIÓN:", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            Text(text = if (cliente.direccion.isNotBlank()) cliente.direccion.uppercase() else "SIN DIRECCIÓN", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            BotonModulo3D(texto = "EDITAR CLIENTE", icono = "✏️", onClick = { onEditarCliente(cliente.id) }, modifier = Modifier.fillMaxWidth())

                            BotonModulo3D(
                                texto = "ELIMINAR CLIENTE", icono = "🗑️",
                                onClick = {
                                    scope.launch {
                                        val cantidadAutos = AutoRepository.contarAutosPorCliente(cliente.nombre)
                                        val mensajeAdvertencia = if (cantidadAutos > 0) {
                                            "¿Está seguro de eliminar al cliente \"${cliente.nombre}\"?\n\n⚠️ ADVERTENCIA: Esta acción eliminará TAMBIÉN:\n• $cantidadAutos auto(s) registrado(s)\n• Todos los registros de servicio asociados\n\nEsta acción NO se puede deshacer."
                                        } else {
                                            "¿Está seguro de eliminar al cliente \"${cliente.nombre}\"?\n\nEste cliente no tiene autos registrados.\n\nEsta acción NO se puede deshacer."
                                        }

                                        AlertDialog.Builder(context).apply {
                                            setTitle("⚠️ ELIMINACIÓN EN CASCADA")
                                            setMessage(mensajeAdvertencia)
                                            setPositiveButton("SÍ, ELIMINAR TODO") { _, _ ->
                                                clientes = clientes.filter { it.id != cliente.id }
                                                scope.launch {
                                                    try {
                                                        ClienteRepository.eliminarCliente(cliente.id)
                                                        Toast.makeText(context, "Cliente y sus datos eliminados", Toast.LENGTH_SHORT).show()
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                        refreshTrigger++
                                                    }
                                                }
                                            }
                                            setNegativeButton("CANCELAR", null)
                                            show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            BotonModulo3D(
                                texto = "VER MIS AUTOS", icono = "🚗",
                                onClick = {
                                    val nombreCodificado = URLEncoder.encode(cliente.nombre, StandardCharsets.UTF_8.toString())
                                    onClienteSeleccionado(cliente.id, nombreCodificado)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        BotonModulo3D(texto = "REGRESAR", icono = "🔙", onClick = onRegresar, modifier = Modifier.fillMaxWidth())
    }
}
