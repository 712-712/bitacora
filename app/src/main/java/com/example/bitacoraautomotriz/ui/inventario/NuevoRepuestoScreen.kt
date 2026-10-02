package com.example.bitacoraautomotriz.ui.inventario

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
import com.example.bitacoraautomotriz.data.Repuesto
import com.example.bitacoraautomotriz.repository.RepuestoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun NuevoRepuestoScreen(
    onGuardar: () -> Unit,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    var repuestosExistentes by remember { mutableStateOf<List<Repuesto>>(emptyList()) }
    var nombre by remember { mutableStateOf("") }
    var marca by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var precioText by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    val nombreFocus = remember { FocusRequester() }
    val marcaFocus = remember { FocusRequester() }
    val categoriaFocus = remember { FocusRequester() }
    val cantidadFocus = remember { FocusRequester() }
    val precioFocus = remember { FocusRequester() }

    val bivNombre = remember { BringIntoViewRequester() }
    val bivMarca = remember { BringIntoViewRequester() }
    val bivCategoria = remember { BringIntoViewRequester() }
    val bivCantidad = remember { BringIntoViewRequester() }
    val bivPrecio = remember { BringIntoViewRequester() }

    LaunchedEffect(Unit) {
        try {
            repuestosExistentes = RepuestoRepository.obtenerRepuestos(context)
        } catch (_: Exception) {
            repuestosExistentes = emptyList()
        }
    }

    val nombreLimpio = nombre.trim()
    val marcaLimpia = marca.trim()

    val repuestoDuplicado = remember(nombreLimpio, marcaLimpia, repuestosExistentes) {
        if (nombreLimpio.isBlank()) null
        else repuestosExistentes.find {
            it.nombre.trim().equals(nombreLimpio, ignoreCase = true) &&
                    (marcaLimpia.isBlank() || it.marca.trim().equals(marcaLimpia, ignoreCase = true))
        }
    }

    val precioDouble = precioText.replace(",", ".").toDoubleOrNull() ?: 0.0
    val cantidadInt = cantidad.toIntOrNull() ?: 0
    val importe = precioDouble * cantidadInt
    val iva = importe * 0.16
    val total = importe + iva

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
            backgroundColor = Color(0xFFD83CFF).copy(alpha = 0.4f)
        )
    )

    fun guardarRepuesto() {
        if (nombre.isBlank()) {
            mensaje = "INGRESE EL NOMBRE DEL REPUESTO"
            nombreFocus.requestFocus()
            return
        }
        if (cantidad.isBlank() || cantidadInt <= 0) {
            mensaje = "INGRESE UNA CANTIDAD VÁLIDA MAYOR A CERO"
            cantidadFocus.requestFocus()
            return
        }
        if (precioText.isBlank() || precioDouble <= 0.0) {
            mensaje = "INGRESE UN PRECIO VÁLIDO MAYOR A CERO"
            precioFocus.requestFocus()
            return
        }

        if (repuestoDuplicado != null) {
            mensaje = "ESTE REPUESTO YA EXISTE EN EL INVENTARIO"
            return
        }

        guardando = true
        mensaje = ""

        val repuesto = Repuesto(
            nombre = nombre.trim().uppercase(),
            marca = marca.trim().uppercase(),
            categoria = categoria.trim().uppercase(),
            cantidad = cantidadInt,
            precio = precioDouble
        )

        scope.launch {
            try {
                RepuestoRepository.guardarRepuesto(repuesto, context)
                withContext(Dispatchers.Main) {
                    guardando = false
                    Toast.makeText(context, "✅ Repuesto guardado exitosamente", Toast.LENGTH_SHORT).show()
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
            Text(
                text = "AGREGAR NUEVO REPUESTO",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ALERTA DE REPUESTO DUPLICADO
            if (repuestoDuplicado != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB51F1F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ REPUESTO YA REGISTRADO", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Este repuesto ya existe en el inventario:\n• Nombre: ${repuestoDuplicado.nombre}\n• Marca: ${repuestoDuplicado.marca}\n• Cantidad actual: ${repuestoDuplicado.cantidad}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // NOMBRE DEL REPUESTO
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(text = "NOMBRE DEL REPUESTO", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it.uppercase(); mensaje = "" },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { marcaFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .focusRequester(nombreFocus)
                    .bringIntoViewRequester(bivNombre)
                    .onFocusEvent { if (it.isFocused) scope.launch { bivNombre.bringIntoView() } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // MARCA
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(text = "MARCA", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = marca,
                onValueChange = { marca = it.uppercase(); mensaje = "" },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { categoriaFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .focusRequester(marcaFocus)
                    .bringIntoViewRequester(bivMarca)
                    .onFocusEvent { if (it.isFocused) scope.launch { bivMarca.bringIntoView() } }
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
                keyboardActions = KeyboardActions(onNext = { cantidadFocus.requestFocus() }),
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

            // CANTIDAD
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(text = "CANTIDAD", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = cantidad,
                onValueChange = { if (it.all { c -> c.isDigit() }) { cantidad = it; mensaje = "" } },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { precioFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .focusRequester(cantidadFocus)
                    .bringIntoViewRequester(bivCantidad)
                    .onFocusEvent { if (it.isFocused) scope.launch { bivCantidad.bringIntoView() } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PRECIO UNITARIO ($)
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(text = "PRECIO UNITARIO ($)", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                prefix = { Text(text = "$ ", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(12.dp),
                colors = coloresCamposTexto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .focusRequester(precioFocus)
                    .bringIntoViewRequester(bivPrecio)
                    .onFocusEvent { if (it.isFocused) scope.launch { bivPrecio.bringIntoView() } }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // RESUMEN
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(text = "Importe: $ " + String.format(Locale.US, "%,.2f", importe), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "I.V.A. (16%): $ " + String.format(Locale.US, "%,.2f", iva), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Total: $ " + String.format(Locale.US, "%,.2f", total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                }
            }

            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(mensaje, color = Color(0xFFFF5252), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(28.dp))

            BotonModulo3D(
                texto = if (guardando) "GUARDANDO..." else "GUARDAR REPUESTO",
                icono = "💾",
                colorClaro = Color(0xFFF3A7FF),
                colorMedio = Color(0xFFD83CFF),
                colorOscuro = Color(0xFF7B1599),
                onClick = { if (!guardando) guardarRepuesto() },
                modifier = Modifier.fillMaxWidth().height(58.dp),
                tamanioTexto = 16,
                colorTexto = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
