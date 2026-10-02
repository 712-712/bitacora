package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.ClienteDatabase
import com.example.bitacoraautomotriz.data.Repuesto
import com.example.bitacoraautomotriz.data.RepuestoDao

object RepuestoRepository {

    private var db: ClienteDatabase? = null

    fun inicializar(context: Context) {
        db = ClienteDatabase.obtenerDatabase(context.applicationContext)
    }

    private fun obtenerDao(context: Context? = null): RepuestoDao? {
        if (db != null) return db?.repuestoDao()
        if (context != null) {
            db = ClienteDatabase.obtenerDatabase(context.applicationContext)
            return db?.repuestoDao()
        }
        return null
    }

    suspend fun guardarRepuesto(repuesto: Repuesto, context: Context? = null) {
        obtenerDao(context)?.insertarRepuesto(repuesto)
    }

    suspend fun obtenerRepuestos(context: Context? = null): List<Repuesto> {
        return obtenerDao(context)?.obtenerRepuestos() ?: emptyList()
    }
}
