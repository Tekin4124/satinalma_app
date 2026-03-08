/*
 * LogisticsScreen.kt
 * Sevkiyat ofis ana liste ekranı ve talep detay ekranı.
 * Şoför seçimi dropdown ile yapılır; plaka şoförden otomatik alınır.
 * İtiraz durumu banner olarak gösterilir.
 */
package com.tekin.satinalma.presentation.logistics

import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.UrgencyLevel
import com.tekin.satinalma.domain.model.User
import com.tekin.satinalma.domain.model.UserRole
import com.tekin.satinalma.presentation.components.AppButton
import com.tekin.satinalma.presentation.components.AppTextField
import com.tekin.satinalma.presentation.components.ButtonVariant
import com.tekin.satinalma.presentation.components.RequestCard
import com.tekin.satinalma.presentation.components.StatusBadge
import com.tekin.satinalma.presentation.components.UrgencyBadge
import com.tekin.satinalma.presentation.driver.UserInfoAction
import com.tekin.satinalma.presentation.theme.SatinalmaTheme

/**
 * Sevkiyat ofis liste ekranı
 */
@Composable
fun LogisticsScreen(
    currentUserFullName: String,
    currentUserRole: String,
    onRequestClick: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: LogisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LogisticsListContent(
        requests = uiState.requests,
        isLoading = uiState.isLoading,
        currentUserFullName = currentUserFullName,
        currentUserRole = currentUserRole,
        onRequestClick = onRequestClick,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogisticsListContent(
    requests: List<PurchaseRequest>,
    isLoading: Boolean,
    currentUserFullName: String,
    currentUserRole: String,
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
                    UserInfoAction(
                        fullName = currentUserFullName,
                        roleName = currentUserRole,
                        onLogout = onLogout
                    )
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
                drivers = formState.drivers,
                selectedDriver = formState.selectedDriver,
                isSaving = formState.isSaving,
                onDriverSelected = viewModel::onDriverSelected,
                onAssignDriver = viewModel::assignDriver,
                onCancelRequest = viewModel::cancelRequest,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogisticsDetailContent(
    request: PurchaseRequest,
    drivers: List<User>,
    selectedDriver: User?,
    isSaving: Boolean,
    onDriverSelected: (User) -> Unit,
    onAssignDriver: () -> Unit,
    onCancelRequest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var driverDropdownExpanded by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReasonText by remember { mutableStateOf("") }
    val isCancelled = request.materialStatus == MaterialStatus.CANCELLED

    // İptal dialog'u
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.logistics_cancel_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.logistics_cancel_confirm),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = cancelReasonText,
                        onValueChange = { cancelReasonText = it },
                        label = { Text(stringResource(R.string.cancel_reason)) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelRequest(cancelReasonText)
                        showCancelDialog = false
                        cancelReasonText = ""
                    }
                ) {
                    Text(
                        text = stringResource(R.string.confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

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

        // Malzeme durumu
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

        // Aciliyet
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

        // İtiraz uyarı banner'ı
        if (request.hasObjection) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "⚠️ ${stringResource(R.string.logistics_objection_banner)} ${request.objectionReason ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
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
        // Atanmış plaka gösterimi
        if (request.licensePlate.isNotBlank()) {
            AppTextField(
                label = stringResource(R.string.field_license_plate),
                value = request.licensePlate,
                onValueChange = {},
                readOnly = true
            )
        }

        HorizontalDivider()

        // Şoför seçimi dropdown — iptal edilmemiş taleplerde aktif
        if (!isCancelled) {
            ExposedDropdownMenuBox(
                expanded = driverDropdownExpanded,
                onExpandedChange = { driverDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedDriver?.let { "${it.fullName} — ${it.licensePlate ?: "-"}" }
                        ?: stringResource(R.string.logistics_select_driver),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.logistics_assign_driver)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = driverDropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = driverDropdownExpanded,
                    onDismissRequest = { driverDropdownExpanded = false }
                ) {
                    drivers.forEach { driver ->
                        DropdownMenuItem(
                            text = { Text("${driver.fullName} — ${driver.licensePlate ?: "-"}") },
                            onClick = {
                                onDriverSelected(driver)
                                driverDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            AppButton(
                text = stringResource(R.string.logistics_assign_driver_button),
                onClick = onAssignDriver,
                isLoading = isSaving
            )

            Spacer(modifier = Modifier.height(8.dp))

            // İşi İptal Et butonu
            AppButton(
                text = stringResource(R.string.logistics_cancel_job),
                onClick = { showCancelDialog = true },
                variant = ButtonVariant.DANGER,
                isLoading = false
            )
        }

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
                licensePlate = "34 ABC 123",
                hasObjection = true,
                objectionReason = "Araç uygun değil"
            ),
            drivers = listOf(
                User("3", "ahmet", "Ahmet Yılmaz", UserRole.DRIVER, "34 ABC 123")
            ),
            selectedDriver = User("3", "ahmet", "Ahmet Yılmaz", UserRole.DRIVER, "34 ABC 123"),
            isSaving = false,
            onDriverSelected = {},
            onAssignDriver = {},
            onCancelRequest = {}
        )
    }
}
