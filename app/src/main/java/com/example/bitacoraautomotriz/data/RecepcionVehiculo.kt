package com.example.bitacoraautomotriz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recepciones_vehiculo")
data class RecepcionVehiculo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cliente: String = "",
    val auto: String = "",
    val placa: String = "",
    val vin: String = "",
    val kilometraje: Int = 0,
    val fechaHora: String = "",
    val fotoFrentePath: String = "",
    val fotoAtrasPath: String = "",
    val fotoIzquierdaPath: String = "",
    val fotoDerechaPath: String = "",
    val fotoTechoPath: String = "",
    val fotoRinesPath: String = "",
    val fotoInteriorPath: String = "",
    val fotoTableroPath: String = "",
    val videoPath: String = "",
    val firmaPath: String = "",
    val hashIntegridadSha256: String = "",
    val tiempoRetencion: String = "2 Años (Recomendado Legal)"
)
