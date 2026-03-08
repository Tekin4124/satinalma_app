/*
 * StatusBadge.kt
 * Malzeme alım durumunu renk kodlu chip olarak gösteren composable.
 * Her durum kendine özgü arka plan rengi ile görselleştirilir.
 */
package com.tekin.satinalma.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tekin.satinalma.domain.model.MaterialStatus
import com.tekin.satinalma.domain.model.UrgencyLevel
import com.tekin.satinalma.presentation.theme.SatinalmaTheme
import com.tekin.satinalma.presentation.theme.StatusCancelled
import com.tekin.satinalma.presentation.theme.StatusDelivered
import com.tekin.satinalma.presentation.theme.StatusInProgress
import com.tekin.satinalma.presentation.theme.StatusPending
import com.tekin.satinalma.presentation.theme.StatusPurchased
import com.tekin.satinalma.presentation.theme.UrgencyHigh
import com.tekin.satinalma.presentation.theme.UrgencyLow
import com.tekin.satinalma.presentation.theme.UrgencyNormal
import com.tekin.satinalma.presentation.theme.UrgencyUrgent

/**
 * Malzeme alım durumu rozet composable'ı
 *
 * @param status Malzeme alım durumu
 * @param modifier Modifier
 */
@Composable
fun StatusBadge(
    status: MaterialStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = statusColors(status)
    BadgeChip(
        text = status.displayName,
        backgroundColor = backgroundColor,
        textColor = textColor,
        modifier = modifier
    )
}

/**
 * Aciliyet seviyesi rozet composable'ı
 *
 * @param urgencyLevel Aciliyet seviyesi
 * @param modifier Modifier
 */
@Composable
fun UrgencyBadge(
    urgencyLevel: UrgencyLevel,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = urgencyColors(urgencyLevel)
    BadgeChip(
        text = urgencyLevel.displayName,
        backgroundColor = backgroundColor,
        textColor = textColor,
        modifier = modifier
    )
}

@Composable
private fun BadgeChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = backgroundColor, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = backgroundColor
        )
    }
}

/** Malzeme durumuna göre arka plan ve metin rengi çifti döner */
fun statusColors(status: MaterialStatus): Pair<Color, Color> {
    return when (status) {
        MaterialStatus.PENDING -> Pair(StatusPending, Color.White)
        MaterialStatus.IN_PROGRESS -> Pair(StatusInProgress, Color.White)
        MaterialStatus.PURCHASED -> Pair(StatusPurchased, Color.White)
        MaterialStatus.DELIVERED -> Pair(StatusDelivered, Color.White)
        MaterialStatus.CANCELLED -> Pair(StatusCancelled, Color.White)
    }
}

/** Aciliyet seviyesine göre arka plan ve metin rengi çifti döner */
fun urgencyColors(urgency: UrgencyLevel): Pair<Color, Color> {
    return when (urgency) {
        UrgencyLevel.LOW -> Pair(UrgencyLow, Color.White)
        UrgencyLevel.NORMAL -> Pair(UrgencyNormal, Color.White)
        UrgencyLevel.HIGH -> Pair(UrgencyHigh, Color.White)
        UrgencyLevel.URGENT -> Pair(UrgencyUrgent, Color.White)
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    SatinalmaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MaterialStatus.entries.forEach { status ->
                StatusBadge(status = status, modifier = Modifier.padding(vertical = 4.dp))
            }
            UrgencyLevel.entries.forEach { urgency ->
                UrgencyBadge(urgencyLevel = urgency, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
