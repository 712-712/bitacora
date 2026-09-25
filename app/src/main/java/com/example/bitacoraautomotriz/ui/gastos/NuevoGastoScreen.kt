package com.example.bitacoraautomotriz.ui.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import com.example.bitacoraautomotriz.data.Gasto
import com.example.bitacoraautomotriz.repository.GastoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import kotlinx.coroutines.launch

@Composable
fun NuevoGastoScreen(
    onGuardar: () -> Unit,
    onRegresar: () -> Unit
) {

    var concepto by remember {
        mutableStateOf("")
    }

    var categoria by remember {
        mutableStateOf("")
    }

    var fecha by remember {
        mutableStateOf("")
    }

    var monto by remember {
        mutableStateOf("")
    }

    var descripcion by remember {
        mutableStateOf("")
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "NUEVO GASTO",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // =========================================
        // CONCEPTO
        // =========================================

        OutlinedTextField(
            value = concepto,
            onValueChange = {
                concepto = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Concepto",
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
                focusedIndicatorColor = Color(0xFFFF4141),
                unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // =========================================
        // CATEGORÍA
        // =========================================

        OutlinedTextField(
            value = categoria,
            onValueChange = {
                categoria = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Categoría",
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
                focusedIndicatorColor = Color(0xFFFF4141),
                unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // =========================================
        // FECHA
        // =========================================

        OutlinedTextField(
            value = fecha,
            onValueChange = {
                fecha = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Fecha",
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
                focusedIndicatorColor = Color(0xFFFF4141),
                unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // =========================================
// MONTO
// =========================================

        OutlinedTextField(
            value = monto,
            onValueChange = {
                monto = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Monto",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            prefix = {
                Text(
                    text = "$ ",
                    color = Color.Black,
                    fontSize = 18.sp,
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
                focusedIndicatorColor = Color(0xFFFF4141),
                unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // =========================================
        // DESCRIPCIÓN
        // =========================================

        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Descripción",
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
                focusedIndicatorColor = Color(0xFFFF4141),
                unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // =========================================
        // GUARDAR GASTO
        // =========================================

        BotonModulo3D(
            texto = "GUARDAR GASTO",
            colorClaro = Color(0xFFFF9999),
            colorMedio = Color(0xFFFF4141),
            colorOscuro = Color(0xFFB51F1F),
            onClick = {

                val montoNumero =
                    monto.toDoubleOrNull() ?: 0.0

                val gasto = Gasto(
                    concepto = concepto,
                    categoria = categoria,
                    fecha = fecha,
                    monto = montoNumero,
                    descripcion = descripcion
                )

                scope.launch {

                    GastoRepository.guardarGasto(gasto)

                    onGuardar()
                }
            }
        )

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
