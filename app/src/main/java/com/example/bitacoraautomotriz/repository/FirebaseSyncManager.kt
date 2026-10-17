package com.example.bitacoraautomotriz.repository

import android.util.Log
import com.example.bitacoraautomotriz.data.Auto
import com.example.bitacoraautomotriz.data.Factura
import com.example.bitacoraautomotriz.data.OrdenServicio
import com.example.bitacoraautomotriz.data.RecepcionVehiculo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
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

    private const val TAG = "FirebaseSyncManager"
    private const val FIREBASE_URL = "https://bitacoraautomotriz-default-rtdb.firebaseio.com"

    /**
     * Getter dinámico para obtener siempre una referencia activa y válida de Firebase Realtime Database.
     * Se evita el uso de 'by lazy' para prevenir que un fallo inicial cachee un 'null' permanente.
     */
    private val dbRef: DatabaseReference?
        get() {
            return try {
                FirebaseDatabase.getInstance(FIREBASE_URL).reference
            } catch (e1: Exception) {
                try {
                    FirebaseDatabase.getInstance().reference
                } catch (e2: Exception) {
                    Log.e(TAG, "❌ Error inicializando FirebaseDatabase: ${e2.message}")
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
            val ref = dbRef ?: run {
                Log.e(TAG, "❌ dbRef es null al intentar subir orden")
                return
            }
            val key = if (orden.id > 0) orden.id.toString() else (ref.child("ordenes").push().key ?: return)
            val ordenFinal = if (orden.id == 0 && key.toIntOrNull() != null) orden.copy(id = key.toInt()) else orden

            // 1.1 RUTA DIRECTA DE ÓRDENES EN NODO /ordenes/key
            ref.child("ordenes").child(key).setValue(ordenFinal)
                .addOnSuccessListener {
                    Log.d(TAG, "✅ Orden #${ordenFinal.id} subida exitosamente a /ordenes/$key")
                }
                .addOnFailureListener { err ->
                    Log.e(TAG, "❌ Error al subir orden a Firebase: ${err.message}")
                }

            // 1.2 RUTA ESTRUCTURADA POR CLIENTE Y SERVICIO EN NODO /servicios
            val clienteCleanKey = ordenFinal.cliente.lowercase().replace(Regex("[^a-z0-9]"), "_")
            if (clienteCleanKey.isNotBlank()) {
                val informesRef = ref.child("servicios").child(clienteCleanKey).child("informes_taller")
                informesRef.child("informe_$key").setValue(ordenFinal)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en subirOrdenAFirebase: ${e.message}")
        }
    }

    /**
     * Elimina permanentemente la orden de cotización de Firebase Realtime Database
     * tanto del nodo central /ordenes como del nodo de /servicios por cliente.
     */
    fun eliminarOrdenDeFirebase(orden: OrdenServicio) {
        try {
            val ref = dbRef ?: return
            val key = orden.id.toString()

            // 1. Eliminar de /ordenes/key
            ref.child("ordenes").child(key).removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "🗑️ Orden #$key eliminada exitosamente de Firebase /ordenes/$key")
                }
                .addOnFailureListener { err ->
                    Log.e(TAG, "❌ Error al eliminar orden de Firebase: ${err.message}")
                }

            // 2. Eliminar de /servicios/clienteCleanKey/informes_taller/informe_key
            val clienteCleanKey = orden.cliente.lowercase().replace(Regex("[^a-z0-9]"), "_")
            if (clienteCleanKey.isNotBlank()) {
                ref.child("servicios").child(clienteCleanKey).child("informes_taller").child("informe_$key").removeValue()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en eliminarOrdenDeFirebase: ${e.message}")
        }
    }

    fun publicarInformeTallerConPush(clienteNombre: String, informeMap: Map<String, Any>) {
        try {
            val ref = dbRef ?: return
            val clienteCleanKey = clienteNombre.lowercase().replace(Regex("[^a-z0-9]"), "_")
            if (clienteCleanKey.isNotBlank()) {
                val informesRef = ref.child("servicios").child(clienteCleanKey).child("informes_taller")
                informesRef.push().setValue(informeMap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun publicarRespuestaClienteConPush(clienteNombre: String, respuestaMap: Map<String, Any>) {
        try {
            val ref = dbRef ?: return
            val clienteCleanKey = clienteNombre.lowercase().replace(Regex("[^a-z0-9]"), "_")
            if (clienteCleanKey.isNotBlank()) {
                val respuestasRef = ref.child("servicios").child(clienteCleanKey).child("respuestas_cliente")
                respuestasRef.push().setValue(respuestaMap)
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
            val ref = dbRef ?: return
            val clienteCleanKey = clienteNombre.lowercase().replace(Regex("[^a-z0-9]"), "_")
            if (clienteCleanKey.isBlank()) return

            val clienteRef = ref.child("servicios").child(clienteCleanKey).child("informes_taller")
            clienteRef.addValueEventListener(object : ValueEventListener {
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
            val ref = dbRef ?: return
            ref.child("ordenes").child(ordenId.toString())
                .addValueEventListener(object : ValueEventListener {
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
            val ref = dbRef ?: run {
                Log.e(TAG, "❌ dbRef es null al escuchar /ordenes")
                return
            }
            ref.child("ordenes").addValueEventListener(object : ValueEventListener {
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
            val ref = dbRef ?: return
            if (auto.id > 0) {
                ref.child("autos").child(auto.id.toString()).setValue(auto)
            } else {
                val nuevoKey = ref.child("autos").push().key ?: return
                ref.child("autos").child(nuevoKey).setValue(auto)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun escucharAutosEnTiempoReal(onAutosActualizados: (List<Auto>) -> Unit) {
        try {
            val ref = dbRef ?: return
            ref.child("autos").addValueEventListener(object : ValueEventListener {
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
            val ref = dbRef ?: run {
                Log.e(TAG, "❌ dbRef es null al intentar subir alerta")
                return
            }
            val key = if (alerta.id.isNotBlank()) alerta.id else ref.child("alertas").push().key ?: return
            val alertaFinal = alerta.copy(id = key)
            ref.child("alertas").child(key).setValue(alertaFinal)
                .addOnSuccessListener {
                    Log.d(TAG, "✅ Alerta #${key} subida exitosamente a /alertas/$key: ${alertaFinal.mensaje}")
                }
                .addOnFailureListener { err ->
                    Log.e(TAG, "❌ Error al subir alerta a Firebase: ${err.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en subirAlertaAFirebase: ${e.message}")
        }
    }

    fun escucharAlertasEnTiempoReal(onAlertasActualizadas: (List<AlertaMantenimiento>) -> Unit) {
        try {
            val ref = dbRef ?: run {
                Log.e(TAG, "❌ dbRef es null al intentar escuchar /alertas")
                return
            }
            ref.child("alertas").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "📥 snapshot de /alertas recibido. Hijos: ${snapshot.childrenCount}")
                    val listaAlertas = mutableListOf<AlertaMantenimiento>()
                    for (child in snapshot.children) {
                        try {
                            val map = child.value as? Map<*, *> ?: continue
                            val id = map["id"] as? String ?: child.key ?: ""
                            val clienteNombre = map["clienteNombre"] as? String ?: ""
                            val autoPlaca = map["autoPlaca"] as? String ?: ""
                            val tipoAlerta = map["tipoAlerta"] as? String ?: "Por Tiempo"
                            val fechaRevision = map["fechaRevision"] as? String ?: ""
                            val mensaje = map["mensaje"] as? String ?: ""

                            if (mensaje.isNotBlank()) {
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
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error procesando hijo en /alertas: ${e.message}")
                        }
                    }
                    Log.d(TAG, "✅ Total alertas recibidas en tiempo real: ${listaAlertas.size}")
                    onAlertasActualizadas(listaAlertas)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "❌ Error escuchando /alertas en Firebase: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error en escucharAlertasEnTiempoReal: ${e.message}")
        }
    }

    // ======================================================
    // 4. SINCRONIZACIÓN DE RECEPCIÓN Y CHECK-IN LEGAL DE INGRESO
    // ======================================================

    fun subirRecepcionAFirebase(recepcion: RecepcionVehiculo) {
        try {
            val ref = dbRef ?: run {
                Log.e(TAG, "❌ dbRef es null al intentar subir recepción")
                return
            }
            val key = if (recepcion.id > 0) recepcion.id.toString() else (ref.child("recepciones").push().key ?: return)
            val recepcionFinal = if (recepcion.id == 0 && key.toIntOrNull() != null) recepcion.copy(id = key.toInt()) else recepcion

            ref.child("recepciones").child(key).setValue(recepcionFinal)
                .addOnSuccessListener {
                    Log.d(TAG, "✅ Recepción #${recepcionFinal.id} subida exitosamente a /recepciones/$key")
                }
                .addOnFailureListener { err ->
                    Log.e(TAG, "❌ Error al subir recepción a Firebase: ${err.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en subirRecepcionAFirebase: ${e.message}")
        }
    }

    fun escucharRecepcionesEnTiempoReal(onRecepcionesActualizadas: (List<RecepcionVehiculo>) -> Unit) {
        try {
            val ref = dbRef ?: run {
                Log.e(TAG, "❌ dbRef es null al escuchar /recepciones")
                return
            }
            ref.child("recepciones").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "📥 snapshot de /recepciones recibido. Hijos: ${snapshot.childrenCount}")
                    val listaRecepciones = mutableListOf<RecepcionVehiculo>()
                    for (child in snapshot.children) {
                        try {
                            val map = child.value as? Map<*, *> ?: continue
                            val keyId = child.key?.toIntOrNull() ?: 0
                            val id = parsearInt(map["id"]).let { if (it == 0) keyId else it }
                            val cliente = map["cliente"] as? String ?: ""
                            val auto = map["auto"] as? String ?: ""
                            val placa = map["placa"] as? String ?: ""
                            val kilometraje = parsearInt(map["kilometraje"])
                            val fechaHora = map["fechaHora"] as? String ?: ""
                            val hashIntegridadSha256 = map["hashIntegridadSha256"] as? String ?: ""
                            val tiempoRetencion = map["tiempoRetencion"] as? String ?: ""

                            if (cliente.isNotBlank() || placa.isNotBlank()) {
                                listaRecepciones.add(
                                    RecepcionVehiculo(
                                        id = id,
                                        cliente = cliente,
                                        auto = auto,
                                        placa = placa,
                                        kilometraje = kilometraje,
                                        fechaHora = fechaHora,
                                        hashIntegridadSha256 = hashIntegridadSha256,
                                        tiempoRetencion = tiempoRetencion
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error procesando hijo en /recepciones: ${e.message}")
                        }
                    }
                    Log.d(TAG, "✅ Total recepciones recibidas en tiempo real: ${listaRecepciones.size}")
                    onRecepcionesActualizadas(listaRecepciones)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "❌ Error escuchando /recepciones en Firebase: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error en escucharRecepcionesEnTiempoReal: ${e.message}")
        }
    }

    // ======================================================
    // 5. FACTURACIÓN (CLIENTE ↔ TALLER)
    // ======================================================

    private fun claveCliente(nombre: String): String =
        nombre.lowercase().replace(Regex("[^a-z0-9]"), "_")

    private fun aMapa(snap: DataSnapshot): Map<String, Any> {
        val mapa = mutableMapOf<String, Any>()
        for (hijo in snap.children) {
            val k = hijo.key ?: continue
            val v = hijo.value ?: continue
            mapa[k] = v
        }
        return mapa
    }

    /** CLIENTE → TALLER: una sola solicitud por cliente (se sobrescribe, no se duplica). */
    fun publicarSolicitudFactura(clienteNombre: String, datos: Map<String, Any>) {
        try {
            val ref = dbRef ?: return
            val key = claveCliente(clienteNombre)
            if (key.isBlank()) return
            val solicitud = datos.toMutableMap()
            solicitud["estado"] = "pendiente"
            solicitud["fechaSolicitud"] = System.currentTimeMillis()
            ref.child("servicios").child(key).child("solicitud_factura").setValue(solicitud)
                .addOnFailureListener { Log.e(TAG, "❌ Error al publicar solicitud: ${it.message}") }
        } catch (e: Exception) {
            Log.e(TAG, "Error en publicarSolicitudFactura: ${e.message}")
        }
    }

    /** TALLER: lee una sola vez la solicitud de factura que mandó el cliente. */
    fun obtenerSolicitudFactura(clienteNombre: String, onResultado: (Map<String, Any>?) -> Unit) {
        try {
            val ref = dbRef ?: run { onResultado(null); return }
            val key = claveCliente(clienteNombre)
            if (key.isBlank()) { onResultado(null); return }
            ref.child("servicios").child(key).child("solicitud_factura").get()
                .addOnSuccessListener { snap -> onResultado(if (snap.exists()) aMapa(snap) else null) }
                .addOnFailureListener { onResultado(null) }
        } catch (e: Exception) {
            onResultado(null)
        }
    }

    /** TALLER: escucha las solicitudes de factura de todos los clientes. */
    fun escucharSolicitudesFacturaEnTiempoReal(onActualizadas: (List<Map<String, Any>>) -> Unit) {
        try {
            val ref = dbRef ?: return
            ref.child("servicios").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = mutableListOf<Map<String, Any>>()
                    for (clienteSnap in snapshot.children) {
                        val sol = clienteSnap.child("solicitud_factura")
                        if (!sol.exists()) continue
                        val mapa = aMapa(sol).toMutableMap()
                        mapa["claveCliente"] = clienteSnap.key ?: ""
                        lista.add(mapa)
                    }
                    onActualizadas(lista)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "❌ Error escuchando solicitudes: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error en escucharSolicitudesFacturaEnTiempoReal: ${e.message}")
        }
    }

    /** TALLER → CLIENTE: publica la factura y marca la solicitud como emitida. */
    fun publicarFacturaEmitida(factura: Factura) {
        try {
            val ref = dbRef ?: return
            val key = claveCliente(factura.cliente)
            if (key.isBlank()) return
            val folio = factura.numero.replace(Regex("[^A-Za-z0-9_-]"), "-")
            val datos = mapOf<String, Any>(
                "numero" to factura.numero,
                "cliente" to factura.cliente,
                "fecha" to factura.fecha,
                "subtotal" to factura.subtotal,
                "iva" to factura.iva,
                "total" to factura.total
            )
            val base = ref.child("servicios").child(key)
            base.child("facturas_emitidas").child(folio).setValue(datos)
                .addOnFailureListener { Log.e(TAG, "❌ Error al publicar factura: ${it.message}") }
            base.child("solicitud_factura").get().addOnSuccessListener { snap ->
                if (snap.exists()) base.child("solicitud_factura").child("estado").setValue("emitida")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en publicarFacturaEmitida: ${e.message}")
        }
    }

    /** CLIENTE: escucha sus facturas emitidas. */
    fun escucharFacturasEmitidasEnTiempoReal(
        clienteNombre: String,
        onActualizadas: (List<Map<String, Any>>) -> Unit
    ) {
        try {
            val ref = dbRef ?: return
            val key = claveCliente(clienteNombre)
            if (key.isBlank()) return
            ref.child("servicios").child(key).child("facturas_emitidas")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        onActualizadas(snapshot.children.map { aMapa(it) })
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(TAG, "❌ Error escuchando facturas: ${error.message}")
                    }
                })
        } catch (e: Exception) {
            Log.e(TAG, "Error en escucharFacturasEmitidasEnTiempoReal: ${e.message}")
        }
    }
}
