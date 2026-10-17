package com.example.bitacoraautomotriz.ui.clientes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

@Composable
fun MisDatosFacturacionScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("datos_facturacion_prefs", Context.MODE_PRIVATE) }
    val esAppCliente = remember { context.packageName.lowercase().contains("cliente") }

    // Inicia en modo lectura/deshabilitado hasta presionar "EDITAR"
    var editando by remember { mutableStateOf(false) }

    var todosLosClientes by remember { mutableStateOf<List<Cliente>>(emptyList()) }
    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var busquedaCliente by remember { mutableStateOf("") }

    // CAMPOS DE DATOS FISCALES Y OPERATIVOS DE FACTURACIÓN
    var rfc by remember { mutableStateOf(prefs.getString("rfc", "") ?: "") }
    var razonSocial by remember { mutableStateOf(prefs.getString("razonSocial", "") ?: "") }
    var direccionFiscal by remember { mutableStateOf(prefs.getString("direccionFiscal", "") ?: "") }
    var codigoPostalFiscal by remember { mutableStateOf(prefs.getString("codigoPostalFiscal", "") ?: "") }
    var regimenFiscal by remember { mutableStateOf(prefs.getString("regimenFiscal", "") ?: "") }
    var usoCfdi by remember { mutableStateOf(prefs.getString("usoCfdi", "") ?: "") }
    var datoOperativo by remember { mutableStateOf(prefs.getString("datoOperativo", "") ?: "") }
    var formaPago by remember { mutableStateOf(prefs.getString("formaPago", "") ?: "") }
    var metodoPago by remember { mutableStateOf(prefs.getString("metodoPago", "") ?: "") }
    var correoElectronico by remember { mutableStateOf(prefs.getString("correoElectronico", "") ?: "") }

    LaunchedEffect(Unit) {
        try {
            val lista = ClienteRepository.obtenerClientes(context)
            todosLosClientes = lista
            if (lista.isNotEmpty()) {
                val primero = lista.first()
                clienteSeleccionado = primero
                rfc = primero.rfc.ifBlank { rfc }
                razonSocial = primero.razonSocial.ifBlank { primero.nombre }
                codigoPostalFiscal = primero.codigoPostal.ifBlank { codigoPostalFiscal }
                direccionFiscal = primero.direccion.ifBlank { direccionFiscal }
                correoElectronico = primero.correo.ifBlank { correoElectronico }
            }
        } catch (_: Exception) {}
    }

    fun cargarDatosCliente(c: Cliente) {
        clienteSeleccionado = c
        rfc = c.rfc
        razonSocial = if (c.razonSocial.isNotBlank()) c.razonSocial else c.nombre
        direccionFiscal = c.direccion
        codigoPostalFiscal = c.codigoPostal
        regimenFiscal = c.regimenFiscalDesc
        usoCfdi = c.usoCfdiDesc
        correoElectronico = c.correo

        // Si el cliente mandó sus datos desde su app, tienen prioridad
        FirebaseSyncManager.obtenerSolicitudFactura(razonSocial) { s ->
            if (s != null) {
                (s["rfc"] as? String)?.takeIf { it.isNotBlank() }?.let { rfc = it }
                (s["razonSocial"] as? String)?.takeIf { it.isNotBlank() }?.let { razonSocial = it }
                (s["direccionFiscal"] as? String)?.let { direccionFiscal = it }
                (s["codigoPostalFiscal"] as? String)?.let { codigoPostalFiscal = it }
                (s["regimenFiscal"] as? String)?.let { regimenFiscal = it }
                (s["usoCfdi"] as? String)?.let { usoCfdi = it }
                (s["formaPago"] as? String)?.let { formaPago = it }
                (s["metodoPago"] as? String)?.let { metodoPago = it }
                (s["correoElectronico"] as? String)?.let { correoElectronico = it }
                Toast.makeText(context, "📥 Datos recibidos de la app del cliente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val query = busquedaCliente.uppercase().trim()
    val clientesFiltrados = remember(todosLosClientes, query) {
        if (query.isBlank()) {
            todosLosClientes
        } else {
            todosLosClientes.filter { c ->
                c.nombre.uppercase().contains(query, ignoreCase = true) ||
                        c.id.toString().contains(query, ignoreCase = true) ||
                        c.rfc.uppercase().contains(query, ignoreCase = true)
            }
        }
    }

    val estiloCampo = TextStyle(
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )

    val estiloCampoCorreoAzul = TextStyle(
        color = Color(0xFF00B0FF),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        textDecoration = TextDecoration.Underline
    )

    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        disabledContainerColor = Colores.FondoSecundario,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White,
        focusedBorderColor = Colores.BordeBoton,
        unfocusedBorderColor = Colores.BordeBoton.copy(alpha = 0.5f),
        focusedLabelColor = Colores.EtiquetaCampo,
        unfocusedLabelColor = Colores.EtiquetaCampo.copy(alpha = 0.7f),
        cursorColor = Color.White
    )

    // Devuelve true si se guardó, false si la validación falló
    fun guardarDatos(): Boolean {
        // VALIDACIÓN: solo en la app cliente, antes de enviar al taller
        if (esAppCliente) {
            if (razonSocial.isBlank() || rfc.trim().length !in 12..13 || codigoPostalFiscal.trim().length != 5) {
                Toast.makeText(
                    context,
                    "Revise: razón social, RFC (12 o 13 caracteres) y código postal (5 dígitos)",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

        prefs.edit().apply {
            putString("rfc", rfc.trim().uppercase())
            putString("razonSocial", razonSocial.trim().uppercase())
            putString("direccionFiscal", direccionFiscal.trim().uppercase())
            putString("codigoPostalFiscal", codigoPostalFiscal.trim())
            putString("regimenFiscal", regimenFiscal.trim().uppercase())
            putString("usoCfdi", usoCfdi.trim().uppercase())
            putString("datoOperativo", datoOperativo.trim().uppercase())
            putString("formaPago", formaPago.trim().uppercase())
            putString("metodoPago", metodoPago.trim().uppercase())
            putString("correoElectronico", correoElectronico.trim().lowercase())
            apply()
        }

        // SI HAY UN CLIENTE SELECCIONADO, ACTUALIZAR EN BASE DE DATOS ROOM
        clienteSeleccionado?.let { c ->
            val cActualizado = c.copy(
                rfc = rfc.trim().uppercase(),
                razonSocial = razonSocial.trim().uppercase(),
                direccion = direccionFiscal.trim().uppercase(),
                codigoPostal = codigoPostalFiscal.trim(),
                correo = correoElectronico.trim().lowercase(),
                regimenFiscalDesc = regimenFiscal.trim().uppercase(),
                usoCfdiDesc = usoCfdi.trim().uppercase()
            )
            scope.launch {
                try {
                    ClienteRepository.actualizarCliente(cActualizado, context)
                } catch (_: Exception) {}
            }
        }

        // TRANSMITIR DATOS A FIREBASE EN LA RUTA DE SERVICIOS
        val datosFacturaMap = mapOf(
            "clienteId" to (clienteSeleccionado?.id ?: 0),
            "rfc" to rfc.trim().uppercase(),
            "razonSocial" to razonSocial.trim().uppercase(),
            "direccionFiscal" to direccionFiscal.trim().uppercase(),
            "codigoPostalFiscal" to codigoPostalFiscal.trim(),
            "regimenFiscal" to regimenFiscal.trim().uppercase(),
            "usoCfdi" to usoCfdi.trim().uppercase(),
            "formaPago" to formaPago.trim().uppercase(),
            "metodoPago" to metodoPago.trim().uppercase(),
            "correoElectronico" to correoElectronico.trim().lowercase()
        )
        val clienteKey = if (razonSocial.isNotBlank()) razonSocial else (clienteSeleccionado?.nombre ?: "cliente")
        if (esAppCliente) {
            FirebaseSyncManager.publicarSolicitudFactura(clienteKey, datosFacturaMap)
        } else {
            FirebaseSyncManager.publicarRespuestaClienteConPush(clienteKey, datosFacturaMap)
        }

        editando = false
        Toast.makeText(
            context,
            if (esAppCliente) "✅ Datos enviados al taller" else "✅ Datos de facturación guardados",
            Toast.LENGTH_SHORT
        ).show()
        return true
    }

    fun abrirAppCorreo() {
        if (correoElectronico.isNotBlank()) {
            val email = correoElectronico.trim().lowercase()
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Datos de Facturación SAT - Taller Bitácora Automotriz")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "DATOS DE FACTURACIÓN SAT (CLIENTE ID: ${clienteSeleccionado?.id ?: "N/A"}):\n\n" +
                            "RFC: $rfc\n" +
                            "Nombre o Razón Social: $razonSocial\n" +
                            "Dirección Fiscal: $direccionFiscal\n" +
                            "Código Postal: $codigoPostalFiscal\n" +
                            "Régimen Fiscal: $regimenFiscal\n" +
                            "Uso CFDI: $usoCfdi\n\n" +
                            "DATOS COMPLEMENTARIOS:\n" +
                            "Dato Operativo: $datoOperativo\n" +
                            "Forma de Pago: $formaPago\n" +
                            "Método de Pago: $metodoPago\n" +
                            "Correo: $email"
                )
            }
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "No hay una aplicación de correo instalada", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show()
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
                text = if (esAppCliente) "MIS DATOS DE FACTURACIÓN" else "DATOS FACTURACION CLIENTE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Complete el formulario con sus datos fiscales para el SAT",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.EtiquetaCampo,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DATOS FISCALES (SAT)",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // BUSCADOR, COINCIDENCIAS Y "CAMBIAR CLIENTE": SOLO EN LA APP TALLER
                    if (!esAppCliente) {
                        OutlinedTextField(
                            value = busquedaCliente,
                            onValueChange = { busquedaCliente = it.uppercase() },
                            placeholder = { Text("Buscar cliente o ID...", color = Color.Gray, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar Cliente",
                                    tint = Color.White
                                )
                            },
                            trailingIcon = {
                                if (busquedaCliente.isNotEmpty()) {
                                    IconButton(onClick = { busquedaCliente = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpiar Búsqueda",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (busquedaCliente.isNotBlank() && clientesFiltrados.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                clientesFiltrados.take(4).forEach { cItem ->
                                    Button(
                                        onClick = {
                                            cargarDatosCliente(cItem)
                                            busquedaCliente = ""
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Colores.FondoSecundario)
                                    ) {
                                        Text(
                                            text = "👤 ID: ${cItem.id} - ${cItem.nombre.uppercase()}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (clienteSeleccionado != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                TextButton(
                                    onClick = { clienteSeleccionado = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("CAMBIAR CLIENTE", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "👤 CLIENTE: ${clienteSeleccionado!!.nombre.uppercase()} (ID: ${clienteSeleccionado!!.id})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7DFFB2)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF004D33))

                    // 1. RFC
                    Column {
                        Text(text = "RFC :", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = rfc,
                            onValueChange = { if (it.length <= 13) rfc = it.uppercase() },
                            enabled = editando,
                            placeholder = { Text("Ej: ABC123456XYZ", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 2. NOMBRE O RAZÓN SOCIAL
                    Column {
                        Text(text = "Nombre o Razón Social :", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = razonSocial,
                            onValueChange = { razonSocial = it.uppercase() },
                            enabled = editando,
                            placeholder = { Text("Nombre o Razón Social exacta según SAT", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 3. DIRECCIÓN FISCAL (OPCIONAL)
                    Column {
                        Text(text = "Dirección Fiscal (Opcional):", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = direccionFiscal,
                            onValueChange = { direccionFiscal = it.uppercase() },
                            enabled = editando,
                            placeholder = { Text("Calle, Número, Colonia, Municipio", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 4. CÓDIGO POSTAL FISCAL
                    Column {
                        Text(text = "Código Postal Fiscal :", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = codigoPostalFiscal,
                            onValueChange = { if (it.length <= 5 && it.all { c -> c.isDigit() }) codigoPostalFiscal = it },
                            enabled = editando,
                            placeholder = { Text("Ej: 64000", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 5. RÉGIMEN FISCAL
                    Column {
                        Text(text = "Régimen Fiscal :", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = regimenFiscal,
                            onValueChange = { regimenFiscal = it.uppercase() },
                            enabled = editando,
                            placeholder = { Text("Ej: 601 - General de Ley Personas Morales", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 6. USO DEL CFDI
                    Column {
                        Text(text = "Uso del CFDI :", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = usoCfdi,
                            onValueChange = { usoCfdi = it.uppercase() },
                            enabled = editando,
                            placeholder = { Text("Ej: G03 - Gastos en general", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF004D33))

                    Text(
                        text = "DATOS COMPLEMENTARIOS (OPERATIVOS)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // 7. DATO OPERATIVO
                    Column {
                        Text(text = "Dato Operativo :", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = datoOperativo,
                            onValueChange = { datoOperativo = it.uppercase() },
                            enabled = editando,
                            placeholder = { Text("Dato operativo adicional", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 8. FORMA DE PAGO
                    Column {
                        Text(text = "Forma de Pago :", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = formaPago,
                            onValueChange = { formaPago = it.uppercase() },
                            enabled = editando,
                            placeholder = { Text("Ej: 01 - Efectivo, 03 - Transferencia", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 9. MÉTODO DE PAGO
                    Column {
                        Text(text = "Método de Pago :", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = metodoPago,
                            onValueChange = { metodoPago = it.uppercase() },
                            enabled = editando,
                            placeholder = { Text("Ej: PUE - Pago en una sola exhibición", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 10. CORREO ELECTRÓNICO (ENLACE AZUL CLICABLE)
                    Column {
                        Text(
                            text = "Correo Electrónico (Toca el enlace para enviar) :",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        OutlinedTextField(
                            value = correoElectronico,
                            onValueChange = { correoElectronico = it.lowercase() },
                            enabled = editando,
                            placeholder = { Text("cliente@correo.com", color = Color(0xFF00B0FF).copy(alpha = 0.6f)) },
                            textStyle = estiloCampoCorreoAzul,
                            colors = coloresCampo,
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { abrirAppCorreo() }) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Enviar Correo",
                                        tint = Color(0xFF00B0FF)
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !editando) { abrirAppCorreo() }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // TRES BOTONES EN UNA SOLA FILA: EDITAR, GUARDAR Y ENVIAR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        BotonModulo3D(
                            texto = "EDITAR",
                            icono = "✏️",
                            colorClaro = Color(0xFF80D8FF),
                            colorMedio = Color(0xFF00B8D4),
                            colorOscuro = Color(0xFF006064),
                            colorTexto = Color.Black,
                            onClick = {
                                editando = true
                                Toast.makeText(context, "✏️ Edición de datos fiscales habilitada", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            tamanioTexto = 11
                        )

                        BotonModulo3D(
                            texto = "GUARDAR",
                            icono = "💾",
                            colorClaro = Color(0xFFB9F6CA),
                            colorMedio = Color(0xFF00C853),
                            colorOscuro = Color(0xFF00695C),
                            colorTexto = Color.Black,
                            onClick = { guardarDatos() },
                            modifier = Modifier.weight(1f).height(46.dp),
                            tamanioTexto = 11
                        )

                        BotonModulo3D(
                            texto = "ENVIAR",
                            icono = "📧",
                            colorClaro = Color(0xFF80D8FF),
                            colorMedio = Color(0xFF00B8D4),
                            colorOscuro = Color(0xFF006064),
                            colorTexto = Color.Black,
                            onClick = {
                                if (guardarDatos()) {
                                    abrirAppCorreo()
                                }
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            tamanioTexto = 11
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // REGRESAR FIJO E INMÓVIL AL FONDO (GRIS)
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
                    colorTexto = Color.White,
                    onClick = onRegresar,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    tamanioTexto = 16
                )
            }
        }
    }
}
