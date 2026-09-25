package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.ClienteDatabase
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.data.OrdenServicioDao

object OrdenServicioRepository {
    private lateinit var dao: OrdenServicioDao

    fun inicializar(context: Context) {
        dao = ClienteDatabase.obtenerDatabase(context).ordenServicioDao()
    }

    suspend fun guardarOrden(orden: OrdenServicio) { dao.insertarOrden(orden) }
    suspend fun obtenerOrdenes(): List<OrdenServicio> { return dao.obtenerOrdenes() }
    suspend fun buscarOrdenesPorTexto(texto: String): List<OrdenServicio> { return dao.buscarOrdenesPorTexto("%$texto%") }
    suspend fun obtenerOrdenesPorCliente(nombreCliente: String): List<OrdenServicio> { return dao.obtenerOrdenesPorCliente(nombreCliente) }
    suspend fun actualizarOrden(orden: OrdenServicio) { dao.actualizarOrden(orden) }

    // ✅ FUNCIÓN AGREGADA PARA ELIMINAR
    suspend fun eliminarOrden(orden: OrdenServicio) { dao.eliminarOrden(orden) }

    suspend fun actualizarEstadoYAvance(id: Int, estado: String, porcentajeAvance: Int, fechaEntrega: String) {
        dao.actualizarEstadoYAvance(id, estado, porcentajeAvance, fechaEntrega)
    }

    suspend fun obtenerOrdenPorId(id: Int): OrdenServicio? { return dao.obtenerOrdenPorId(id) }

    suspend fun actualizarAceptacion(id: Int, aceptada: Boolean) {
        val orden = dao.obtenerOrdenPorId(id) ?: return
        val ordenActualizada = orden.copy(
            porcentajeAvance = if (aceptada) 100 else 0,
            estado = if (aceptada) "EN REPARACIÓN" else "RECHAZADA POR CLIENTE"
        )
        dao.actualizarOrden(ordenActualizada)
    }
}
