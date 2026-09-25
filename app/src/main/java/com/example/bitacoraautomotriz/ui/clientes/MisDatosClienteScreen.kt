package com.example.bitacoraautomotriz.ui.clientes

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores
import kotlinx.coroutines.launch

@Composable
fun MisDatosClienteScreen(
    onRegresar: () -> Unit
) {
    var telefono by remember { mutableStateOf("") }
    var cliente by remember { mutableStateOf<Cliente?>(null) }
    var mensaje by remember { mutableStateOf("") }
    var buscando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val teclado = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 42.dp,
                bottom = 28.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // TÍTULO
        Text(
            text = "DATOS CLIENTE Y AUTOS",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.TituloPrincipal
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Consulte sus datos registrados",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Colores.EtiquetaCampo
        )

        Spacer(modifier = Modifier.height(30.dp))

        // CAMPO TELÉFONO
        OutlinedTextField(
            value = telefono,
            onValueChange = {
                telefono = it
                mensaje = ""
                cliente = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            label = {
                Text(
                    text = "TELÉFONO",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Colores.EtiquetaCampo
                )
            },
            placeholder = {
                Text(
                    text = "Ingrese su número de teléfono",
                    fontSize = 17.sp,
                    color = Colores.EtiquetaCampo.copy(alpha = 0.6f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Colores.FondoSecundario,
                unfocusedContainerColor = Colores.FondoSecundario,
                disabledContainerColor = Colores.FondoSecundario,
                focusedTextColor = Colores.TextoTarjeta,
                unfocusedTextColor = Colores.TextoTarjeta,
                focusedPlaceholderColor = Colores.EtiquetaCampo.copy(alpha = 0.6f),
                unfocusedPlaceholderColor = Colores.EtiquetaCampo.copy(alpha = 0.6f),
                focusedBorderColor = Colores.BordeBoton,
                unfocusedBorderColor = Colores.BordeBoton.copy(alpha = 0.5f),
                focusedLabelColor = Colores.EtiquetaCampo,
                unfocusedLabelColor = Colores.EtiquetaCampo.copy(alpha = 0.7f),
                cursorColor = Colores.TextoBoton
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // BUSCAR
        BotonModulo3D(
            texto = if (buscando) "BUSCANDO..." else "BUSCAR MIS DATOS",
            icono = "🔍",
            onClick = {
                teclado?.hide()

                if (telefono.isBlank()) {
                    mensaje = "INGRESE SU NÚMERO DE TELÉFONO"
                } else {
                    buscando = true
                    mensaje = ""
                    cliente = null

                    scope.launch {
                        val resultado = ClienteRepository.obtenerClientePorTelefono(telefono.trim())
                        cliente = resultado

                        mensaje = if (resultado == null) {
                            "NO SE ENCONTRÓ UN CLIENTE CON ESE TELÉFONO"
                        } else {
                            ""
                        }

                        buscando = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(22.dp))

        // MENSAJE
        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = Colores.TextoTarjeta,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(18.dp))
        }

        // DATOS DEL CLIENTE
        cliente?.let { datos ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Colores.FondoTarjeta
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "DATOS DEL CLIENTE",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // NOMBRE
                    Text(
                        text = "NOMBRE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo
                    )
                    Text(
                        text = datos.nombre,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // TELÉFONO
                    Text(
                        text = "TELÉFONO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo
                    )
                    Text(
                        text = datos.telefono,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // CORREO
                    Text(
                        text = "CORREO ELECTRÓNICO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo
                    )
                    Text(
                        text = datos.correo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // DIRECCIÓN
                    Text(
                        text = "DIRECCIÓN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.EtiquetaCampo
                    )
                    Text(
                        text = datos.direccion,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Colores.TextoTarjeta
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // REGRESAR
        BotonModulo3D(
            texto = "REGRESAR",
            icono = "🔙",
            onClick = onRegresar,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}
