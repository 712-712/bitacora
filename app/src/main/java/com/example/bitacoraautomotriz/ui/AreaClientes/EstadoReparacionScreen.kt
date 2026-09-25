package com.example.bitacoraautomotriz.ui.areacliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EstadoReparacionScreen(
    onRegresar: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF001B44))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "ESTADO DE REPARACIÓN",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Consulta el avance de la reparación de su vehículo",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE3F2FD)
        )

        Spacer(
            modifier = Modifier.height(35.dp)
        )

        Text(
            text = "AVANCE DE LA REPARACIÓN",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        LinearProgressIndicator(
            progress = { 0.50f }
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        Text(
            text = "50% COMPLETADO",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        Text(
            text = "Reparación en proceso",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(
            modifier = Modifier.height(35.dp)
        )

        Button(
            onClick = onRegresar
        ) {
            Text(
                text = "REGRESAR",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}