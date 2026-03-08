/*
 * PurchasingDetailScreen.kt
 * Satınalma personeli talep detay/form ekranı.
 * Yeni talep oluşturma veya mevcut talebi güncelleme formu sunar.
 */
package com.tekin.satinalma.presentation.purchasing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tekin.satinalma.R
import com.tekin.satinalma.domain.model.MaterialStatus
import com.tekin.satinalma.domain.model.UrgencyLevel
import com.tekin.satinalma.presentation.components.AppButton
import com.tekin.satinalma.presentation.components.AppTextField
import com.tekin.satinalma.presentation.components.StatusBadge
import com.tekin.satinalma.presentation.theme.SatinalmaTheme
import com.tekin.satinalma.util.Constants

/**
 * Satınalma personeli talep detay/form ekranı
 *
 * @param requestId Düzenlenecek talep ID'si ("new" ise yeni talep)
 * @param onNavigateBack Geri dönme geri çağrısı
 * @param viewModel ViewModel
 */
@Composable
fun PurchasingDetailScreen(
    requestId: String,
    onNavigateBack: () -> Unit,
    viewModel: PurchasingViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isNewRequest = requestId == Constants.NEW_REQUEST_ID

    // Mevcut talebi forma yükle
    LaunchedEffect(requestId) {
        viewModel.loadRequestForEdit(requestId)
    }

    // Kayıt başarılıysa geri dön
    LaunchedEffect(formState.saveSuccess) {
        if (formState.saveSuccess) {
            snackbarHostState.showSnackbar(
                if (isNewRequest) "Kayıt başarıyla oluşturuldu" else "Güncelleme başarıyla kaydedildi"
            )
            viewModel.clearFormMessages()
            onNavigateBack()
        }
    }

    // Hata varsa göster
    LaunchedEffect(formState.errorMessage) {
        formState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFormMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PurchasingDetailTopBar(
                isNewRequest = isNewRequest,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        PurchasingDetailContent(
            formState = formState,
            isNewRequest = isNewRequest,
            requestId = if (isNewRequest) null else requestId,
            currentStatus = uiState.requests.find { it.id == requestId }?.materialStatus,
            onItemToPurchaseChange = viewModel::onItemToPurchaseChange,
            onCompanyNameChange = viewModel::onCompanyNameChange,
            onCompanyAddressChange = viewModel::onCompanyAddressChange,
            onContactNumberChange = viewModel::onContactNumberChange,
            onProductDimensionsChange = viewModel::onProductDimensionsChange,
            onProductWeightChange = viewModel::onProductWeightChange,
            onUrgencyLevelChange = viewModel::onUrgencyLevelChange,
            onPurchaseDateChange = viewModel::onPurchaseDateChange,
            onNotesChange = viewModel::onNotesChange,
            onSave = { viewModel.saveRequest(if (isNewRequest) null else requestId) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchasingDetailTopBar(
    isNewRequest: Boolean,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (isNewRequest) {
                    stringResource(R.string.purchasing_create_title)
                } else {
                    stringResource(R.string.purchasing_detail_title)
                },
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchasingDetailContent(
    formState: PurchasingFormState,
    isNewRequest: Boolean,
    requestId: String?,
    currentStatus: MaterialStatus?,
    onItemToPurchaseChange: (String) -> Unit,
    onCompanyNameChange: (String) -> Unit,
    onCompanyAddressChange: (String) -> Unit,
    onContactNumberChange: (String) -> Unit,
    onProductDimensionsChange: (String) -> Unit,
    onProductWeightChange: (String) -> Unit,
    onUrgencyLevelChange: (UrgencyLevel) -> Unit,
    onPurchaseDateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var urgencyDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Talep Numarası (otomatik, salt okunur)
        if (!isNewRequest && formState.requestNumber.isNotBlank()) {
            AppTextField(
                label = stringResource(R.string.field_request_number),
                value = formState.requestNumber,
                onValueChange = {},
                readOnly = true
            )
        }

        // Malzeme Alım Durumu (salt okunur rozet)
        currentStatus?.let { status ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.field_material_status) + ": ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusBadge(status = status)
            }
            HorizontalDivider()
        }

        // Alım Yapılacak Ürün
        AppTextField(
            label = stringResource(R.string.field_item_to_purchase),
            value = formState.itemToPurchase,
            onValueChange = onItemToPurchaseChange,
            isError = formState.errorMessage?.contains("ürün") == true,
            errorMessage = if (formState.errorMessage?.contains("ürün") == true) formState.errorMessage else null
        )

        // Firma Adı
        AppTextField(
            label = stringResource(R.string.field_company_name),
            value = formState.companyName,
            onValueChange = onCompanyNameChange
        )

        // Firma Adresi
        AppTextField(
            label = stringResource(R.string.field_company_address),
            value = formState.companyAddress,
            onValueChange = onCompanyAddressChange,
            singleLine = false,
            maxLines = 3
        )

        // İletişim Numarası
        AppTextField(
            label = stringResource(R.string.field_contact_number),
            value = formState.contactNumber,
            onValueChange = onContactNumberChange
        )

        // Ürün Ölçü Bilgisi
        AppTextField(
            label = stringResource(R.string.field_product_dimensions),
            value = formState.productDimensions,
            onValueChange = onProductDimensionsChange
        )

        // Ürün Ağırlık Bilgisi
        AppTextField(
            label = stringResource(R.string.field_product_weight),
            value = formState.productWeight,
            onValueChange = onProductWeightChange
        )

        // Aciliyet Durumu
        ExposedDropdownMenuBox(
            expanded = urgencyDropdownExpanded,
            onExpandedChange = { urgencyDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = formState.urgencyLevel.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.field_urgency_level)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = urgencyDropdownExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = urgencyDropdownExpanded,
                onDismissRequest = { urgencyDropdownExpanded = false }
            ) {
                UrgencyLevel.entries.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level.displayName) },
                        onClick = {
                            onUrgencyLevelChange(level)
                            urgencyDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Alım Yapılacak Tarih
        AppTextField(
            label = stringResource(R.string.field_purchase_date),
            value = formState.purchaseDate,
            onValueChange = onPurchaseDateChange,
            placeholder = "YYYY-AA-GG"
        )

        // Notlar
        AppTextField(
            label = stringResource(R.string.field_notes),
            value = formState.notes,
            onValueChange = onNotesChange,
            singleLine = false,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Kaydet / Güncelle butonu
        AppButton(
            text = if (isNewRequest) {
                stringResource(R.string.purchasing_save)
            } else {
                stringResource(R.string.purchasing_update)
            },
            onClick = onSave,
            isLoading = formState.isSaving
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PurchasingDetailScreenPreview() {
    SatinalmaTheme {
        PurchasingDetailContent(
            formState = PurchasingFormState(
                requestNumber = "TLB-10001",
                itemToPurchase = "Çelik Profil 100x50mm",
                companyName = "Test Firma A.Ş.",
                companyAddress = "Organize Sanayi Bölgesi, Ankara",
                contactNumber = "0312 000 00 01"
            ),
            isNewRequest = false,
            requestId = "mock-001",
            currentStatus = MaterialStatus.IN_PROGRESS,
            onItemToPurchaseChange = {},
            onCompanyNameChange = {},
            onCompanyAddressChange = {},
            onContactNumberChange = {},
            onProductDimensionsChange = {},
            onProductWeightChange = {},
            onUrgencyLevelChange = {},
            onPurchaseDateChange = {},
            onNotesChange = {},
            onSave = {}
        )
    }
}
