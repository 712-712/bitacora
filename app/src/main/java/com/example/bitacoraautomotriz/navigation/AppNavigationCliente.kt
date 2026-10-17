package com.example.bitacoraautomotriz.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bitacoraautomotriz.ui.clientes.CitaEntregaScreen
import com.example.bitacoraautomotriz.ui.clientes.EstadoReparacionScreen
import com.example.bitacoraautomotriz.ui.clientes.HistorialServiciosScreen
import com.example.bitacoraautomotriz.ui.clientes.MisDatosClienteScreen
import com.example.bitacoraautomotriz.ui.clientes.MisDatosFacturacionScreen
import com.example.bitacoraautomotriz.ui.clientes.PortalClienteScreen
import com.example.bitacoraautomotriz.ui.clientes.VerReporteClienteScreen
import com.example.bitacoraautomotriz.ui.ordenes.VerRecepcionesScreen
import com.example.bitacoraautomotriz.ui.welcome.WelcomeScreen

@Composable
fun AppNavigationCliente() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as? Activity

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onTallerClick = {},
                onClienteClick = { navController.navigate("area_cliente") }
            )
        }

        composable("area_cliente") {
            PortalClienteScreen(
                onDatosClienteAutos = { navController.navigate("mis_datos") },
                onDatosFacturacion = { navController.navigate("mis_datos_facturacion") },
                onEstadoReparacion = { navController.navigate("estado_reparacion") },
                onVerReporteCliente = { navController.navigate("ver_reporte_cliente") },
                onRecepcionVehiculoClick = { navController.navigate("recepcion_vehiculo") },
                onCitaEntrega = { navController.navigate("cita_entrega") },
                onHistorial = { navController.navigate("historial") },
                onRegresar = { activity?.finishAffinity() }
            )
        }

        composable("mis_datos") { MisDatosClienteScreen(onRegresar = { navController.popBackStack() }) }
        composable("mis_datos_facturacion") { MisDatosFacturacionScreen(onRegresar = { navController.popBackStack() }) }
        composable("cita_entrega") { CitaEntregaScreen(onRegresar = { navController.popBackStack() }) }
        composable("estado_reparacion") { EstadoReparacionScreen(onRegresar = { navController.popBackStack() }) }
        composable("ver_reporte_cliente") { VerReporteClienteScreen(esModoCliente = true, onRegresar = { navController.popBackStack() }) }
        composable("recepcion_vehiculo") { VerRecepcionesScreen(onRegresar = { navController.popBackStack() }) }
        composable("historial") { HistorialServiciosScreen(onRegresar = { navController.popBackStack() }) }
    }
}
