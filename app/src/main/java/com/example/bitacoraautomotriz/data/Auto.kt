package com.example.bitacoraautomotriz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "autos")
data class Auto(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val cliente: String,

    val marca: String,

    val modelo: String,

    val anio: Int,

    val placa: String,

    val vin: String,

    val color: String,

    val kilometraje: Int
)
