/*
 * Theme.kt
 * Uygulama Material 3 tema yapılandırması.
 * Renk şeması, tipografi ve şekil sistemi bir araya getirilir.
 */
package com.tekin.satinalma.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Açık tema renk şeması — kurumsal mavi tonları
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    background = Background,
    surface = Surface,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceVariant = SurfaceVariant,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    outline = Outline,
    outlineVariant = OutlineVariant
)

/**
 * Satınalma uygulaması ana tema composable'ı
 */
@Composable
fun SatinalmaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
