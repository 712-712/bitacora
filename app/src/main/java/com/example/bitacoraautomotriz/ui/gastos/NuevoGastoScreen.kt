package com.example.bitacoraautomotriz.ui.gastos

import android.app.DatePickerDialog
import android.content.Context
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.R
import com.example.bitacoraautomotriz.data.Gasto
import com.example.bitacoraautomotriz.repository.GastoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun NuevoGastoScreen(
    onGuardar: () -> Unit,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val formatoFecha = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }
    val fechaHoy = remember { formatoFecha.format(Date()) }

    var concepto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(fechaHoy) }
    var precioText by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    val conceptoFocus = remember { FocusRequester() }
    val categoriaFocus = remember { FocusRequester() }
    val precioFocus = remember { FocusRequester() }
    val descripcionFocus = remember { FocusRequester() }

    val bivConcepto = remember { BringIntoViewRequester() }
    val bivCategoria = remember { BringIntoViewRequester() }
    val bivPrecio = remember { BringIntoViewRequester() }
    val bivDescripcion = remember { BringIntoViewRequester() }

    val precioDouble = precioText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val iva = precioDouble * 0.16
    val total = precioDouble + iva

    val coloresCamposTexto = TextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        disabledContainerColor = Colores.FondoSecundario,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedIndicatorColor = Colores.BordeBoton,
        unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
        cursorColor = Color.White,
        selectionColors = TextSelectionColors(
            handleColor = Color.White,
            backgroundColor = Color(0xFFFF4141).copy(alpha = 0.4f)
        )
    )

    fun guardarGasto() {
        if (concepto.isBlank()) {
            mensaje = "INGRESE EL CONCEPTO DEL GASTO"
            conceptoFocus.requestFocus()
            return
        }
        if (precioText.isBlank() || precioDouble <= 0.0) {
            mensaje = "INGRESE UN PRECIO VÁLIDO MAYOR A CERO"
            precioFocus.requestFocus()
            return
        }

        guardando = true
        mensaje = ""

        val gasto = Gasto(
            concepto = concepto.trim().uppercase(),
            categoria = categoria.trim().uppercase(),
            fecha = fecha.trim(),
            monto = total,
            descripcion = descripcion.trim().uppercase()
        )

        scope.launch {
            try {
                GastoRepository.guardarGasto(gasto, context)
                withContext(Dispatchers.Main) {
                    guardando = false
                    Toast.makeText(context, "✅ Gasto registrado exitosamente", Toast.LENGTH_SHORT).show()
                    onGuardar()
                }
            } catch (e: Exception) {
                guardando = false
                mensaje = "ERROR AL GUARDAR: ${e.message}"
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
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "REGISTRAR NUEVO GASTO",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(24.dp))

            // CONCEPTO DEL GASTO
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(text = "CONCEPTO DEL GASTO", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = concepto,
                onValueChange = { concepto = it.uppercase(); mensaje = "" },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { categoriaFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .focusRequester(conceptoFocus)
                    .bringIntoViewRequester(bivConcepto)
                    .onFocusEvent { if (it.isFocused) scope.launch { bivConcepto.bringIntoView() } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // CATEGORÍA
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(text = "CATEGORÍA", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = categoria,
                onValueChange = { categoria = it.uppercase(); mensaje = "" },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { precioFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .focusRequester(categoriaFocus)
                    .bringIntoViewRequester(bivCategoria)
                    .onFocusEvent { if (it.isFocused) scope.launch { bivCategoria.bringIntoView() } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // FECHA CON CALENDARIO
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(text = "FECHA DE GASTO", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                readOnly = true,
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                trailingIcon = {
                    IconButton(onClick = {
                        abrirDatePickerGasto(context, fecha) {
                            fecha = it
                            scope.launch {
                                precioFocus.requestFocus()
                                keyboardController?.show()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar Fecha",
                            tint = Color.White
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier.fillMaxWidth().height(70.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PRECIO ($)
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(text = "PRECIO ($)", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = precioText,
                onValueChange = { input ->
                    val normalizado = input.replace(",", ".")
                    if (normalizado.isEmpty() || normalizado.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        precioText = normalizado
                        mensaje = ""
                    }
                },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                prefix = { Text(text = "$ ", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { descripcionFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .focusRequester(precioFocus)
                    .bringIntoViewRequester(bivPrecio)
                    .onFocusEvent { if (it.isFocused) scope.launch { bivPrecio.bringIntoView() } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DESCRIPCIÓN
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(text = "DESCRIPCIÓN", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it.uppercase(); mensaje = "" },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .focusRequester(descripcionFocus)
                    .bringIntoViewRequester(bivDescripcion)
                    .onFocusEvent { if (it.isFocused) scope.launch { bivDescripcion.bringIntoView() } }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // RESUMEN / TOTALES
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "PRECIO / IMPORTE:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = String.format(Locale.US, "$ %,.2f", precioDouble), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "I.V.A. (16%):", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = String.format(Locale.US, "$ %,.2f", iva), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "TOTAL DEL GASTO:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = String.format(Locale.US, "$ %,.2f", total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                    }
                }
            }

            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(mensaje, color = Color(0xFFFF5252), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOTÓN GUARDAR GASTO
            BotonModulo3D(
                texto = if (guardando) "GUARDANDO..." else "GUARDAR GASTO",
                icono = "💾",
                colorClaro = Color(0xFFFF9999),
                colorMedio = Color(0xFFFF4141),
                colorOscuro = Color(0xFFB51F1F),
                onClick = { if (!guardando) guardarGasto() },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                tamanioTexto = 16,
                colorTexto = Color.Black
            )

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
