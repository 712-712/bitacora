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
        Factura::class,
        RecepcionVehiculo::class
    ],
    version = 9,
    exportSchema = false
)
abstract class ClienteDatabase : RoomDatabase() {

    abstract fun clienteDao(): ClienteDao
    abstract fun autoDao(): AutoDao
    abstract fun ordenServicioDao(): OrdenServicioDao
    abstract fun repuestoDao(): RepuestoDao
    abstract fun gastoDao(): GastoDao
    abstract fun facturaDao(): FacturaDao
    abstract fun recepcionVehiculoDao(): RecepcionVehiculoDao

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
