package com.example.bitacoraautomotriz.ui.welcome

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.bitacoraautomotriz.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.delay

data class AppLanguage(val code: String, val displayName: String, val flag: String)

val supportedLanguages = listOf(
    AppLanguage("es", "Español", "🇪🇸"),
    AppLanguage("en", "English", "🇺🇸"),
    AppLanguage("pt", "Português", "🇧🇷"),
    AppLanguage("fr", "Français", "🇫🇷"),
    AppLanguage("de", "Deutsch", "🇩🇪"),
    AppLanguage("it", "Italiano", "🇮🇹"),
)

// GENERADOR DE CÓDIGO QR REAL ESTÁNDAR CON ALTA CORRECCIÓN DE ERRORES (LEVEL H)
fun generarQrBitmapRealConAltaCorreccion(contenido: String, ancho: Int = 450, alto: Int = 450): Bitmap? {
    return try {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H
        )
        val bitMatrix = MultiFormatWriter().encode(
            contenido,
            BarcodeFormat.QR_CODE,
            ancho,
            alto,
            hints
        )
        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun WelcomeScreen(
    onTallerClick: () -> Unit,
    onClienteClick: () -> Unit,
) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2200L)
        showSplash = false
    }

    if (showSplash) {
        SplashContent()
    } else {
        WelcomeContent(
            onTallerClick = onTallerClick,
            onClienteClick = onClienteClick,
        )
    }
}

@Composable
private fun SplashContent() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(id = R.drawable.ford_model_a_feliz),
            contentDescription = stringResource(id = R.string.app_name),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun WelcomeContent(
    onTallerClick: () -> Unit,
    onClienteClick: () -> Unit,
) {
    val context = LocalContext.current
    val esAppCliente = remember { context.packageName.lowercase().contains("cliente") }

    var selectedLanguage by remember {
        val currentTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val match = supportedLanguages.find { currentTag.startsWith(it.code) }
        mutableStateOf(match ?: supportedLanguages[0])
    }
    var menuExpanded by remember { mutableStateOf(false) }

    val urlDescarga = "https://appdistribution.firebase.dev/i/d15eaf1dda6c6929"
    val qrBitmap = remember(urlDescarga) { generarQrBitmapRealConAltaCorreccion(urlDescarga, 450, 450) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF03070D), Color(0xFF0B1E33), Color(0xFF03070D))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {

            // ---------- Selector de idioma REAL ----------
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                Surface(
                    onClick = { menuExpanded = true },
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF102338),
                    tonalElevation = 6.dp,
                    border = BorderStroke(1.dp, Color(0xFF3FA9F5).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedLanguage.flag, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedLanguage.displayName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Seleccionar idioma",
                            tint = Color.White
                        )
                    }
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF102338))
                ) {
                    supportedLanguages.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(lang.flag, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(lang.displayName, color = Color.White)
                                }
                            },
                            onClick = {
                                selectedLanguage = lang
                                menuExpanded = false
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags(lang.code)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---------- Logo / Título ----------
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (esAppCliente) Icons.Default.Person else Icons.Default.Build,
                    contentDescription = null,
                    tint = if (esAppCliente) Color(0xFF43A047) else Color(0xFF3FA9F5),
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = stringResource(id = R.string.brand_bitacora),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (esAppCliente) "CLIENTE" else stringResource(id = R.string.brand_automotriz),
                    color = if (esAppCliente) Color(0xFF7DFFB2) else Color(0xFFFFB300),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (esAppCliente) {
                // VISTA EXCLUSIVA APP CLIENTE: SÓLO MUESTRA LA TARJETA DEL ÁREA DEL CLIENTE (SIN TALLER Y SIN QR)
                SectionCard(
                    labelIcon = Icons.Default.Person,
                    label = "INGRESAR AL ÁREA DEL CLIENTE",
                    buttonLabel = stringResource(id = R.string.ingresar),
                    accentColor = Color(0xFF43A047),
                    accentColorDark = Color(0xFF1B5E20),
                    onClick = onClienteClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                // VISTA APP TALLER (MÓVIL PRINCIPAL/MECÁNICO): SECCIÓN TALLER CON BOTÓN INGRESAR + CÓDIGO QR CLIENTE
                SectionCard(
                    labelIcon = Icons.Default.Build,
                    label = stringResource(id = R.string.taller),
                    buttonLabel = stringResource(id = R.string.ingresar),
                    accentColor = Color(0xFF1E88E5),
                    accentColorDark = Color(0xFF0D47A1),
                    onClick = onTallerClick,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .border(2.dp, Color(0xFF43A047), RoundedCornerShape(28.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ford_model_a_feliz),
                            contentDescription = "Fondo Cliente",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                onClick = onClienteClick,
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White,
                                shadowElevation = 10.dp
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    if (qrBitmap != null) {
                                        Image(
                                            bitmap = qrBitmap.asImageBitmap(),
                                            contentDescription = "Código QR App Cliente",
                                            modifier = Modifier.size(140.dp)
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.5.dp, Color.Black),
                                            shadowElevation = 4.dp,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ford_model_a_feliz),
                                                contentDescription = "Logo Ford 1928",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.padding(2.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier.size(140.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1B5E20))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.cliente),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onClienteClick,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(stringResource(id = R.string.ingresar), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(">")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    labelIcon: ImageVector,
    label: String,
    buttonLabel: String,
    accentColor: Color,
    accentColorDark: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .border(2.dp, accentColor, RoundedCornerShape(28.dp))
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.ford_model_a_feliz),
                contentDescription = "Ford Model A Roadster 1928",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColorDark)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = labelIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(buttonLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(">")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 780)
@Composable
private fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(onTallerClick = {}, onClienteClick = {})
    }
}
