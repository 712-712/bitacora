package com.example.bitacoraautomotriz.ui.autos

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

@Composable
fun VerAutosScreen(
    onAgregarAuto: () -> Unit,
    onEditarAuto: (Int) -> Unit,
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var autos by remember { mutableStateOf<List<Auto>>(value = emptyList()) }
    var busqueda by remember { mutableStateOf(value = "") }
    var refreshTrigger by remember { mutableStateOf(value = 0) }
    var cargando by remember { mutableStateOf(value = true) }

    LaunchedEffect(refreshTrigger) {
        try {
            autos = AutoRepository.obtenerAutos(context).sortedByDescending { it.id }
        } catch (_: Exception) {
            autos = emptyList()
        } finally {
            cargando = false
        }
    }

    val query = busqueda.uppercase().trim()
    val autosFiltrados = autos.filter { auto ->
        auto.placa.orEmpty().uppercase().contains(query, ignoreCase = true) ||
                auto.marca.orEmpty().uppercase().contains(query, ignoreCase = true) ||
                auto.modelo.orEmpty().uppercase().contains(query, ignoreCase = true) ||
                auto.cliente.orEmpty().uppercase().contains(query, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TODOS LOS AUTOS",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )
            Spacer(modifier = Modifier.height(24.dp))

            BotonModulo3D(
                texto = "AGREGAR AUTO NUEVO",
                colorClaro = Color(0xFFB9F6CA),
                colorMedio = Color(0xFF00C853),
                colorOscuro = Color(0xFF00695C),
                onClick = onAgregarAuto,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colorTexto = Color.Black
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PLACA, MARCA, MODELO O CLIENTE:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 4.dp)
            )

            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it.uppercase() },
                textStyle = TextStyle(color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold),
                placeholder = {
                    Text(
                        "Escriba aquí...",
                        fontSize = 16.sp,
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
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
                modifier = Modifier.fillMaxWidth().height(60.dp)
            )

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
            } else if (autosFiltrados.isEmpty()) {
                Text(
                    text = if (autos.isEmpty()) "NO HAY AUTOS REGISTRADOS" else "NO SE ENCONTRARON AUTOS",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TextoGlobal
                )
            } else {
                autosFiltrados.forEach { auto ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(19.dp)) {
                            Text(
                                text = "${auto.marca.orEmpty()} ${auto.modelo.orEmpty()} (${auto.anio})",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Placa: ${auto.placa.orEmpty()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Cliente: ${auto.cliente.orEmpty()}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BotonModulo3D(
                                    texto = "EDITAR",
                                    colorClaro = Color(0xFF90CAF9),
                                    colorMedio = Color(0xFF1976D2),
                                    colorOscuro = Color(0xFF0D47A1),
                                    onClick = { onEditarAuto(auto.id) },
                                    modifier = Modifier.weight(1f).height(54.dp),
                                    tamanioTexto = 15,
                                    colorTexto = Color.Black
                                )

                                BotonModulo3D(
                                    texto = "ELIMINAR",
                                    colorClaro = Color(0xFFEF9A9A),
                                    colorMedio = Color(0xFFE53935),
                                    colorOscuro = Color(0xFFB71C1C),
                                    onClick = {
                                        scope.launch {
                                            val mensajeAdvertencia = "¿Está seguro de eliminar el auto:\n\"${auto.marca} ${auto.modelo} (${auto.placa})\"?\n\n⚠️ ADVERTENCIA: Esta acción es irreversible.\n\nEsta acción NO se puede deshacer."

                                            AlertDialog.Builder(context).apply {
                                                setTitle("⚠️ ELIMINAR AUTO")
                                                setMessage(mensajeAdvertencia)
                                                setPositiveButton("SÍ, ELIMINAR") { _, _ ->
                                                    scope.launch {
                                                        try {
                                                            AutoRepository.eliminarAuto(auto, context)
                                                            refreshTrigger++
                                                            Toast.makeText(context, "✅ Auto eliminado correctamente", Toast.LENGTH_SHORT).show()
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                                setNegativeButton("CANCELAR", null)
                                                show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(54.dp),
                                    tamanioTexto = 15,
                                    colorTexto = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // BOTÓN REGRESAR FIJO E INMÓVIL AL FONDO DE LA PANTALLA
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Colores.FondoPantalla)
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
