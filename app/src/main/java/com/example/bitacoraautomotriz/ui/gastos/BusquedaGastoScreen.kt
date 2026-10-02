package com.example.bitacoraautomotriz.ui.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Gasto
import com.example.bitacoraautomotriz.repository.GastoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun BusquedaGastoScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var textoBusqueda by remember { mutableStateOf(value = "") }
    var todosLosGastos by remember { mutableStateOf<List<Gasto>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = true) }

    var editandoId by remember { mutableStateOf<Int?>(value = null) }
    var editConcepto by remember { mutableStateOf(value = "") }
    var editCategoria by remember { mutableStateOf(value = "") }
    var editFecha by remember { mutableStateOf(value = "") }
    var editPrecio by remember { mutableStateOf(value = "") }
    var editDescripcion by remember { mutableStateOf(value = "") }

    val coloresCampoTexto = TextFieldDefaults.colors(
        focusedContainerColor = Colores.FondoSecundario,
        unfocusedContainerColor = Colores.FondoSecundario,
        disabledContainerColor = Colores.FondoSecundario,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        disabledTextColor = Color.White,
        focusedIndicatorColor = Colores.BordeBoton,
        unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
        cursorColor = Color.White
    )

    LaunchedEffect(Unit) {
        try {
            todosLosGastos = GastoRepository.obtenerGastos(context)
        } catch (_: Exception) {
            todosLosGastos = emptyList()
        } finally {
            cargando = false
        }
    }

    // BÚSQUEDA POR CONCEPTO, CATEGORÍA, FECHA Y DESCRIPCIÓN
    val resultados = if (textoBusqueda.isBlank()) {
        todosLosGastos
    } else {
        val query = textoBusqueda.trim()
        todosLosGastos.filter { gasto ->
            gasto.concepto.contains(query, ignoreCase = true) ||
                    gasto.categoria.contains(query, ignoreCase = true) ||
                    gasto.fecha.contains(query, ignoreCase = true) ||
                    gasto.descripcion.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "BUSCAR GASTO",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "CONCEPTO, CATEGORÍA, FECHA O DESCRIPCIÓN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.EtiquetaCampo
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
            placeholder = { Text("Escriba para buscar...", fontSize = 18.sp, color = Colores.EtiquetaCampo.copy(alpha = 0.6f)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() },
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = coloresCampoTexto,
            modifier = Modifier.fillMaxWidth().height(70.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (resultados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (todosLosGastos.isEmpty())
                        "NO HAY GASTOS REGISTRADOS"
                    else
                        "NO SE ENCONTRARON COINCIDENCIAS",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resultados) { gasto ->
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
                            // ENCABEZADO DE TARJETA: NOMBRE Y FOLIO
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (esEditando) "EDITANDO GASTO" else gasto.concepto.uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "FOLIO: #${String.format(Locale.US, "%04d", gasto.id)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }

                            HorizontalDivider(color = Color(0xFF004D33))

                            if (esEditando) {
                                Text(text = "CONCEPTO", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                                OutlinedTextField(
                                    value = editConcepto,
                                    onValueChange = { editConcepto = it },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = coloresCampoTexto,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(text = "CATEGORÍA", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                                OutlinedTextField(
                                    value = editCategoria,
                                    onValueChange = { editCategoria = it },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = coloresCampoTexto,
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
                                    colors = coloresCampoTexto,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(text = "PRECIO / BASE ($)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Colores.EtiquetaCampo)
                                OutlinedTextField(
                                    value = editPrecio,
                                    onValueChange = { editPrecio = it },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    prefix = { Text("$ ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = coloresCampoTexto,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                val pVal = editPrecio.toDoubleOrNull() ?: 0.0
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
                                    onValueChange = { editDescripcion = it },
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = coloresCampoTexto,
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
                                            val p = editPrecio.toDoubleOrNull() ?: 0.0
                                            val tot = p * 1.16
                                            val gastoActualizado = gasto.copy(
                                                concepto = editConcepto,
                                                categoria = editCategoria,
                                                fecha = editFecha,
                                                monto = tot,
                                                descripcion = editDescripcion
                                            )
                                            scope.launch {
                                                GastoRepository.actualizarGasto(gastoActualizado, context)
                                                todosLosGastos = GastoRepository.obtenerGastos(context)
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
                                // MODO LECTURA NORMAL
                                val total = if (gasto.monto.isNaN() || gasto.monto < 0) 0.0 else gasto.monto
                                val precioBase = total / 1.16
                                val iva = total - precioBase

                                Text(
                                    text = "CATEGORÍA: ${gasto.categoria.uppercase()}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "FECHA: ${gasto.fecha}",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                if (gasto.descripcion.isNotBlank()) {
                                    Text(
                                        text = "DESCRIPCIÓN: ${gasto.descripcion}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "PRECIO / BASE:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = String.format(Locale.US, "$ %,.2f", precioBase), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "I.V.A. (16%):", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
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
                                        editConcepto = gasto.concepto
                                        editCategoria = gasto.categoria
                                        editFecha = gasto.fecha
                                        editPrecio = String.format(Locale.US, "%.2f", gasto.monto / 1.16)
                                        editDescripcion = gasto.descripcion
                                    },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    tamanioTexto = 14,
                                    colorTexto = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth().height(58.dp),
            tamanioTexto = 16,
            colorTexto = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
