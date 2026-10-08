package com.example.bitacoraautomotriz.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.bitacoraautomotriz.R

object AudioUtils {

    fun reproducirSonidoMotorTresVeces(context: Context) {
        try {
            var repeticiones = 0
            val maxRepeticiones = 3
            var mp: MediaPlayer? = null

            mp = MediaPlayer.create(context, R.raw.sonido_motor)
            mp?.apply {
                setOnCompletionListener {
                    repeticiones++
                    if (repeticiones < maxRepeticiones) {
                        try {
                            seekTo(0)
                            start()
                        } catch (_: Exception) {
                            try { release() } catch (_: Exception) {}
                        }
                    } else {
                        try { release() } catch (_: Exception) {}
                    }
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun reproducirSonidoMotorUnaVez(context: Context) {
        try {
            val mp = MediaPlayer.create(context, R.raw.sonido_motor)
            mp?.apply {
                setOnCompletionListener {
                    try { release() } catch (_: Exception) {}
                }
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
