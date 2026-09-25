package com.example.bitacoraautomotriz.ui.theme

import androidx.compose.ui.graphics.Color

object Colores {
    // ✅ Fondos de Pantalla
    val FondoPantalla = Color(0xFF050A18) // Casi negro
    val FondoSecundario = Color(0xFF1A2744) // Azul oscuro para campos de texto

    // ✅ Títulos y Textos Globales (Azul unificado)
    val TituloPrincipal = Color(0xFF90CAF9)
    val TextoGlobal = Color(0xFF90CAF9)

    // ✅ TARJETAS: Verde Esmeralda Oscuro (El que elegiste)
    val FondoTarjeta = Color(0xFF006C4C)

    // ✅ Textos DENTRO de las tarjetas y formularios (REVERTIDO A BLANCO)
    // Este es el cambio clave: al ser blanco, se leerá perfecto sobre el fondo oscuro.
    val TextoTarjeta = Color.White

    // ✅ Etiquetas y Botones
    val EtiquetaCampo = Color(0xFF7BA7C9)
    val TextoBoton = Color(0xFF9FE0E5)
    val BordeBoton = Color(0xFF7DD4D9)
}
