package com.example.bitacoraautomotriz.ui.clientes

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import java.util.Locale

@Composable
fun VerClientesScreen(
    onEditarCliente: (Int) -> Unit,
    onVerAutos: (Int, String) -> Unit,
    onRegresar: () -> Unit
) {
    var clientes by remember { mutableStateOf(emptyList<Cliente>()) }
    var busquedaQuery by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(refreshTrigger) {
        clientes = try {
            ClienteRepository.obtenerClientes(context).sortedByDescending { it.id }
        } catch (_: Exception) {
            emptyList()
        }
    }

    val clientesFiltrados by remember(clientes, busquedaQuery) {
        derivedStateOf {
            if (busquedaQuery.isBlank()) {
                clientes
            } else {
                val qRaw = busquedaQuery.trim().lowercase(Locale.getDefault())
                val digitsOnly = busquedaQuery.replace(Regex("[^0-9]"), "")

                clientes.filter { c ->
                    val idStr = c.id.toString()
                    val idFormatted = String.format(Locale.US, "%05d", c.id)
                    val idTag = "id cliente: ${c.id}"
                    val idShortTag = "id: ${c.id}"

                    // Coincidencia Inteligente por ID (acepta número solo, ID: 6, ID CLIENTE: 6, #6, 00006)
                    val matchId = (digitsOnly.isNotBlank() && idStr.contains(digitsOnly)) ||
                                  (digitsOnly.isNotBlank() && idFormatted.contains(digitsOnly)) ||
                                  idStr.contains(qRaw) ||
                                  idFormatted.contains(qRaw) ||
                                  idTag.contains(qRaw) ||
                                  idShortTag.contains(qRaw)

                    // Coincidencia por otros datos de texto
                    val matchTexto = c.nombre.lowercase(Locale.getDefault()).contains(qRaw) ||
                                     c.telefono.lowercase(Locale.getDefault()).contains(qRaw) ||
                                     (digitsOnly.isNotBlank() && c.telefono.replace(Regex("[^0-9]"), "").contains(digitsOnly)) ||
                                     c.correo.lowercase(Locale.getDefault()).contains(qRaw) ||
                                     c.direccion.lowercase(Locale.getDefault()).contains(qRaw) ||
                                     c.rfc.lowercase(Locale.getDefault()).contains(qRaw) ||
                                     c.razonSocial.lowercase(Locale.getDefault()).contains(qRaw)

                    matchId || matchTexto
                }
            }
        }
    }

    val coloresCamposTexto = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        disabledContainerColor = Colores.FondoSecundario,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White,
        focusedBorderColor = Colores.BordeBoton,
        unfocusedBorderColor = Colores.BordeBoton.copy(alpha = 0.5f),
        cursorColor = Color.White,
        selectionColors = TextSelectionColors(
            handleColor = Color.White,
            backgroundColor = Color(0xFF00DDEB).copy(alpha = 0.4f)
        )
    )

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

            Spacer(modifier = Modifier.height(16.dp))

            // CUADRO DE BÚSQUEDA INTEGRADO EN TIEMPO REAL CON BOTÓN LIMPIAR
            OutlinedTextField(
                value = busquedaQuery,
                onValueChange = { busquedaQuery = it },
                placeholder = {
                    Text(
                        text = "BUSCAR CLIENTE (NOMBRE, TELÉFONO, ID...)",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar Cliente",
                        tint = Color.White
                    )
                },
                trailingIcon = {
                    if (busquedaQuery.isNotEmpty()) {
                        IconButton(onClick = { busquedaQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar Búsqueda",
                                tint = Color.White
                            )
                        }
                    }
                },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (clientesFiltrados.isEmpty()) {
                Text(
                    text = if (busquedaQuery.isBlank()) "NO HAY CLIENTES REGISTRADOS" else "NO SE ENCONTRARON CLIENTES PARA \"$busquedaQuery\"",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TextoGlobal
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
                                text = cliente.nombre.uppercase(),
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
                    tamanioTexto = 16,
                    colorTexto = Color.White
                )
            }
        }
    }
}
