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
    val direccion: String,

    // DATOS DE FACTURACIÓN SAT
    val rfc: String = "",
    val razonSocial: String = "",
    val codigoPostal: String = "",
    val regimenFiscalClave: String = "",
    val regimenFiscalDesc: String = "",
    val usoCfdiClave: String = "",
    val usoCfdiDesc: String = ""
)
