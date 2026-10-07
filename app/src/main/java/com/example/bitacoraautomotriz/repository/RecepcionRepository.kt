package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.ClienteDatabase
import com.example.bitacoraautomotriz.data.RecepcionVehiculo
import java.security.MessageDigest
import java.util.Locale

object RecepcionRepository {

    suspend fun guardarRecepcion(recepcion: RecepcionVehiculo, context: Context): Long {
        val db = ClienteDatabase.obtenerDatabase(context)
        return db.recepcionVehiculoDao().insertar(recepcion)
    }

    suspend fun obtenerRecepciones(context: Context): List<RecepcionVehiculo> {
        val db = ClienteDatabase.obtenerDatabase(context)
        return db.recepcionVehiculoDao().obtenerTodas()
    }

    fun calcularHashSha256(texto: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(texto.toByteArray())
            bytes.joinToString("") { String.format(Locale.US, "%02x", it) }
        } catch (_: Exception) {
            "HASH_ERROR"
        }
    }
}
