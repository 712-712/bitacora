package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.ClienteDatabase
import com.example.bitacoraautomotriz.data.Gasto

object GastoRepository {

    private lateinit var database: ClienteDatabase

    fun inicializar(context: Context) {
        database = ClienteDatabase.obtenerDatabase(context)
    }

    suspend fun guardarGasto(gasto: Gasto) {
        database.gastoDao().insertarGasto(gasto)
    }

    suspend fun obtenerGastos(): List<Gasto> {
        return database.gastoDao().obtenerGastos()
    }
}
