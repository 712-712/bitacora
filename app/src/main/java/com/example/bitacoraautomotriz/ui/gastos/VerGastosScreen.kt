package com.example.bitacoraautomotriz.ui.gastos

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.R
import com.example.bitacoraautomotriz.data.Gasto
import com.example.bitacoraautomotriz.repository.GastoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun abrirDatePickerGasto(context: Context, fechaActual: String, onFechaSeleccionada: (String) -> Unit) {
    val cal = Calendar.getInstance()
    try {
        if (fechaActual.isNotBlank()) {
            val parsed = SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(fechaActual)
            if (parsed != null) cal.time = parsed
        }
    } catch (_: Exception) { }

    DatePickerDialog(
        context,
        R.style.CalendarioVerdeTheme,
        { _, y, m, d ->
            cal.set(y, m, d)
            val fechaFormateada = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(cal.time)
            onFechaSeleccionada(fechaFormateada)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
fun VerGastosScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var gastos by remember { mutableStateOf<List<Gasto>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = true) }

    var editandoId by remember { mutableStateOf<Int?>(value = null) }
    var editConcepto by remember { mutableStateOf(value = "") }
    var editCategoria by remember { mutableStateOf(value = "") }
    var editFecha by remember { mutableStateOf(value = "") }
    var editPrecio by remember { mutableStateOf(value = "") }
    var editDescripcion by remember { mutableStateOf(value = "") }

    LaunchedEffect(Unit) {
        try {
            gastos = GastoRepository.obtenerGastos(context)
        } catch (_: Exception) {
            gastos = emptyList()
        } finally {
            cargando = false
        }
    }

    val totalGeneralGastos = gastos.sumOf { if (it.monto.isNaN() || it.monto < 0) 0.0 else it.monto }

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
                text = "GASTOS REGISTRADOS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(16.dp))

            // TARJETA TOTAL GENERAL DE GASTOS POSICIONADA AL PRINCIPIO (MANTENIENDO ROJO GENERAL)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB51F1F)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOTAL GENERAL DE GASTOS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format(Locale.US, "$ %,.2f", totalGeneralGastos),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (gastos.isEmpty()) {
                Text(
                    text = "NO HAY GASTOS REGISTRADOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                gastos.forEach { gasto ->
                    val esEditando = editandoId == gasto.id

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
                            // ENCABEZADO DE TARJETA: FOLIO EN MENTA BRILLANTE Y NOMBRE EN BLANCO
                            Text(
                                text = "FOLIO GASTO: #${String.format(Locale.US, "%04d", gasto.id)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7DFFB2)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (esEditando) "EDITANDO GASTO" else gasto.concepto.orEmpty().uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFF004D33))

                            if (esEditando) {
                                // FORMULARIO DE EDICIÓN DENTRO DE LA TARJETA
                                Text(text = "CONCEPTO", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                                OutlinedTextField(
                                    value = editConcepto,
                                    onValueChange = { editConcepto = it.uppercase() },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Colores.FondoSecundario,
                                        unfocusedContainerColor = Colores.FondoSecundario,
                                        disabledContainerColor = Colores.FondoSecundario,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(text = "CATEGORÍA", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                                OutlinedTextField(
                                    value = editCategoria,
                                    onValueChange = { editCategoria = it.uppercase() },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Colores.FondoSecundario,
                                        unfocusedContainerColor = Colores.FondoSecundario,
                                        disabledContainerColor = Colores.FondoSecundario,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(text = "FECHA", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                                OutlinedTextField(
                                    value = editFecha,
                                    onValueChange = { editFecha = it },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            abrirDatePickerGasto(context, editFecha) { editFecha = it }
                                        }) {
                                            Icon(Icons.Default.DateRange, contentDescription = "Calendario", tint = Color.White)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Colores.FondoSecundario,
                                        unfocusedContainerColor = Colores.FondoSecundario,
                                        disabledContainerColor = Colores.FondoSecundario,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(text = "PRECIO / BASE ($)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                                OutlinedTextField(
                                    value = editPrecio,
                                    onValueChange = { editPrecio = it.replace(",", ".") },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    prefix = { Text("$ ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Colores.FondoSecundario,
                                        unfocusedContainerColor = Colores.FondoSecundario,
                                        disabledContainerColor = Colores.FondoSecundario,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                val pVal = editPrecio.replace(",", ".").toDoubleOrNull() ?: 0.0
                                val ivaVal = pVal * 0.16
                                val totalVal = pVal + ivaVal

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Colores.FondoSecundario)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = String.format(Locale.US, "I.V.A. (16%%): $ %,.2f", ivaVal), fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(text = String.format(Locale.US, "TOTAL: $ %,.2f", totalVal), fontSize = 18.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text(text = "DESCRIPCIÓN", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                                OutlinedTextField(
                                    value = editDescripcion,
                                    onValueChange = { editDescripcion = it.uppercase() },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Colores.FondoSecundario,
                                        unfocusedContainerColor = Colores.FondoSecundario,
                                        disabledContainerColor = Colores.FondoSecundario,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BotonModulo3D(
                                        texto = "GUARDAR",
                                        colorClaro = Color(0xFFFF9999),
                                        colorMedio = Color(0xFFFF4141),
                                        colorOscuro = Color(0xFFB51F1F),
                                        onClick = {
                                            val p = editPrecio.replace(",", ".").toDoubleOrNull() ?: 0.0
                                            val tot = p * 1.16
                                            val gastoActualizado = gasto.copy(
                                                concepto = editConcepto.uppercase(),
                                                categoria = editCategoria.uppercase(),
                                                fecha = editFecha,
                                                monto = tot,
                                                descripcion = editDescripcion.uppercase()
                                            )
                                            scope.launch {
                                                GastoRepository.actualizarGasto(gastoActualizado, context)
                                                gastos = GastoRepository.obtenerGastos(context)
                                                editandoId = null
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        tamanioTexto = 14,
                                        colorTexto = Color.Black
                                    )

                                    BotonModulo3D(
                                        texto = "CANCELAR",
                                        colorClaro = Colores.RegresarClaro,
                                        colorMedio = Colores.RegresarMedio,
                                        colorOscuro = Colores.RegresarOscuro,
                                        onClick = { editandoId = null },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        tamanioTexto = 14,
                                        colorTexto = Color.White
                                    )
                                }

                            } else {
                                // MODO LECTURA NORMAL (ETIQUETAS EN NEGRO Y VALORES EN BLANCO)
                                val total = if (gasto.monto.isNaN() || gasto.monto < 0) 0.0 else gasto.monto
                                val precioBase = total / 1.16
                                val iva = total - precioBase

                                Text(text = "CATEGORÍA DEL GASTO:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = gasto.categoria.orEmpty().uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

                                Text(text = "FECHA DE REGISTRO:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = gasto.fecha.orEmpty(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

                                if (gasto.descripcion.isNotBlank()) {
                                    Text(text = "DESCRIPCIÓN DEL GASTO:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = gasto.descripcion.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "PRECIO / BASE:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = String.format(Locale.US, "$ %,.2f", precioBase), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "I.V.A. (16%):", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = String.format(Locale.US, "$ %,.2f", iva), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "TOTAL DEL GASTO:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = String.format(Locale.US, "$ %,.2f", total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                BotonModulo3D(
                                    texto = "EDITAR GASTO",
                                    colorClaro = Color(0xFFFF9999),
                                    colorMedio = Color(0xFFFF4141),
                                    colorOscuro = Color(0xFFB51F1F),
                                    onClick = {
                                        editandoId = gasto.id
                                        editConcepto = gasto.concepto.uppercase()
                                        editCategoria = gasto.categoria.uppercase()
                                        editFecha = gasto.fecha
                                        editPrecio = String.format(Locale.US, "%.2f", gasto.monto / 1.16)
                                        editDescripcion = gasto.descripcion.uppercase()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    tamanioTexto = 14,
                                    colorTexto = Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

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
