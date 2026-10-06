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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    }
}
