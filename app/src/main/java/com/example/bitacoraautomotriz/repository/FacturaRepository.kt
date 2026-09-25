package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.ClienteDatabase
import com.example.bitacoraautomotriz.data.Factura

object FacturaRepository {

    private lateinit var database: ClienteDatabase

    fun inicializar(context: Context) {
        database = ClienteDatabase.obtenerDatabase(context)
    }

    suspend fun guardarFactura(factura: Factura) {
        database.facturaDao().insertarFactura(factura)
    }

    suspend fun obtenerFacturas(): List<Factura> {
        return database.facturaDao().obtenerFacturas()
    }
}
