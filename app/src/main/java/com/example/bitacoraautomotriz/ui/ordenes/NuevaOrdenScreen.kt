package com.example.bitacoraautomotriz.ui.ordenes

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

    var paso by remember { mutableStateOf(1) }

    var todosLosClientes by remember { mutableStateOf(emptyList<Cliente>()) }
    var busquedaCliente by remember { mutableStateOf("") }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var autosDelCliente by remember { mutableStateOf(emptyList<Auto>()) }
    var autoSeleccionado by remember { mutableStateOf<Auto?>(null) }

    LaunchedEffect(Unit) { todosLosClientes = ClienteRepository.obtenerClientes() }
    LaunchedEffect(clienteSeleccionado) {
        if (clienteSeleccionado != null) {
            autosDelCliente = AutoRepository.obtenerAutosPorCliente(clienteSeleccionado!!.nombre)
        } else { autosDelCliente = emptyList() }
        autoSeleccionado = null
    }
    // ✅ BÚSQUEDA POR NOMBRE O POR ID
    val clientesFiltrados = todosLosClientes.filter {
        it.nombre.contains(busquedaCliente.uppercase(), ignoreCase = true) ||
                it.id.toString().contains(busquedaCliente, ignoreCase = true)
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
    var ordenId by remember { mutableStateOf<Int?>(null) }

    val bivKm = remember { BringIntoViewRequester() }
    val bivFalla = remember { BringIntoViewRequester() }
    val bivDiag = remember { BringIntoViewRequester() }
    val bivTrabajo = remember { BringIntoViewRequester() }
    val bivManoObra = remember { BringIntoViewRequester() }
    val bivRefacciones = remember { BringIntoViewRequester() }

    @Composable
    fun EtiquetaCampo(texto: String) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = texto,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90CAF9),
                modifier = Modifier.align(Alignment.CenterStart)
            )
        }
    }

    fun mostrarSelectorFecha(fechaInicial: String, onFechaSeleccionada: (String) -> Unit) {
        val calendario = Calendar.getInstance()
        try {
            if (fechaInicial.isNotBlank()) {
                val fechaConvertida = formatoFecha.parse(fechaInicial)
                if (fechaConvertida != null) calendario.time = fechaConvertida
            }
        } catch (_: Exception) { }
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            onFechaSeleccionada(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year))
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show()
    }

    val manoObraNum = costoManoObra.toDoubleOrNull() ?: 0.0
    val refaccionesNum = costoRefacciones.toDoubleOrNull() ?: 0.0
    val subtotal = manoObraNum + refaccionesNum
    val iva = subtotal * 0.16
    val total = subtotal + iva

    fun guardarOrden() {
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
            fallaReportada = fallaReportada.trim(),
            diagnostico = diagnostico.trim(),
            trabajoRealizado = trabajoRealizado.trim(),
            estado = estado.trim(),
            porcentajeAvance = 0,
            fechaEntrega = fechaEntrega.trim(),
            costoManoObra = manoObraNum,
            costoRefacciones = refaccionesNum,
            iva = iva,
            total = total
        )

        guardando = true; mensaje = ""
        scope.launch {
            try {
                OrdenServicioRepository.guardarOrden(orden)
                ordenId = OrdenServicioRepository.obtenerOrdenes().maxByOrNull { it.id }?.id
                guardando = false; mensaje = "✅ COTIZACIÓN GUARDADA CON ÉXITO."; onGuardar()
            } catch (e: Exception) { guardando = false; mensaje = "ERROR AL GUARDAR: ${e.message}" }
        }
    }

    @Composable
    fun ResumenSeleccion() {
        if (clienteSeleccionado != null && autoSeleccionado != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF80CBC4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "CLIENTE: ${clienteSeleccionado!!.nombre}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AUTO: ${autoSeleccionado!!.marca} ${autoSeleccionado!!.modelo} (${autoSeleccionado!!.placa})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050A18))
            .imePadding()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus(); keyboardController?.hide() }) }
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        if (paso == 1) {
            Text(
                text = "PASO 1 DE 3",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90CAF9),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "SELECCIONAR CLIENTE Y AUTO",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00F5FF),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))

            EtiquetaCampo("BUSCAR CLIENTE:")
            OutlinedTextField(
                value = busquedaCliente,
                onValueChange = { busquedaCliente = it.uppercase() },
                textStyle = TextStyle(color = Color.Black, fontSize = 22.sp, fontWeight = FontWeight.Bold),
                placeholder = { Text("Escriba el nombre...", fontSize = 20.sp, color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedIndicatorColor = Color(0xFF00AEEF),
                    unfocusedIndicatorColor = Color(0xFF607D8B),
                    cursorColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (busquedaCliente.isNotEmpty()) {
                if (clientesFiltrados.isEmpty()) {
                    Text(
                        text = "NO SE ENCONTRARON CLIENTES",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5252),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            clientesFiltrados.forEach { cliente ->
                                Card(
                                    onClick = { clienteSeleccionado = cliente; busquedaCliente = ""; mensaje = "" },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF80CBC4)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                                        Text(
                                            text = cliente.nombre,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Teléfono: ${cliente.telefono}",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (clienteSeleccionado != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF80CBC4)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Text(
                            text = "✅ CLIENTE SELECCIONADO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = clienteSeleccionado!!.nombre,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Teléfono: ${clienteSeleccionado!!.telefono}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BotonModulo3D(
                            texto = "CAMBIAR CLIENTE",
                            colorClaro = Color(0xFFFFCDD2),
                            colorMedio = Color(0xFFEF5350),
                            colorOscuro = Color(0xFFC62828),
                            onClick = { clienteSeleccionado = null; autoSeleccionado = null; mensaje = "" }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (clienteSeleccionado != null) {
                EtiquetaCampo("SELECCIONAR AUTO:")
                Spacer(modifier = Modifier.height(12.dp))
                if (autosDelCliente.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                            Text(
                                text = "⚠️ ESTE CLIENTE NO TIENE AUTOS REGISTRADOS",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                } else {
                    autosDelCliente.forEach { auto ->
                        val isSelected = autoSeleccionado?.id == auto.id
                        Card(
                            onClick = { autoSeleccionado = auto; kilometraje = auto.kilometraje.toString(); mensaje = "" },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF80CBC4)),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                                if (isSelected) {
                                    Text(
                                        text = "✅ AUTO SELECCIONADO",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(
                                    text = "${auto.marca} ${auto.modelo} ${auto.anio}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Placa: ${auto.placa}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))

            BotonModulo3D(
                texto = "CONTINUAR AL PASO 2",
                colorClaro = if (clienteSeleccionado != null && autoSeleccionado != null) Color(0xFF7DFFB2) else Color.Gray,
                colorMedio = if (clienteSeleccionado != null && autoSeleccionado != null) Color(0xFF00D96B) else Color.DarkGray,
                colorOscuro = if (clienteSeleccionado != null && autoSeleccionado != null) Color(0xFF008844) else Color.Black,
                onClick = {
                    if (clienteSeleccionado != null && autoSeleccionado != null) { paso = 2; mensaje = "" }
                    else { mensaje = "⚠️ DEBE SELECCIONAR UN CLIENTE Y UN AUTO" }
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            BotonModulo3D(
                texto = "REGRESAR",
                colorClaro = Color(0xFFD5E1E6),
                colorMedio = Color(0xFF90A4AE),
                colorOscuro = Color(0xFF455A64),
                onClick = onRegresar
            )
        }

        else if (paso == 2) {
            Text(
                text = "PASO 2 DE 3",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90CAF9),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "REPORTE DE FALLAS, DIAGNÓSTICO Y TRABAJO",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00F5FF),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(24.dp))
            ResumenSeleccion()

            EtiquetaCampo("KILOMETRAJE ACTUAL:")
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF80CBC4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                OutlinedTextField(
                    value = kilometraje,
                    onValueChange = { kilometraje = it.filter { char -> char.isDigit() }; mensaje = "" },
                    textStyle = TextStyle(color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF00AEEF),
                        unfocusedIndicatorColor = Color(0xFF607D8B),
                        cursorColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(4.dp)
                        .bringIntoViewRequester(bivKm)
                        .onFocusEvent { if (it.isFocused) scope.launch { bivKm.bringIntoView() } }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            EtiquetaCampo("REPORTE DE FALLAS")
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF80CBC4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                OutlinedTextField(
                    value = fallaReportada,
                    onValueChange = { fallaReportada = it.uppercase(); mensaje = "" },
                    textStyle = TextStyle(color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    minLines = 4,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF00AEEF),
                        unfocusedIndicatorColor = Color(0xFF607D8B),
                        cursorColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .bringIntoViewRequester(bivFalla)
                        .onFocusEvent { if (it.isFocused) scope.launch { bivFalla.bringIntoView() } }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            EtiquetaCampo("DIAGNÓSTICO")
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF80CBC4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                OutlinedTextField(
                    value = diagnostico,
                    onValueChange = { diagnostico = it.uppercase(); mensaje = "" },
                    textStyle = TextStyle(color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    minLines = 4,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF00AEEF),
                        unfocusedIndicatorColor = Color(0xFF607D8B),
                        cursorColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .bringIntoViewRequester(bivDiag)
                        .onFocusEvent { if (it.isFocused) scope.launch { bivDiag.bringIntoView() } }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            EtiquetaCampo("TRABAJO POR REALIZAR")
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF80CBC4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                OutlinedTextField(
                    value = trabajoRealizado,
                    onValueChange = { trabajoRealizado = it.uppercase(); mensaje = "" },
                    textStyle = TextStyle(color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    minLines = 4,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboardController?.hide() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedIndicatorColor = Color(0xFF00AEEF),
                        unfocusedIndicatorColor = Color(0xFF607D8B),
                        cursorColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .bringIntoViewRequester(bivTrabajo)
                        .onFocusEvent { if (it.isFocused) scope.launch { bivTrabajo.bringIntoView() } }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            BotonModulo3D(
                texto = "CONTINUAR AL PASO 3",
                colorClaro = Color(0xFF7DFFB2),
                colorMedio = Color(0xFF00D96B),
                colorOscuro = Color(0xFF008844),
                onClick = {
                    if (fallaReportada.isBlank()) { mensaje = "⚠️ DEBE INGRESAR EL REPORTE DE FALLAS" }
                    else if (diagnostico.isBlank()) { mensaje = "⚠️ DEBE INGRESAR EL DIAGNÓSTICO" }
                    else if (trabajoRealizado.isBlank()) { mensaje = "⚠️ DEBE INGRESAR EL TRABAJO POR REALIZAR" }
                    else { paso = 3; mensaje = "" }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            BotonModulo3D(
                texto = "ATRÁS (PASO 1)",
                colorClaro = Color(0xFFD5E1E6),
                colorMedio = Color(0xFF90A4AE),
                colorOscuro = Color(0xFF455A64),
                onClick = { paso = 1 }
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        else if (paso == 3) {
            Text(
                text = "PASO 3 DE 3",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF90CAF9),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "COTIZACIÓN Y FECHAS",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00F5FF),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))

            ResumenSeleccion()

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF80CBC4)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "💰 COTIZACIÓN DEL SERVICIO",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "MANO DE OBRA $",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = costoManoObra,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) { costoManoObra = it; mensaje = "" } },
                        textStyle = TextStyle(color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedIndicatorColor = Color(0xFF00AEEF),
                            unfocusedIndicatorColor = Color(0xFF607D8B),
                            cursorColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .bringIntoViewRequester(bivManoObra)
                            .onFocusEvent { if (it.isFocused) scope.launch { bivManoObra.bringIntoView() } }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "REFACCIONES $",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = costoRefacciones,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) { costoRefacciones = it; mensaje = "" } },
                        textStyle = TextStyle(color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); keyboardController?.hide() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedIndicatorColor = Color(0xFF00AEEF),
                            unfocusedIndicatorColor = Color(0xFF607D8B),
                            cursorColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .bringIntoViewRequester(bivRefacciones)
                            .onFocusEvent { if (it.isFocused) scope.launch { bivRefacciones.bringIntoView() } }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "SUBTOTAL", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = "$ ${String.format("%.2f", subtotal)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "I.V.A. 16%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = "$ ${String.format("%.2f", iva)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF4DB6AC), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "TOTAL", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = "$ ${String.format("%.2f", total)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                }
            }

            BotonModulo3D(
                texto = if (fechaCotizacion.isEmpty()) "📅 SELECCIONAR FECHA DE COTIZACIÓN" else "📅 $fechaCotizacion",
                colorClaro = Color(0xFF90CAF9),
                colorMedio = Color(0xFF1976D2),
                colorOscuro = Color(0xFF0D47A1),
                onClick = { mostrarSelectorFecha(fechaCotizacion) { fechaCotizacion = it } },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                tamanioTexto = 18
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (mensaje.isNotEmpty()) {
                Text(
                    text = mensaje,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (mensaje.contains("ERROR") || mensaje.contains("⚠️")) Color(0xFFFF5252) else Color(0xFF7DFFB2),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            BotonModulo3D(
                texto = if (guardando) "GUARDANDO..." else "💾 GUARDAR COTIZACIÓN",
                colorClaro = Color(0xFFD7B899),
                colorMedio = Color(0xFF9B6B43),
                colorOscuro = Color(0xFF5D3A1A),
                onClick = { if (!guardando) guardarOrden() }
            )
            Spacer(modifier = Modifier.height(16.dp))
            BotonModulo3D(
                texto = "ATRÁS (PASO 2)",
                colorClaro = Color(0xFFFFCC80),
                colorMedio = Color(0xFFFF9800),
                colorOscuro = Color(0xFFE65100),
                onClick = { paso = 2 }
            )
            Spacer(modifier = Modifier.height(16.dp))
            BotonModulo3D(
                texto = "CANCELAR TODO",
                colorClaro = Color(0xFFEF9A9A),
                colorMedio = Color(0xFFE53935),
                colorOscuro = Color(0xFFB71C1C),
                onClick = onRegresar
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
