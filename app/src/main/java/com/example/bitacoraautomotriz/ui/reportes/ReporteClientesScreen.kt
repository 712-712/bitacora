package com.example.bitacoraautomotriz.ui.reportes

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun ReporteClientesScreen(
    onRegresar: () -> Unit,
) {
    val context = LocalContext.current
    var clientes by remember { mutableStateOf<List<Cliente>>(value = emptyList()) }
    var cargando by remember { mutableStateOf(value = true) }

    fun abrirCorreo(correoDestino: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$correoDestino")
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No se encontró una aplicación de correo", Toast.LENGTH_SHORT).show()
        }
    }

    fun abrirTelefono(telefonoDestino: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$telefonoDestino")
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo abrir el marcados telefónico", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        try {
            clientes = ClienteRepository.obtenerClientes(context)
        } catch (_: Exception) {
            clientes = emptyList()
        } finally {
            cargando = false
        }
    }

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
                text = "REPORTE DE CLIENTES",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (clientes.isEmpty()) {
                Text(
                    text = "NO HAY CLIENTES REGISTRADOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Row {
                    Text(text = "Total de clientes: ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "${clientes.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                }

                Spacer(modifier = Modifier.height(16.dp))

                clientes.forEach { cliente ->
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
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row {
                                Text(text = "ID: ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = "${cliente.id}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Row {
                                Text(text = "NOMBRE: ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = cliente.nombre.uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Row {
                                Text(text = "TELÉFONO: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(
                                    text = cliente.telefono,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0033FF),
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable { abrirTelefono(cliente.telefono) }
                                )
                            }

                            if (cliente.correo.isNotBlank()) {
                                Row {
                                    Text(text = "CORREO: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(
                                        text = cliente.correo,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0033FF),
                                        textDecoration = TextDecoration.Underline,
                                        modifier = Modifier.clickable { abrirCorreo(cliente.correo) }
                                    )
                                }
                            }

                            if (cliente.direccion.isNotBlank()) {
                                Row {
                                    Text(text = "DIRECCIÓN: ", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Text(text = cliente.direccion.uppercase(), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
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
                    tamanioTexto = 16,
                    colorTexto = Color.White
                )
            }
        }
    }
}
