/*
 * PurchasingScreen.kt
 * Satınalma personeli ana ekranı — mevcut taleplerin listesi ve yeni talep oluşturma FAB butonu.
 */
package com.tekin.satinalma.presentation.purchasing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tekin.satinalma.R
import com.tekin.satinalma.domain.model.PurchaseRequest
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

    PurchasingContent(
        requests = uiState.requests,
        isLoading = uiState.isLoading,
        onRequestClick = onRequestClick,
        onCreateNew = onCreateNew,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchasingContent(
    requests: List<PurchaseRequest>,
    isLoading: Boolean,
    onRequestClick: (String) -> Unit,
    onCreateNew: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.purchasing_title),
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
                    Text(
                        text = stringResource(R.string.purchasing_no_requests),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
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
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PurchasingScreenPreview() {
    SatinalmaTheme {
        PurchasingContent(
            requests = listOf(
                PurchaseRequest(
                    requestNumber = "TLB-10001",
                    itemToPurchase = "Çelik Profil 100x50mm",
                    companyName = "Test Firma A.Ş."
                )
            ),
            isLoading = false,
            onRequestClick = {},
            onCreateNew = {},
            onLogout = {}
        )
    }
}
