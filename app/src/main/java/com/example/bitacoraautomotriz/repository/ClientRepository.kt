package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.Cliente
import com.example.bitacoraautomotriz.data.ClienteDao
import com.example.bitacoraautomotriz.data.ClienteDatabase

object ClienteRepository {

    private lateinit var dao: ClienteDao

    // ✅ ESTA ES LA FUNCIÓN QUE FALTABA (Arregla el error de MainActivity)
    fun inicializar(context: Context) {
        dao = ClienteDatabase.obtenerDatabase(context).clienteDao()
    }

    suspend fun guardarCliente(cliente: Cliente) {
        dao.insertarCliente(cliente)
    }

    suspend fun obtenerClientes(): List<Cliente> {
        return dao.obtenerClientes()
    }

    suspend fun obtenerClientePorId(id: Int): Cliente? {
        return dao.obtenerClientePorId(id)
    }

    suspend fun eliminarCliente(id: Int) {
        dao.eliminarCliente(id)
    }

    suspend fun actualizarCliente(cliente: Cliente) {
        dao.actualizarCliente(cliente)
    }

    // ✅ LA NUEVA FUNCIÓN PARA EVITAR DUPLICADOS
    suspend fun obtenerClientePorTelefono(telefono: String): Cliente? {
        return dao.obtenerClientePorTelefono(telefono)
    }
}
