package com.example.bitacoraautomotriz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FacturaDao {

    @Insert
    suspend fun insertarFactura(factura: Factura)

    @Query("SELECT * FROM facturas")
    suspend fun obtenerFacturas(): List<Factura>
}
