package com.example.bitacoraautomotriz.ui.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

@Composable
fun MisDatosClienteScreen(
    onRegresar: () -> Unit,
) {
    var telefono by remember { mutableStateOf(value = "") }
    var cliente by remember { mutableStateOf<Cliente?>(value = null) }
    var autos by remember { mutableStateOf<List<Auto>>(value = emptyList()) }
    var mensaje by remember { mutableStateOf(value = "") }
    var buscando by remember { mutableStateOf(value = false) }
    var editando by remember { mutableStateOf(value = false) }

    // CAMPOS DE FACTURACIÓN Y CONTACTO
    var rfc by remember { mutableStateOf(value = "") }
    var razonSocial by remember { mutableStateOf(value = "") }
    var codigoPostal by remember { mutableStateOf(value = "") }
    var regimenClave by remember { mutableStateOf(value = "") }
    var regimenDesc by remember { mutableStateOf(value = "") }
    var usoCfdiClave by remember { mutableStateOf(value = "") }
    var usoCfdiDesc by remember { mutableStateOf(value = "") }
    var correo by remember { mutableStateOf(value = "") }
    var direccion by remember { mutableStateOf(value = "") }

    val scope = rememberCoroutineScope()
    val teclado = LocalSoftwareKeyboardController.current

    val estiloCampo = TextStyle(
        color = Colores.TextoTarjeta,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    )

    val coloresCampo = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        disabledContainerColor = Colores.FondoSecundario,
        focusedTextColor = Colores.TextoTarjeta,
        unfocusedTextColor = Colores.TextoTarjeta,
        disabledTextColor = Colores.TextoTarjeta,
        focusedBorderColor = Colores.BordeBoton,
        unfocusedBorderColor = Colores.BordeBoton.copy(alpha = 0.5f),
        focusedLabelColor = Colores.EtiquetaCampo,
        unfocusedLabelColor = Colores.EtiquetaCampo.copy(alpha = 0.7f),
        cursorColor = Colores.TextoBoton
    )

    fun cargarDatosCliente(c: Cliente) {
        cliente = c
        rfc = c.rfc
        razonSocial = c.razonSocial.ifBlank { c.nombre }
        codigoPostal = c.codigoPostal
        regimenClave = c.regimenFiscalClave
        regimenDesc = c.regimenFiscalDesc
        usoCfdiClave = c.usoCfdiClave
        usoCfdiDesc = c.usoCfdiDesc
        correo = c.correo
        direccion = c.direccion

        scope.launch {
            autos = AutoRepository.obtenerAutosPorCliente(c.nombre.uppercase())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 42.dp,
                bottom = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // TÍTULO EN 2 LÍNEAS
        Text(
            text = "MIS DATOS DE FACTURACIÓN\nY MIS AUTOS REGISTRADOS",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Consulte y actualice sus datos fiscales y vehículos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // CAMPO TELÉFONO
        OutlinedTextField(
            value = telefono,
            onValueChange = {
                telefono = it
                mensaje = ""
                cliente = null
                autos = emptyList()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            textStyle = estiloCampo,
            label = {
                Text(
                    text = "NÚMERO DE TELÉFONO",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo
                )
            },
            placeholder = {
                Text(
                    text = "Ingrese su teléfono para buscar",
                    fontSize = 16.sp,
                    color = Colores.EtiquetaCampo.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = coloresCampo,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // BUSCAR MIS AUTOS (COLOR CAFÉ)
        BotonModulo3D(
            texto = if (buscando) "BUSCANDO..." else "BUSCAR MIS AUTOS",
            icono = "🔍",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            colorTexto = Color.Black,
            onClick = {
                teclado?.hide()

                if (telefono.isBlank()) {
                    mensaje = "INGRESE SU NÚMERO DE TELÉFONO"
                } else {
                    buscando = true
                    mensaje = ""
                    cliente = null
                    autos = emptyList()

                    scope.launch {
                        val resultado = ClienteRepository.obtenerClientePorTelefono(telefono.trim())
                        if (resultado == null) {
                            mensaje = "NO SE ENCONTRÓ UN CLIENTE CON ESE TELÉFONO"
                        } else {
                            cargarDatosCliente(resultado)
                        }
                        buscando = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(22.dp))

        // MENSAJE DE ERROR
        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = Colores.TextoTarjeta,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // DATOS DE FACTURACIÓN Y AUTOS
        cliente?.let { clienteActual ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Colores.FondoTarjeta
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DATOS DE FACTURACIÓN SAT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )

                    // RFC
                    OutlinedTextField(
                        value = rfc,
                        onValueChange = { if (it.length <= 13) rfc = it.uppercase() },
                        enabled = editando,
                        label = { Text("RFC (12-13 caracteres)") },
                        textStyle = estiloCampo,
                        colors = coloresCampo,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // RAZÓN SOCIAL / NOMBRE SAT
                    OutlinedTextField(
                        value = razonSocial,
                        onValueChange = { razonSocial = it.uppercase() },
                        enabled = editando,
                        label = { Text("RAZÓN SOCIAL (NOMBRE SAT)") },
                        textStyle = estiloCampo,
                        colors = coloresCampo,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // CÓDIGO POSTAL
                    OutlinedTextField(
                        value = codigoPostal,
                        onValueChange = { if (it.length <= 5 && it.all { char -> char.isDigit() }) codigoPostal = it },
                        enabled = editando,
                        label = { Text("CÓDIGO POSTAL (5 DÍGITOS)") },
                        textStyle = estiloCampo,
                        colors = coloresCampo,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // RÉGIMEN FISCAL CLAVE
                    OutlinedTextField(
                        value = regimenClave,
                        onValueChange = { if (it.length <= 3) regimenClave = it },
                        enabled = editando,
                        label = { Text("RÉGIMEN FISCAL CLAVE (Ej: 601, 612, 626)") },
                        textStyle = estiloCampo,
                        colors = coloresCampo,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // RÉGIMEN FISCAL DESCRIPCIÓN
                    OutlinedTextField(
                        value = regimenDesc,
                        onValueChange = { regimenDesc = it },
                        enabled = editando,
                        label = { Text("RÉGIMEN FISCAL DESCRIPCIÓN") },
                        textStyle = estiloCampo,
                        colors = coloresCampo,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // USO CFDI CLAVE
                    OutlinedTextField(
                        value = usoCfdiClave,
                        onValueChange = { if (it.length <= 3) usoCfdiClave = it.uppercase() },
                        enabled = editando,
                        label = { Text("USO CFDI CLAVE (Ej: G03, P01, S01)") },
                        textStyle = estiloCampo,
                        colors = coloresCampo,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // USO CFDI DESCRIPCIÓN
                    OutlinedTextField(
                        value = usoCfdiDesc,
                        onValueChange = { usoCfdiDesc = it },
                        enabled = editando,
                        label = { Text("USO CFDI DESCRIPCIÓN") },
                        textStyle = estiloCampo,
                        colors = coloresCampo,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // CORREO ELECTRÓNICO DE FACTURACIÓN
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it.lowercase() },
                        enabled = editando,
                        label = { Text("CORREO ELECTRÓNICO (ENVÍO XML/PDF)") },
                        textStyle = estiloCampo,
                        colors = coloresCampo,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // DIRECCIÓN FISCAL
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it.uppercase() },
                        enabled = editando,
                        label = { Text("DIRECCIÓN FISCAL") },
                        textStyle = estiloCampo,
                        colors = coloresCampo,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // BOTONES EDITAR Y GUARDAR (CAFÉ)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!editando) {
                            BotonModulo3D(
                                texto = "EDITAR DATOS",
                                icono = "✏️",
                                colorClaro = Color(0xFFD7B899),
                                colorMedio = Color(0xFF9B6B43),
                                colorOscuro = Color(0xFF5D3A1A),
                                colorTexto = Color.Black,
                                onClick = { editando = true },
                                modifier = Modifier.weight(1f).height(50.dp)
                            )
                        } else {
                            BotonModulo3D(
                                texto = "GUARDAR DATOS",
                                icono = "💾",
                                colorClaro = Color(0xFFD7B899),
                                colorMedio = Color(0xFF9B6B43),
                                colorOscuro = Color(0xFF5D3A1A),
                                colorTexto = Color.Black,
                                onClick = {
                                    val clienteActualizado = clienteActual.copy(
                                        rfc = rfc.trim().uppercase(),
                                        razonSocial = razonSocial.trim().uppercase(),
                                        codigoPostal = codigoPostal.trim(),
                                        regimenFiscalClave = regimenClave.trim(),
                                        regimenFiscalDesc = regimenDesc.trim(),
                                        usoCfdiClave = usoCfdiClave.trim().uppercase(),
                                        usoCfdiDesc = usoCfdiDesc.trim(),
                                        correo = correo.trim().lowercase(),
                                        direccion = direccion.trim().uppercase()
                                    )
                                    scope.launch {
                                        ClienteRepository.actualizarCliente(clienteActualizado)
                                        cliente = clienteActualizado
                                        editando = false
                                        mensaje = "DATOS ACTUALIZADOS CORRECTAMENTE"
                                    }
                                },
                                modifier = Modifier.weight(1f).height(50.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TARJETA AUTOS REGISTRADOS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Colores.FondoTarjeta
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AUTOS REGISTRADOS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )

                    if (autos.isEmpty()) {
                        Text(
                            text = "NO TIENE AUTOS REGISTRADOS",
                            fontSize = 16.sp,
                            color = Colores.EtiquetaCampo,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        autos.forEach { auto ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🚗 ${auto.marca} ${auto.modelo} (${auto.anio})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Colores.TextoTarjeta
                                )
                                Text(
                                    text = "PLACA: ${auto.placa}  |  VIN: ${auto.vin.ifBlank { "N/A" }}",
                                    fontSize = 15.sp,
                                    color = Colores.EtiquetaCampo,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // REGRESAR (GRIS)
        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            colorTexto = Color.White,
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
