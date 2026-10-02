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
fun HistorialServiciosScreen(
    onRegresar: () -> Unit,
) {
    var textoBusqueda by remember { mutableStateOf(value = "") }
    var ordenes by remember { mutableStateOf<List<OrdenServicio>>(value = emptyList()) }
    var mensaje by remember { mutableStateOf(value = "") }
    var buscando by remember { mutableStateOf(value = false) }
    val scope = rememberCoroutineScope()

    fun buscarHistorial() {
        if (textoBusqueda.isBlank()) {
            mensaje = "INGRESE LA PLACA, VIN, NOMBRE, AUTO O TELÉFONO"
            ordenes = emptyList()
            return
        }

        buscando = true
        mensaje = ""
        ordenes = emptyList()

        scope.launch {
            try {
                val query = textoBusqueda.trim().uppercase()
                val todasLasOrdenes = OrdenServicioRepository.obtenerOrdenes()

                // Buscar coincidencias por Cliente, Auto (Marca/Modelo/Placa/VIN) o ID de Orden
                val ordenesEncontradas = todasLasOrdenes.filter { orden ->
                    orden.cliente.uppercase().contains(query) ||
                            orden.auto.uppercase().contains(query) ||
                            orden.id.toString().contains(query) ||
                            orden.fallaReportada.uppercase().contains(query)
                }.toMutableList()

                // Si no encontró directamente, buscar si es un teléfono de cliente
                if (ordenesEncontradas.isEmpty()) {
                    val cliente = ClienteRepository.obtenerClientePorTelefono(query)
                    if (cliente != null) {
                        val porCliente = OrdenServicioRepository.obtenerOrdenesPorCliente(cliente.nombre)
                        ordenesEncontradas.addAll(porCliente)
                    }
                }

                ordenes = ordenesEncontradas

                if (ordenes.isEmpty()) {
                    mensaje = "NO SE ENCONTRARON SERVICIOS REGISTRADOS CON ESOS DATOS"
                }

                buscando = false
            } catch (_: Exception) {
                mensaje = "ERROR AL CONSULTAR EL HISTORIAL DE SERVICIOS"
                buscando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 42.dp,
                bottom = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // TÍTULO
        Text(
            text = "HISTORIAL DE SERVICIOS",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Consulte el historial de servicios anteriores",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo
        )

        Spacer(modifier = Modifier.height(28.dp))

        // TEXTO ETIQUETA ENCIMA DEL CAMPO DE BÚSQUEDA
        Text(
            text = "BUSCAR POR PLACA, VIN, NOMBRE, AUTO O TELÉFONO:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        // CAMPO DE BÚSQUEDA
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = {
                textoBusqueda = it
                mensaje = ""
                ordenes = emptyList()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            textStyle = TextStyle(
                color = Colores.TextoTarjeta,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            placeholder = {
                Text(
                    text = "Ej: ABC-1234, VIN, Juan Pérez...",
                    fontSize = 16.sp,
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

        // BOTÓN BUSCAR HISTORIAL (COLOR CAFÉ)
        BotonModulo3D(
            texto = if (buscando) "BUSCANDO..." else "BUSCAR HISTORIAL",
            icono = "🔍",
            colorClaro = Color(0xFFD7B899),
            colorMedio = Color(0xFF9B6B43),
            colorOscuro = Color(0xFF5D3A1A),
            colorTexto = Color.Black,
            onClick = { if (!buscando) buscarHistorial() },
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

        // HISTORIAL
        if (ordenes.isNotEmpty()) {
            Text(
                text = "SERVICIOS ENCONTRADOS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )
            Spacer(modifier = Modifier.height(18.dp))

            ordenes.forEach { orden ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
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
                        // CLIENTE
                        Text(
                            text = "CLIENTE: ${orden.cliente.uppercase()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.EtiquetaCampo
                        )

                        // VEHÍCULO
                        Text(
                            text = orden.auto,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.TextoTarjeta
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // ORDEN
                        Text(
                            text = "ORDEN DE SERVICIO #${orden.id}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.EtiquetaCampo
                        )

                        // FECHA
                        Text(
                            text = "FECHA DE INGRESO: ${orden.fecha}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.TextoTarjeta
                        )

                        // KILOMETRAJE
                        Text(
                            text = "KILOMETRAJE: ${orden.kilometraje} km",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.TextoTarjeta
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // FALLA REPORTADA
                        Text(
                            text = "FALLA REPORTADA",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Colores.EtiquetaCampo
                        )
                        Text(
                            text = orden.fallaReportada,
                            fontSize = 17.sp,
                            color = Colores.TextoTarjeta
                        )

                        // DIAGNÓSTICO
                        if (orden.diagnostico.isNotBlank()) {
                            Text(
                                text = "DIAGNÓSTICO",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Colores.EtiquetaCampo
                            )
                            Text(
                                text = orden.diagnostico,
                                fontSize = 17.sp,
                                color = Colores.TextoTarjeta
                            )
                        }

                        // TRABAJO REALIZADO
                        if (orden.trabajoRealizado.isNotBlank()) {
                            Text(
                                text = "TRABAJO REALIZADO",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Colores.EtiquetaCampo
                            )
                            Text(
                                text = orden.trabajoRealizado,
                                fontSize = 17.sp,
                                color = Colores.TextoTarjeta
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // ESTADO (color semántico naranja)
                        Text(
                            text = "ESTADO: ${orden.estado.uppercase()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57C00)
                        )

                        // AVANCE (color semántico verde)
                        Text(
                            text = "AVANCE: ${orden.porcentajeAvance}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        // FECHA DE ENTREGA
                        if (orden.fechaEntrega.isNotBlank()) {
                            Text(
                                text = "ENTREGA ESTIMADA: ${orden.fechaEntrega}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Colores.TextoTarjeta
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // REGRESAR (GRIS)
        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            colorTexto = Color.White,
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
