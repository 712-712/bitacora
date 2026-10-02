package com.example.bitacoraautomotriz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface GastoDao {

    @Insert
    suspend fun insertarGasto(gasto: Gasto)

    @Update
    suspend fun actualizarGasto(gasto: Gasto)

    @Query("SELECT * FROM gastos ORDER BY id DESC")
    suspend fun obtenerGastos(): List<Gasto>
}
