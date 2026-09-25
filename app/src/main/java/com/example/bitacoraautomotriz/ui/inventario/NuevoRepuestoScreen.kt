package com.example.bitacoraautomotriz.ui.inventario

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
import com.example.bitacoraautomotriz.data.Repuesto
import com.example.bitacoraautomotriz.repository.RepuestoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import kotlinx.coroutines.launch

@Composable
fun NuevoRepuestoScreen(
    onGuardar: () -> Unit,
    onRegresar: () -> Unit
) {

    var nombre by remember {
        mutableStateOf("")
    }

    var marca by remember {
        mutableStateOf("")
    }

    var categoria by remember {
        mutableStateOf("")
    }

    var cantidad by remember {
        mutableStateOf("")
    }

    var precio by remember {
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
        verticalArrangement = Arrangement.Top
    ) {

        // =========================================
        // TÍTULO
        // =========================================

        Text(
            text = "NUEVO REPUESTO",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // =========================================
        // NOMBRE
        // =========================================

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Nombre",
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

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // =========================================
        // MARCA
        // =========================================

        OutlinedTextField(
            value = marca,
            onValueChange = {
                marca = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Marca",
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

        Spacer(
            modifier = Modifier.height(12.dp)
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
                focusedIndicatorColor = Color(0xFF00AEEF),
                unfocusedIndicatorColor = Color(0xFF607D8B),
                cursorColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // =========================================
        // CANTIDAD
        // =========================================

        OutlinedTextField(
            value = cantidad,
            onValueChange = {
                cantidad = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Cantidad",
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

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // =========================================
// PRECIO
// =========================================

        OutlinedTextField(
            value = precio,
            onValueChange = {
                precio = it
            },
            textStyle = TextStyle(
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            label = {
                Text(
                    text = "Precio",
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

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // =========================================
        // GUARDAR REPUESTO
        // =========================================

        BotonModulo3D(
            texto = "GUARDAR REPUESTO",
            colorClaro = Color(0xFFF3A7FF),
            colorMedio = Color(0xFFD83CFF),
            colorOscuro = Color(0xFF7B1599),
            onClick = {

                val cantidadNumero =
                    cantidad.toIntOrNull() ?: 0

                val precioNumero =
                    precio.toDoubleOrNull() ?: 0.0

                val repuesto = Repuesto(
                    nombre = nombre,
                    marca = marca,
                    categoria = categoria,
                    cantidad = cantidadNumero,
                    precio = precioNumero
                )

                scope.launch {
                    RepuestoRepository.guardarRepuesto(repuesto)
                    onGuardar()
                }
            }
        )

        Spacer(
            modifier = Modifier.height(16.dp)
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
