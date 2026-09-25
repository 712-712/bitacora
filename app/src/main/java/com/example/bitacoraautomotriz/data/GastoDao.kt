package com.example.bitacoraautomotriz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GastoDao {

    @Insert
    suspend fun insertarGasto(gasto: Gasto)

    @Query("SELECT * FROM gastos")
    suspend fun obtenerGastos(): List<Gasto>
}
