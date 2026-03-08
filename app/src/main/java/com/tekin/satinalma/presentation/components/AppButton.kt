/*
 * AppButton.kt
 * Tekrar kullanılabilir buton composable'ı.
 * Primary, secondary ve danger (tehlike) varyantları ile yükleme durumu desteği sunar.
 */
package com.tekin.satinalma.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tekin.satinalma.presentation.theme.Error
import com.tekin.satinalma.presentation.theme.OnPrimary
import com.tekin.satinalma.presentation.theme.SatinalmaTheme

/**
 * Buton varyant türleri
 */
enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    DANGER
}

/**
 * Ortak buton bileşeni — primary, secondary ve danger varyantları desteklenir
 *
 * @param text Buton metni
 * @param onClick Tıklama geri çağrısı
 * @param modifier Modifier
 * @param variant Buton varyantı
 * @param isLoading Yükleniyor durumu
 * @param enabled Etkin mi?
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    when (variant) {
        ButtonVariant.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = modifier.fillMaxWidth(),
                enabled = enabled && !isLoading,
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                ButtonContent(text = text, isLoading = isLoading, textColor = OnPrimary)
            }
        }

        ButtonVariant.SECONDARY -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.fillMaxWidth(),
                enabled = enabled && !isLoading,
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                ButtonContent(
                    text = text,
                    isLoading = isLoading,
                    textColor = MaterialTheme.colorScheme.primary
                )
            }
        }

        ButtonVariant.DANGER -> {
            Button(
                onClick = onClick,
                modifier = modifier.fillMaxWidth(),
                enabled = enabled && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Error,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                ButtonContent(text = text, isLoading = isLoading, textColor = Color.White)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    textColor: Color
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = textColor,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = textColor
    )
}

@Preview(showBackground = true)
@Composable
private fun AppButtonPreview() {
    SatinalmaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppButton(text = "Kaydet", onClick = {})
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(text = "İptal", onClick = {}, variant = ButtonVariant.SECONDARY)
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(text = "Sil", onClick = {}, variant = ButtonVariant.DANGER)
            Spacer(modifier = Modifier.height(8.dp))
            AppButton(text = "Yükleniyor", onClick = {}, isLoading = true)
        }
    }
}
