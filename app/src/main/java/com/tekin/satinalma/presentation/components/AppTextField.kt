/*
 * AppTextField.kt
 * Tekrar kullanılabilir metin girdi alanı composable'ı.
 * Label, salt okunur mod, hata mesajı ve tek satır/çok satır desteği sunar.
 */
package com.tekin.satinalma.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tekin.satinalma.presentation.theme.SatinalmaTheme

/**
 * Ortak metin girdi alanı — tüm formlarda kullanılacak standart bileşen
 *
 * @param label Alanın etiketi
 * @param value Mevcut değer
 * @param onValueChange Değer değişikliği geri çağrısı
 * @param modifier Modifier
 * @param readOnly Salt okunur mod
 * @param isError Hata durumu
 * @param errorMessage Hata mesajı metni
 * @param singleLine Tek satır mı?
 * @param maxLines Maksimum satır sayısı
 * @param placeholder Yer tutucu metin
 */
@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 5,
    placeholder: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(text = label) },
            readOnly = readOnly,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            placeholder = if (placeholder != null) {
                { Text(text = placeholder) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            supportingText = if (isError && !errorMessage.isNullOrBlank()) {
                {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else null
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppTextFieldPreview() {
    SatinalmaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AppTextField(
                label = "Firma Adı",
                value = "Test Firma A.Ş.",
                onValueChange = {}
            )
            AppTextField(
                label = "Notlar",
                value = "Acil sipariş",
                onValueChange = {},
                singleLine = false,
                maxLines = 3
            )
            AppTextField(
                label = "Gerekli Alan",
                value = "",
                onValueChange = {},
                isError = true,
                errorMessage = "Bu alan zorunludur"
            )
        }
    }
}
