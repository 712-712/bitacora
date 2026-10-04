package com.example.bitacoraautomotriz.ui.ordenes

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.R
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.AlertaMantenimiento
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ProgramarAlertaScreen(
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val formatoHora = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val horaActual = remember { formatoHora.format(Calendar.getInstance().time) }

    val fechaProximaConHora = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        "${formatoFecha.format(cal.time)} $horaActual"
    }

    var clientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var autosDelCliente by remember { mutableStateOf<List<Auto>>(emptyList()) }
    var autoSeleccionado by remember { mutableStateOf<Auto?>(null) }

    var tipoAlerta by remember { mutableStateOf("Por Tiempo") }
    var fechaRevision by remember { mutableStateOf(fechaProximaConHora) }
    var mensajeEditado by remember { mutableStateOf("") }
    var editandoMensaje by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }

    val mensajeFocus = remember { FocusRequester() }
    val bivMensaje = remember { BringIntoViewRequester() }

    LaunchedEffect(Unit) {
        try {
            val lista = ClienteRepository.obtenerClientes(context)
            clientes = lista
            if (lista.isNotEmpty()) {
                clienteSeleccionado = lista.first()
            }
        } catch (_: Exception) {
            clientes = emptyList()
        } finally {
            cargando = false
        }
    }

    LaunchedEffect(clienteSeleccionado) {
        if (clienteSeleccionado != null) {
            try {
                autosDelCliente = AutoRepository.obtenerAutosPorCliente(clienteSeleccionado!!.nombre, context)
                if (autosDelCliente.isNotEmpty()) {
                    autoSeleccionado = autosDelCliente.first()
                } else {
                    autoSeleccionado = null
                }
            } catch (_: Exception) {
                autosDelCliente = emptyList()
                autoSeleccionado = null
            }
        }
    }

    val nombreClienteText = clienteSeleccionado?.nombre ?: "JUAN PÉREZ"
    val autoText = if (autoSeleccionado != null) "${autoSeleccionado!!.marca} ${autoSeleccionado!!.modelo} (${autoSeleccionado!!.placa})" else "TOYOTA COROLLA (AB-CD-12)"
    val telefonoCliente = clienteSeleccionado?.telefono ?: ""

    val plantillaAutomatica = remember(nombreClienteText, autoText, tipoAlerta, fechaRevision) {
        "HOLA $nombreClienteText, TE RECORDAMOS QUE A TU $autoText LE CORRESPONDE SU REVISIÓN DE SERVICIO ($tipoAlerta) PROGRAMADA PARA LA FECHA $fechaRevision. AGENDA TU CITA DE INGRESO AQUÍ."
    }

    val mensajeNotificacionFinal = if (mensajeEditado.isNotBlank()) mensajeEditado else plantillaAutomatica

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

    fun mostrarCalendarioRevision() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            R.style.CalendarioVerdeTheme,
            { _, y, m, d ->
                cal.set(y, m, d)
                val horaStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Calendar.getInstance().time)
                fechaRevision = "${formatoFecha.format(cal.time)} $horaStr"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun enviarAlerta() {
        val alerta = AlertaMantenimiento(
            clienteNombre = nombreClienteText,
            autoPlaca = autoSeleccionado?.placa ?: "",
            tipoAlerta = tipoAlerta,
            fechaRevision = fechaRevision,
            mensaje = mensajeNotificacionFinal
        )
        try {
            FirebaseSyncManager.subirAlertaAFirebase(alerta)
        } catch (_: Exception) {}

        if (telefonoCliente.isNotBlank()) {
            val telLimpio = telefonoCliente.replace(Regex("[^0-9]"), "")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/52$telLimpio?text=${Uri.encode(mensajeNotificacionFinal)}")
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "✅ Alerta enviada en tiempo real a la app del cliente", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "✅ Alerta enviada en tiempo real a la app del cliente", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .imePadding()
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
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // TÍTULO EN 2 LÍNEAS ALINEADAS SOLICITADO
            Text(
                text = "PANEL DE ALERTAS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "AL CLIENTE",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // TARJETA 1: BUSCAR CLIENTE (FONDO ROJO AL SELECCIONAR)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "BUSCAR CLIENTE:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (cargando) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else if (clientes.isEmpty()) {
                        Text("No hay clientes registrados", color = Color.White, fontSize = 15.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            clientes.take(4).forEach { c ->
                                val sel = c == clienteSeleccionado
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (sel) Color(0xFFB51F1F) else Colores.FondoSecundario // ✅ FONDO ROJO AL SELECCIONAR
                                    ),
                                    onClick = { clienteSeleccionado = c }
                                ) {
                                    Text(
                                        text = "${c.nombre} (ID: ${c.id})",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TARJETA 2: SELECCIONAR VEHÍCULO DEL CLIENTE (FONDO ROJO AL SELECCIONAR)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SELECCIONAR VEHÍCULO DEL CLIENTE:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (autosDelCliente.isEmpty()) {
                        Text("Este cliente no tiene vehículos registrados", color = Color.White, fontSize = 14.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            autosDelCliente.forEach { a ->
                                val sel = a == autoSeleccionado
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (sel) Color(0xFFB51F1F) else Colores.FondoSecundario // ✅ FONDO ROJO AL SELECCIONAR
                                    ),
                                    onClick = { autoSeleccionado = a }
                                ) {
                                    Text(
                                        text = "${a.marca} ${a.modelo} (Placa: ${a.placa})",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TARJETA 3: TIPO DE ALERTA Y FECHA CON HORA EN TIEMPO REAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TIPO DE ALERTA:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = tipoAlerta == "Por Kilometraje",
                                onClick = { tipoAlerta = "Por Kilometraje" },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00DDEB), unselectedColor = Color.White)
                            )
                            Text("Por Kilometraje", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = tipoAlerta == "Por Tiempo",
                                onClick = { tipoAlerta = "Por Tiempo" },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF00DDEB), unselectedColor = Color.White)
                            )
                            Text("Por Tiempo", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = Color(0xFF004D33))

                    Text(
                        text = "FECHA Y HORA DE REVISIÓN:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    OutlinedTextField(
                        value = fechaRevision,
                        onValueChange = { fechaRevision = it },
                        readOnly = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold),
                        trailingIcon = {
                            IconButton(onClick = { mostrarCalendarioRevision() }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Calendario", tint = Color(0xFF00DDEB))
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = coloresCamposTexto,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TARJETA 4: NOTIFICACIÓN PERSONALIZADA CON CAMPO MÁS GRANDE Y BOTONES EDITAR (AZUL) Y GUARDAR (VERDE)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "NOTIFICACIÓN PERSONALIZADA:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // CAMPO DE TEXTO AMPLIO (230 DP DE ALTURA) PARA VER LA NOTIFICACIÓN COMPLETA
                    OutlinedTextField(
                        value = mensajeNotificacionFinal,
                        onValueChange = {
                            mensajeEditado = it.uppercase()
                            editandoMensaje = true
                        },
                        enabled = editandoMensaje,
                        textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        maxLines = 10,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(10.dp),
                        colors = coloresCamposTexto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .focusRequester(mensajeFocus)
                            .bringIntoViewRequester(bivMensaje)
                            .onFocusEvent { if (it.isFocused) scope.launch { bivMensaje.bringIntoView() } }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // BOTONES MEDIANOS DENTRO DE LA TARJETA: EDITAR (AZUL) Y GUARDAR (VERDE)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // BOTÓN EDITAR (AZUL)
                        BotonModulo3D(
                            texto = "EDITAR",
                            icono = "✏️",
                            colorClaro = Color(0xFF80D8FF),
                            colorMedio = Color(0xFF00B8D4),
                            colorOscuro = Color(0xFF006064),
                            colorTexto = Color.Black,
                            onClick = {
                                editandoMensaje = true
                                if (mensajeEditado.isBlank()) mensajeEditado = plantillaAutomatica
                                scope.launch {
                                    bivMensaje.bringIntoView()
                                    mensajeFocus.requestFocus()
                                }
                                Toast.makeText(context, "✏️ Edición de mensaje habilitada", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            tamanioTexto = 14
                        )

                        // BOTÓN GUARDAR (VERDE)
                        BotonModulo3D(
                            texto = "GUARDAR",
                            icono = "💾",
                            colorClaro = Color(0xFFB9F6CA),
                            colorMedio = Color(0xFF00C853),
                            colorOscuro = Color(0xFF00695C),
                            colorTexto = Color.Black,
                            onClick = {
                                editandoMensaje = false
                                focusManager.clearFocus()
                                Toast.makeText(context, "✅ Mensaje de alerta guardado", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            tamanioTexto = 14
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // BOTÓN ENVIAR ALERTA A CLIENTE
                    BotonModulo3D(
                        texto = "ENVIAR ALERTA A CLIENTE",
                        icono = "📱",
                        colorClaro = Color(0xFFD7B899),
                        colorMedio = Color(0xFF9B6B43),
                        colorOscuro = Color(0xFF5D3A1A),
                        colorTexto = Color.Black,
                        onClick = { enviarAlerta() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        tamanioTexto = 15
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BOTÓN REGRESAR (GRIS)
            BotonModulo3D(
                texto = "REGRESAR",
                icono = "🔙",
                colorClaro = Colores.RegresarClaro,
                colorMedio = Colores.RegresarMedio,
                colorOscuro = Colores.RegresarOscuro,
                colorTexto = Color.White,
                onClick = onRegresar,
                modifier = Modifier.fillMaxWidth().height(58.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
