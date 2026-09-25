package com.example.bitacoraautomotriz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val telefono: String,
    val correo: String,
    val direccion: String
)
