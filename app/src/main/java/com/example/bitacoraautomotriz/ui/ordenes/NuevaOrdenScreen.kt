package com.example.bitacoraautomotriz.ui.ordenes

import android.app.DatePickerDialog
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun NuevaOrdenScreen(
    onGuardar: () -> Unit,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val kmFocusRequester = remember { FocusRequester() }

    var todosLosClientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var busquedaCliente by remember { mutableStateOf("") }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var autosDelCliente by remember { mutableStateOf<List<Auto>>(emptyList()) }
    var autoSeleccionado by remember { mutableStateOf<Auto?>(null) }
    val scrollState = rememberScrollState()

    // AL SELECCIONAR AUTO: DESPLAZA AL INICIO Y PONE EL CURSOR EN KILOMETRAJE
    LaunchedEffect(autoSeleccionado) {
        if (autoSeleccionado != null) {
            scrollState.animateScrollTo(0)
            kmFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(Unit) {
        try {
            todosLosClientes = ClienteRepository.obtenerClientes(context)
        } catch (_: Exception) {
            todosLosClientes = emptyList()
        }
    }

    LaunchedEffect(clienteSeleccionado) {
        if (clienteSeleccionado != null) {
            try {
                autosDelCliente = AutoRepository.obtenerAutosPorCliente(clienteSeleccionado!!.nombre, context)
            } catch (_: Exception) {
                autosDelCliente = emptyList()
            }
        } else {
            autosDelCliente = emptyList()
        }
        autoSeleccionado = null
    }

    val queryCliente = busquedaCliente.uppercase().trim()
    val clientesFiltrados = todosLosClientes.filter { cliente ->
        cliente.nombre.uppercase().contains(queryCliente, ignoreCase = true) ||
                cliente.id.toString().contains(queryCliente, ignoreCase = true)
    }

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val fechaActual = remember { formatoFecha.format(Calendar.getInstance().time) }

    var kilometraje by remember { mutableStateOf("") }
    var fallaReportada by remember { mutableStateOf("") }
    var diagnostico by remember { mutableStateOf("") }
    var trabajoRealizado by remember { mutableStateOf("") }

    var costoManoObra by remember { mutableStateOf("") }
    var costoRefacciones by remember { mutableStateOf("") }

    var estado by remember { mutableStateOf("EN ESPERA") }
    var fechaCotizacion by remember { mutableStateOf(fechaActual) }
    var fechaEntrega by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    val bivKm = remember { BringIntoViewRequester() }
    val bivFalla = remember { BringIntoViewRequester() }
    val bivDiag = remember { BringIntoViewRequester() }
    val bivTrabajo = remember { BringIntoViewRequester() }
    val bivManoObra = remember { BringIntoViewRequester() }
    val bivRefacciones = remember { BringIntoViewRequester() }

    val coloresCamposTexto = TextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedIndicatorColor = Colores.BordeBoton,
        unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
        cursorColor = Color.White,
        selectionColors = TextSelectionColors(
            handleColor = Color.White,
            backgroundColor = Color(0xFF90CAF9).copy(alpha = 0.4f)
        )
    )

    @Composable
    fun EtiquetaCampo(texto: String) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = texto,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }

    val manoObraNum = costoManoObra.replace(",", ".").toDoubleOrNull() ?: 0.0
    val refaccionesNum = costoRefacciones.replace(",", ".").toDoubleOrNull() ?: 0.0
    val subtotal = manoObraNum + refaccionesNum
    val iva = subtotal * 0.16
    val total = subtotal + iva

    fun guardarOrden() {
        if (clienteSeleccionado == null) { mensaje = "SELECCIONE UN CLIENTE"; return }
        if (autoSeleccionado == null) { mensaje = "SELECCIONE UN AUTO"; return }
        if (fechaCotizacion.isBlank()) { mensaje = "SELECCIONE LA FECHA DE COTIZACIÓN"; return }
        if (kilometraje.isBlank()) { mensaje = "INGRESE EL KILOMETRAJE"; return }
        val kilometrajeNumero = kilometraje.toIntOrNull()
        if (kilometrajeNumero == null || kilometrajeNumero < 0) { mensaje = "INGRESE UN KILOMETRAJE VÁLIDO"; return }
        if (fallaReportada.isBlank()) { mensaje = "INGRESE EL REPORTE DE FALLAS"; return }
        if (diagnostico.isBlank()) { mensaje = "INGRESE EL DIAGNÓSTICO"; return }
        if (trabajoRealizado.isBlank()) { mensaje = "INGRESE EL TRABAJO POR REALIZAR"; return }
        if (costoManoObra.isBlank()) { mensaje = "INGRESE EL COSTO DE MANO DE OBRA"; return }

        val cliente = clienteSeleccionado!!
        val auto = autoSeleccionado!!
        val datosAuto = "${auto.marca} ${auto.modelo} ${auto.anio} - ${auto.placa}"

        val orden = OrdenServicio(
            cliente = cliente.nombre.trim(),
            auto = datosAuto,
            fecha = fechaCotizacion.trim(),
            kilometraje = kilometrajeNumero,
            fallaReportada = fallaReportada.trim().uppercase(),
            diagnostico = diagnostico.trim().uppercase(),
            trabajoRealizado = trabajoRealizado.trim().uppercase(),
            estado = estado.trim(),
            porcentajeAvance = 0,
            fechaEntrega = fechaEntrega.trim(),
            costoManoObra = manoObraNum,
            costoRefacciones = refaccionesNum,
            iva = iva,
            total = total
        )

        guardando = true
        mensaje = ""
        scope.launch {
            try {
                OrdenServicioRepository.guardarOrden(orden, context)
                guardando = false
                mensaje = "✅ COTIZACIÓN GUARDADA CON ÉXITO."
                onGuardar()
            } catch (e: Exception) {
                guardando = false
                mensaje = "ERROR AL GUARDAR: ${e.message}"
            }
        }
    }

    // TARJETA ÚNICA CON BOTONES ALINEADOS DEBAJO DEL NOMBRE Y AUTO
    @Composable
    fun TarjetaUnificadaClienteYAuto() {
        if (clienteSeleccionado != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "CLIENTE: ${clienteSeleccionado!!.nombre.uppercase()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        TextButton(
                            onClick = { clienteSeleccionado = null; autoSeleccionado = null },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("CAMBIAR DE CLIENTE", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    if (autoSeleccionado != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0xFF004D33))
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "AUTO: ${autoSeleccionado!!.marca.uppercase()} ${autoSeleccionado!!.modelo.uppercase()} (${autoSeleccionado!!.placa.uppercase()})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7DFFB2)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            TextButton(
                                onClick = { autoSeleccionado = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("CAMBIAR DE AUTO", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
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
                .verticalScroll(scrollState)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NUEVA ORDEN DE COTIZACIÓN",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )
            Spacer(modifier = Modifier.height(24.dp))

            TarjetaUnificadaClienteYAuto()

            if (clienteSeleccionado == null) {
                EtiquetaCampo("BUSCAR CLIENTE:")
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = busquedaCliente,
                    onValueChange = { busquedaCliente = it.uppercase() },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    placeholder = { Text("Nombre o ID del cliente...", fontSize = 16.sp, color = Color.Gray) },
                    singleLine = true,
                    colors = coloresCamposTexto,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (clientesFiltrados.isEmpty()) {
                    Text("NO SE ENCONTRARON CLIENTES", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        clientesFiltrados.forEach { cliente ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                                onClick = { clienteSeleccionado = cliente }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = cliente.nombre.uppercase(), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "ID: ${cliente.id}", color = Color(0xFF7DFFB2), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else if (autoSeleccionado == null) {
                EtiquetaCampo("SELECCIONAR AUTO DEL CLIENTE:")
                Spacer(modifier = Modifier.height(8.dp))

                if (autosDelCliente.isEmpty()) {
                    Text("ESTE CLIENTE NO TIENE AUTOS REGISTRADOS", color = Color(0xFFFF5252), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        autosDelCliente.forEach { auto ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                                onClick = { autoSeleccionado = auto }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "${auto.marca.uppercase()} ${auto.modelo.uppercase()} (${auto.placa.uppercase()})", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "TOCAR", color = Color(0xFF7DFFB2), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                // KILOMETRAJE
                EtiquetaCampo("KILOMETRAJE ACTUAL:")
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = kilometraje,
                    onValueChange = { if (it.all { c -> c.isDigit() }) kilometraje = it },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    colors = coloresCamposTexto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(kmFocusRequester)
                        .bringIntoViewRequester(bivKm)
                        .onFocusEvent { if (it.isFocused) scope.launch { bivKm.bringIntoView() } }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // REPORTE DE FALLAS
                EtiquetaCampo("REPORTE DE FALLAS:")
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = fallaReportada,
                    onValueChange = { fallaReportada = it.uppercase() },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = coloresCamposTexto,
                    modifier = Modifier.fillMaxWidth().height(100.dp).bringIntoViewRequester(bivFalla).onFocusEvent { if (it.isFocused) scope.launch { bivFalla.bringIntoView() } }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // DIAGNÓSTICO
                EtiquetaCampo("DIAGNÓSTICO:")
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = diagnostico,
                    onValueChange = { diagnostico = it.uppercase() },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = coloresCamposTexto,
                    modifier = Modifier.fillMaxWidth().height(100.dp).bringIntoViewRequester(bivDiag).onFocusEvent { if (it.isFocused) scope.launch { bivDiag.bringIntoView() } }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // TRABAJO POR REALIZAR
                EtiquetaCampo("TRABAJO POR REALIZAR:")
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = trabajoRealizado,
                    onValueChange = { trabajoRealizado = it.uppercase() },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = coloresCamposTexto,
                    modifier = Modifier.fillMaxWidth().height(100.dp).bringIntoViewRequester(bivTrabajo).onFocusEvent { if (it.isFocused) scope.launch { bivTrabajo.bringIntoView() } }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // MANO DE OBRA
                EtiquetaCampo("COSTO MANO DE OBRA ($):")
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = costoManoObra,
                    onValueChange = { input ->
                        val normalizado = input.replace(",", ".")
                        if (normalizado.isEmpty() || normalizado.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            costoManoObra = normalizado
                        }
                    },
                    placeholder = { Text("0.00", color = Color.Gray) },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    prefix = { Text("$ ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = coloresCamposTexto,
                    modifier = Modifier.fillMaxWidth().bringIntoViewRequester(bivManoObra).onFocusEvent { if (it.isFocused) scope.launch { bivManoObra.bringIntoView() } }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // REFACCIONES
                EtiquetaCampo("COSTO REFACCIONES ($):")
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = costoRefacciones,
                    onValueChange = { input ->
                        val normalizado = input.replace(",", ".")
                        if (normalizado.isEmpty() || normalizado.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            costoRefacciones = normalizado
                        }
                    },
                    placeholder = { Text("0.00", color = Color.Gray) },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    prefix = { Text("$ ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = coloresCamposTexto,
                    modifier = Modifier.fillMaxWidth().bringIntoViewRequester(bivRefacciones).onFocusEvent { if (it.isFocused) scope.launch { bivRefacciones.bringIntoView() } }
                )
                Spacer(modifier = Modifier.height(20.dp))

                // RESUMEN COSTOS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mano de Obra:", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(String.format(Locale.US, "$ %,.2f", manoObraNum), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Refacciones:", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(String.format(Locale.US, "$ %,.2f", refaccionesNum), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF004D33))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SUBTOTAL:", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(String.format(Locale.US, "$ %,.2f", subtotal), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("I.V.A. (16%):", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(String.format(Locale.US, "$ %,.2f", iva), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL COTIZACIÓN:", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(String.format(Locale.US, "$ %,.2f", total), color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                if (mensaje.isNotBlank()) {
                    Text(mensaje, color = Color(0xFFFF5252), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                BotonModulo3D(
                    texto = if (guardando) "GUARDANDO..." else "GUARDAR COTIZACIÓN",
                    icono = "💾",
                    colorClaro = Color(0xFFD7B899),
                    colorMedio = Color(0xFF9B6B43),
                    colorOscuro = Color(0xFF5D3A1A),
                    colorTexto = Color.Black,
                    onClick = { if (!guardando) guardarOrden() },
                    modifier = Modifier.fillMaxWidth().height(58.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // BOTÓN REGRESAR FIJO E INMÓVIL AL FONDO (DETRÁS DEL TECLADO)
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
                colorTexto = Color.White,
                onClick = onRegresar,
                modifier = Modifier.fillMaxWidth().height(58.dp)
            )
        }
    }
}
