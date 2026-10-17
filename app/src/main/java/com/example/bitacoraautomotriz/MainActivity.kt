package com.example.bitacoraautomotriz

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.bitacoraautomotriz.navigation.AppNavigationCliente
import com.example.bitacoraautomotriz.navigation.AppNavigationTaller
import com.example.bitacoraautomotriz.repository.AutoRepository
import com.example.bitacoraautomotriz.repository.ClienteRepository
import com.example.bitacoraautomotriz.repository.FacturaRepository
import com.example.bitacoraautomotriz.repository.GastoRepository
import com.example.bitacoraautomotriz.repository.OrdenServicioRepository
import com.example.bitacoraautomotriz.repository.RepuestoRepository
import com.example.bitacoraautomotriz.ui.theme.BitacoraAutomotrizTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iniciarSesionYArrancar()
    }

    private fun iniciarSesionYArrancar() {
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            iniciarApp()
            return
        }

        auth.signInAnonymously()
            .addOnSuccessListener { iniciarApp() }
            .addOnFailureListener { e ->
                Log.e("Auth", "Error de login: ${e.message}")
                iniciarApp() // la app abre igual, pero Firebase no cargará datos sin sesión
            }
    }

    private fun iniciarApp() {
        // Inicialización de repositorios locales
        ClienteRepository.inicializar(applicationContext)
        AutoRepository.inicializar(applicationContext)
        OrdenServicioRepository.inicializar(applicationContext)
        RepuestoRepository.inicializar(applicationContext)
        GastoRepository.inicializar(applicationContext)
        FacturaRepository.inicializar(applicationContext)

        // Detectamos dinámicamente si es la variante "cliente" o "taller"
        val pkgName = applicationContext.packageName.lowercase()
        val esAppCliente = pkgName.contains("cliente")

        setContent {
            BitacoraAutomotrizTheme {
                if (esAppCliente) {
                    AppNavigationCliente()
                } else {
                    AppNavigationTaller()
                }
            }
        }
    }
}
