/*
 * MainActivity.kt
 * Uygulamanın tek aktivitesi — Jetpack Compose UI'nın başlangıç noktası.
 * Navigasyon grafiği burada başlatılır.
 */
package com.tekin.satinalma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tekin.satinalma.navigation.AppNavGraph
import com.tekin.satinalma.presentation.theme.SatinalmaTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Uygulamanın ana aktivitesi — Single Activity Architecture
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SatinalmaTheme {
                AppNavGraph()
            }
        }
    }
}
