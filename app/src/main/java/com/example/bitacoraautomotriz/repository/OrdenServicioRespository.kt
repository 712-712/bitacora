package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.ClienteDatabase
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.data.OrdenServicioDao

object OrdenServicioRepository {

    private var db: ClienteDatabase? = null

    fun inicializar(context: Context) {
        db = ClienteDatabase.obtenerDatabase(context.applicationContext)
    }

    private fun obtenerDao(context: Context? = null): OrdenServicioDao? {
        if (db != null) return db?.ordenServicioDao()
        if (context != null) {
            db = ClienteDatabase.obtenerDatabase(context.applicationContext)
            return db?.ordenServicioDao()
        }
        return null
    }

    suspend fun guardarOrden(orden: OrdenServicio, context: Context? = null) {
        obtenerDao(context)?.insertarOrden(orden)
        try { FirebaseSyncManager.subirOrdenAFirebase(orden) } catch (_: Exception) {}
    }

    suspend fun obtenerOrdenes(context: Context? = null): List<OrdenServicio> {
        return obtenerDao(context)?.obtenerOrdenes() ?: emptyList()
    }

    suspend fun buscarOrdenesPorTexto(texto: String, context: Context? = null): List<OrdenServicio> {
        return obtenerDao(context)?.buscarOrdenesPorTexto("%$texto%") ?: emptyList()
    }

    suspend fun obtenerOrdenesPorCliente(nombreCliente: String, context: Context? = null): List<OrdenServicio> {
        return obtenerDao(context)?.obtenerOrdenesPorCliente(nombreCliente) ?: emptyList()
    }

    suspend fun actualizarOrden(orden: OrdenServicio, context: Context? = null) {
        obtenerDao(context)?.actualizarOrden(orden)
        try { FirebaseSyncManager.subirOrdenAFirebase(orden) } catch (_: Exception) {}
    }

    suspend fun eliminarOrden(orden: OrdenServicio, context: Context? = null) {
        obtenerDao(context)?.eliminarOrden(orden)
    }

    suspend fun actualizarEstadoYAvance(id: Int, estado: String, porcentajeAvance: Int, fechaEntrega: String, context: Context? = null) {
        val dao = obtenerDao(context)
        dao?.actualizarEstadoYAvance(id, estado, porcentajeAvance, fechaEntrega)
        try {
            val ordenModificada = dao?.obtenerOrdenPorId(id)
            if (ordenModificada != null) {
                FirebaseSyncManager.subirOrdenAFirebase(ordenModificada)
            }
        } catch (_: Exception) {}
    }

    suspend fun obtenerOrdenPorId(id: Int, context: Context? = null): OrdenServicio? {
        return obtenerDao(context)?.obtenerOrdenPorId(id)
    }

    suspend fun actualizarAceptacion(id: Int, aceptada: Boolean, context: Context? = null) {
        val dao = obtenerDao(context) ?: return
        val orden = dao.obtenerOrdenPorId(id) ?: return
        val ordenActualizada = orden.copy(
            porcentajeAvance = if (aceptada) 100 else 0,
            estado = if (aceptada) "EN REPARACIÓN" else "RECHAZADA POR CLIENTE"
        )
        dao.actualizarOrden(ordenActualizada)
        try { FirebaseSyncManager.subirOrdenAFirebase(ordenActualizada) } catch (_: Exception) {}
    }
}
