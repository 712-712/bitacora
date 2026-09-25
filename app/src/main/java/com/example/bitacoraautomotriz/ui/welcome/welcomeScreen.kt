package com.example.bitacoraautomotriz.ui.welcome

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.bitacoraautomotriz.R
import kotlinx.coroutines.delay

// =========================================
// IDIOMAS SOPORTADOS
// Los NOMBRES de los idiomas (Español, English...) se dejan escritos así
// a propósito: son "endónimos" (el nombre que cada idioma usa para sí
// mismo) y normalmente NO se traducen, ni siquiera en apps grandes.
//
// Lo que SÍ cambia con el idioma es el contenido de la app, que ahora
// viene de res/values*/strings.xml en vez de un mapa en Kotlin.
// =========================================
data class AppLanguage(val code: String, val displayName: String, val flag: String)

val supportedLanguages = listOf(
    AppLanguage("es", "Español", "🇪🇸"),
    AppLanguage("en", "English", "🇺🇸"),
    AppLanguage("pt", "Português", "🇧🇷"),
    AppLanguage("fr", "Français", "🇫🇷"),
    AppLanguage("de", "Deutsch", "🇩🇪"),
    AppLanguage("it", "Italiano", "🇮🇹"),
)

/**
 * Pantalla de bienvenida completa:
 * 1) Splash de ~2 s con la imagen del Ford a pantalla completa.
 * 2) Panel con selector de idioma REAL (cambia toda la app, no solo esta
 *    pantalla) + secciones TALLER / CLIENTE.
 */
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
    // Idioma actualmente aplicado a la app (lo lee de AppCompatDelegate al
    // entrar, para que el botón muestre el idioma correcto tras reabrir la app).
    var selectedLanguage by remember {
        val currentTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val match = supportedLanguages.find { currentTag.startsWith(it.code) }
        mutableStateOf(match ?: supportedLanguages[0])
    }
    var menuExpanded by remember { mutableStateOf(false) }

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

            // ---------- Selector de idioma REAL (cambia toda la app) ----------
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
                                // 🔑 Esto es lo que cambia el idioma de TODA la app,
                                // no solo de esta pantalla.
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
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = Color(0xFF3FA9F5),
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
                    text = stringResource(id = R.string.brand_automotriz),
                    color = Color(0xFFFFB300),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ---------- Sección TALLER (mitad superior) ----------
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

            Spacer(modifier = Modifier.height(14.dp))

            // ---------- Sección CLIENTE (mitad inferior) ----------
            SectionCard(
                labelIcon = Icons.Default.Person,
                label = stringResource(id = R.string.cliente),
                buttonLabel = stringResource(id = R.string.ingresar),
                accentColor = Color(0xFF43A047),
                accentColorDark = Color(0xFF1B5E20),
                onClick = onClienteClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
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
        // ---------- Foto del auto (sin overlay, se ve completa) ----------
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.ford_model_a_feliz),
                contentDescription = "Ford Model A Roadster 1928",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ---------- Franja inferior: ícono + texto + botón ----------
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
