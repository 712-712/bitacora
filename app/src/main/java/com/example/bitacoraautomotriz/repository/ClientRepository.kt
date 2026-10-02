package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.data.ClienteDao
import com.example.bitacoraautomotriz.data.ClienteDatabase

object ClienteRepository {

    private var db: ClienteDatabase? = null

    fun inicializar(context: Context) {
        db = ClienteDatabase.obtenerDatabase(context.applicationContext)
    }

    private fun obtenerDao(context: Context? = null): ClienteDao? {
        if (db != null) return db?.clienteDao()
        if (context != null) {
            db = ClienteDatabase.obtenerDatabase(context.applicationContext)
            return db?.clienteDao()
        }
        return null
    }

    suspend fun guardarCliente(cliente: Cliente, context: Context? = null) {
        obtenerDao(context)?.insertarCliente(cliente)
    }

    suspend fun obtenerClientes(context: Context? = null): List<Cliente> {
        return obtenerDao(context)?.obtenerClientes() ?: emptyList()
    }

    suspend fun obtenerClientePorId(id: Int, context: Context? = null): Cliente? {
        return obtenerDao(context)?.obtenerClientePorId(id)
    }

    suspend fun eliminarCliente(id: Int, context: Context? = null) {
        obtenerDao(context)?.eliminarCliente(id)
    }

    suspend fun actualizarCliente(cliente: Cliente, context: Context? = null) {
        obtenerDao(context)?.actualizarCliente(cliente)
    }

    suspend fun obtenerClientePorTelefono(telefono: String, context: Context? = null): Cliente? {
        return obtenerDao(context)?.obtenerClientePorTelefono(telefono)
    }
}
