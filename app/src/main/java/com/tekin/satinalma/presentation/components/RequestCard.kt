/*
 * RequestCard.kt
 * Satın alma talebini liste görünümünde kartlı olarak gösteren composable.
 * Aciliyet rengine göre sol kenar vurgusu, "YENİ" badge'i ve pill şekilli durum chip'i içerir.
 */
package com.tekin.satinalma.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tekin.satinalma.R
import com.tekin.satinalma.domain.model.MaterialStatus
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.UrgencyLevel
import com.tekin.satinalma.presentation.theme.NewBadge
import com.tekin.satinalma.presentation.theme.SatinalmaTheme
import com.tekin.satinalma.presentation.theme.UrgencyHigh
import com.tekin.satinalma.presentation.theme.UrgencyLow
import com.tekin.satinalma.presentation.theme.UrgencyNormal
import com.tekin.satinalma.presentation.theme.UrgencyUrgent
import com.tekin.satinalma.util.toFormattedDate

/**
 * Talep listesi kart bileşeni
 *
 * @param request Görüntülenecek satın alma talebi
 * @param onClick Kart tıklama geri çağrısı
 * @param showNewBadge "YENİ" rozetini göster (şoför ekranı için)
 * @param onCancelClick İptal butonuna tıklama (null ise buton gizlenir)
 * @param modifier Modifier
 */
@Composable
fun RequestCard(
    request: PurchaseRequest,
    onClick: () -> Unit,
    showNewBadge: Boolean = false,
    onCancelClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Aciliyet düzeyine göre kart kenar rengi
    val borderColor = when (request.urgencyLevel) {
        UrgencyLevel.URGENT -> UrgencyUrgent
        UrgencyLevel.HIGH -> UrgencyHigh
        UrgencyLevel.NORMAL -> UrgencyNormal
        UrgencyLevel.LOW -> UrgencyLow
    }

    // İptal edilmiş talepler için soluk görünüm
    val isCancelled = request.materialStatus == MaterialStatus.CANCELLED
    val cardAlpha = if (isCancelled) 0.5f else 1f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isCancelled, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(width = 2.dp, color = borderColor.copy(alpha = cardAlpha)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        )
    ) {
        Box {
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
                        text = request.requestNumber.ifBlank { stringResource(R.string.request_number_prefix) },
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

                // Şoför itiraz uyarısı
                if (request.hasObjection && request.objectionReason != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ ${stringResource(R.string.driver_objection_label)}: ${request.objectionReason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

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

                // İptal butonu (opsiyonel)
                if (onCancelClick != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onCancelClick,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel_request_title),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // "YENİ" badge'i — sağ üst köşede
            if (showNewBadge && request.isNew) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = NewBadge
                ) {
                    Text(
                        text = stringResource(R.string.new_badge),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RequestCardPreview() {
    SatinalmaTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RequestCard(
                request = PurchaseRequest(
                    requestNumber = "TLP-2026-001",
                    itemToPurchase = "Çelik Profil 100x50mm",
                    companyName = "Test Firma A.Ş.",
                    urgencyLevel = UrgencyLevel.URGENT,
                    materialStatus = MaterialStatus.IN_PROGRESS,
                    purchaseDate = "2024-12-15"
                ),
                onClick = {},
                showNewBadge = true
            )
            RequestCard(
                request = PurchaseRequest(
                    requestNumber = "TLP-2026-002",
                    itemToPurchase = "Boru 2 inç",
                    companyName = "Örnek Ltd.",
                    urgencyLevel = UrgencyLevel.NORMAL,
                    materialStatus = MaterialStatus.PENDING,
                    hasObjection = true,
                    objectionReason = "Adres yanlış"
                ),
                onClick = {}
            )
        }
    }
}
