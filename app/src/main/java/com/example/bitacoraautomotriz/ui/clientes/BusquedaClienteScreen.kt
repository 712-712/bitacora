package com.example.bitacoraautomotriz.ui.clientes

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

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

    var textoBusqueda by remember { mutableStateOf(value = "") }
    var todosLosClientes by remember { mutableStateOf<List<Cliente>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = true) }

    var clienteAEliminar by remember { mutableStateOf<Cliente?>(null) }

    LaunchedEffect(Unit) {
        try {
            todosLosClientes = ClienteRepository.obtenerClientes(context)
        } catch (_: Exception) {
            todosLosClientes = emptyList()
        } finally {
            cargando = false
        }
    }

    val resultados = if (textoBusqueda.isBlank()) {
        todosLosClientes
    } else {
        val query = textoBusqueda.trim().uppercase()
        todosLosClientes.filter { cliente ->
            cliente.nombre.uppercase().contains(query) ||
                    cliente.id.toString().contains(query) ||
                    cliente.telefono.contains(query)
        }
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
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "BUSCAR CLIENTE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "NOMBRE, TELÉFONO O ID DEL CLIENTE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it.uppercase() },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                placeholder = { Text("Escriba para buscar...", fontSize = 18.sp, color = Colores.EtiquetaCampo.copy(alpha = 0.6f)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus(); keyboardController?.hide() },
                    onDone = { focusManager.clearFocus(); keyboardController?.hide() }
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Colores.FondoSecundario,
                    unfocusedContainerColor = Colores.FondoSecundario,
                    disabledContainerColor = Colores.FondoSecundario,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White,
                    focusedIndicatorColor = Colores.BordeBoton,
                    unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(70.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (resultados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (todosLosClientes.isEmpty()) "NO HAY CLIENTES REGISTRADOS" else "NO SE ENCONTRARON COINCIDENCIAS",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(resultados) { cliente ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "ID DE REGISTRO: #${cliente.id}",
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

                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF004D33))

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
                                    Text(
                                        text = "CORREO:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = cliente.correo,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                if (cliente.direccion.isNotBlank()) {
                                    Text(
                                        text = "DIRECCIÓN:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = cliente.direccion.uppercase(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BotonModulo3D(
                                        texto = "AUTOS",
                                        icono = "🚗",
                                        colorClaro = Color(0xFF80D8FF),
                                        colorMedio = Color(0xFF00B8D4),
                                        colorOscuro = Color(0xFF006064),
                                        colorTexto = Color.Black,
                                        onClick = { onClienteSeleccionado(cliente.id, cliente.nombre) },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        tamanioTexto = 13
                                    )

                                    BotonModulo3D(
                                        texto = "EDITAR",
                                        icono = "✏️",
                                        colorClaro = Color(0xFFFFF59D),
                                        colorMedio = Color(0xFFFFEB3B),
                                        colorOscuro = Color(0xFFFBC02D),
                                        colorTexto = Color.Black,
                                        onClick = { onEditarCliente(cliente.id) },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        tamanioTexto = 13
                                    )

                                    BotonModulo3D(
                                        texto = "ELIMINAR",
                                        icono = "🗑️",
                                        colorClaro = Color(0xFFFF9999),
                                        colorMedio = Color(0xFFFF4141),
                                        colorOscuro = Color(0xFFB51F1F),
                                        colorTexto = Color.Black,
                                        onClick = { clienteAEliminar = cliente },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        tamanioTexto = 13
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // BOTÓN REGRESAR FIJO E INMÓVIL AL FONDO
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = Colores.FondoPantalla
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
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
                    tamanioTexto = 16,
                    colorTexto = Color.White
                )
            }
        }

        // DIÁLOGO CONFIRMAR ELIMINAR CLIENTE
        if (clienteAEliminar != null) {
            val c = clienteAEliminar!!
            AlertDialog(
                onDismissRequest = { clienteAEliminar = null },
                containerColor = Colores.FondoTarjeta,
                title = { Text("CONFIRMAR ELIMINACIÓN", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("¿Desea eliminar permanentemente a ${c.nombre.uppercase()}?", color = Color.White) },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    ClienteRepository.eliminarCliente(c.id, context)
                                    todosLosClientes = ClienteRepository.obtenerClientes(context)
                                    Toast.makeText(context, "🗑️ Cliente eliminado", Toast.LENGTH_SHORT).show()
                                } catch (_: Exception) {} finally {
                                    clienteAEliminar = null
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4141))
                    ) {
                        Text("ELIMINAR", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { clienteAEliminar = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Colores.RegresarMedio)
                    ) {
                        Text("CANCELAR", color = Color.White)
                    }
                }
            )
        }
    }
}
