package com.example.bitacoraautomotriz.repository

import android.util.Log
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class AlertaMantenimiento(
    val id: String = "",
    val clienteNombre: String = "",
    val autoPlaca: String = "",
    val tipoAlerta: String = "Por Tiempo",
    val fechaRevision: String = "",
    val mensaje: String = ""
)

object FirebaseSyncManager {

    private val TAG = "FirebaseSyncManager"
    private const val FIREBASE_URL = "https://bitacoraautomotriz-default-rtdb.firebaseio.com"

    private val dbRef by lazy {
        try {
            FirebaseDatabase.getInstance(FIREBASE_URL).reference
        } catch (e1: Exception) {
            try {
                FirebaseDatabase.getInstance().reference
            } catch (e2: Exception) {
                Log.e(TAG, "Error inicializando FirebaseDatabase: ${e2.message}")
                null
            }
        }
    }

    private fun parsearInt(valor: Any?): Int {
        return when (valor) {
            is Number -> valor.toInt()
            is String -> valor.toIntOrNull() ?: 0
            else -> 0
        }
    }

    private fun parsearDouble(valor: Any?): Double {
        return when (valor) {
            is Number -> valor.toDouble()
            is String -> valor.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    // ======================================================
    // 1. SINCRONIZACIÓN DE ÓRDENES Y COTIZACIONES (TALLER ↔ CLIENTE)
    // ======================================================

    fun subirOrdenAFirebase(orden: OrdenServicio) {
        try {
            val key = if (orden.id > 0) orden.id.toString() else (dbRef?.child("ordenes")?.push()?.key ?: return)
            
            // 1.1 RUTA DIRECTA DE ÓRDENES EN NODO /ordenes/key
            dbRef?.child("ordenes")?.child(key)?.setValue(orden)
                ?.addOnSuccessListener {
                    Log.d(TAG, "✅ Orden #${orden.id} subida exitosamente a /ordenes/$key")
                }
                ?.addOnFailureListener { err ->
                    Log.e(TAG, "❌ Error al subir orden a Firebase: ${err.message}")
                }

            // 1.2 RUTA ESTRUCTURADA POR CLIENTE Y SERVICIO EN NODO /servicios
            val clienteCleanKey = orden.cliente.lowercase().replace(Regex("[^a-z0-9]"), "_")
            if (clienteCleanKey.isNotBlank()) {
                val informesRef = dbRef?.child("servicios")?.child(clienteCleanKey)?.child("informes_taller")
                informesRef?.child("informe_$key")?.setValue(orden)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en subirOrdenAFirebase: ${e.message}")
        }
    }

    fun publicarInformeTallerConPush(clienteNombre: String, informeMap: Map<String, Any>) {
        try {
            val clienteCleanKey = clienteNombre.lowercase().replace(Regex("[^a-z0-9]"), "_")
            if (clienteCleanKey.isNotBlank()) {
                val informesRef = dbRef?.child("servicios")?.child(clienteCleanKey)?.child("informes_taller")
                informesRef?.push()?.setValue(informeMap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun publicarRespuestaClienteConPush(clienteNombre: String, respuestaMap: Map<String, Any>) {
        try {
            val clienteCleanKey = clienteNombre.lowercase().replace(Regex("[^a-z0-9]"), "_")
            if (clienteCleanKey.isNotBlank()) {
                val respuestasRef = dbRef?.child("servicios")?.child(clienteCleanKey)?.child("respuestas_cliente")
                respuestasRef?.push()?.setValue(respuestaMap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun escucharInformesClienteEnTiempoReal(
        clienteNombre: String,
        onInformesActualizados: (List<Map<String, Any>>) -> Unit
    ) {
        try {
            val clienteCleanKey = clienteNombre.lowercase().replace(Regex("[^a-z0-9]"), "_")
            if (clienteCleanKey.isBlank()) return

            val clienteRef = dbRef?.child("servicios")?.child(clienteCleanKey)?.child("informes_taller")
            clienteRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaInformes = mutableListOf<Map<String, Any>>()
                    for (informeSnapshot in snapshot.children) {
                        try {
                            val map = informeSnapshot.value as? Map<String, Any> ?: continue
                            listaInformes.add(map)
                        } catch (_: Exception) {}
                    }
                    onInformesActualizados(listaInformes)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error en escucharInformesClienteEnTiempoReal: ${error.message}")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun escucharOrdenEnTiempoReal(ordenId: Int, onOrdenActualizada: (OrdenServicio) -> Unit) {
        try {
            if (ordenId <= 0) return
            dbRef?.child("ordenes")?.child(ordenId.toString())
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            val ordenMap = snapshot.value as? Map<*, *> ?: return
                            val id = parsearInt(ordenMap["id"]).let { if (it == 0) ordenId else it }
                            val cliente = ordenMap["cliente"] as? String ?: ""
                            val auto = ordenMap["auto"] as? String ?: ""
                            val fecha = ordenMap["fecha"] as? String ?: ""
                            val kilometraje = parsearInt(ordenMap["kilometraje"])
                            val fallaReportada = ordenMap["fallaReportada"] as? String ?: ""
                            val diagnostico = ordenMap["diagnostico"] as? String ?: ""
                            val trabajoRealizado = ordenMap["trabajoRealizado"] as? String ?: ""
                            val estado = ordenMap["estado"] as? String ?: "EN ESPERA"
                            val porcentajeAvance = parsearInt(ordenMap["porcentajeAvance"])
                            val fechaEntrega = ordenMap["fechaEntrega"] as? String ?: ""
                            val costoManoObra = parsearDouble(ordenMap["costoManoObra"])
                            val costoRefacciones = parsearDouble(ordenMap["costoRefacciones"])
                            val iva = parsearDouble(ordenMap["iva"])
                            val total = parsearDouble(ordenMap["total"])

                            val ordenDescargada = OrdenServicio(
                                id = id,
                                cliente = cliente,
                                auto = auto,
                                fecha = fecha,
                                kilometraje = kilometraje,
                                fallaReportada = fallaReportada,
                                diagnostico = diagnostico,
                                trabajoRealizado = trabajoRealizado,
                                estado = estado,
                                porcentajeAvance = porcentajeAvance,
                                fechaEntrega = fechaEntrega,
                                costoManoObra = costoManoObra,
                                costoRefacciones = costoRefacciones,
                                iva = iva,
                                total = total
                            )
                            onOrdenActualizada(ordenDescargada)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al deserializar orden $ordenId: ${e.message}")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "Error en escucharOrdenEnTiempoReal: ${error.message}")
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun escucharTodasLasOrdenesEnTiempoReal(onOrdenesActualizadas: (List<OrdenServicio>) -> Unit) {
        try {
            dbRef?.child("ordenes")?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "📥 snapshot de /ordenes recibido. Hijos: ${snapshot.childrenCount}")
                    val lista = mutableListOf<OrdenServicio>()
                    for (child in snapshot.children) {
                        try {
                            val ordenMap = child.value as? Map<*, *> ?: continue
                            val keyId = child.key?.toIntOrNull() ?: 0
                            val id = parsearInt(ordenMap["id"]).let { if (it == 0) keyId else it }
                            val cliente = ordenMap["cliente"] as? String ?: ""
                            val auto = ordenMap["auto"] as? String ?: ""
                            val fecha = ordenMap["fecha"] as? String ?: ""
                            val kilometraje = parsearInt(ordenMap["kilometraje"])
                            val fallaReportada = ordenMap["fallaReportada"] as? String ?: ""
                            val diagnostico = ordenMap["diagnostico"] as? String ?: ""
                            val trabajoRealizado = ordenMap["trabajoRealizado"] as? String ?: ""
                            val estado = ordenMap["estado"] as? String ?: "EN ESPERA"
                            val porcentajeAvance = parsearInt(ordenMap["porcentajeAvance"])
                            val fechaEntrega = ordenMap["fechaEntrega"] as? String ?: ""
                            val costoManoObra = parsearDouble(ordenMap["costoManoObra"])
                            val costoRefacciones = parsearDouble(ordenMap["costoRefacciones"])
                            val iva = parsearDouble(ordenMap["iva"])
                            val total = parsearDouble(ordenMap["total"])

                            lista.add(
                                OrdenServicio(
                                    id = id,
                                    cliente = cliente,
                                    auto = auto,
                                    fecha = fecha,
                                    kilometraje = kilometraje,
                                    fallaReportada = fallaReportada,
                                    diagnostico = diagnostico,
                                    trabajoRealizado = trabajoRealizado,
                                    estado = estado,
                                    porcentajeAvance = porcentajeAvance,
                                    fechaEntrega = fechaEntrega,
                                    costoManoObra = costoManoObra,
                                    costoRefacciones = costoRefacciones,
                                    iva = iva,
                                    total = total
                                )
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error procesando hijo en /ordenes: ${e.message}")
                        }
                    }
                    Log.d(TAG, "✅ Total órdenes deserializadas correctamente: ${lista.size}")
                    onOrdenesActualizadas(lista)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "❌ Error escuchando /ordenes en Firebase: ${error.message} (${error.details})")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error al suscribirse a /ordenes: ${e.message}")
        }
    }

    // ======================================================
    // 2. SINCRONIZACIÓN DE AUTOS DEL CLIENTE
    // ======================================================

    fun subirAutoAFirebase(auto: Auto) {
        try {
            if (auto.id > 0) {
                dbRef?.child("autos")?.child(auto.id.toString())?.setValue(auto)
            } else {
                val nuevoKey = dbRef?.child("autos")?.push()?.key ?: return
                dbRef?.child("autos")?.child(nuevoKey)?.setValue(auto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun escucharAutosEnTiempoReal(onAutosActualizados: (List<Auto>) -> Unit) {
        try {
            dbRef?.child("autos")?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaAutos = mutableListOf<Auto>()
                    for (child in snapshot.children) {
                        try {
                            val autoMap = child.value as? Map<*, *> ?: continue
                            val id = parsearInt(autoMap["id"])
                            val cliente = autoMap["cliente"] as? String ?: ""
                            val marca = autoMap["marca"] as? String ?: ""
                            val modelo = autoMap["modelo"] as? String ?: ""
                            val anio = parsearInt(autoMap["anio"])
                            val placa = autoMap["placa"] as? String ?: ""
                            val kilometraje = parsearInt(autoMap["kilometraje"])
                            val vin = autoMap["vin"] as? String ?: ""
                            val color = autoMap["color"] as? String ?: ""

                            listaAutos.add(
                                Auto(
                                    id = id,
                                    cliente = cliente,
                                    marca = marca,
                                    modelo = modelo,
                                    anio = anio,
                                    placa = placa,
                                    kilometraje = kilometraje,
                                    vin = vin,
                                    color = color
                                )
                            )
                        } catch (_: Exception) {}
                    }
                    onAutosActualizados(listaAutos)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ======================================================
    // 3. SINCRONIZACIÓN DE ALERTAS DE MANTENIMIENTO (TIEMPO REAL)
    // ======================================================

    fun subirAlertaAFirebase(alerta: AlertaMantenimiento) {
        try {
            val key = if (alerta.id.isNotBlank()) alerta.id else dbRef?.child("alertas")?.push()?.key ?: return
            val alertaFinal = alerta.copy(id = key)
            dbRef?.child("alertas")?.child(key)?.setValue(alertaFinal)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun escucharAlertasEnTiempoReal(onAlertasActualizadas: (List<AlertaMantenimiento>) -> Unit) {
        try {
            dbRef?.child("alertas")?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaAlertas = mutableListOf<AlertaMantenimiento>()
                    for (child in snapshot.children) {
                        try {
                            val map = child.value as? Map<*, *> ?: continue
                            val id = map["id"] as? String ?: ""
                            val clienteNombre = map["clienteNombre"] as? String ?: ""
                            val autoPlaca = map["autoPlaca"] as? String ?: ""
                            val tipoAlerta = map["tipoAlerta"] as? String ?: "Por Tiempo"
                            val fechaRevision = map["fechaRevision"] as? String ?: ""
                            val mensaje = map["mensaje"] as? String ?: ""

                            listaAlertas.add(
                                AlertaMantenimiento(
                                    id = id,
                                    clienteNombre = clienteNombre,
                                    autoPlaca = autoPlaca,
                                    tipoAlerta = tipoAlerta,
                                    fechaRevision = fechaRevision,
                                    mensaje = mensaje
                                )
                            )
                        } catch (_: Exception) {}
                    }
                    onAlertasActualizadas(listaAlertas)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
