package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.ClienteDatabase
import com.example.bitacoraautomotriz.data.Repuesto

object RepuestoRepository {

    private lateinit var database: ClienteDatabase

    fun inicializar(context: Context) {
        database = ClienteDatabase.obtenerDatabase(context)
    }

    suspend fun guardarRepuesto(repuesto: Repuesto) {
        database.repuestoDao().insertarRepuesto(repuesto)
    }

    suspend fun obtenerRepuestos(): List<Repuesto> {
        return database.repuestoDao().obtenerRepuestos()
    }
}
