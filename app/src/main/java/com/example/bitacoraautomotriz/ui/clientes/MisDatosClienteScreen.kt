package com.example.bitacoraautomotriz.ui.clientes

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

@Composable
fun MisDatosClienteScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var autos by remember { mutableStateOf<List<Auto>>(emptyList()) }
    var autoEditandoId by remember { mutableStateOf<Int?>(null) }
    var cargando by remember { mutableStateOf(true) }

    // CAMPOS DEL FORMULARIO DE VEHÍCULO
    var marca by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf("") }
    var placa by remember { mutableStateOf("") }
    var colorAuto by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var kilometraje by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf("") }
    var guardando by remember { mutableStateOf(false) }

    val marcaFocus = remember { FocusRequester() }
    val modeloFocus = remember { FocusRequester() }
    val anioFocus = remember { FocusRequester() }
    val placaFocus = remember { FocusRequester() }
    val colorFocus = remember { FocusRequester() }
    val vinFocus = remember { FocusRequester() }
    val kmFocus = remember { FocusRequester() }

    fun cargarAutos() {
        scope.launch {
            try {
                autos = AutoRepository.obtenerAutos(context).sortedByDescending { it.id }
            } catch (_: Exception) {
                autos = emptyList()
            } finally {
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) {
        cargarAutos()
    }

    fun limpiarFormulario() {
        autoEditandoId = null
        marca = ""
        modelo = ""
        anio = ""
        placa = ""
        colorAuto = ""
        vin = ""
        kilometraje = ""
        mensaje = ""
    }

    fun prepararEdicion(auto: Auto) {
        autoEditandoId = auto.id
        marca = auto.marca
        modelo = auto.modelo
        anio = if (auto.anio > 0) auto.anio.toString() else ""
        placa = auto.placa
        colorAuto = auto.color
        vin = auto.vin
        kilometraje = if (auto.kilometraje > 0) auto.kilometraje.toString() else ""
        mensaje = ""

        Toast.makeText(context, "✏️ Editando: ${auto.marca} (${auto.placa})", Toast.LENGTH_SHORT).show()

        scope.launch {
            scrollState.animateScrollTo(0)
            marcaFocus.requestFocus()
        }
    }

    fun guardar() {
        if (marca.isBlank()) {
            mensaje = "INGRESE LA MARCA DEL VEHÍCULO"
            marcaFocus.requestFocus()
            return
        }
        if (modelo.isBlank()) {
            mensaje = "INGRESE EL MODELO DEL VEHÍCULO"
            modeloFocus.requestFocus()
            return
        }
        if (anio.isBlank()) {
            mensaje = "INGRESE EL AÑO DEL VEHÍCULO"
            anioFocus.requestFocus()
            return
        }
        val anioNum = anio.toIntOrNull()
        if (anioNum == null || anioNum < 1900 || anioNum > 2100) {
            mensaje = "INGRESE UN AÑO VÁLIDO (1900-2100)"
            anioFocus.requestFocus()
            return
        }
        if (placa.isBlank()) {
            mensaje = "INGRESE LA PLACA DEL VEHÍCULO"
            placaFocus.requestFocus()
            return
        }

        guardando = true
        mensaje = ""

        val idActual = autoEditandoId
        val autoGuardar = Auto(
            id = idActual ?: 0,
            cliente = "CLIENTE ÁREA MI AUTO",
            marca = marca.trim().uppercase(),
            modelo = modelo.trim().uppercase(),
            anio = anioNum,
            placa = placa.trim().uppercase(),
            color = colorAuto.trim().uppercase(),
            vin = vin.trim().uppercase(),
            kilometraje = kilometraje.toIntOrNull() ?: 0
        )

        scope.launch {
            try {
                if (idActual == null || idActual == 0) {
                    AutoRepository.guardarAuto(autoGuardar, context)
                    Toast.makeText(context, "✅ Vehículo agregado exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    AutoRepository.actualizarAuto(autoGuardar, context)
                    Toast.makeText(context, "✅ Vehículo actualizado exitosamente", Toast.LENGTH_SHORT).show()
                }
                limpiarFormulario()
                cargarAutos()
            } catch (e: Exception) {
                mensaje = "ERROR AL GUARDAR: ${e.message}"
            } finally {
                guardando = false
            }
        }
    }

    val estiloCampo = TextStyle(
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
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
        focusedLabelColor = Colores.TituloPrincipal,
        unfocusedLabelColor = Colores.EtiquetaCampo,
        cursorColor = Color.White,
        selectionColors = TextSelectionColors(
            handleColor = Color.White,
            backgroundColor = Color(0xFF00C853).copy(alpha = 0.4f)
        )
    )

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
                .verticalScroll(scrollState)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // TÍTULO SOLICITADO
            Text(
                text = "AGREGAR MI AUTO / AUTOS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Registre y administre sus vehículos para cotizaciones y servicio",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.EtiquetaCampo,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // TARJETA DE FORMULARIO DE VEHÍCULO
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (autoEditandoId == null) "REGISTRAR NUEVO VEHÍCULO" else "EDITAR VEHÍCULO SELECCIONADO",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // MARCA
                    Column {
                        Text("MARCA:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = marca,
                            onValueChange = { marca = it.uppercase(); mensaje = "" },
                            placeholder = { Text("Ej: Toyota, Ford, Nissan", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { modeloFocus.requestFocus() }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().focusRequester(marcaFocus)
                        )
                    }

                    // MODELO
                    Column {
                        Text("MODELO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = modelo,
                            onValueChange = { modelo = it.uppercase(); mensaje = "" },
                            placeholder = { Text("Ej: Corolla, Mustang, Sentra", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { anioFocus.requestFocus() }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().focusRequester(modeloFocus)
                        )
                    }

                    // AÑO
                    Column {
                        Text("AÑO:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = anio,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { anio = it; mensaje = "" } },
                            placeholder = { Text("Ej: 2022", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { placaFocus.requestFocus() }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().focusRequester(anioFocus)
                        )
                    }

                    // PLACA
                    Column {
                        Text("PLACA:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = placa,
                            onValueChange = { placa = it.uppercase(); mensaje = "" },
                            placeholder = { Text("Ej: ABC-123", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { colorFocus.requestFocus() }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().focusRequester(placaFocus)
                        )
                    }

                    // COLOR
                    Column {
                        Text("COLOR:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = colorAuto,
                            onValueChange = { colorAuto = it.uppercase(); mensaje = "" },
                            placeholder = { Text("Ej: Rojo, Blanco, Negro", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { vinFocus.requestFocus() }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().focusRequester(colorFocus)
                        )
                    }

                    // VIN (NÚMERO DE SERIE)
                    Column {
                        Text("VIN (NÚMERO DE SERIE):", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = vin,
                            onValueChange = { vin = it.uppercase(); mensaje = "" },
                            placeholder = { Text("17 caracteres del chasis", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { kmFocus.requestFocus() }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().focusRequester(vinFocus)
                        )
                    }

                    // KILOMETRAJE
                    Column {
                        Text("KILOMETRAJE:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        OutlinedTextField(
                            value = kilometraje,
                            onValueChange = { if (it.all { c -> c.isDigit() }) { kilometraje = it; mensaje = "" } },
                            placeholder = { Text("Ej: 45000", color = Color.Gray) },
                            textStyle = estiloCampo,
                            colors = coloresCampo,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().focusRequester(kmFocus)
                        )
                    }

                    if (mensaje.isNotBlank()) {
                        Text(mensaje, color = Color(0xFFFF5252), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // BOTÓN GUARDAR / CANCELAR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BotonModulo3D(
                            texto = if (guardando) "GUARDANDO..." else if (autoEditandoId == null) "GUARDAR AUTO" else "ACTUALIZAR",
                            icono = "💾",
                            colorClaro = Color(0xFFB9F6CA),
                            colorMedio = Color(0xFF00C853),
                            colorOscuro = Color(0xFF00695C),
                            colorTexto = Color.Black,
                            onClick = { if (!guardando) guardar() },
                            modifier = Modifier.weight(1f).height(52.dp),
                            tamanioTexto = 15
                        )

                        if (autoEditandoId != null) {
                            BotonModulo3D(
                                texto = "CANCELAR",
                                icono = "❌",
                                colorClaro = Colores.RegresarClaro,
                                colorMedio = Colores.RegresarMedio,
                                colorOscuro = Colores.RegresarOscuro,
                                colorTexto = Color.White,
                                onClick = { limpiarFormulario() },
                                modifier = Modifier.weight(1f).height(52.dp),
                                tamanioTexto = 15
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LISTA DE AUTOS REGISTRADOS CON BOTONES EDITAR Y ELIMINAR
            Text(
                text = "MIS VEHÍCULOS REGISTRADOS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (cargando) {
                CircularProgressIndicator(color = Color.White)
            } else if (autos.isEmpty()) {
                Text(
                    text = "NO TIENE VEHÍCULOS REGISTRADOS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                autos.forEach { auto ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🚗 ${auto.marca.uppercase()} ${auto.modelo.uppercase()} (${auto.anio})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "PLACA: ${auto.placa.uppercase()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7DFFB2)
                                )
                            }

                            if (auto.color.isNotBlank() || auto.vin.isNotBlank()) {
                                Text(
                                    text = "COLOR: ${auto.color.ifBlank { "N/A" }}   |   VIN: ${auto.vin.ifBlank { "N/A" }}",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            HorizontalDivider(color = Color(0xFF004D33))

                            // BOTONES DE ACCIÓN: EDITAR (VERDE) Y ELIMINAR (ROJO)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // EDITAR
                                BotonModulo3D(
                                    texto = "EDITAR",
                                    icono = "✏️",
                                    colorClaro = Color(0xFFB9F6CA),
                                    colorMedio = Color(0xFF00C853),
                                    colorOscuro = Color(0xFF00695C),
                                    colorTexto = Color.Black,
                                    onClick = { prepararEdicion(auto) },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    tamanioTexto = 14
                                )

                                // ELIMINAR
                                BotonModulo3D(
                                    texto = "ELIMINAR",
                                    icono = "🗑️",
                                    colorClaro = Color(0xFFEF9A9A),
                                    colorMedio = Color(0xFFE53935),
                                    colorOscuro = Color(0xFFB71C1C),
                                    colorTexto = Color.Black,
                                    onClick = {
                                        AlertDialog.Builder(context).apply {
                                            setTitle("⚠️ ELIMINAR VEHÍCULO")
                                            setMessage("¿Está seguro de eliminar el auto \"${auto.marca} ${auto.modelo} (${auto.placa})\"?\n\nEsta acción no se puede deshacer.")
                                            setPositiveButton("SÍ, ELIMINAR") { _, _ ->
                                                scope.launch {
                                                    try {
                                                        AutoRepository.eliminarAuto(auto, context)
                                                        Toast.makeText(context, "✅ Vehículo eliminado", Toast.LENGTH_SHORT).show()
                                                        if (autoEditandoId == auto.id) limpiarFormulario()
                                                        cargarAutos()
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                            setNegativeButton("CANCELAR", null)
                                            show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    tamanioTexto = 14
                                )
                            }
                        }
                    }
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
                    colorTexto = Color.White
                )
            }
        }
    }
}
