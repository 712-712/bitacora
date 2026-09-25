package com.example.bitacoraautomotriz.ui.escaner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.repository.AutoRepository

@Composable
fun VerAutoScreen(vin: String) {
    var auto by remember { mutableStateOf<Auto?>(null) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(vin) {
        auto = AutoRepository.obtenerAutoPorVin(vin)
        cargando = false
    }

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp)) {
            if (cargando) {
                CircularProgressIndicator()
            } else if (auto == null) {
                Text("No se encontró el auto.")
            } else {
                val a = auto!!
                Campo("Cliente", a.cliente)
                Campo("Marca", a.marca)
                Campo("Modelo", a.modelo)
                Campo("Año", a.anio.toString())
                Campo("Placa", a.placa)
                Campo("VIN", a.vin)
                Campo("Color", a.color)
                Campo("Kilometraje", a.kilometraje.toString())
            }
        }
    }
}

@Composable
private fun Campo(etiqueta: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(etiqueta)
        Text(valor)
    }
}
