package com.example.bitacoraautomotriz.ui.areacliente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bitacoraautomotriz.repository.AlertaMantenimiento
import com.example.bitacoraautomotriz.repository.FirebaseSyncManager
import com.example.bitacoraautomotriz.ui.componentes.BotonModulo3D
import com.example.bitacoraautomotriz.ui.theme.Colores

@Composable
fun AreaClienteScreen(
    onDatosClienteAutos: () -> Unit,
    onDatosFacturacion: () -> Unit,
    onEstadoReparacion: () -> Unit,
    onVerReporteCliente: () -> Unit = {},
    onCentroNotificacionesClick: () -> Unit = {},
    onCitaEntrega: () -> Unit,
    onHistorial: () -> Unit,
    onRegresar: () -> Unit
) {
    val context = LocalContext.current
    val esAppCliente = remember { context.packageName.lowercase().contains("cliente") }

    var alertasEnTiempoReal by remember { mutableStateOf<List<AlertaMantenimiento>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            FirebaseSyncManager.escucharAlertasEnTiempoReal { lista ->
                alertasEnTiempoReal = lista
            }
        } catch (_: Exception) {}
    }

    val ultimaAlerta = alertasEnTiempoReal.lastOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Colores.FondoPantalla)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 84.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // TÍTULO DE LA PANTALLA
            Text(
                text = "ÁREA DEL CLIENTE",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.TituloPrincipal
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Consulta y seguimiento de su vehículo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Colores.EtiquetaCampo,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // TARJETA M3 DE ALERTA DE MANTENIMIENTO RECIBIDA EN TIEMPO REAL DESDE EL TALLER
            if (ultimaAlerta != null && ultimaAlerta.mensaje.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB51F1F)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "🔔 ALERTA DE REVISIÓN Y MANTENIMIENTO",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ultimaAlerta.mensaje,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (esAppCliente) {
                            Spacer(modifier = Modifier.height(14.dp))
                            BotonModulo3D(
                                texto = "📅 AGENDAR CITA DE INGRESO",
                                icono = "📅",
                                colorClaro = Color(0xFFB9F6CA),
                                colorMedio = Color(0xFF00C853),
                                colorOscuro = Color(0xFF00695C),
                                colorTexto = Color.Black,
                                onClick = onCitaEntrega,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                tamanioTexto = 14
                            )
                        }
                    }
                }
            }

            // BOTÓN VERDE CENTRO DE NOTIFICACIONES Y AVISOS AL CLIENTE (SÓLO VISIBLE EN APP TALLER)
            if (!esAppCliente) {
                BotonModulo3D(
                    texto = "CENTRO DE AVISOS Y NOTIFICACIONES AL CLIENTE",
                    icono = "📱",
                    colorClaro = Color(0xFFB9F6CA),
                    colorMedio = Color(0xFF00C853),
                    colorOscuro = Color(0xFF00695C),
                    colorTexto = Color.Black,
                    onClick = onCentroNotificacionesClick,
                    modifier = Modifier.fillMaxWidth().height(62.dp),
                    tamanioTexto = 14
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 1. MIS AUTOS
            BotonModulo3D(
                texto = "MIS AUTOS / AGREGAR MI AUTO",
                icono = "🚗",
                colorClaro = Color(0xFFD7B899),
                colorMedio = Color(0xFF9B6B43),
                colorOscuro = Color(0xFF5D3A1A),
                onClick = onDatosClienteAutos,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                tamanioTexto = 16,
                colorTexto = Color.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. MIS DATOS DE FACTURACIÓN (SÓLO PARA LA APP CLIENTE)
            if (esAppCliente) {
                BotonModulo3D(
                    texto = "MIS DATOS DE FACTURACIÓN",
                    icono = "📄",
                    colorClaro = Color(0xFFD7B899),
                    colorMedio = Color(0xFF9B6B43),
                    colorOscuro = Color(0xFF5D3A1A),
                    onClick = onDatosFacturacion,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    tamanioTexto = 16,
                    colorTexto = Color.Black
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 3. ESTADO DE MI REPARACIÓN
            BotonModulo3D(
                texto = "ESTADO DE MI REPARACIÓN",
                icono = "🔧",
                colorClaro = Color(0xFFD7B899),
                colorMedio = Color(0xFF9B6B43),
                colorOscuro = Color(0xFF5D3A1A),
                onClick = onEstadoReparacion,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                tamanioTexto = 16,
                colorTexto = Color.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. REPORTE DE COTIZACIÓN Y SERVICIO
            BotonModulo3D(
                texto = "REPORTE DE COTIZACIÓN Y SERVICIO",
                icono = "📋",
                colorClaro = Color(0xFFD7B899),
                colorMedio = Color(0xFF9B6B43),
                colorOscuro = Color(0xFF5D3A1A),
                onClick = onVerReporteCliente,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                tamanioTexto = 15,
                colorTexto = Color.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. CITA DE INGRESO AL TALLER (SÓLO PARA LA APP CLIENTE)
            if (esAppCliente) {
                BotonModulo3D(
                    texto = "CITA DE INGRESO AL TALLER",
                    icono = "📅",
                    colorClaro = Color(0xFFD7B899),
                    colorMedio = Color(0xFF9B6B43),
                    colorOscuro = Color(0xFF5D3A1A),
                    onClick = onCitaEntrega,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    tamanioTexto = 16,
                    colorTexto = Color.Black
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 6. HISTORIAL
            BotonModulo3D(
                texto = "HISTORIAL",
                icono = "📜",
                colorClaro = Color(0xFFD7B899),
                colorMedio = Color(0xFF9B6B43),
                colorOscuro = Color(0xFF5D3A1A),
                onClick = onHistorial,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                tamanioTexto = 16,
                colorTexto = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // BOTÓN SALIR DE LA APP FIJO E INMÓVIL AL FONDO DE LA PANTALLA
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Colores.FondoPantalla
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                BotonModulo3D(
                    texto = "SALIR DE LA APP",
                    icono = "🚪",
                    colorClaro = Colores.RegresarClaro,
                    colorMedio = Colores.RegresarMedio,
                    colorOscuro = Colores.RegresarOscuro,
                    onClick = onRegresar,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    tamanioTexto = 16,
                    colorTexto = Color.White
                )
            }
        }
    }
}
