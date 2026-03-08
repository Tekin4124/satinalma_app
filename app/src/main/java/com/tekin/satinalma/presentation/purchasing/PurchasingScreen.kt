/*
 * PurchasingScreen.kt
 * Satınalma personeli ana ekranı — mevcut taleplerin listesi, yeni talep FAB ve iptal işlemi.
 * AppTopBar ile kullanıcı avatarı ve bildirim ikonu içerir.
 */
package com.tekin.satinalma.presentation.purchasing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tekin.satinalma.R
import com.tekin.satinalma.domain.model.AppNotification
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.User
import com.tekin.satinalma.domain.model.UserRole
import com.tekin.satinalma.presentation.components.AppTopBar
import com.tekin.satinalma.presentation.components.RequestCard
import com.tekin.satinalma.presentation.theme.SatinalmaTheme

/**
 * Satınalma personeli ana liste ekranı
 *
 * @param onRequestClick Talep kartı tıklaması
 * @param onCreateNew Yeni talep oluşturma
 * @param onLogout Çıkış yapma
 * @param viewModel ViewModel
 */
@Composable
fun PurchasingScreen(
    onRequestClick: (String) -> Unit,
    onCreateNew: () -> Unit,
    onLogout: () -> Unit,
    viewModel: PurchasingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    PurchasingContent(
        requests = uiState.requests,
        isLoading = uiState.isLoading,
        currentUser = viewModel.getCurrentUser(),
        notifications = viewModel.getNotifications(),
        onRequestClick = onRequestClick,
        onCreateNew = onCreateNew,
        onLogout = onLogout,
        onCancelRequest = viewModel::cancelRequest,
        onNotificationRead = viewModel::markNotificationRead,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun PurchasingContent(
    requests: List<PurchaseRequest>,
    isLoading: Boolean,
    currentUser: User?,
    notifications: List<AppNotification>,
    onRequestClick: (String) -> Unit,
    onCreateNew: () -> Unit,
    onLogout: () -> Unit,
    onCancelRequest: (String, String?) -> Unit,
    onNotificationRead: (String) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    // İptal dialog durumu
    var cancelRequestId by remember { mutableStateOf<String?>(null) }
    var cancelReason by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = stringResource(R.string.purchasing_title),
                titleIcon = Icons.Default.List,
                currentUser = currentUser,
                notifications = notifications,
                onLogout = onLogout,
                onNotificationRead = onNotificationRead
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNew,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.purchasing_new_request))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                requests.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📋",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.purchasing_no_requests),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(requests, key = { it.id }) { request ->
                            RequestCard(
                                request = request,
                                onClick = { onRequestClick(request.id) },
                                onCancelClick = if (request.materialStatus.name != "CANCELLED") {
                                    { cancelRequestId = request.id }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }

    // İptal onay dialog'u
    cancelRequestId?.let { reqId ->
        AlertDialog(
            onDismissRequest = { cancelRequestId = null; cancelReason = "" },
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
                    onCancelRequest(reqId, cancelReason.ifBlank { null })
                    cancelRequestId = null
                    cancelReason = ""
                }) {
                    Text(stringResource(R.string.confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelRequestId = null; cancelReason = "" }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PurchasingScreenPreview() {
    SatinalmaTheme {
        PurchasingContent(
            requests = listOf(
                PurchaseRequest(
                    requestNumber = "TLP-2026-001",
                    itemToPurchase = "Çelik Profil 100x50mm",
                    companyName = "Test Firma A.Ş."
                )
            ),
            isLoading = false,
            currentUser = User("1", "mehmet", "Mehmet Demir", UserRole.PURCHASING),
            notifications = emptyList(),
            onRequestClick = {},
            onCreateNew = {},
            onLogout = {},
            onCancelRequest = { _, _ -> },
            onNotificationRead = {}
        )
    }
}
