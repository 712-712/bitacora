package com.example.bitacoraautomotriz.ui.ordenes

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun VerOrdenesScreen(
    onRegresar: () -> Unit
) {


// =========================================
// ÓRDENES
// =========================================

    var ordenes by remember {
        mutableStateOf(emptyList<OrdenServicio>())
    }

// =========================================
// CARGAR ÓRDENES
// =========================================

    LaunchedEffect(Unit) {

        ordenes =
            OrdenServicioRepository.obtenerOrdenes()
    }

// =========================================
// PANTALLA
// =========================================

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B44))
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 48.dp,
                bottom = 28.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Top
    ) {

        // =========================================
        // TÍTULO
        // =========================================

        Text(
            text = "ÓRDENES DE SERVICIO",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // =========================================
        // SIN ÓRDENES
        // =========================================

        if (ordenes.isEmpty()) {

            Text(
                text = "NO HAY ÓRDENES REGISTRADAS",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

        } else {

            Text(
                text = "ÓRDENES REGISTRADAS: ${ordenes.size}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            // =========================================
            // CADA ORDEN = UNA TARJETA INDIVIDUAL
            // =========================================

            ordenes.forEach { orden ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth(),

                    shape =
                        RoundedCornerShape(16.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color.White
                        ),

                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation =
                                8.dp
                        )
                ) {

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(18.dp),

                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        // =====================================
                        // ENCABEZADO DE LA TARJETA
                        // =====================================

                        Text(
                            text =
                                "ORDEN #${orden.id}",

                            fontSize =
                                22.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color(0xFF0D47A1)
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        // =====================================
                        // CLIENTE
                        // =====================================

                        Text(
                            text =
                                "CLIENTE: ${orden.cliente}",

                            fontSize =
                                18.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color.Black
                        )

                        // =====================================
                        // AUTO
                        // =====================================

                        Text(
                            text =
                                "AUTO: ${orden.auto}",

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color.Black
                        )

                        // =====================================
                        // FECHA
                        // =====================================

                        Text(
                            text =
                                "FECHA: ${orden.fecha}",

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color.Black
                        )

                        // =====================================
                        // KILOMETRAJE
                        // =====================================

                        Text(
                            text =
                                "KILOMETRAJE: ${orden.kilometraje} km",

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color.Black
                        )

                        // =====================================
                        // FALLA REPORTADA
                        // =====================================

                        Text(
                            text =
                                "FALLA REPORTADA: ${
                                    if (orden.fallaReportada.isBlank())
                                        "SIN REGISTRAR"
                                    else
                                        orden.fallaReportada
                                }",

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color.Black
                        )

                        // =====================================
                        // DIAGNÓSTICO
                        // =====================================

                        Text(
                            text =
                                "DIAGNÓSTICO: ${
                                    if (orden.diagnostico.isBlank())
                                        "SIN REGISTRAR"
                                    else
                                        orden.diagnostico
                                }",

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color.Black
                        )

                        // =====================================
                        // TRABAJO REALIZADO
                        // =====================================

                        Text(
                            text =
                                "TRABAJO REALIZADO: ${
                                    if (orden.trabajoRealizado.isBlank())
                                        "SIN REGISTRAR"
                                    else
                                        orden.trabajoRealizado
                                }",

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color.Black
                        )

                        // =====================================
                        // ESTADO
                        // =====================================

                        Text(
                            text =
                                "ESTADO: ${orden.estado}",

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color.Black
                        )

                        // =====================================
                        // AVANCE
                        // =====================================

                        Text(
                            text =
                                "AVANCE: ${orden.porcentajeAvance}%",

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color.Black
                        )

                        // =====================================
                        // FECHA DE ENTREGA
                        // =====================================

                        Text(
                            text =
                                "FECHA DE ENTREGA: ${
                                    if (orden.fechaEntrega.isBlank())
                                        "SIN REGISTRAR"
                                    else
                                        orden.fechaEntrega
                                }",

                            fontSize =
                                17.sp,

                            fontWeight =
                                FontWeight.Bold,

                            color =
                                Color.Black
                        )
                    }
                }

                // =========================================
                // SEPARACIÓN ENTRE TARJETAS
                // =========================================

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )
            }
        }

        // =========================================
        // REGRESAR
        // =========================================

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        BotonModulo3D(
            texto = "REGRESAR",
            colorClaro = Colores.RegresarClaro,
            colorMedio = Colores.RegresarMedio,
            colorOscuro = Colores.RegresarOscuro,
            onClick = onRegresar,
            colorTexto = Color.White
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )
    }


}
