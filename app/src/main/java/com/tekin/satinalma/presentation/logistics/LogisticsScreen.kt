/*
 * LogisticsScreen.kt
 * Sevkiyat ofis ana liste ekranı ve talep detay ekranı.
 * Talep alanları büyük bölümü salt okunur; plaka girişi düzenlenebilir.
 */
package com.tekin.satinalma.presentation.logistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.UrgencyLevel
import com.tekin.satinalma.presentation.components.AppButton
import com.tekin.satinalma.presentation.components.AppTextField
import com.tekin.satinalma.presentation.components.RequestCard
import com.tekin.satinalma.presentation.components.StatusBadge
import com.tekin.satinalma.presentation.components.UrgencyBadge
import com.tekin.satinalma.presentation.theme.SatinalmaTheme

/**
 * Sevkiyat ofis liste ekranı
 */
@Composable
fun LogisticsScreen(
    onRequestClick: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: LogisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LogisticsListContent(
        requests = uiState.requests,
        isLoading = uiState.isLoading,
        onRequestClick = onRequestClick,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogisticsListContent(
    requests: List<PurchaseRequest>,
    isLoading: Boolean,
    onRequestClick: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.logistics_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = stringResource(R.string.logout)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (requests.isEmpty()) {
                Text(
                    text = stringResource(R.string.purchasing_no_requests),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(requests, key = { it.id }) { request ->
                        RequestCard(
                            request = request,
                            onClick = { onRequestClick(request.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sevkiyat ofis talep detay ekranı
 */
@Composable
fun LogisticsDetailScreen(
    requestId: String,
    onNavigateBack: () -> Unit,
    viewModel: LogisticsViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(requestId) {
        viewModel.loadRequest(requestId)
    }

    LaunchedEffect(formState.successMessage) {
        formState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFormMessages()
        }
    }

    LaunchedEffect(formState.errorMessage) {
        formState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFormMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LogisticsDetailTopBar(onNavigateBack = onNavigateBack)
        }
    ) { paddingValues ->
        formState.request?.let { request ->
            LogisticsDetailContent(
                request = request,
                editableLicensePlate = formState.editableLicensePlate,
                isSaving = formState.isSaving,
                onLicensePlateChange = viewModel::onLicensePlateChange,
                onSaveLicensePlate = viewModel::saveLicensePlate,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogisticsDetailTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.logistics_detail_title),
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

@Composable
private fun LogisticsDetailContent(
    request: PurchaseRequest,
    editableLicensePlate: String,
    isSaving: Boolean,
    onLicensePlateChange: (String) -> Unit,
    onSaveLicensePlate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Talep numarası
        AppTextField(
            label = stringResource(R.string.field_request_number),
            value = request.requestNumber,
            onValueChange = {},
            readOnly = true
        )

        // Malzeme durumu (salt okunur)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.field_material_status) + ": ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusBadge(status = request.materialStatus)
        }

        // Aciliyet durumu (salt okunur rozet)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.field_urgency_level) + ": ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            UrgencyBadge(urgencyLevel = request.urgencyLevel)
        }

        HorizontalDivider()

        // Salt okunur alanlar
        AppTextField(
            label = stringResource(R.string.field_item_to_purchase),
            value = request.itemToPurchase,
            onValueChange = {},
            readOnly = true
        )
        AppTextField(
            label = stringResource(R.string.field_company_name),
            value = request.companyName,
            onValueChange = {},
            readOnly = true
        )
        AppTextField(
            label = stringResource(R.string.field_company_address),
            value = request.companyAddress,
            onValueChange = {},
            readOnly = true,
            singleLine = false,
            maxLines = 3
        )
        AppTextField(
            label = stringResource(R.string.field_contact_number),
            value = request.contactNumber,
            onValueChange = {},
            readOnly = true
        )
        AppTextField(
            label = stringResource(R.string.field_product_dimensions),
            value = request.productDimensions,
            onValueChange = {},
            readOnly = true
        )
        AppTextField(
            label = stringResource(R.string.field_product_weight),
            value = request.productWeight,
            onValueChange = {},
            readOnly = true
        )
        AppTextField(
            label = stringResource(R.string.field_purchase_date),
            value = request.purchaseDate,
            onValueChange = {},
            readOnly = true
        )
        if (request.notes.isNotBlank()) {
            AppTextField(
                label = stringResource(R.string.field_notes),
                value = request.notes,
                onValueChange = {},
                readOnly = true,
                singleLine = false,
                maxLines = 3
            )
        }

        HorizontalDivider()

        // Plaka girişi — düzenlenebilir
        AppTextField(
            label = stringResource(R.string.field_license_plate),
            value = editableLicensePlate,
            onValueChange = onLicensePlateChange,
            placeholder = "34 ABC 123"
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppButton(
            text = stringResource(R.string.logistics_save_changes),
            onClick = onSaveLicensePlate,
            isLoading = isSaving
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LogisticsDetailScreenPreview() {
    SatinalmaTheme {
        LogisticsDetailContent(
            request = PurchaseRequest(
                requestNumber = "TLB-10001",
                itemToPurchase = "Çelik Profil 100x50mm",
                companyName = "Test Firma A.Ş.",
                companyAddress = "Organize Sanayi Bölgesi, Ankara",
                urgencyLevel = UrgencyLevel.HIGH,
                materialStatus = MaterialStatus.IN_PROGRESS,
                licensePlate = "34 ABC 123"
            ),
            editableLicensePlate = "34 ABC 123",
            isSaving = false,
            onLicensePlateChange = {},
            onSaveLicensePlate = {}
        )
    }
}
