package com.example.lapuja.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(

    primary = RosaPrincipal,

    secondary = RosaClaro,

    tertiary = Dorado,

    background = FondoOscuro,

    surface = TarjetaOscura,

    onPrimary = TextoBlanco,

    onSecondary = TextoBlanco,

    onBackground = TextoBlanco,

    onSurface = TextoBlanco
)

@Composable
fun LaPujaTheme(

    darkTheme: Boolean = true,

    content: @Composable () -> Unit
) {

    MaterialTheme(

        colorScheme = DarkColorScheme,

        typography = Typography,

        content = content
    )
}