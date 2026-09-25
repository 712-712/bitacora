package com.example.bitacoraautomotriz.ui.autos

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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.componentes.TarjetaAuto
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

@Composable
fun BusquedaAutoScreen(
    onEditarAuto: (Int) -> Unit,
    onRegresar: () -> Unit
) {
    var textoBusqueda by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf(emptyList<Auto>()) }
    var busquedaRealizada by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val teclado = LocalSoftwareKeyboardController.current

    fun repetirBusqueda() {
        scope.launch {
            val texto = textoBusqueda.trim()
            resultados = if (texto.isEmpty()) {
                AutoRepository.obtenerAutos()
            } else {
                AutoRepository.buscarAutos(texto)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // TÍTULO PRINCIPAL
        Text(
            text = "BUSCAR AUTO",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ETIQUETA FIJA PARA AUTOS
        Text(
            text = "PLACA, MARCA, MODELO O VIN:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo,
            modifier = Modifier.align(Alignment.Start)
        )

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = {
                textoBusqueda = it
                busquedaRealizada = false
            },
            textStyle = TextStyle(
                color = Colores.TextoTarjeta,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Colores.FondoSecundario,
                unfocusedContainerColor = Colores.FondoSecundario,
                disabledContainerColor = Colores.FondoSecundario,
                focusedTextColor = Colores.TextoTarjeta,
                unfocusedTextColor = Colores.TextoTarjeta,
                disabledTextColor = Colores.TextoTarjeta,
                focusedIndicatorColor = Colores.BordeBoton,
                unfocusedIndicatorColor = Colores.BordeBoton.copy(alpha = 0.5f),
                cursorColor = Colores.TextoBoton
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BOTÓN BUSCAR
        BotonModulo3D(
            texto = "BUSCAR",
            icono = "🔍",
            onClick = {
                teclado?.hide()
                val texto = textoBusqueda.trim()
                scope.launch {
                    resultados = if (texto.isEmpty()) {
                        AutoRepository.obtenerAutos()
                    } else {
                        AutoRepository.buscarAutos(texto)
                    }
                    busquedaRealizada = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // RESULTADOS
        if (busquedaRealizada) {
            Spacer(modifier = Modifier.height(24.dp))

            if (resultados.isEmpty()) {
                Text(
                    text = "NO SE ENCONTRARON AUTOS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TextoBoton
                )
            } else {
                Text(
                    text = "AUTOS ENCONTRADOS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.TituloPrincipal
                )
                Spacer(modifier = Modifier.height(16.dp))

                resultados.forEach { auto ->
                    TarjetaAuto(
                        auto = auto,
                        onEditar = { onEditarAuto(it.id) },
                        onEliminar = { autoAEliminar ->
                            scope.launch {
                                AutoRepository.eliminarAuto(autoAEliminar)
                                repetirBusqueda()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // REGRESAR
        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
