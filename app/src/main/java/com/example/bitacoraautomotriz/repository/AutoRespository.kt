package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.AutoDao
import com.example.bitacoraautomotriz.data.ClienteDatabase

object AutoRepository {

    private var db: ClienteDatabase? = null

    fun inicializar(context: Context) {
        db = ClienteDatabase.obtenerDatabase(context.applicationContext)
    }

    private fun obtenerDao(context: Context? = null): AutoDao? {
        if (db != null) return db?.autoDao()
        if (context != null) {
            db = ClienteDatabase.obtenerDatabase(context.applicationContext)
            return db?.autoDao()
        }
        return null
    }

    suspend fun guardarAuto(auto: Auto, context: Context? = null) {
        obtenerDao(context)?.insertarAuto(auto)
    }

    suspend fun actualizarAuto(auto: Auto, context: Context? = null) {
        obtenerDao(context)?.actualizarAuto(auto)
    }

    suspend fun eliminarAuto(auto: Auto, context: Context? = null) {
        obtenerDao(context)?.eliminarAuto(auto)
    }

    suspend fun obtenerAutos(context: Context? = null): List<Auto> {
        return obtenerDao(context)?.obtenerAutos() ?: emptyList()
    }

    suspend fun obtenerAutoPorId(id: Int, context: Context? = null): Auto? {
        return obtenerDao(context)?.obtenerAutoPorId(id)
    }

    suspend fun obtenerAutosPorCliente(nombreCliente: String, context: Context? = null): List<Auto> {
        return obtenerDao(context)?.obtenerAutosPorCliente(nombreCliente) ?: emptyList()
    }

    suspend fun buscarAutos(termino: String, context: Context? = null): List<Auto> {
        return obtenerDao(context)?.buscarAutos(termino) ?: emptyList()
    }

    suspend fun obtenerAutoPorVin(vin: String, context: Context? = null): Auto? {
        return obtenerDao(context)?.obtenerAutoPorVin(vin)
    }

    suspend fun obtenerAutoPorPlaca(placa: String, context: Context? = null): Auto? {
        return obtenerDao(context)?.obtenerAutoPorPlaca(placa)
    }

    suspend fun eliminarAutosPorCliente(nombreCliente: String, context: Context? = null) {
        obtenerDao(context)?.eliminarAutosPorCliente(nombreCliente)
    }

    suspend fun contarAutosPorCliente(nombreCliente: String, context: Context? = null): Int {
        return try {
            obtenerDao(context)?.contarAutosPorCliente(nombreCliente) ?: 0
        } catch (_: Exception) {
            0
        }
    }
}
