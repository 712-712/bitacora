package com.example.bitacoraautomotriz.ui.mapa

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun MapaScreen(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->


            Configuration.getInstance().load(
                context,
                context.getSharedPreferences(
                    "osmdroid",
                    0
                )
            )

            MapView(context).apply {

                setMultiTouchControls(true)

                controller.setZoom(15.0)

                controller.setCenter(
                    GeoPoint(
                        19.4326,
                        -99.1332
                    )
                )

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }
    )


}
