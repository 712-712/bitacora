package com.example.bitacoraautomotriz.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bitacoraautomotriz.ui.mapa.MapaScreen
import com.example.bitacoraautomotriz.ui.areacliente.AreaClienteScreen
import com.example.bitacoraautomotriz.ui.autos.AutosDelClienteScreen
import com.example.bitacoraautomotriz.ui.autos.AutosScreen
import com.example.bitacoraautomotriz.ui.autos.BusquedaAutoScreen
import com.example.bitacoraautomotriz.ui.autos.EditarAutoScreen
import com.example.bitacoraautomotriz.ui.autos.NuevoAutoScreen
import com.example.bitacoraautomotriz.ui.autos.VerAutosScreen
import com.example.bitacoraautomotriz.ui.clientes.BusquedaClienteScreen
import com.example.bitacoraautomotriz.ui.clientes.CitaEntregaScreen
import com.example.bitacoraautomotriz.ui.clientes.ClientesScreen
import com.example.bitacoraautomotriz.ui.clientes.EditarClienteScreen
import com.example.bitacoraautomotriz.ui.clientes.EstadoReparacionScreen
import com.example.bitacoraautomotriz.ui.clientes.HistorialServiciosScreen
import com.example.bitacoraautomotriz.ui.clientes.MisDatosClienteScreen
import com.example.bitacoraautomotriz.ui.clientes.MisDatosFacturacionScreen
import com.example.bitacoraautomotriz.ui.clientes.NuevoClienteScreen
import com.example.bitacoraautomotriz.ui.clientes.VerClientesScreen
import com.example.bitacoraautomotriz.ui.clientes.VerReporteClienteScreen
import com.example.bitacoraautomotriz.ui.configuracion.AcercaDeScreen
import com.example.bitacoraautomotriz.ui.configuracion.ConfiguracionScreen
import com.example.bitacoraautomotriz.ui.dashboard.DashboardScreen
import com.example.bitacoraautomotriz.ui.escaner.BarcodeScannerScreen
import com.example.bitacoraautomotriz.ui.escaner.RegistrarAutoScreen
import com.example.bitacoraautomotriz.ui.escaner.VerAutoScreen
import com.example.bitacoraautomotriz.ui.facturacion.BusquedaFacturaScreen
import com.example.bitacoraautomotriz.ui.facturacion.FacturacionScreen
import com.example.bitacoraautomotriz.ui.facturacion.NuevaFacturaScreen
import com.example.bitacoraautomotriz.ui.facturacion.VerFacturasScreen
import com.example.bitacoraautomotriz.ui.gastos.BusquedaGastoScreen
import com.example.bitacoraautomotriz.ui.gastos.GastosScreen
import com.example.bitacoraautomotriz.ui.gastos.NuevoGastoScreen
import com.example.bitacoraautomotriz.ui.gastos.VerGastosScreen
import com.example.bitacoraautomotriz.ui.inventario.BuscarRefaccionMapaScreen
import com.example.bitacoraautomotriz.ui.inventario.BuscarRepuestosScreen
import com.example.bitacoraautomotriz.ui.inventario.InventarioScreen
import com.example.bitacoraautomotriz.ui.inventario.MapaRutaScreen
import com.example.bitacoraautomotriz.ui.inventario.NuevoRepuestoScreen
import com.example.bitacoraautomotriz.ui.inventario.VerInventarioScreen
import com.example.bitacoraautomotriz.ui.ordenes.BuscarOrdenScreen
import com.example.bitacoraautomotriz.ui.ordenes.NuevaOrdenScreen
import com.example.bitacoraautomotriz.ui.ordenes.OrdenesScreen
import com.example.bitacoraautomotriz.ui.ordenes.ProgramarAlertaScreen
import com.example.bitacoraautomotriz.ui.ordenes.SeguimientoReparacionScreen
import com.example.bitacoraautomotriz.ui.reportes.ReporteAutosScreen
import com.example.bitacoraautomotriz.ui.reportes.ReporteClientesScreen
import com.example.bitacoraautomotriz.ui.reportes.ReporteFacturacionScreen
import com.example.bitacoraautomotriz.ui.reportes.ReporteGastosScreen
import com.example.bitacoraautomotriz.ui.reportes.ReporteInventarioScreen
import com.example.bitacoraautomotriz.ui.reportes.ReporteOrdenesServicioScreen
import com.example.bitacoraautomotriz.ui.reportes.ReportesScreen
import com.example.bitacoraautomotriz.ui.welcome.WelcomeScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as? Activity

    NavHost(
        navController = navController,
        startDestination = "welcome"
    ) {
        composable("welcome") {
            WelcomeScreen(
                onTallerClick = { navController.navigate("dashboard") },
                onClienteClick = { navController.navigate("area_cliente") }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onClientesClick = { navController.navigate("clientes") },
                onAreaClienteClick = { navController.navigate("area_cliente") },
                onOrdenesClick = { navController.navigate("ordenes") },
                onInventarioClick = { navController.navigate("inventario") },
                onGastosClick = { navController.navigate("gastos") },
                onFacturacionClick = { navController.navigate("facturacion") },
                onReportesClick = { navController.navigate("reportes") },
                onConfiguracionClick = { navController.navigate("configuracion") },
                onRegresar = { activity?.finishAffinity() }
            )
        }

        composable("clientes") {
            ClientesScreen(
                onNuevoCliente = { navController.navigate("nuevo_cliente") },
                onVerClientes = { navController.navigate("ver_clientes") },
                onBuscarCliente = { navController.navigate("buscar_cliente") },
                onVerAutos = { navController.navigate("ver_autos") },
                onAgregarAuto = { navController.navigate("nuevo_auto") },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable("nuevo_cliente") {
            NuevoClienteScreen(
                onClienteCreado = {
                    navController.popBackStack()
                },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable("ver_clientes") {
            VerClientesScreen(
                onEditarCliente = { id -> navController.navigate("editar_cliente/$id") },
                onVerAutos = { id, nombre ->
                    navController.navigate("autos_del_cliente/$id/$nombre")
                },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable("buscar_cliente") {
            BusquedaClienteScreen(
                onClienteSeleccionado = { id, nombre ->
                    navController.navigate("autos_del_cliente/$id/$nombre")
                },
                onEditarCliente = { id -> navController.navigate("editar_cliente/$id") },
                onEliminarCliente = { id ->
                    navController.popBackStack()
                },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable(
            route = "editar_cliente/{clienteId}",
            arguments = listOf(navArgument("clienteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getInt("clienteId") ?: 0
            EditarClienteScreen(
                clienteId = clienteId,
                onGuardado = { navController.popBackStack() },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable("autos") {
            AutosScreen(
                onNuevoAuto = { navController.navigate("nuevo_auto") },
                onVerAutos = { navController.navigate("ver_autos") },
                onBuscarAuto = { navController.navigate("buscar_auto") },
                onEscanearVin = { navController.navigate("escanear_vin") },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(
            route = "autos_del_cliente/{clienteId}/{nombreCliente}",
            arguments = listOf(
                navArgument("clienteId") { type = NavType.IntType },
                navArgument("nombreCliente") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getInt("clienteId") ?: 0
            val nombreCodificado = backStackEntry.arguments?.getString("nombreCliente") ?: ""

            val nombreDecodificado = try {
                URLDecoder.decode(nombreCodificado, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) {
                nombreCodificado
            }

            AutosDelClienteScreen(
                clienteId = clienteId,
                nombreCliente = nombreDecodificado,
                onAgregarAuto = {
                    val encoded = URLEncoder.encode(nombreDecodificado, StandardCharsets.UTF_8.toString())
                    navController.navigate("nuevo_auto?cliente=$encoded")
                },
                onEditarAuto = { autoId ->
                    navController.navigate("editar_auto/$autoId")
                },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable("area_cliente") {
            AreaClienteScreen(
                onDatosClienteAutos = { navController.navigate("mis_datos") },
                onDatosFacturacion = { navController.navigate("mis_datos_facturacion") },
                onEstadoReparacion = { navController.navigate("estado_reparacion") },
                onVerReporteCliente = { navController.navigate("ver_reporte_cliente") },
                onCitaEntrega = { navController.navigate("cita_entrega") },
                onHistorial = { navController.navigate("historial") },
                onRegresar = { activity?.finishAffinity() }
            )
        }
        composable("mis_datos") { MisDatosClienteScreen(onRegresar = { navController.popBackStack() }) }
        composable("mis_datos_facturacion") { MisDatosFacturacionScreen(onRegresar = { navController.popBackStack() }) }
        composable("cita_entrega") { CitaEntregaScreen(onRegresar = { navController.popBackStack() }) }
        composable("estado_reparacion") { EstadoReparacionScreen(onRegresar = { navController.popBackStack() }) }
        composable("ver_reporte_cliente") { VerReporteClienteScreen(onRegresar = { navController.popBackStack() }) }
        composable("historial") { HistorialServiciosScreen(onRegresar = { navController.popBackStack() }) }

        composable("nuevo_auto") { backStackEntry ->
            val vinEscaneado = backStackEntry.savedStateHandle.get<String>("vin_escaneado") ?: ""
            val vinFinal = if (vinEscaneado.isNotBlank()) vinEscaneado else ""

            NuevoAutoScreen(
                nombreCliente = "",
                vinInicial = vinFinal,
                onEscanearVin = { navController.navigate("escanear_vin") },
                onGuardar = {
                    Toast.makeText(context, "✅ Auto guardado exitosamente", Toast.LENGTH_SHORT).show()
                    navController.popBackStack("dashboard", inclusive = false)
                },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(
            route = "nuevo_auto?cliente={cliente}&vin={vin}",
            arguments = listOf(
                navArgument("cliente") { defaultValue = "" },
                navArgument("vin") { defaultValue = "" }
            )
        ) { backStackEntry ->
            val nombreCodificado = backStackEntry.arguments?.getString("cliente") ?: ""
            val vinInicial = backStackEntry.arguments?.getString("vin") ?: ""
            val vinEscaneado = backStackEntry.savedStateHandle.get<String>("vin_escaneado") ?: ""
            val vinFinal = if (vinEscaneado.isNotBlank()) vinEscaneado else vinInicial

            val nombreDecodificado = try {
                URLDecoder.decode(nombreCodificado, StandardCharsets.UTF_8.toString())
            } catch (e: Exception) {
                nombreCodificado
            }

            NuevoAutoScreen(
                nombreCliente = nombreDecodificado,
                vinInicial = vinFinal,
                onEscanearVin = { navController.navigate("escanear_vin") },
                onGuardar = {
                    Toast.makeText(context, "✅ Auto guardado exitosamente", Toast.LENGTH_SHORT).show()
                    navController.popBackStack("dashboard", inclusive = false)
                },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable("ver_autos") {
            VerAutosScreen(
                onAgregarAuto = { navController.navigate("nuevo_auto") },
                onEditarAuto = { autoId -> navController.navigate("editar_auto/$autoId") },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable("buscar_auto") {
            BusquedaAutoScreen(
                onEditarAuto = { autoId -> navController.navigate("editar_auto/$autoId") },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(
            route = "editar_auto/{autoId}",
            arguments = listOf(navArgument("autoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val autoId = backStackEntry.arguments?.getInt("autoId") ?: 0
            EditarAutoScreen(
                autoId = autoId,
                onGuardado = { navController.popBackStack() },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable("escanear_vin") { _ ->
            BarcodeScannerScreen(
                onAutoEncontrado = { vin ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("vin_escaneado", vin)
                    navController.popBackStack()
                },
                onAutoNoEncontrado = { vin ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("vin_escaneado", vin)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "ver_auto_vin/{vin}",
            arguments = listOf(navArgument("vin") { type = NavType.StringType })
        ) { backStackEntry ->
            VerAutoScreen(vin = backStackEntry.arguments?.getString("vin") ?: "")
        }

        composable(
            route = "registrar_auto_vin/{vin}",
            arguments = listOf(navArgument("vin") { type = NavType.StringType })
        ) { backStackEntry ->
            RegistrarAutoScreen(
                vin = backStackEntry.arguments?.getString("vin") ?: "",
                onGuardado = { navController.popBackStack() }
            )
        }

        composable("inventario") {
            InventarioScreen(
                onNuevoRepuesto = { navController.navigate("nuevo_repuesto") },
                onVerInventario = { navController.navigate("ver_inventario") },
                onBuscarRepuesto = { navController.navigate("buscar_repuesto") },
                onBuscarRefaccionMapa = { navController.navigate("buscar_refaccion_mapa") },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable("nuevo_repuesto") { NuevoRepuestoScreen(onGuardar = { navController.popBackStack() }, onRegresar = { navController.popBackStack() }) }
        composable("ver_inventario") { VerInventarioScreen(onRegresar = { navController.popBackStack() }) }
        composable("buscar_repuesto") { BuscarRepuestosScreen(onRegresar = { navController.popBackStack() }) }
        composable("buscar_refaccion_mapa") {
            BuscarRefaccionMapaScreen(
                onBuscarResultados = { query ->
                    val busquedaFinal = if (query.isBlank()) "refaccionaria taller autopartes" else "refaccionaria $query"
                    val queryEncoded = URLEncoder.encode(busquedaFinal, StandardCharsets.UTF_8.toString())
                    val mapIntentUri = Uri.parse("geo:0,0?q=$queryEncoded")
                    val mapIntent = Intent(Intent.ACTION_VIEW, mapIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    try {
                        context.startActivity(mapIntent)
                    } catch (_: Exception) {
                        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$queryEncoded")
                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                    }
                },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable(
            route = "mapa_busqueda/{query}",
            arguments = listOf(navArgument("query") { type = NavType.StringType })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: "refaccionaria taller"
            MapaRutaScreen(
                queryBusqueda = query,
                onRegresar = { navController.popBackStack() }
            )
        }
        composable(
            route = "mapa_ruta/{lat}/{lon}/{nombre}/{direccion}",
            arguments = listOf(
                navArgument("lat") { type = NavType.FloatType },
                navArgument("lon") { type = NavType.FloatType },
                navArgument("nombre") { type = NavType.StringType },
                navArgument("direccion") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getFloat("lat")?.toDouble() ?: 0.0
            val lon = backStackEntry.arguments?.getFloat("lon")?.toDouble() ?: 0.0
            val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
            val direccion = backStackEntry.arguments?.getString("direccion") ?: ""

            MapaRutaScreen(
                latDestino = lat,
                lonDestino = lon,
                nombreDestino = nombre,
                direccionDestino = direccion,
                onRegresar = { navController.popBackStack() }
            )
        }

        composable("ordenes") {
            OrdenesScreen(
                onNuevaOrden = { navController.navigate("nueva_orden") },
                onBuscarOrden = { navController.navigate("buscar_orden") },
                onProgramarAlerta = { navController.navigate("programar_alerta") },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable("programar_alerta") {
            ProgramarAlertaScreen(onRegresar = { navController.popBackStack() })
        }
        composable("nueva_orden") {
            NuevaOrdenScreen(onGuardar = { navController.popBackStack() }, onRegresar = { navController.popBackStack() })
        }
        composable("buscar_orden") {
            BuscarOrdenScreen(
                onRegresar = { navController.popBackStack() },
                onEditarOrden = { ordenId -> navController.navigate("seguimiento_reparacion/$ordenId") }
            )
        }
        composable(
            route = "seguimiento_reparacion/{ordenId}",
            arguments = listOf(navArgument("ordenId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ordenId = backStackEntry.arguments?.getInt("ordenId") ?: 0
            SeguimientoReparacionScreen(ordenId = ordenId, onRegresar = { navController.popBackStack() })
        }

        composable("gastos") {
            GastosScreen(
                onNuevoGasto = { navController.navigate("nuevo_gasto") },
                onVerGastos = { navController.navigate("ver_gastos") },
                onBuscarGasto = { navController.navigate("buscar_gasto") },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable("nuevo_gasto") { NuevoGastoScreen(onGuardar = { navController.popBackStack() }, onRegresar = { navController.popBackStack() }) }
        composable("ver_gastos") { VerGastosScreen(onRegresar = { navController.popBackStack() }) }
        composable("buscar_gasto") { BusquedaGastoScreen(onRegresar = { navController.popBackStack() }) }

        composable("facturacion") {
            FacturacionScreen(
                onNuevaFactura = { navController.navigate("nueva_factura") },
                onVerFacturas = { navController.navigate("ver_facturas") },
                onBuscarFactura = { navController.navigate("buscar_factura") },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable("nueva_factura") { NuevaFacturaScreen(onGuardar = { navController.popBackStack() }, onRegresar = { navController.popBackStack() }) }
        composable("ver_facturas") { VerFacturasScreen(onRegresar = { navController.popBackStack() }) }
        composable("buscar_factura") { BusquedaFacturaScreen(onRegresar = { navController.popBackStack() }) }

        composable("reportes") {
            ReportesScreen(
                onReporteClientes = { navController.navigate("reporte_clientes") },
                onReporteAutos = { navController.navigate("reporte_autos") },
                onReporteOrdenes = { navController.navigate("reporte_ordenes") },
                onReporteInventario = { navController.navigate("reporte_inventario") },
                onReporteGastos = { navController.navigate("reporte_gastos") },
                onReporteFacturacion = { navController.navigate("reporte_facturacion") },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable("reporte_clientes") { ReporteClientesScreen(onRegresar = { navController.popBackStack() }) }
        composable("reporte_autos") { ReporteAutosScreen(onRegresar = { navController.popBackStack() }) }
        composable("reporte_ordenes") { ReporteOrdenesServicioScreen(onRegresar = { navController.popBackStack() }) }
        composable("reporte_inventario") { ReporteInventarioScreen(onRegresar = { navController.popBackStack() }) }
        composable("reporte_gastos") { ReporteGastosScreen(onRegresar = { navController.popBackStack() }) }
        composable("reporte_facturacion") { ReporteFacturacionScreen(onRegresar = { navController.popBackStack() }) }

        composable("mapa") { MapaScreen(modifier = Modifier.fillMaxSize()) }

        composable("configuracion") { ConfiguracionScreen(onAcercaDe = { navController.navigate("acerca_de") }, onRegresar = { navController.popBackStack() }) }
        composable("acerca_de") { AcercaDeScreen(onRegresar = { navController.popBackStack() }) }
    }
}
