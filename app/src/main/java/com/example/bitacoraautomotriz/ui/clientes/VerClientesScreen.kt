package com.example.bitacoraautomotriz.ui.clientes

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.net.URLEncoder

@Composable
fun VerClientesScreen(
    onEditarCliente: (Int) -> Unit,
    onVerAutos: (Int, String) -> Unit,
    onRegresar: () -> Unit
) {
    var clientes by remember { mutableStateOf(emptyList<Cliente>()) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(refreshTrigger) {
        clientes = try {
            ClienteRepository.obtenerClientes(context).sortedByDescending { it.id }
        } catch (_: Exception) {
            emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
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
                text = "CLIENTES REGISTRADOS",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (clientes.isEmpty()) {
                Text(
                    text = "NO HAY CLIENTES REGISTRADOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TextoGlobal
                )
            } else {
                clientes.forEach { cliente ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

                            Text(text = cliente.nombre, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                            Text(
                                text = "📞 ${cliente.telefono}   |   ID: ${cliente.id}",
                                fontSize = 22.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${cliente.telefono}")
                                        }
                                        context.startActivity(intent)
                                    }
                                    .padding(top = 4.dp)
                            )

                            if (cliente.correo.isNotBlank()) {
                                ClickableText(
                                    text = buildAnnotatedString {
                                        append("✉️ ")
                                        pushStyle(SpanStyle(color = Color(0xFF0033FF), textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold))
                                        append(cliente.correo)
                                        pop()
                                    },
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:${cliente.correo}") }
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            if (cliente.direccion.isNotBlank()) {
                                Text(text = "📍 ${cliente.direccion}", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(top = 4.dp))
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BotonModulo3D(
                                    texto = "EDITAR",
                                    colorClaro = Color(0xFF90CAF9),
                                    colorMedio = Color(0xFF1976D2),
                                    colorOscuro = Color(0xFF0D47A1),
                                    onClick = { onEditarCliente(cliente.id) },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    tamanioTexto = 16,
                                    colorTexto = Color.Black
                                )

                                // BOTÓN ELIMINAR CON ADVERTENCIA DE ELIMINACIÓN EN CASCADA
                                BotonModulo3D(
                                    texto = "ELIMINAR",
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
                                                            refreshTrigger++
                                                        } catch (_: Exception) { }
                                                    }
                                                }
                                                setNegativeButton("CANCELAR", null)
                                                show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    tamanioTexto = 16,
                                    colorTexto = Color.Black
                                )

                                BotonModulo3D(
                                    texto = "VER AUTOS CLIENTES",
                                    colorClaro = Color(0xFFB9F6CA),
                                    colorMedio = Color(0xFF00C853),
                                    colorOscuro = Color(0xFF00695C),
                                    onClick = {
                                        val nombreCodificado = URLEncoder.encode(cliente.nombre, "UTF-8")
                                        onVerAutos(cliente.id, nombreCodificado)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    tamanioTexto = 16,
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
                    onClick = onRegresar,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    colorTexto = Color.White
                )
            }
        }
    }
}
