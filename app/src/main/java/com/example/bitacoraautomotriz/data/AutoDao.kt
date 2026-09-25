package com.example.bitacoraautomotriz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AutoDao {

    @Insert
    suspend fun insertarAuto(auto: Auto)

    @Update
    suspend fun actualizarAuto(auto: Auto)

    @Delete
    suspend fun eliminarAuto(auto: Auto)

    @Query("SELECT * FROM autos")
    suspend fun obtenerAutos(): List<Auto>

    @Query("SELECT * FROM autos WHERE id = :id LIMIT 1")
    suspend fun obtenerAutoPorId(id: Int): Auto?

    @Query("SELECT * FROM autos WHERE cliente = :nombreCliente")
    suspend fun obtenerAutosPorCliente(nombreCliente: String): List<Auto>

    // ✅ CORREGIDO: Ahora incluye VIN y es insensible a mayúsculas/minúsculas
    @Query("""
        SELECT * FROM autos 
        WHERE UPPER(placa) LIKE '%' || UPPER(:termino) || '%' 
           OR UPPER(marca) LIKE '%' || UPPER(:termino) || '%' 
           OR UPPER(modelo) LIKE '%' || UPPER(:termino) || '%' 
           OR UPPER(vin) LIKE '%' || UPPER(:termino) || '%'
    """)
    suspend fun buscarAutos(termino: String): List<Auto>

    @Query("SELECT * FROM autos WHERE vin = :vin LIMIT 1")
    suspend fun obtenerAutoPorVin(vin: String): Auto?

    @Query("SELECT * FROM autos WHERE placa = :placa LIMIT 1")
    suspend fun obtenerAutoPorPlaca(placa: String): Auto?

    @Query("DELETE FROM autos WHERE cliente = :nombreCliente")
    suspend fun eliminarAutosPorCliente(nombreCliente: String)

    @Query("SELECT COUNT(*) FROM autos WHERE cliente = :nombreCliente")
    suspend fun contarAutosPorCliente(nombreCliente: String): Int
}
