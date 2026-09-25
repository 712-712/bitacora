package com.example.bitacoraautomotriz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repuestos")
data class Repuesto(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,

    val marca: String,

    val categoria: String,

    val cantidad: Int,

    val precio: Double
)
