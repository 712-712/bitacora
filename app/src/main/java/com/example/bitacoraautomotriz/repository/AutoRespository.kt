package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.AutoDao
import com.example.bitacoraautomotriz.data.ClienteDatabase

object AutoRepository {

    private lateinit var dao: AutoDao

    // ✅ 1. Inicialización de la base de datos
    fun inicializar(context: Context) {
        dao = ClienteDatabase.obtenerDatabase(context).autoDao()
    }

    // ✅ 2. Función "guardarAuto" que llama a "insertarAuto" del DAO
    suspend fun guardarAuto(auto: Auto) {
        dao.insertarAuto(auto)
    }

    suspend fun actualizarAuto(auto: Auto) {
        dao.actualizarAuto(auto)
    }

    // ✅ 3. El DAO espera el objeto Auto completo, NO un Int
    suspend fun eliminarAuto(auto: Auto) {
        dao.eliminarAuto(auto)
    }

    suspend fun obtenerAutos(): List<Auto> {
        return dao.obtenerAutos()
    }

    suspend fun obtenerAutoPorId(id: Int): Auto? {
        return dao.obtenerAutoPorId(id)
    }

    // ✅ 4. El DAO espera un String (nombreCliente), NO un Int (clienteId)
    suspend fun obtenerAutosPorCliente(nombreCliente: String): List<Auto> {
        return dao.obtenerAutosPorCliente(nombreCliente)
    }

    suspend fun buscarAutos(termino: String): List<Auto> {
        return dao.buscarAutos(termino)
    }

    suspend fun obtenerAutoPorVin(vin: String): Auto? {
        return dao.obtenerAutoPorVin(vin)
    }

    // ✅ 5. LA NUEVA FUNCIÓN PARA EVITAR DUPLICADOS DE PLACA
    suspend fun obtenerAutoPorPlaca(placa: String): Auto? {
        return dao.obtenerAutoPorPlaca(placa)
    }

    suspend fun eliminarAutosPorCliente(nombreCliente: String) {
        dao.eliminarAutosPorCliente(nombreCliente)
    }

    // ✅ 6. FUNCIÓN PARA CONTAR AUTOS DE UN CLIENTE (Para la advertencia en cascada)
    suspend fun contarAutosPorCliente(nombreCliente: String): Int {
        return try {
            dao.contarAutosPorCliente(nombreCliente)
        } catch (e: Exception) {
            0 // Si hay error, asumimos 0 autos
        }
    }

} // <--- ESTA ES LA ÚNICA LLAVE DE CIERRE DEL OBJECT
