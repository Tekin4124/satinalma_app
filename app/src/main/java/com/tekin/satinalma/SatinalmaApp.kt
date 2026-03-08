/*
 * SatinalmaApp.kt
 * Hilt uygulama sınıfı — dependency injection kapsayıcısını başlatır.
 * Application sınıfı tüm Hilt bağımlılıklarının kök noktasıdır.
 */
package com.tekin.satinalma

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Satınalma uygulaması Application sınıfı
 * Hilt DI kapsayıcısını otomatik olarak başlatır
 */
@HiltAndroidApp
class SatinalmaApp : Application()
