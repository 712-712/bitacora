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
import androidx.compose.material.icons.filled.Email
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun MisDatosFacturacionScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val prefs = remember { context.getSharedPreferences("datos_facturacion_prefs", Context.MODE_PRIVATE) }

    // Inicia en modo lectura/deshabilitado hasta presionar "EDITAR"
    var editando by remember { mutableStateOf(value = false) }

    // SECCIÓN 1: DATOS FISCALES (SAT)
    var rfc by remember { mutableStateOf(value = prefs.getString("rfc", "") ?: "") }
    var razonSocial by remember { mutableStateOf(value = prefs.getString("razonSocial", "") ?: "") }
    var direccionFiscal by remember { mutableStateOf(value = prefs.getString("direccionFiscal", "") ?: "") }
    var codigoPostalFiscal by remember { mutableStateOf(value = prefs.getString("codigoPostalFiscal", "") ?: "") }
    var regimenFiscal by remember { mutableStateOf(value = prefs.getString("regimenFiscal", "") ?: "") }
    var usoCfdi by remember { mutableStateOf(value = prefs.getString("usoCfdi", "") ?: "") }

    // SECCIÓN 2: DATOS COMPLEMENTARIOS (OPERATIVOS)
    var datoOperativo by remember { mutableStateOf(value = prefs.getString("datoOperativo", "") ?: "") }
    var formaPago by remember { mutableStateOf(value = prefs.getString("formaPago", "") ?: "") }
    var metodoPago by remember { mutableStateOf(value = prefs.getString("metodoPago", "") ?: "") }
    var correoElectronico by remember { mutableStateOf(value = prefs.getString("correoElectronico", "") ?: "") }

    val estiloCampo = TextStyle(
        color = Colores.TextoTarjeta,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
    )

    // ESTILO DE CORREO ELECTRÓNICO EN COLOR AZUL Y SUBRAYADO COMO ENLACE
    val estiloCampoCorreoAzul = TextStyle(
        color = Color(0xFF00B0FF),
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        textDecoration = TextDecoration.Underline
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
        cursorColor = Color.White
    )

    fun guardarDatos() {
        prefs.edit().apply {
            putString("rfc", rfc.trim().uppercase())
            putString("razonSocial", razonSocial.trim().uppercase())
            putString("direccionFiscal", direccionFiscal.trim().uppercase())
            putString("codigoPostalFiscal", codigoPostalFiscal.trim())
            putString("regimenFiscal", regimenFiscal.trim())
            putString("usoCfdi", usoCfdi.trim())
            putString("datoOperativo", datoOperativo.trim())
            putString("formaPago", formaPago.trim())
            putString("metodoPago", metodoPago.trim())
            putString("correoElectronico", correoElectronico.trim().lowercase())
            apply()
        }
        editando = false
        Toast.makeText(context, "✅ Datos de facturación guardados exitosamente", Toast.LENGTH_SHORT).show()
    }

    fun abrirAppCorreo() {
        if (correoElectronico.isNotBlank()) {
            val email = correoElectronico.trim().lowercase()
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "Datos de Facturación - Taller Bitácora Automotriz")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "DATOS DE FACTURACIÓN SAT:\n\n" +
                            "RFC: $rfc\n" +
                            "Razón Social: $razonSocial\n" +
                            "Dirección Fiscal: $direccionFiscal\n" +
                            "Código Postal: $codigoPostalFiscal\n" +
                            "Régimen Fiscal: $regimenFiscal\n" +
                            "Uso CFDI: $usoCfdi\n\n" +
                            "DATOS OPERATIVOS:\n" +
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 24.dp,
                bottom = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // TÍTULO
        Text(
            text = "MIS DATOS DE FACTURACIÓN",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Complete el formulario con sus datos fiscales para el SAT",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // TARJETA 1: DATOS FISCALES
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "DATOS FISCALES (SAT)",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TextoTarjeta
                )

                // 1. RFC
                Column {
                    Text(
                        text = "RFC :",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = rfc,
                        onValueChange = { if (it.length <= 13) rfc = it.uppercase() },
                        enabled = editando,
                        placeholder = { Text("Ej: ABC123456XYZ") },
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
                    Text(
                        text = "Nombre o Razón Social :",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = razonSocial,
                        onValueChange = { razonSocial = it.uppercase() },
                        enabled = editando,
                        placeholder = { Text("Nombre o Razón Social exacta según SAT") },
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
                    Text(
                        text = "direccion fiscal (opcional ) :",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = direccionFiscal,
                        onValueChange = { direccionFiscal = it.uppercase() },
                        enabled = editando,
                        placeholder = { Text("Calle, Número, Colonia, Municipio") },
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
                    Text(
                        text = "Código Postal Fiscal :",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = codigoPostalFiscal,
                        onValueChange = { if (it.length <= 5 && it.all { c -> c.isDigit() }) codigoPostalFiscal = it },
                        enabled = editando,
                        placeholder = { Text("Ej: 64000") },
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
                    Text(
                        text = "Régimen Fiscal :",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = regimenFiscal,
                        onValueChange = { regimenFiscal = it },
                        enabled = editando,
                        placeholder = { Text("Ej: 601 - General de Ley Personas Morales") },
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
                    Text(
                        text = "Uso del CFDI :",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = usoCfdi,
                        onValueChange = { usoCfdi = it },
                        enabled = editando,
                        placeholder = { Text("Ej: G03 - Gastos en general") },
                        textStyle = estiloCampo,
                        colors = coloresCampo,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // TARJETA 2: DATOS COMPLEMENTARIOS (OPERATIVOS)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "DATOS COMPLEMENTARIOS (OPERATIVOS)",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TextoTarjeta
                )

                // 7. DATO OPERATIVO
                Column {
                    Text(
                        text = "Dato Operativo :",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = datoOperativo,
                        onValueChange = { datoOperativo = it },
                        enabled = editando,
                        placeholder = { Text("Dato operativo adicional") },
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
                    Text(
                        text = "Forma de Pago :",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = formaPago,
                        onValueChange = { formaPago = it },
                        enabled = editando,
                        placeholder = { Text("Ej: 01 - Efectivo, 03 - Transferencia") },
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
                    Text(
                        text = "Método de Pago :",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = metodoPago,
                        onValueChange = { metodoPago = it },
                        enabled = editando,
                        placeholder = { Text("Ej: PUE - Pago en una sola exhibición") },
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
                        color = Colores.EtiquetaCampo,
                        modifier = Modifier.padding(bottom = 4.dp)
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
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // BOTONES DE TAMAÑO MEDIANO (EDITAR EN AZUL, GUARDAR EN CAFÉ, ENVIAR EN VERDE)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    editando = true
                    Toast.makeText(context, "✏️ Edición habilitada", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                tamanioTexto = 13
            )

            // BOTÓN GUARDAR (CAFÉ)
            BotonModulo3D(
                texto = "GUARDAR",
                icono = "💾",
                colorClaro = Color(0xFFD7B899),
                colorMedio = Color(0xFF9B6B43),
                colorOscuro = Color(0xFF5D3A1A),
                colorTexto = Color.Black,
                onClick = {
                    guardarDatos()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                tamanioTexto = 13
            )

            // BOTÓN ENVIAR (VERDE)
            BotonModulo3D(
                texto = "ENVIAR",
                icono = "📧",
                colorClaro = Color(0xFFB9F6CA),
                colorMedio = Color(0xFF00C853),
                colorOscuro = Color(0xFF00695C),
                colorTexto = Color.Black,
                onClick = {
                    guardarDatos()
                    abrirAppCorreo()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                tamanioTexto = 13
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REGRESAR (GRIS)
        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            colorTexto = Color.White,
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            tamanioTexto = 15
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
