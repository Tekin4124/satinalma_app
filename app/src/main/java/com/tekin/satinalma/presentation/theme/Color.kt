/*
 * Color.kt
 * Material 3 renk paleti tanımları — kurumsal mavi tonları ağırlıklı tema.
 * Malzeme durum renkleri ve aciliyet renkleri de burada tanımlanır.
 */
package com.tekin.satinalma.presentation.theme

import androidx.compose.ui.graphics.Color

// Birincil renkler — Koyu Mavi
val Primary = Color(0xFF1565C0)
val PrimaryVariant = Color(0xFF0D47A1)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFD6E4FF)
val OnPrimaryContainer = Color(0xFF001A41)

// İkincil renkler — Açık Mavi
val Secondary = Color(0xFF42A5F5)
val SecondaryVariant = Color(0xFF1E88E5)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFCCE5FF)
val OnSecondaryContainer = Color(0xFF001D36)

// Arka Plan ve Yüzey
val Background = Color(0xFFFAFAFA)
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFE7EAF0)
val OnBackground = Color(0xFF1C1B1F)
val OnSurface = Color(0xFF1C1B1F)
val OnSurfaceVariant = Color(0xFF44474F)

// Hata
val Error = Color(0xFFB00020)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)

// Outline
val Outline = Color(0xFF79747E)
val OutlineVariant = Color(0xFFCAC4D0)

// Başarı ve Uyarı
val Success = Color(0xFF2E7D32)
val Warning = Color(0xFFE65100)
val Info = Color(0xFF0277BD)

// Malzeme Alım Durumu Renkleri
val StatusPending = Color(0xFFFF8F00)
val StatusInProgress = Color(0xFF1565C0)
val StatusPurchased = Color(0xFF2E7D32)
val StatusDelivered = Color(0xFF6A1B9A)
val StatusCancelled = Color(0xFFB00020)

// Aciliyet Renkleri
val UrgencyLow = Color(0xFF558B2F)
val UrgencyNormal = Color(0xFF1565C0)
val UrgencyHigh = Color(0xFFE65100)
val UrgencyUrgent = Color(0xFFB71C1C)

// Gradient renkleri — TopAppBar için koyu→orta mavi geçiş
val GradientStart = Color(0xFF0D47A1)
val GradientEnd = Color(0xFF1976D2)

// Şoför "Yeni" badge rengi
val NewBadge = Color(0xFFD32F2F)
