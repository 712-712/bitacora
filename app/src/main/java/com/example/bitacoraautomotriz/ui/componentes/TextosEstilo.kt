package com.example.bitacoraautomotriz.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

// ==========================================
// COLORES ESTÁNDAR DEL PROYECTO
// ==========================================
object ColoresApp {
    val GrisEtiqueta = Color(0xFF424242)   // Gris oscuro para etiquetas
    val RojoValor = Color(0xFFD32F2F)      // Rojo intenso para valores
    val AzulTitulo = Color(0xFF90CAF9)     // Azul claro para títulos
    val Blanco = Color.White
    val RojoError = Color(0xFFFF5252)
}

// ==========================================
// COMPONENTE 1: Fila con ETIQUETA a la izquierda y VALOR a la derecha
// Ejemplo: PLACA:          712 ZYF
// ==========================================
@Composable
fun FilaEtiquetaValor(
    etiqueta: String,
    valor: String,
    tamanoEtiqueta: Float = 14f,
    tamanoValor: Float = 16f,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = ColoresApp.GrisEtiqueta)) {
                    append(etiqueta)
                }
            },
            fontSize = tamanoEtiqueta.sp
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = ColoresApp.RojoValor)) {
                    append(valor)
                }
            },
            fontSize = tamanoValor.sp
        )
    }
}

// ==========================================
// COMPONENTE 2: Texto en línea con ETIQUETA + VALOR
// Ejemplo: DETALLES: VERDE - VIN: 12345QWERT
// ==========================================
@Composable
fun TextoEtiquetaValor(
    etiqueta: String,
    valor: String,
    tamanoTexto: Float = 14f,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = ColoresApp.GrisEtiqueta)) {
                append(etiqueta)
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = ColoresApp.RojoValor)) {
                append(valor)
            }
        },
        fontSize = tamanoTexto.sp,
        modifier = modifier
    )
}

// ==========================================
// COMPONENTE 3: Título principal (ej: "AUTOS DE:")
// ==========================================
@Composable
fun TituloPrincipal(
    texto: String,
    color: Color = ColoresApp.AzulTitulo,
    tamano: Float = 18f,
    modifier: Modifier = Modifier
) {
    Text(
        text = texto,
        fontSize = tamano.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

// ==========================================
// COMPONENTE 4: Subtítulo grande (ej: nombre del cliente)
// ==========================================
@Composable
fun SubtituloGrande(
    texto: String,
    color: Color = ColoresApp.Blanco,
    tamano: Float = 26f,
    modifier: Modifier = Modifier
) {
    Text(
        text = texto,
        fontSize = tamano.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}
