package com.example.bitacoraautomotriz.ui.ordenes

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.BitacoraAutomotrizTheme
import kotlinx.coroutines.launch

class AceptacionClienteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ordenId = intent.data?.getQueryParameter("id")?.toIntOrNull()

        if (ordenId == null) {
            Toast.makeText(this, "Enlace de orden inválido", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            BitacoraAutomotrizTheme {
                PantallaAceptacionCliente(
                    ordenId = ordenId,
                    onResultado = { aceptada ->
                        val mensaje = if (aceptada) "¡Gracias por aceptar la cotización!" else "Cotización rechazada"
                        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaAceptacionCliente(
    ordenId: Int,
    onResultado: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    var orden by remember { mutableStateOf<OrdenServicio?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(ordenId) {
        try {
            // ✅ CORRECCIÓN: Usar el método correcto del repository
            orden = OrdenServicioRepository.obtenerOrdenPorId(ordenId)
        } catch (e: Exception) {
            // Manejo de error si la orden no existe
        } finally {
            cargando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B44))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (cargando) {
            Text(text = "CARGANDO ORDEN...", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        } else if (orden == null) {
            Text(text = "ORDEN NO ENCONTRADA", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ORDEN DE SERVICIO #${orden!!.id}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001B44)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Auto: ${orden!!.auto}",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Falla: ${orden!!.fallaReportada}",
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "¿Desea aceptar esta cotización?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001B44)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    BotonModulo3D(
                        texto = "✅ ACEPTAR COTIZACIÓN",
                        colorClaro = Color(0xFFA5D6A7),
                        colorMedio = Color(0xFF43A047),
                        colorOscuro = Color(0xFF1B5E20),
                        onClick = {
                            scope.launch {
                                OrdenServicioRepository.actualizarAceptacion(ordenId, true)
                                onResultado(true)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BotonModulo3D(
                        texto = "❌ RECHAZAR COTIZACIÓN",
                        colorClaro = Color(0xFFEF9A9A),
                        colorMedio = Color(0xFFE53935),
                        colorOscuro = Color(0xFFB71C1C),
                        onClick = {
                            scope.launch {
                                OrdenServicioRepository.actualizarAceptacion(ordenId, false)
                                onResultado(false)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
