package com.example.bitacoraautomotriz.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppUtils {

    /**
     * Abre WhatsApp directamente con el número (sin mensaje predefinido)
     */
    fun abrirWhatsApp(context: Context, telefono: String) {
        if (telefono.isBlank()) return

        val numeroLimpio = telefono.replace(Regex("[^0-9+]"), "")
        val codigoPais = "52"
        val numeroFinal = if (numeroLimpio.startsWith(codigoPais) || numeroLimpio.startsWith("+")) {
            numeroLimpio.replace("+", "")
        } else {
            "$codigoPais$numeroLimpio"
        }

        val uri = Uri.parse("https://wa.me/$numeroFinal")

        val intentApp = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
        }

        val intentWeb = Intent(Intent.ACTION_VIEW, uri)

        try {
            context.startActivity(intentApp)
        } catch (e: Exception) {
            context.startActivity(intentWeb)
        }
    }

    /**
     * ✅ NUEVA FUNCIÓN: Abre WhatsApp con un mensaje predefinido (para Deep Links)
     */
    fun abrirWhatsAppConMensaje(context: Context, telefono: String, mensaje: String) {
        if (telefono.isBlank()) return

        val numeroLimpio = telefono.replace(Regex("[^0-9+]"), "")
        val codigoPais = "52"
        val numeroFinal = if (numeroLimpio.startsWith(codigoPais) || numeroLimpio.startsWith("+")) {
            numeroLimpio.replace("+", "")
        } else {
            "$codigoPais$numeroLimpio"
        }

        // Codificamos el mensaje para que los espacios y saltos de línea funcionen en la URL
        val mensajeCodificado = Uri.encode(mensaje)
        val uri = Uri.parse("https://wa.me/$numeroFinal?text=$mensajeCodificado")

        val intentApp = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.whatsapp")
        }

        val intentWeb = Intent(Intent.ACTION_VIEW, uri)

        try {
            context.startActivity(intentApp)
        } catch (e: Exception) {
            context.startActivity(intentWeb)
        }
    }
}
