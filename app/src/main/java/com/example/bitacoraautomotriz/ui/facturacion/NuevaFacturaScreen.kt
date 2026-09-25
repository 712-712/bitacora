package com.example.bitacoraautomotriz.ui.facturacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.bitacoraautomotriz.data.Factura
import com.example.bitacoraautomotriz.repository.FacturaRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import kotlinx.coroutines.launch

@Composable
fun NuevaFacturaScreen(
    onGuardar: () -> Unit,
    onRegresar: () -> Unit
) {

    var numero by remember {
        mutableStateOf("")
    }

    var cliente by remember {
        mutableStateOf("")
    }

    var fecha by remember {
        mutableStateOf("")
    }

    var subtotal by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    // =========================================
    // CÁLCULO AUTOMÁTICO DEL IVA Y TOTAL
    // =========================================

    val subtotalNumero = subtotal.toDoubleOrNull() ?: 0.0

    val ivaNumero = subtotalNumero * 0.16

    val totalNumero = subtotalNumero + ivaNumero

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

        // =========================================
        // TÍTULO
        // =========================================

        Text(
            text = "NUEVA FACTURA",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // =========================================
        // NÚMERO DE FACTURA
        // =========================================

        OutlinedTextField(
            value = numero,
            onValueChange = {
                numero = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Número de factura",
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
        // CLIENTE
        // =========================================

        OutlinedTextField(
            value = cliente,
            onValueChange = {
                cliente = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Cliente",
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
                focusedIndicatorColor = Color(0xFF00AEEF),
                unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // =========================================
        // SUBTOTAL
        // =========================================

        OutlinedTextField(
            value = subtotal,
            onValueChange = {
                subtotal = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Subtotal",
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
                focusedIndicatorColor = Color(0xFF00AEEF),
                unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // =========================================
        // IVA AUTOMÁTICO 16%
        // =========================================

        OutlinedTextField(
            value = "%.2f".format(ivaNumero),
            onValueChange = {},
            readOnly = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "IVA (16%)",
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
                focusedIndicatorColor = Color(0xFF00AEEF),
                unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // =========================================
        // TOTAL AUTOMÁTICO
        // =========================================

        OutlinedTextField(
            value = "%.2f".format(totalNumero),
            onValueChange = {},
            readOnly = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Total",
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
                focusedIndicatorColor = Color(0xFF00AEEF),
                unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // =========================================
        // GUARDAR
        // =========================================

        BotonModulo3D(
            texto = "GUARDAR",
            colorClaro = Color(0xFF8FFFFF),
            colorMedio = Color(0xFF00DDEB),
            colorOscuro = Color(0xFF007F88),
            onClick = {

                val factura = Factura(
                    numero = numero,
                    cliente = cliente,
                    fecha = fecha,
                    subtotal = subtotalNumero,
                    iva = ivaNumero,
                    total = totalNumero
                )

                scope.launch {

                    FacturaRepository.guardarFactura(factura)

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
