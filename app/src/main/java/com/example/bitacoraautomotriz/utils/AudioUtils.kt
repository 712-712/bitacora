package com.example.bitacoraautomotriz.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.bitacoraautomotriz.R

/**
 * Utilidad exclusiva para la reproducción controlada del sonido de motor
 * únicamente en la pantalla de transición (Splash / WelcomeScreen).
 */
object AudioUtils {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Reproduce el sonido de motor de automóvil EXACTAMENTE UNA SOLA VEZ
     * durante la pantalla de transición/Splash. Notifica mediante [onTerminado]
     * al finalizar la reproducción completa para ir automáticamente a la pantalla de bienvenida.
     */
    fun reproducirSonidoMotorUnaVez(context: Context, onTerminado: (() -> Unit)? = null) {
        try {
            detenerSonido()

            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.sonido_motor)?.apply {
                isLooping = false
                setOnCompletionListener {
                    detenerSonido()
                    onTerminado?.invoke()
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onTerminado?.invoke()
        }
    }

    /**
     * Alias para compatibilidad que invoca la reproducción de una sola vez.
     */
    fun reproducirSonidoMotorDosVeces(context: Context, onTerminado: (() -> Unit)? = null) {
        reproducirSonidoMotorUnaVez(context, onTerminado)
    }

    /**
     * Detiene y libera los recursos del reproductor de audio de forma segura.
     */
    fun detenerSonido() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        } catch (_: Exception) {
        } finally {
            mediaPlayer = null
        }
    }
}
