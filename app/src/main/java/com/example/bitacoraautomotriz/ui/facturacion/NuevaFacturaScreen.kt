package com.example.bitacoraautomotriz.ui.facturacion

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.example.bitacoraautomotriz.R
import com.example.bitacoraautomotriz.data.Factura
import com.example.bitacoraautomotriz.repository.FacturaRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun NuevaFacturaScreen(
    onGuardar: () -> Unit,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val subtotalFocusRequester = remember { FocusRequester() }

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val fechaActual = remember { formatoFecha.format(Calendar.getInstance().time) }

    var numero by remember { mutableStateOf("") }
    var cliente by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(fechaActual) }
    var subtotalText by remember { mutableStateOf("") }

    var mensajeError by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    // GENERAR FOLIO AUTOMÁTICO AL CARGAR
    LaunchedEffect(Unit) {
        try {
            val facturasExistentes = FacturaRepository.obtenerFacturas(context)
            val siguienteFolio = 1001 + facturasExistentes.size
            if (numero.isBlank()) {
                numero = "FAC-$siguienteFolio"
            }
        } catch (_: Exception) {
            if (numero.isBlank()) {
                numero = "FAC-1001"
            }
        }
    }

    // CÁLCULOS AUTOMÁTICOS
    val subtotalNumero = subtotalText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val ivaNumero = subtotalNumero * 0.16
    val totalNumero = subtotalNumero + ivaNumero

    val estiloTextoCampo = TextStyle(
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )

    val coloresCampoTexto = OutlinedTextFieldDefaults.colors(
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

    fun mostrarSelectorFecha() {
        val calendario = Calendar.getInstance()
        try {
            if (fecha.isNotBlank()) {
                val fechaConvertida = formatoFecha.parse(fecha)
                if (fechaConvertida != null) calendario.time = fechaConvertida
            }
        } catch (_: Exception) { }

        DatePickerDialog(
            context,
            R.style.CalendarioVerdeTheme,
            { _, year, month, dayOfMonth ->
                fecha = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year)
                // AL SELECCIONAR LA FECHA, SALTA AUTOMÁTICAMENTE AL CAMPO SUBTOTAL
                scope.launch {
                    subtotalFocusRequester.requestFocus()
                    keyboardController?.show()
                }
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun guardar() {
        if (numero.isBlank()) {
            mensajeError = "INGRESE UN NÚMERO O FOLIO DE FACTURA"
            return
        }
        if (cliente.isBlank()) {
            mensajeError = "INGRESE EL NOMBRE DEL CLIENTE"
            return
        }
        if (subtotalText.isBlank() || subtotalNumero <= 0) {
            mensajeError = "INGRESE UN SUBTOTAL VÁLIDO MAYOR A CERO"
            return
        }

        guardando = true
        mensajeError = ""

        val factura = Factura(
            numero = numero.trim().uppercase(),
            cliente = cliente.trim().uppercase(),
            fecha = fecha.trim(),
            subtotal = subtotalNumero,
            iva = ivaNumero,
            total = totalNumero
        )

        scope.launch {
            try {
                FacturaRepository.guardarFactura(factura, context)
                guardando = false
                Toast.makeText(context, "✅ Factura generada exitosamente", Toast.LENGTH_SHORT).show()
                onGuardar()
            } catch (e: Exception) {
                guardando = false
                mensajeError = "ERROR AL GUARDAR FACTURA: ${e.message}"
            }
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
            // TÍTULO DE LA PANTALLA
            Text(
                text = "NUEVA FACTURA",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 1. NÚMERO / FOLIO DE FACTURA
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FOLIO / NÚMERO DE FACTURA:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = numero,
                    onValueChange = { numero = it.uppercase() },
                    textStyle = estiloTextoCampo,
                    singleLine = true,
                    colors = coloresCampoTexto,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. CLIENTE
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CLIENTE / RAZÓN SOCIAL:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = cliente,
                    onValueChange = { cliente = it.uppercase() },
                    placeholder = { Text("Escriba el nombre o razón social...", color = Color.Gray) },
                    textStyle = estiloTextoCampo,
                    singleLine = true,
                    colors = coloresCampoTexto,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { mostrarSelectorFecha() }),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. FECHA DE FACTURACIÓN (CON CALENDARIO Y SALTO AL SUBTOTAL)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "FECHA DE FACTURACIÓN:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    readOnly = true,
                    textStyle = estiloTextoCampo,
                    singleLine = true,
                    colors = coloresCampoTexto,
                    trailingIcon = {
                        IconButton(onClick = { mostrarSelectorFecha() }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleccionar fecha",
                                tint = Color(0xFF00DDEB)
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. SUBTOTAL (FOCUS AUTOMÁTICO TRAS ELEGIR FECHA)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SUBTOTAL ($):",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = subtotalText,
                    onValueChange = { input ->
                        val normalizado = input.replace(",", ".")
                        if (normalizado.isEmpty() || normalizado.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            subtotalText = normalizado
                        }
                    },
                    placeholder = { Text("0.00", color = Color.Gray) },
                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    prefix = { Text("$ ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    colors = coloresCampoTexto,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(subtotalFocusRequester)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // TARJETA DE RESUMEN: SUBTOTAL, I.V.A. Y TOTAL CÁLCULO AUTOMÁTICO (DÍGITOS DE PRECIO EN ROJO)
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "SUBTOTAL:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(
                            text = String.format(Locale.US, "$ %,.2f", subtotalNumero),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "I.V.A. (16%):", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(
                            text = String.format(Locale.US, "$ %,.2f", ivaNumero),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF004D33))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "TOTAL FACTURA:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(
                            text = String.format(Locale.US, "$ %,.2f", totalNumero),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (mensajeError.isNotBlank()) {
                Text(
                    text = mensajeError,
                    color = Color(0xFFFF5252),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // BOTÓN GUARDAR (CIAN FACTURACIÓN)
            BotonModulo3D(
                texto = if (guardando) "GUARDANDO..." else "GUARDAR",
                icono = "💾",
                colorClaro = Color(0xFF8FFFFF),
                colorMedio = Color(0xFF00DDEB),
                colorOscuro = Color(0xFF007F88),
                colorTexto = Color.Black,
                onClick = { if (!guardando) guardar() },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                tamanioTexto = 16
            )

            Spacer(modifier = Modifier.height(16.dp))

            // BOTÓN REGRESAR (GRIS)
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

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
