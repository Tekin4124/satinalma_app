/*
 * RequestCard.kt
 * Satın alma talebini liste görünümünde kartlı olarak gösteren composable.
 * Talep numarası, firma adı, aciliyet seviyesi ve malzeme durumu görüntülenir.
 */
package com.tekin.satinalma.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tekin.satinalma.domain.model.MaterialStatus
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.UrgencyLevel
import com.tekin.satinalma.presentation.theme.SatinalmaTheme
import com.tekin.satinalma.util.toFormattedDate

/**
 * Talep listesi kart bileşeni
 *
 * @param request Görüntülenecek satın alma talebi
 * @param onClick Kart tıklama geri çağrısı
 * @param modifier Modifier
 */
@Composable
fun RequestCard(
    request: PurchaseRequest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Üst satır: Talep numarası ve durum rozeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Talep #${request.requestNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(status = request.materialStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ürün adı
            Text(
                text = request.itemToPurchase,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Firma adı
            Text(
                text = request.companyName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Alt satır: Aciliyet ve tarih
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UrgencyBadge(urgencyLevel = request.urgencyLevel)
                Text(
                    text = if (request.purchaseDate.isNotBlank()) request.purchaseDate
                    else request.createdAt.toFormattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RequestCardPreview() {
    SatinalmaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            RequestCard(
                request = PurchaseRequest(
                    requestNumber = "TLB-10001",
                    itemToPurchase = "Çelik Profil 100x50mm",
                    companyName = "Test Firma A.Ş.",
                    urgencyLevel = UrgencyLevel.HIGH,
                    materialStatus = MaterialStatus.IN_PROGRESS,
                    purchaseDate = "2024-12-15"
                ),
                onClick = {}
            )
        }
    }
}
