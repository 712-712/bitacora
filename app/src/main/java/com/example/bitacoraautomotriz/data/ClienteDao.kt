package com.example.bitacoraautomotriz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ClienteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCliente(cliente: Cliente): Long

    @Update
    suspend fun actualizarCliente(cliente: Cliente): Int

    @Query("SELECT * FROM clientes ORDER BY id DESC")
    suspend fun obtenerClientes(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE id = :id LIMIT 1")
    suspend fun obtenerClientePorId(id: Int): Cliente?

    // ✅ ESTA ES LA FUNCIÓN CLAVE (Escrita una sola vez)
    @Query("SELECT * FROM clientes WHERE telefono = :telefono LIMIT 1")
    suspend fun obtenerClientePorTelefono(telefono: String): Cliente?

    @Query("DELETE FROM clientes WHERE id = :id")
    suspend fun eliminarCliente(id: Int)
}
