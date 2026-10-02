package com.example.bitacoraautomotriz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RepuestoDao {

    @Insert
    suspend fun insertarRepuesto(repuesto: Repuesto)

    @Query("SELECT * FROM repuestos ORDER BY id DESC")
    suspend fun obtenerRepuestos(): List<Repuesto>
}
