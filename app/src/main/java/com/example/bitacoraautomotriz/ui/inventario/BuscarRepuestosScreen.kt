package com.example.bitacoraautomotriz.ui.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Repuesto
import com.example.bitacoraautomotriz.repository.RepuestoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import java.util.Locale

@Composable
fun BuscarRepuestosScreen(
    onRegresar: () -> Unit = {},
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var textoBusqueda by remember { mutableStateOf(value = "") }
    var repuestosLocales by remember { mutableStateOf<List<Repuesto>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = true) }

    LaunchedEffect(Unit) {
        try {
            repuestosLocales = RepuestoRepository.obtenerRepuestos(context)
        } catch (_: Exception) {
            repuestosLocales = emptyList()
        } finally {
            cargando = false
        }
    }

    val repuestosFiltrados = if (textoBusqueda.isBlank()) {
        repuestosLocales
    } else {
        val query = textoBusqueda.trim()
        repuestosLocales.filter { rep ->
            rep.nombre.contains(query, ignoreCase = true) ||
                    rep.marca.contains(query, ignoreCase = true) ||
                    rep.categoria.contains(query, ignoreCase = true)
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
            text = "BUSCAR REPUESTO EN INVENTARIO",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "NOMBRE, MARCA O CATEGORÍA",
                fontSize = 17.sp,
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
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Colores.FondoSecundario,
                unfocusedContainerColor = Colores.FondoSecundario,
                disabledContainerColor = Colores.FondoSecundario,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Colores.BordeBoton,
                unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
                cursorColor = Color.White
            ),
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
        } else if (repuestosFiltrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (repuestosLocales.isEmpty())
                        "NO HAY REPUESTOS EN EL INVENTARIO"
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
                items(repuestosFiltrados) { repuesto ->
                    val cantidad = repuesto.cantidad.coerceAtLeast(0)
                    val precioUnitario = if (repuesto.precio.isNaN() || repuesto.precio < 0) 0.0 else repuesto.precio
                    val importe = cantidad * precioUnitario
                    val iva = importe * 0.16
                    val total = importe + iva

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
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = repuesto.nombre.uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "MARCA: ${repuesto.marca.uppercase()}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "CATEGORÍA: ${repuesto.categoria.uppercase()}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFF004D33))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "CANTIDAD:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "$cantidad", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "PRECIO:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = String.format(Locale.US, "$ %,.2f", precioUnitario), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "IMPORTE:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = String.format(Locale.US, "$ %,.2f", importe), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "I.V.A. 16%:", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = String.format(Locale.US, "$ %,.2f", iva), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "TOTAL:", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = String.format(Locale.US, "$ %,.2f", total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
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
