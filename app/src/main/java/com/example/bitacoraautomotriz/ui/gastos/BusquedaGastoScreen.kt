
package com.example.bitacoraautomotriz.ui.gastos

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
import androidx.compose.runtime.Composable
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
import com.example.bitacoraautomotriz.data.Gasto
import com.example.bitacoraautomotriz.repository.GastoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import kotlinx.coroutines.launch

@Composable
fun BusquedaGastoScreen(
    onRegresar: () -> Unit
) {

    var textoBusqueda by remember {
        mutableStateOf("")
    }

    var resultados by remember {
        mutableStateOf(emptyList<Gasto>())
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
            text = "BUSCAR GASTO",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )


        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = {
                textoBusqueda = it
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Concepto, categoría o descripción",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = androidx.compose.material3.TextFieldDefaults.colors(
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



        BotonModulo3D(
            texto = "BUSCAR GASTO",
            colorClaro = Color(0xFFFF9999),
            colorMedio = Color(0xFFFF4141),
            colorOscuro = Color(0xFFB51F1F),
            onClick = {

                val texto = textoBusqueda.trim()

                scope.launch {

                    resultados = GastoRepository
                        .obtenerGastos()
                        .filter { gasto ->

                            gasto.concepto.contains(
                                texto,
                                ignoreCase = true
                            ) ||
                                    gasto.categoria.contains(
                                        texto,
                                        ignoreCase = true
                                    ) ||
                                    gasto.descripcion.contains(
                                        texto,
                                        ignoreCase = true
                                    )
                        }

                    busquedaRealizada = true
                }
            }
        )

        if (busquedaRealizada) {

            if (resultados.isEmpty()) {

                Text(
                    text = "No se encontraron gastos.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

            } else {

                Text(
                    text = "GASTOS ENCONTRADOS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                resultados.forEach { gasto ->

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
                            text = "Concepto: ${gasto.concepto}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = "Categoría: ${gasto.categoria}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = "Fecha: ${gasto.fecha}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = "Monto: ${gasto.monto}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = "Descripción: ${gasto.descripcion}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Color(0xFFD5E1E6),
            colorMedio = Color(0xFF90A4AE),
            colorOscuro = Color(0xFF455A64),
            onClick = onRegresar
        )
    }
}

