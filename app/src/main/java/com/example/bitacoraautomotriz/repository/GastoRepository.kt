package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.ClienteDatabase
import com.example.bitacoraautomotriz.data.Gasto
import com.example.bitacoraautomotriz.data.GastoDao

object GastoRepository {

    private var db: ClienteDatabase? = null

    fun inicializar(context: Context) {
        db = ClienteDatabase.obtenerDatabase(context.applicationContext)
    }

    private fun obtenerDao(context: Context? = null): GastoDao? {
        if (db != null) return db?.gastoDao()
        if (context != null) {
            db = ClienteDatabase.obtenerDatabase(context.applicationContext)
            return db?.gastoDao()
        }
        return null
    }

    suspend fun guardarGasto(gasto: Gasto, context: Context? = null) {
        obtenerDao(context)?.insertarGasto(gasto)
    }

    suspend fun actualizarGasto(gasto: Gasto, context: Context? = null) {
        obtenerDao(context)?.actualizarGasto(gasto)
    }

    suspend fun obtenerGastos(context: Context? = null): List<Gasto> {
        return obtenerDao(context)?.obtenerGastos() ?: emptyList()
    }
}
