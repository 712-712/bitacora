package com.example.bitacoraautomotriz.repository

import android.content.Context
import com.example.bitacoraautomotriz.data.ClienteDatabase
import com.example.bitacoraautomotriz.data.RecepcionVehiculo
import java.security.MessageDigest
import java.util.Locale

object RecepcionRepository {

    suspend fun guardarRecepcion(recepcion: RecepcionVehiculo, context: Context): Long {
        val db = ClienteDatabase.obtenerDatabase(context)
        val idGenerado = db.recepcionVehiculoDao().insertar(recepcion)
        val idFinal = if (recepcion.id == 0 && idGenerado > 0L) idGenerado.toInt() else recepcion.id
        val recepcionConId = recepcion.copy(id = idFinal)
        try { FirebaseSyncManager.subirRecepcionAFirebase(recepcionConId) } catch (_: Exception) {}
        return idGenerado
    }

    suspend fun obtenerRecepciones(context: Context): List<RecepcionVehiculo> {
        val db = ClienteDatabase.obtenerDatabase(context)
        return db.recepcionVehiculoDao().obtenerTodas()
    }

    suspend fun eliminarRecepcion(recepcion: RecepcionVehiculo, context: Context) {
        val db = ClienteDatabase.obtenerDatabase(context)
        db.recepcionVehiculoDao().eliminar(recepcion)
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
