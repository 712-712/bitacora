package com.example.bitacoraautomotriz.ui.clientes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

@Composable
fun EstadoReparacionScreen(
    onRegresar: () -> Unit
) {
    var telefono by remember { mutableStateOf("") }
    var orden by remember { mutableStateOf<OrdenServicio?>(null) }
    var mensaje by remember { mutableStateOf("") }
    var buscando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun buscarEstado() {
        if (telefono.isBlank()) {
            mensaje = "INGRESE SU NÚMERO DE TELÉFONO"
            orden = null
            return
        }

        buscando = true
        mensaje = ""
        orden = null

        scope.launch {
            try {
                val cliente = ClienteRepository.obtenerClientePorTelefono(telefono.trim())

                if (cliente == null) {
                    mensaje = "NO SE ENCONTRÓ UN CLIENTE CON ESE TELÉFONO"
                    buscando = false
                    return@launch
                }

                val ordenes = OrdenServicioRepository.obtenerOrdenesPorCliente(cliente.nombre)

                if (ordenes.isEmpty()) {
                    mensaje = "NO HAY ÓRDENES DE SERVICIO PARA ESTE CLIENTE"
                    buscando = false
                    return@launch
                }

                orden = ordenes.last()
                mensaje = ""
                buscando = false
            } catch (e: Exception) {
                mensaje = "ERROR AL CONSULTAR EL ESTADO DE REPARACIÓN"
                buscando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 42.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // TÍTULO
        Text(
            text = "ESTADO DE REPARACIÓN",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Seguimiento de su vehículo",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo
        )

        Spacer(modifier = Modifier.height(28.dp))

        // TELÉFONO
        OutlinedTextField(
            value = telefono,
            onValueChange = {
                telefono = it
                mensaje = ""
                orden = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            textStyle = TextStyle(
                color = Colores.TextoTarjeta,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "TELÉFONO",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo
                )
            },
            placeholder = {
                Text(
                    text = "Ingrese su número de teléfono",
                    fontSize = 17.sp,
                    color = Colores.EtiquetaCampo.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Colores.FondoSecundario,
                unfocusedContainerColor = Colores.FondoSecundario,
                focusedTextColor = Colores.TextoTarjeta,
                unfocusedTextColor = Colores.TextoTarjeta,
                focusedPlaceholderColor = Colores.EtiquetaCampo.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = Colores.EtiquetaCampo.copy(alpha = 0.6f),
                focusedBorderColor = Colores.BordeBoton,
                unfocusedBorderColor = Colores.BordeBoton.copy(alpha = 0.5f),
                focusedLabelColor = Colores.EtiquetaCampo,
                unfocusedLabelColor = Colores.EtiquetaCampo.copy(alpha = 0.7f),
                cursorColor = Colores.TextoBoton
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // BOTÓN BUSCAR
        BotonModulo3D(
            texto = if (buscando) "BUSCANDO..." else "BUSCAR ESTADO",
            icono = "🔍",
            onClick = { if (!buscando) buscarEstado() },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(22.dp))

        // MENSAJE
        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = Colores.TextoTarjeta,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // INFORMACIÓN DE LA ORDEN
        orden?.let { ordenActual ->
            val avance = ordenActual.porcentajeAvance.coerceIn(0, 100)

            // COLORES SEGÚN EL PORCENTAJE (se mantienen semánticos)
            val coloresPorTramo = listOf(
                Color(0xFFD32F2F),   // 0 - 9
                Color(0xFFE64A19),   // 10 - 19
                Color(0xFFF57C00),   // 20 - 29
                Color(0xFFFFA000),   // 30 - 39
                Color(0xFFFBC02D),   // 40 - 49
                Color(0xFFC0CA33),   // 50 - 59
                Color(0xFF9CCC65),   // 60 - 69
                Color(0xFF7CB342),   // 70 - 79
                Color(0xFF558B2F),   // 80 - 89
                Color(0xFF2E7D32)    // 90 - 100
            )

            val tramo = (avance / 10).coerceIn(0, coloresPorTramo.lastIndex)
            val colorAvance = coloresPorTramo[tramo]

            // TARJETA PRINCIPAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Colores.FondoTarjeta
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // VEHÍCULO
                    Text(
                        text = "VEHÍCULO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo
                    )
                    Text(
                        text = ordenActual.auto,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    // ORDEN
                    Text(
                        text = "ORDEN DE SERVICIO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo
                    )
                    Text(
                        text = "#${ordenActual.id}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )

                    // FECHA
                    Text(
                        text = "FECHA DE INGRESO: ${ordenActual.fecha}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // AVANCE
                    Text(
                        text = "AVANCE DE LA REPARACIÓN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo
                    )
                    Text(
                        text = "$avance%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorAvance
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    // BARRA DE AVANCE
                    LinearProgressIndicator(
                        progress = { avance / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp),
                        color = colorAvance,
                        trackColor = Colores.TextoTarjeta.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    // ESTADO ACTUAL
                    Text(
                        text = "ESTADO ACTUAL",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo
                    )
                    Text(
                        text = ordenActual.estado.uppercase(),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorAvance
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // DIAGNÓSTICO
                    if (ordenActual.diagnostico.isNotBlank()) {
                        Text(
                            text = "DIAGNÓSTICO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.EtiquetaCampo
                        )
                        Text(
                            text = ordenActual.diagnostico,
                            fontSize = 17.sp,
                            color = Colores.TextoTarjeta
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // TRABAJO REALIZADO
                    if (ordenActual.trabajoRealizado.isNotBlank()) {
                        Text(
                            text = "TRABAJO REALIZADO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.EtiquetaCampo
                        )
                        Text(
                            text = ordenActual.trabajoRealizado,
                            fontSize = 17.sp,
                            color = Colores.TextoTarjeta
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // ETAPAS
            Text(
                text = "ETAPAS DE REPARACIÓN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )
            Spacer(modifier = Modifier.height(12.dp))

            mostrarEtapas(porcentaje = avance)
            Spacer(modifier = Modifier.height(25.dp))

            // FECHA DE ENTREGA
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Colores.FondoTarjeta
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FECHA ESTIMADA DE ENTREGA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (ordenActual.fechaEntrega.isBlank()) {
                            "PENDIENTE DE DEFINIR"
                        } else {
                            ordenActual.fechaEntrega
                        },
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))
        }

        // REGRESAR
        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

// =====================================================
// ETAPAS DE REPARACIÓN
// =====================================================

@Composable
private fun mostrarEtapas(porcentaje: Int) {
    val avance = porcentaje.coerceIn(0, 100)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        etapa(texto = "Vehículo recibido", completada = avance >= 10)
        etapa(texto = "Diagnóstico realizado", completada = avance >= 25)
        etapa(texto = "Presupuesto autorizado", completada = avance >= 40)
        etapa(texto = "Refacciones solicitadas", completada = avance >= 50)
        etapa(texto = "Reparación en proceso", completada = avance >= 60)
        etapa(texto = "Pruebas finales", completada = avance >= 90)
        etapa(texto = "Vehículo listo para entrega", completada = avance >= 100)
    }
}

// =====================================================
// UNA ETAPA
// =====================================================

@Composable
private fun etapa(texto: String, completada: Boolean) {
    Text(
        text = if (completada) "✓ $texto" else "○ $texto",
        fontSize = 17.sp,
        fontWeight = if (completada) FontWeight.Bold else FontWeight.Normal,
        color = if (completada) Colores.TextoBoton else Colores.TextoTarjeta.copy(alpha = 0.5f)
    )
}
