/*
 * DriverScreen.kt
 * Şoför ana ekranı ve talep detay ekranı.
 * Atanmış ve atanmamış talepler gösterilir.
 * Şoför kendi plakasıyla üstüne alabilir; atanmamış taleplerin durumunu değiştiremez.
 * Üstündeki talep için itiraz gönderebilir.
 */
package com.tekin.satinalma.presentation.driver

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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.tekin.satinalma.presentation.components.AppButton
import com.tekin.satinalma.presentation.components.AppTextField
import com.tekin.satinalma.presentation.components.ButtonVariant
import com.tekin.satinalma.presentation.components.RequestCard
import com.tekin.satinalma.presentation.components.StatusBadge
import com.tekin.satinalma.presentation.components.UrgencyBadge
import com.tekin.satinalma.presentation.theme.SatinalmaTheme

/**
 * Şoför ana ekranı — atanmış ve atanmamış talepler sekmeleri
 */
@Composable
fun DriverScreen(
    currentUserFullName: String,
    currentUserRole: String,
    onRequestClick: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DriverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DriverListContent(
        myRequests = uiState.myRequests,
        unassignedRequests = uiState.unassignedRequests,
        currentUserFullName = currentUserFullName,
        currentUserRole = currentUserRole,
        onRequestClick = onRequestClick,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverListContent(
    myRequests: List<PurchaseRequest>,
    unassignedRequests: List<PurchaseRequest>,
    currentUserFullName: String,
    currentUserRole: String,
    onRequestClick: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.driver_assigned_to_me),
        stringResource(R.string.driver_unassigned)
    )
    val currentList = if (selectedTab == 0) myRequests else unassignedRequests

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.driver_title),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (currentList.isEmpty()) {
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
                        items(currentList, key = { it.id }) { request ->
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
}

/**
 * Şoför talep detay ekranı
 */
@Composable
fun DriverDetailScreen(
    requestId: String,
    onNavigateBack: () -> Unit,
    viewModel: DriverViewModel = hiltViewModel()
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
            DriverDetailTopBar(onNavigateBack = onNavigateBack)
        }
    ) { paddingValues ->
        formState.request?.let { request ->
            DriverDetailContent(
                request = request,
                driverLicensePlate = viewModel.currentDriverLicensePlate,
                selectedStatus = formState.selectedStatus,
                isSaving = formState.isSaving,
                isAssignedToCurrentDriver = request.assignedDriverId == viewModel.currentDriverId,
                onStatusSelected = viewModel::onStatusSelected,
                onTakeOver = viewModel::takeOverRequest,
                onUpdateStatus = viewModel::updateStatus,
                onSubmitObjection = viewModel::submitObjection,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverDetailTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.driver_detail_title),
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
private fun DriverDetailContent(
    request: PurchaseRequest,
    driverLicensePlate: String,
    selectedStatus: MaterialStatus,
    isSaving: Boolean,
    isAssignedToCurrentDriver: Boolean,
    onStatusSelected: (MaterialStatus) -> Unit,
    onTakeOver: () -> Unit,
    onUpdateStatus: () -> Unit,
    onSubmitObjection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var showObjectionDialog by remember { mutableStateOf(false) }
    var objectionText by remember { mutableStateOf("") }

    // İtiraz dialog'u
    if (showObjectionDialog) {
        AlertDialog(
            onDismissRequest = { showObjectionDialog = false },
            title = { Text(stringResource(R.string.driver_objection_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.driver_objection_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = objectionText,
                        onValueChange = { objectionText = it },
                        label = { Text(stringResource(R.string.driver_objection_reason)) },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSubmitObjection(objectionText)
                        showObjectionDialog = false
                        objectionText = ""
                    }
                ) {
                    Text(stringResource(R.string.driver_objection_send))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showObjectionDialog = false
                    objectionText = ""
                }) {
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

        // Durum rozetleri
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

        // İtiraz durumu göstergesi
        if (request.hasObjection) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️ ${stringResource(R.string.driver_objection_sent)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        HorizontalDivider()

        // Şoförün plaka bilgisi (salt okunur — hesabından otomatik gelir)
        AppTextField(
            label = stringResource(R.string.field_license_plate),
            value = driverLicensePlate.ifBlank { "-" },
            onValueChange = {},
            readOnly = true
        )

        HorizontalDivider()

        // Salt okunur talep alanları
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
            maxLines = 2
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

        HorizontalDivider()

        // Üstüne al butonu — henüz atanmamışsa göster
        if (!isAssignedToCurrentDriver) {
            AppButton(
                text = stringResource(R.string.driver_take_over),
                onClick = onTakeOver,
                isLoading = isSaving
            )
        }

        // Durum güncelleme — sadece atanmış taleplerde aktif
        if (isAssignedToCurrentDriver) {
            ExposedDropdownMenuBox(
                expanded = statusDropdownExpanded,
                onExpandedChange = { statusDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedStatus.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.field_material_status)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = statusDropdownExpanded,
                    onDismissRequest = { statusDropdownExpanded = false }
                ) {
                    MaterialStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.displayName) },
                            onClick = {
                                onStatusSelected(status)
                                statusDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            AppButton(
                text = stringResource(R.string.driver_update_status),
                onClick = onUpdateStatus,
                isLoading = isSaving
            )

            // İtiraz butonu
            AppButton(
                text = stringResource(R.string.driver_objection_button),
                onClick = { showObjectionDialog = true },
                variant = ButtonVariant.SECONDARY,
                isLoading = false
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * TopAppBar sağ tarafında kullanıcı adı ve çıkış butonu
 */
@Composable
fun UserInfoAction(
    fullName: String,
    roleName: String,
    onLogout: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = fullName
            )
        }
        if (fullName.isNotBlank()) {
            Column(modifier = Modifier.padding(end = 4.dp)) {
                Text(
                    text = fullName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = roleName,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

    // Çıkış dropdown menüsü
    androidx.compose.material3.DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.logout)) },
            onClick = {
                menuExpanded = false
                onLogout()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null
                )
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DriverDetailScreenPreview() {
    SatinalmaTheme {
        DriverDetailContent(
            request = PurchaseRequest(
                requestNumber = "TLB-10001",
                itemToPurchase = "Çelik Profil 100x50mm",
                companyName = "Test Firma A.Ş.",
                companyAddress = "Organize Sanayi Bölgesi, Ankara",
                urgencyLevel = UrgencyLevel.HIGH,
                materialStatus = MaterialStatus.IN_PROGRESS,
                licensePlate = "34 ABC 123",
                assignedDriverId = "3"
            ),
            driverLicensePlate = "34 ABC 123",
            selectedStatus = MaterialStatus.IN_PROGRESS,
            isSaving = false,
            isAssignedToCurrentDriver = true,
            onStatusSelected = {},
            onTakeOver = {},
            onUpdateStatus = {},
            onSubmitObjection = {}
        )
    }
}
