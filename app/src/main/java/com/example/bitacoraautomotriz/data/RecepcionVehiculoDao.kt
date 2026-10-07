package com.example.bitacoraautomotriz.data

import androidx.room.*

@Dao
interface RecepcionVehiculoDao {
    @Query("SELECT * FROM recepciones_vehiculo ORDER BY id DESC")
    suspend fun obtenerTodas(): List<RecepcionVehiculo>

    @Query("SELECT * FROM recepciones_vehiculo WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): RecepcionVehiculo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(recepcion: RecepcionVehiculo): Long

    @Delete
    suspend fun eliminar(recepcion: RecepcionVehiculo)
}
