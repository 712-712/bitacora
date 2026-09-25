
package com.example.bitacoraautomotriz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Cliente::class,
        Auto::class,
        OrdenServicio::class,
        Repuesto::class,
        Gasto::class,
        Factura::class
    ],
    version = 7,
    exportSchema = false
)
abstract class ClienteDatabase : RoomDatabase() {

    // =========================================
    // DAO CLIENTES
    // =========================================

    abstract fun clienteDao(): ClienteDao

    // =========================================
    // DAO AUTOS
    // =========================================

    abstract fun autoDao(): AutoDao

    // =========================================
    // DAO ÓRDENES DE SERVICIO
    // =========================================

    abstract fun ordenServicioDao(): OrdenServicioDao

    // =========================================
    // DAO REPUESTOS
    // =========================================

    abstract fun repuestoDao(): RepuestoDao

    // =========================================
    // DAO GASTOS
    // =========================================

    abstract fun gastoDao(): GastoDao

    // =========================================
    // DAO FACTURAS
    // =========================================

    abstract fun facturaDao(): FacturaDao

    companion object {

        @Volatile
        private var INSTANCE: ClienteDatabase? = null

        fun obtenerDatabase(context: Context): ClienteDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClienteDatabase::class.java,
                    "bitacora_automotriz_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}

