package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.ClienteDatabase
import com.example.bitacoraautomotriz.data.Factura
import com.example.bitacoraautomotriz.data.FacturaDao

object FacturaRepository {

    private var db: ClienteDatabase? = null

    fun inicializar(context: Context) {
        db = ClienteDatabase.obtenerDatabase(context.applicationContext)
    }

    private fun obtenerDao(context: Context? = null): FacturaDao? {
        if (db != null) return db?.facturaDao()
        if (context != null) {
            db = ClienteDatabase.obtenerDatabase(context.applicationContext)
            return db?.facturaDao()
        }
        return null
    }

    suspend fun guardarFactura(factura: Factura, context: Context? = null) {
        obtenerDao(context)?.insertarFactura(factura)
    }

    suspend fun obtenerFacturas(context: Context? = null): List<Factura> {
        return obtenerDao(context)?.obtenerFacturas() ?: emptyList()
    }
}
