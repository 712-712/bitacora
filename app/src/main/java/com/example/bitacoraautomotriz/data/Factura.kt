package com.example.bitacoraautomotriz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facturas")
data class Factura(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val numero: String,

    val cliente: String,

    val fecha: String,

    val subtotal: Double,

    val iva: Double,

    val total: Double
)
