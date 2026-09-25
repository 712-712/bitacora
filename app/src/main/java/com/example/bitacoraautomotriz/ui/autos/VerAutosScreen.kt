package com.example.bitacoraautomotriz.ui.autos

import android.app.AlertDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    var autos by remember { mutableStateOf(emptyList<Auto>()) }
    var busqueda by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(refreshTrigger) {
        autos = AutoRepository.obtenerAutos().sortedByDescending { it.id }
    }

    val autosFiltrados = autos.filter {
        it.placa.contains(busqueda.uppercase(), ignoreCase = true) ||
                it.marca.contains(busqueda.uppercase(), ignoreCase = true) ||
                it.modelo.contains(busqueda.uppercase(), ignoreCase = true) ||
                it.cliente.contains(busqueda.uppercase(), ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
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
            onClick = onAgregarAuto
        )
        Spacer(modifier = Modifier.height(24.dp))

        // ✅ ETIQUETA ARRIBA con color de título (Azul claro)
        Text(
            text = "PLACA, MARCA, MODELO O CLIENTE:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, bottom = 4.dp)
        )

        // ✅ CAMPO DE BÚSQUEDA LIMPIO
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
            singleLine = true, // ✅ Evita saltos de línea al presionar Enter
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Colores.FondoSecundario,
                unfocusedContainerColor = Colores.FondoSecundario,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Colores.BordeBoton,
                unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
                cursorColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (autosFiltrados.isEmpty()) {
            Text(
                text = "NO SE ENCONTRARON AUTOS",
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
                            text = "${auto.marca} ${auto.modelo} (${auto.anio})",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Placa: ${auto.placa}",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Cliente: ${auto.cliente}",
                            fontSize = 19.sp,
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
                                modifier = Modifier.weight(1f).height(48.dp),
                                tamanioTexto = 14,
                                colorTexto = Color.White
                            )

                            BotonModulo3D(
                                texto = "ELIMINAR",
                                colorClaro = Color(0xFFEF9A9A),
                                colorMedio = Color(0xFFE53935),
                                colorOscuro = Color(0xFFB71C1C),
                                onClick = {
                                    scope.launch {
                                        val mensajeAdvertencia = "¿Está seguro de eliminar el auto:\n\"${auto.marca} ${auto.modelo} (${auto.placa})\"?\n\n⚠️ ADVERTENCIA: Esta acción es irreversible y podría afectar los registros de servicio (órdenes) asociados a este vehículo.\n\nEsta acción NO se puede deshacer."

                                        AlertDialog.Builder(context).apply {
                                            setTitle("⚠️ ELIMINAR AUTO")
                                            setMessage(mensajeAdvertencia)
                                            setPositiveButton("SÍ, ELIMINAR") { _, _ ->
                                                scope.launch {
                                                    try {
                                                        AutoRepository.eliminarAuto(auto)
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
                                modifier = Modifier.weight(1f).height(48.dp),
                                tamanioTexto = 14,
                                colorTexto = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Color(0xFFD5E1E6),
            colorMedio = Color(0xFF90A4AE),
            colorOscuro = Color(0xFF455A64),
            onClick = onRegresar
        )
    }
}
