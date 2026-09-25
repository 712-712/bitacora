package com.example.bitacoraautomotriz.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun TarjetaAuto(
    auto: Auto,
    onEditar: (Auto) -> Unit,
    onEliminar: (Auto) -> Unit,
) {
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Colores.FondoTarjeta
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // VEHÍCULO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Colores.EtiquetaCampo)) {
                            append("VEHÍCULO:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoTarjeta)) {
                            append("${auto.marca} ${auto.modelo}")
                        }
                    },
                    fontSize = 18.sp
                )
            }

            // CLIENTE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Colores.EtiquetaCampo)) {
                            append("CLIENTE:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoTarjeta)) {
                            append(auto.cliente.uppercase())
                        }
                    },
                    fontSize = 16.sp
                )
            }

            // AÑO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Colores.EtiquetaCampo)) {
                            append("AÑO:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoTarjeta)) {
                            append(auto.anio.toString())
                        }
                    },
                    fontSize = 16.sp
                )
            }

            // PLACA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Colores.EtiquetaCampo)) {
                            append("PLACA:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoTarjeta)) {
                            append(auto.placa.uppercase())
                        }
                    },
                    fontSize = 16.sp
                )
            }

            // VIN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Colores.EtiquetaCampo)) {
                            append("VIN:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoTarjeta)) {
                            append(auto.vin.uppercase())
                        }
                    },
                    fontSize = 14.sp
                )
            }

            // COLOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Colores.EtiquetaCampo)) {
                            append("COLOR:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoTarjeta)) {
                            append(auto.color.uppercase())
                        }
                    },
                    fontSize = 16.sp
                )
            }

            // KILOMETRAJE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Colores.EtiquetaCampo)) {
                            append("KILOMETRAJE:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoTarjeta)) {
                            append("${auto.kilometraje} km")
                        }
                    },
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BOTONES
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BotonModulo3D(
                    texto = "EDITAR AUTO",
                    icono = "✏️",
                    onClick = { onEditar(auto) },
                    modifier = Modifier.fillMaxWidth()
                )
                BotonModulo3D(
                    texto = "ELIMINAR AUTO",
                    icono = "🗑️",
                    onClick = { mostrarConfirmacionEliminar = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEliminar = false },
            title = {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Colores.TextoBoton)) {
                            append("ELIMINAR AUTO")
                        }
                    },
                    fontSize = 22.sp
                )
            },
            text = {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoTarjeta)) {
                            append("¿Está seguro de eliminar el auto ")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoBoton)) {
                            append("${auto.marca} ${auto.modelo}")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoTarjeta)) {
                            append(" (placa ")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoBoton)) {
                            append(auto.placa.uppercase())
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Colores.TextoTarjeta)) {
                            append(")?\n\nEsta acción no se puede deshacer.")
                        }
                    },
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacionEliminar = false
                        onEliminar(auto)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colores.TextoBoton,
                        contentColor = Colores.FondoPantalla
                    )
                ) {
                    Text("ELIMINAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { mostrarConfirmacionEliminar = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colores.EtiquetaCampo,
                        contentColor = Colores.FondoPantalla
                    )
                ) {
                    Text("CANCELAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
