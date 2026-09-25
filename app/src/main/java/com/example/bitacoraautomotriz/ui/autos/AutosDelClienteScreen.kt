package com.example.bitacoraautomotriz.ui.autos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun AutosDelClienteScreen(
    clienteId: Int,
    nombreCliente: String,
    onAgregarAuto: () -> Unit,
    onEditarAuto: (Int) -> Unit,
    onRegresar: () -> Unit
) {
    var autos by remember { mutableStateOf(emptyList<Auto>()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        autos = AutoRepository.obtenerAutosPorCliente(nombreCliente).sortedByDescending { it.id }
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
            text = "AUTOS DE:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo
        )
        Text(
            text = nombreCliente.uppercase(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (autos.isEmpty()) {
            Text(
                text = "ESTE CLIENTE AÚN NO TIENE AUTOS REGISTRADOS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TextoGlobal
            )
        } else {
            autos.forEach { auto ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Colores.FondoTarjeta),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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
                            text = "Kilometraje: ${auto.kilometraje} km",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // ✅ SOLO BOTÓN EDITAR (ELIMINAR REMOVIDO)
                        BotonModulo3D(
                            texto = "EDITAR",
                            colorClaro = Color(0xFF90CAF9),
                            colorMedio = Color(0xFF1976D2),
                            colorOscuro = Color(0xFF0D47A1),
                            onClick = { onEditarAuto(auto.id) },
                            modifier = Modifier.fillMaxWidth().height(48.dp), // ✅ AHORA OCUPA TODO EL ANCHO
                            tamanioTexto = 14,
                            colorTexto = Color.White // ✅ TEXTO BLANCO PARA CONTRASTE
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        BotonModulo3D(
            texto = "AGREGAR AUTO",
            colorClaro = Color(0xFFB9F6CA),
            colorMedio = Color(0xFF00C853),
            colorOscuro = Color(0xFF00695C),
            onClick = onAgregarAuto
        )
        Spacer(modifier = Modifier.height(12.dp))

        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Color(0xFFD5E1E6),
            colorMedio = Color(0xFF90A4AE),
            colorOscuro = Color(0xFF455A64),
            onClick = onRegresar
        )
    }
}
