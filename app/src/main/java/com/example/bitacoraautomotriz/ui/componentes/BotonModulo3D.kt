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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.ui.theme.Colores

/**
 * Componente unificado para botones de la aplicación.
 * Diseño plano, accesible y elegante adaptado a la paleta de colores por módulo de AGENTS.md.
 */
@Composable
fun BotonModulo3D(
    texto: String,
    modifier: Modifier = Modifier,
    colorClaro: Color = Colores.TituloPrincipal,
    colorMedio: Color = Colores.BordeBoton,
    colorOscuro: Color = Colores.FondoSecundario,
    onClick: () -> Unit,
    tamanioTexto: Int = 18,
    icono: String? = null,
    colorTexto: Color = Color.Black
) {
    val modifierAplicado = if (modifier == Modifier) Modifier.fillMaxWidth().height(58.dp) else modifier

    fun atenuar(color: Color): Color {
        return Color(
            red = ((color.red * 0.50f) + 0.50f).coerceAtMost(1f),
            green = ((color.green * 0.50f) + 0.50f).coerceAtMost(1f),
            blue = ((color.blue * 0.50f) + 0.50f).coerceAtMost(1f),
            alpha = color.alpha
        )
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifierAplicado
            .clip(RoundedCornerShape(14.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(atenuar(colorClaro), atenuar(colorMedio), atenuar(colorOscuro))
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (!icono.isNullOrEmpty()) {
                Text(
                    text = icono,
                    fontSize = (tamanioTexto + 1).sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Text(
                text = texto,
                fontSize = tamanioTexto.sp,
                fontWeight = FontWeight.Bold,
                color = colorTexto,
                textAlign = TextAlign.Center,
                lineHeight = (tamanioTexto + 2).sp,
                maxLines = if (texto.contains("\n")) 2 else 1,
                softWrap = texto.contains("\n")
            )
        }
    }
}
