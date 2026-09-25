package com.example.bitacoraautomotriz.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface OrdenServicioDao {

    // =========================================
    // INSERTAR ORDEN
    // =========================================
    @Insert
    suspend fun insertarOrden(orden: OrdenServicio)

    // =========================================
    // OBTENER TODAS LAS ÓRDENES (De la más reciente a la más antigua)
    // =========================================
    @Query("SELECT * FROM ordenes_servicio ORDER BY id DESC")
    suspend fun obtenerOrdenes(): List<OrdenServicio>

    // =========================================
    // OBTENER ÓRDENES DE UN CLIENTE
    // =========================================
    @Query("""
        SELECT * FROM ordenes_servicio
        WHERE TRIM(cliente) = TRIM(:nombreCliente) COLLATE NOCASE
        ORDER BY id DESC
    """)
    suspend fun obtenerOrdenesPorCliente(nombreCliente: String): List<OrdenServicio>

    // =========================================
    // ✅ NUEVO: BUSCAR ÓRDENES POR TEXTO (Cliente, Auto, Placa o Estado)
    // =========================================
    @Query("""
        SELECT * FROM ordenes_servicio
        WHERE cliente LIKE :texto OR auto LIKE :texto OR estado LIKE :texto
        ORDER BY id DESC
    """)
    suspend fun buscarOrdenesPorTexto(texto: String): List<OrdenServicio>

    // =========================================
    // ACTUALIZAR ORDEN COMPLETA
    // =========================================
    @Update
    suspend fun actualizarOrden(orden: OrdenServicio)

    // =========================================
    // ✅ ACTUALIZAR SOLO ESTADO, AVANCE Y FECHA DE ENTREGA
    // =========================================
    @Query("""
        UPDATE ordenes_servicio 
        SET estado = :estado, 
            porcentajeAvance = :porcentajeAvance, 
            fechaEntrega = :fechaEntrega 
        WHERE id = :id
    """)
    suspend fun actualizarEstadoYAvance(
        id: Int,
        estado: String,
        porcentajeAvance: Int,
        fechaEntrega: String
    )

    // =========================================
    // ✅ OBTENER UNA SOLA ORDEN POR SU ID (Para Deep Links)
    // =========================================
    @Query("SELECT * FROM ordenes_servicio WHERE id = :id")
    suspend fun obtenerOrdenPorId(id: Int): OrdenServicio?

    // =========================================
    // ELIMINAR ORDEN
    // =========================================
    @Delete
    suspend fun eliminarOrden(orden: OrdenServicio)
}
