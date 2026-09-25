package com.example.bitacoraautomotriz.ui.autos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.data.Cliente

@Composable
fun SelectorClientesDialog(
    clientes: List<Cliente>,
    onClienteSeleccionado: (Cliente) -> Unit,
    onCerrar: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onCerrar,

        title = {
            Text(
                text = "SELECCIONAR CLIENTE",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            if (clientes.isEmpty()) {

                Text(
                    text = "NO HAY CLIENTES REGISTRADOS",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),

                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = clientes,
                        key = { it.id }
                    ) { cliente ->

                        Card(
                            modifier = Modifier.fillMaxWidth(),

                            shape = RoundedCornerShape(12.dp),

                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),

                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            ),

                            onClick = {
                                onClienteSeleccionado(cliente)
                            }
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {

                                Text(
                                    text = cliente.nombre,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Spacer(
                                    modifier = Modifier.height(6.dp)
                                )

                                Text(
                                    text = "Teléfono: ${cliente.telefono}",
                                    fontSize = 16.sp,
                                    color = Color.DarkGray
                                )

                                Spacer(
                                    modifier = Modifier.height(4.dp)
                                )

                                Text(
                                    text = "Correo: ${cliente.correo}",
                                    fontSize = 16.sp,
                                    color = Color.DarkGray
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(
                                    text = "TOCAR PARA SELECCIONAR",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF006494)
                                )
                            }
                        }
                    }
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = onCerrar
            ) {

                Text(
                    text = "CERRAR",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
