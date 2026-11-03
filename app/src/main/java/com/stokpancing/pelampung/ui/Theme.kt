package com.stokpancing.pelampung.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val WarnaMasuk = Color(0xFF2E7D32)
val WarnaKeluar = Color(0xFFC62828)
val WarnaMenipis = Color(0xFFE65100)

private val skemaWarna = lightColorScheme(
    primary = Color(0xFF0D47A1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFFE65100),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBC8),
    onSecondaryContainer = Color(0xFF331200),
    background = Color(0xFFF5F7FB),
    surface = Color(0xFFF5F7FB),
)

@Composable
fun StokTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = skemaWarna, content = content)
}
