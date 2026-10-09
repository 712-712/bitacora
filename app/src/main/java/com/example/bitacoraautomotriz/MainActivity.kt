package com.example.bitacoraautomotriz

import android.os.Bundle
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() { // <-- Abre la clase correctamente aquí

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Llamamos a tu función de prueba de Firebase
        enviarMensajePrueba()

        // 2. Tu código original de inicialización (dentro de onCreate)
        ClienteRepository.inicializar(applicationContext)
        AutoRepository.inicializar(applicationContext)
        OrdenServicioRepository.inicializar(applicationContext)
        RepuestoRepository.inicializar(applicationContext)
        GastoRepository.inicializar(applicationContext)
        FacturaRepository.inicializar(applicationContext)

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
    } // <-- Cierra el onCreate aquí de forma correcta

    // 3. Tu función de prueba de Firebase va aquí abajo
    fun enviarMensajePrueba() {
        val database = Firebase.database
        val referencia = database.getReference("conexion_prueba")

        referencia.setValue("¡Hola desde Bitácora Automotriz!")
            .addOnSuccessListener {
                println("¡Conexión exitosa! El mensaje se guardó correctamente.")
            }
            .addOnFailureListener { error ->
                println("Hubo un error al conectar con Firebase: ${error.message}")
            }
    }
} // <-- Cierra la clase MainActivity al final de todo
