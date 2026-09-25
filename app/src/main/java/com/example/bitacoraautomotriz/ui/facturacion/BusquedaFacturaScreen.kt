
package com.example.bitacoraautomotriz.ui.facturacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.example.bitacoraautomotriz.data.Factura
import com.example.bitacoraautomotriz.repository.FacturaRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import kotlinx.coroutines.launch

@Composable
fun BusquedaFacturaScreen(
    onRegresar: () -> Unit
) {

    var textoBusqueda by remember {
        mutableStateOf("")
    }

    var resultados by remember {
        mutableStateOf(emptyList<Factura>())
    }

    var busquedaRealizada by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B44))
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 48.dp,
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "BUSCAR FACTURA",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // =========================================
        // CAMPO DE BÚSQUEDA
        // =========================================

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = {
                textoBusqueda = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Número o cliente",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,

                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black,

                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                disabledLabelColor = Color.Black,

                focusedIndicatorColor = Color(0xFF00AEEF),
                unfocusedIndicatorColor = Color(0xFF607D8B),

                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // =========================================
        // BUSCAR
        // =========================================

        BotonModulo3D(
            texto = "BUSCAR FACTURA",
            colorClaro = Color(0xFF8FFFFF),
            colorMedio = Color(0xFF00DDEB),
            colorOscuro = Color(0xFF007F88),
            onClick = {

                val texto = textoBusqueda.trim()

                scope.launch {

                    resultados = FacturaRepository
                        .obtenerFacturas()
                        .filter { factura ->

                            factura.numero.contains(
                                texto,
                                ignoreCase = true
                            ) ||
                                    factura.cliente.contains(
                                        texto,
                                        ignoreCase = true
                                    )
                        }

                    busquedaRealizada = true
                }
            }
        )

        // =========================================
        // RESULTADOS
        // =========================================

        if (busquedaRealizada) {

            if (resultados.isEmpty()) {

                Text(
                    text = "No se encontraron facturas.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

            } else {

                Text(
                    text = "FACTURAS ENCONTRADAS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                resultados.forEach { factura ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        Text(
                            text = "Número: ${factura.numero}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001B44)
                        )

                        Text(
                            text = "Cliente: ${factura.cliente}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = "Fecha: ${factura.fecha}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = "Subtotal: ${factura.subtotal}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = "IVA: ${factura.iva}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = "Total: ${factura.total}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF001B44)
                        )
                    }
                }
            }
        }

        // =========================================
        // REGRESAR
        // =========================================

        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Color(0xFFD5E1E6),
            colorMedio = Color(0xFF90A4AE),
            colorOscuro = Color(0xFF455A64),
            onClick = onRegresar
        )
    }
}

