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
import com.example.bitacoraautomotriz.data.Cliente

@Composable
fun TarjetaCliente(
    cliente: Cliente,
    onEditar: (Cliente) -> Unit,
    onEliminar: (Cliente) -> Unit,
) {
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // NOMBRE (Etiqueta + Valor en filas separadas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Color(0xFF424242))) {
                            append("NOMBRE:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))) {
                            append(cliente.nombre.uppercase())
                        }
                    },
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // TELÉFONO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Color(0xFF424242))) {
                            append("TELÉFONO:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))) {
                            append(cliente.telefono)
                        }
                    },
                    fontSize = 16.sp
                )
            }

            // CORREO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Color(0xFF424242))) {
                            append("CORREO:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))) {
                            append(cliente.correo)
                        }
                    },
                    fontSize = 14.sp
                )
            }

            // DIRECCIÓN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Color(0xFF424242))) {
                            append("DIRECCIÓN:")
                        }
                    },
                    fontSize = 14.sp
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))) {
                            append(cliente.direccion.uppercase())
                        }
                    },
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTONES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onEditar(cliente) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00AEEF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    Text("EDITAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { mostrarConfirmacionEliminar = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    Text("ELIMINAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEliminar = false },
            title = {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Black, color = Color(0xFFC62828))) {
                            append("ELIMINAR CLIENTE")
                        }
                    },
                    fontSize = 22.sp
                )
            },
            text = {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF424242))) {
                            append("¿Está seguro de eliminar al cliente \"")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))) {
                            append(cliente.nombre)
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF424242))) {
                            append("\"?\n\nEsta acción no se puede deshacer.")
                        }
                    },
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacionEliminar = false
                        onEliminar(cliente)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828),
                        contentColor = Color.White
                    )
                ) {
                    Text("ELIMINAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { mostrarConfirmacionEliminar = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF607D8B),
                        contentColor = Color.White
                    )
                ) {
                    Text("CANCELAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
