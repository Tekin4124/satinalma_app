/*
 * AppTopBar.kt
 * Uygulama genelinde kullanılan ortak TopAppBar bileşeni.
 * Sol tarafta ikon + başlık, sağ tarafta kullanıcı avatarı + bildirim ikonu.
 * Avatar tıklandığında profil/çıkış menüsü, zil tıklandığında bildirim listesi açılır.
 */
package com.tekin.satinalma.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tekin.satinalma.R
import com.tekin.satinalma.domain.model.AppNotification
import com.tekin.satinalma.domain.model.User
import com.tekin.satinalma.presentation.theme.GradientEnd
import com.tekin.satinalma.presentation.theme.GradientStart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Uygulamanın tüm ana ekranlarında kullanılan ortak TopAppBar bileşeni.
 * Gradient arka plan, kullanıcı avatarı ve bildirim ikonu içerir.
 *
 * @param title Ekran başlığı
 * @param currentUser Oturum açmış kullanıcı
 * @param notifications Kullanıcıya ait bildirim listesi
 * @param onLogout Çıkış yapma geri çağrısı
 * @param onNotificationRead Bildirim okundu işaretleme
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    currentUser: User?,
    notifications: List<AppNotification> = emptyList(),
    onLogout: () -> Unit,
    onNotificationRead: (String) -> Unit = {}
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(GradientStart, GradientEnd)
    )

    var profileMenuExpanded by remember { mutableStateOf(false) }
    var notificationDialogVisible by remember { mutableStateOf(false) }

    val unreadCount = notifications.count { !it.isRead }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = gradientBrush)
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                actionIconContentColor = Color.White
            ),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            actions = {
                // Bildirim ikonu
                IconButton(onClick = { notificationDialogVisible = true }) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge { Text(unreadCount.toString()) }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.notifications),
                            tint = Color.White
                        )
                    }
                }

                // Kullanıcı avatarı + profil menüsü
                if (currentUser != null) {
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { profileMenuExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Yuvarlak avatar — baş harfler
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.initials(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = profileMenuExpanded,
                            onDismissRequest = { profileMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            currentUser.fullName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            currentUser.role.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (currentUser.licensePlate != null) {
                                            Text(
                                                "🚗 ${currentUser.licensePlate}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = { profileMenuExpanded = false }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.logout)) },
                                onClick = {
                                    profileMenuExpanded = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            }
        )
    }

    // Bildirim listesi dialog'u
    if (notificationDialogVisible) {
        NotificationDialog(
            notifications = notifications,
            onDismiss = { notificationDialogVisible = false },
            onMarkRead = { id ->
                onNotificationRead(id)
            }
        )
    }
}

/**
 * Bildirim listesi dialog'u
 */
@Composable
private fun NotificationDialog(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit,
    onMarkRead: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notifications)) },
        text = {
            if (notifications.isEmpty()) {
                Text(
                    stringResource(R.string.no_notifications),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notif ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (!notif.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else Color.Transparent
                                )
                                .clickable { onMarkRead(notif.id) }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (!notif.isRead) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    text = dateFormat.format(Date(notif.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!notif.isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
