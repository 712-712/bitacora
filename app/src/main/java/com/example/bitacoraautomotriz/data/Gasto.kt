package com.example.bitacoraautomotriz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")
data class Gasto(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val concepto: String,

    val categoria: String,

    val fecha: String,

    val monto: Double,

    val descripcion: String
)
