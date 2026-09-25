package com.example.bitacoraautomotriz.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun BotonModulo3D(
    texto: String,
    colorClaro: Color = Colores.TituloPrincipal,
    colorMedio: Color = Colores.BordeBoton,
    colorOscuro: Color = Colores.FondoSecundario,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().height(60.dp),
    tamanioTexto: Int = 20,
    icono: String? = null,
    colorTexto: Color = Color.Black // ✅ NUEVO: Por defecto es negro, pero se puede cambiar a blanco
) {
    fun atenuar(color: Color): Color {
        return Color(
            red = (color.red * 0.50f + 0.50f).coerceAtMost(1f),
            green = (color.green * 0.50f + 0.50f).coerceAtMost(1f),
            blue = (color.blue * 0.50f + 0.50f).coerceAtMost(1f),
            alpha = color.alpha
        )
    }

    fun atenuarSombra(color: Color): Color {
        return Color(
            red = (color.red * 0.70f + 0.30f).coerceAtMost(1f),
            green = (color.green * 0.70f + 0.30f).coerceAtMost(1f),
            blue = (color.blue * 0.70f + 0.30f).coerceAtMost(1f),
            alpha = color.alpha
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .clip(RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .background(atenuarSombra(colorOscuro), RoundedCornerShape(14.dp))
                .align(Alignment.BottomCenter)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(atenuar(colorClaro), atenuar(colorMedio)),
                        startY = 0f,
                        endY = 60f
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icono != null) {
                    Text(
                        text = icono,
                        fontSize = (tamanioTexto + 4).sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = texto,
                    fontSize = tamanioTexto.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto // ✅ AQUÍ USAMOS EL NUEVO PARÁMETRO
                )
            }
        }
    }
}
