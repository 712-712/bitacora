package com.example.bitacoraautomotriz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ordenes_servicio")
data class OrdenServicio(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cliente: String,
    val auto: String,
    val fecha: String,
    val kilometraje: Int,
    val fallaReportada: String,
    val diagnostico: String,
    val trabajoRealizado: String,
    val estado: String,
    val porcentajeAvance: Int,
    val fechaEntrega: String,

    // ✅ NUEVOS CAMPOS PARA GUARDAR LOS COSTOS
    val costoManoObra: Double = 0.0,
    val costoRefacciones: Double = 0.0,
    val iva: Double = 0.0,
    val total: Double = 0.0
)
