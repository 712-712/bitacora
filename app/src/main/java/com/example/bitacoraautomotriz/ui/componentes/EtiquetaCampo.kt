package com.example.bitacoraautomotriz.ui.componentes

// Etiqueta reutilizable para el parámetro "label" de OutlinedTextField.
//
// Por qué existe: cuando un OutlinedTextField flota su etiqueta sobre el
// borde superior, Compose deja ver lo que hay DETRÁS de la etiqueta (el
// fondo de la pantalla), no el fondo blanco del campo. Como esta app usa
// un fondo azul marino oscuro (casi negro) detrás de los formularios,
// eso se veía como un "recuadro negro" pegado a cada etiqueta.
//
// Al envolver el texto en un Box con fondo blanco, forzamos a que
// siempre se vea blanco detrás de la etiqueta, sin importar qué color
// tenga la pantalla por detrás.

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EtiquetaCampo(texto: String) {
    Box(
        modifier = Modifier
            .background(Color.White)
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = texto,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
