package com.example.bitacoraautomotriz.ui.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun AcercaDeScreen(
    onRegresar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
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
        Text(
            text = "ACERCA DE",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Colores.FondoTarjeta,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "BITÁCORA AUTOMOTRIZ",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TextoTarjeta
            )

            Text(
                text = "Versión 1.0",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TextoTarjeta
            )

            Text(
                text = "Aplicación para la administración de talleres automotrices.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TextoTarjeta
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
