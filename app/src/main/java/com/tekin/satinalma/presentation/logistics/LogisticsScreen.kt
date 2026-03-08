/*
 * LogisticsScreen.kt
 * Sevkiyat ofis ana liste ekranı ve talep detay ekranı.
 * Şoför seçimi dropdown, iş iptali, itiraz banner ve yönlendirme özellikleri içerir.
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.tekin.satinalma.presentation.components.AppTopBar
import com.tekin.satinalma.presentation.components.ButtonVariant
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = stringResource(R.string.logistics_title),
                titleIcon = Icons.Default.LocalShipping,
                currentUser = viewModel.getCurrentUser(),
                notifications = viewModel.getNotifications(),
                onLogout = onLogout,
                onNotificationRead = viewModel::markNotificationRead
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.requests.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🚛", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.purchasing_no_requests),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.requests, key = { it.id }) { request ->
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
    val drivers by viewModel.drivers.collectAsStateWithLifecycle()
    val allRequests by viewModel.uiState.collectAsStateWithLifecycle()
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
                drivers = drivers,
                selectedDriver = formState.selectedDriver,
                allRequests = allRequests.requests.filter { it.id != requestId },
                redirectTargetId = formState.redirectTargetId,
                isSaving = formState.isSaving,
                onDriverSelected = viewModel::onDriverSelected,
                onAssignDriver = viewModel::assignDriver,
                onCancelRequest = { reason -> viewModel.cancelRequest(requestId, reason) },
                onRedirectTargetSelected = viewModel::onRedirectTargetSelected,
                onRedirect = viewModel::redirectRequest,
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
    allRequests: List<PurchaseRequest>,
    redirectTargetId: String?,
    isSaving: Boolean,
    onDriverSelected: (User) -> Unit,
    onAssignDriver: () -> Unit,
    onCancelRequest: (String?) -> Unit,
    onRedirectTargetSelected: (String?) -> Unit,
    onRedirect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var driverDropdownExpanded by remember { mutableStateOf(false) }
    var redirectDropdownExpanded by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }

    val isCancelled = request.materialStatus == MaterialStatus.CANCELLED

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

        // Şoför itiraz banner'ı
        if (request.hasObjection && request.objectionReason != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF3E0),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️ ${stringResource(R.string.driver_objection_label)}: ${request.objectionReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE65100),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        HorizontalDivider()

        // Salt okunur alanlar
        AppTextField(label = stringResource(R.string.field_item_to_purchase), value = request.itemToPurchase, onValueChange = {}, readOnly = true)
        AppTextField(label = stringResource(R.string.field_company_name), value = request.companyName, onValueChange = {}, readOnly = true)
        AppTextField(label = stringResource(R.string.field_company_address), value = request.companyAddress, onValueChange = {}, readOnly = true, singleLine = false, maxLines = 3)
        AppTextField(label = stringResource(R.string.field_contact_number), value = request.contactNumber, onValueChange = {}, readOnly = true)
        AppTextField(label = stringResource(R.string.field_product_dimensions), value = request.productDimensions, onValueChange = {}, readOnly = true)
        AppTextField(label = stringResource(R.string.field_product_weight), value = request.productWeight, onValueChange = {}, readOnly = true)
        AppTextField(label = stringResource(R.string.field_purchase_date), value = request.purchaseDate, onValueChange = {}, readOnly = true)
        if (request.notes.isNotBlank()) {
            AppTextField(label = stringResource(R.string.field_notes), value = request.notes, onValueChange = {}, readOnly = true, singleLine = false, maxLines = 3)
        }

        HorizontalDivider()

        // Şoför seçimi dropdown (manuel plaka yerine)
        if (!isCancelled) {
            ExposedDropdownMenuBox(
                expanded = driverDropdownExpanded,
                onExpandedChange = { driverDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedDriver?.let { "${it.fullName} — ${it.licensePlate ?: "-"}" }
                        ?: (request.assignedDriverId?.let { id ->
                            drivers.find { it.id == id }?.let { "${it.fullName} — ${it.licensePlate ?: "-"}" }
                        } ?: stringResource(R.string.logistics_select_driver)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.logistics_select_driver)) },
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

            AppButton(
                text = stringResource(R.string.logistics_assign_driver),
                onClick = onAssignDriver,
                isLoading = isSaving,
                enabled = selectedDriver != null
            )

            HorizontalDivider()

            // Talep yönlendirme
            if (allRequests.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.logistics_redirect),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                ExposedDropdownMenuBox(
                    expanded = redirectDropdownExpanded,
                    onExpandedChange = { redirectDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = redirectTargetId?.let { id ->
                            allRequests.find { it.id == id }?.requestNumber ?: id
                        } ?: stringResource(R.string.field_redirect_request),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.field_redirect_request)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = redirectDropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = redirectDropdownExpanded,
                        onDismissRequest = { redirectDropdownExpanded = false }
                    ) {
                        allRequests.forEach { req ->
                            DropdownMenuItem(
                                text = { Text("${req.requestNumber} — ${req.itemToPurchase}") },
                                onClick = {
                                    onRedirectTargetSelected(req.id)
                                    redirectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                AppButton(
                    text = stringResource(R.string.logistics_redirect),
                    onClick = onRedirect,
                    variant = ButtonVariant.SECONDARY,
                    isLoading = isSaving,
                    enabled = redirectTargetId != null
                )
                HorizontalDivider()
            }

            // İptal butonu
            AppButton(
                text = stringResource(R.string.cancel_request_title),
                onClick = { showCancelDialog = true },
                variant = ButtonVariant.DANGER
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // İptal onay dialog'u
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false; cancelReason = "" },
            title = { Text(stringResource(R.string.cancel_request_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.cancel_request_confirm))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text(stringResource(R.string.cancel_reason_optional)) },
                        singleLine = false,
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onCancelRequest(cancelReason.ifBlank { null })
                    showCancelDialog = false
                    cancelReason = ""
                }) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false; cancelReason = "" }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LogisticsDetailScreenPreview() {
    SatinalmaTheme {
        LogisticsDetailContent(
            request = PurchaseRequest(
                requestNumber = "TLP-2026-001",
                itemToPurchase = "Çelik Profil 100x50mm",
                companyName = "Test Firma A.Ş.",
                companyAddress = "Organize Sanayi Bölgesi, Ankara",
                urgencyLevel = UrgencyLevel.HIGH,
                materialStatus = MaterialStatus.IN_PROGRESS,
                licensePlate = "34 ABC 123",
                hasObjection = true,
                objectionReason = "Araç arızalı"
            ),
            drivers = listOf(
                User("3", "ahmet", "Ahmet Yılmaz", UserRole.DRIVER, "34 ABC 123")
            ),
            selectedDriver = null,
            allRequests = emptyList(),
            redirectTargetId = null,
            isSaving = false,
            onDriverSelected = {},
            onAssignDriver = {},
            onCancelRequest = {},
            onRedirectTargetSelected = {},
            onRedirect = {}
        )
    }
}
