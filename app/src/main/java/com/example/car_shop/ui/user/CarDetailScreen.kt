package com.example.car_shop.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    carId: String,
    viewModel: CarDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    LaunchedEffect(carId) {
        viewModel.loadCar(carId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Car Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text("Error: ${uiState.error}")
                }
            }
            uiState.car != null -> {
                val car = uiState.car!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Car Image
                        AsyncImage(
                            model = car.imageUrl.ifBlank { "https://via.placeholder.com/400" },
                            contentDescription = car.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )

                        // Car Details
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = car.name,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "${car.brand} ${car.model}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "$${String.format(Locale.US, "%,.0f", car.price)}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DetailRow("Year", car.year.toString())
                            DetailRow("Mileage", "${car.mileage.toLocaleString()} km")
                            DetailRow("Fuel Type", car.fuelType)
                            DetailRow("Transmission", car.transmission)

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = car.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    // Send Inquiry Button
                    Button(
                        onClick = { viewModel.showInquiryDialog() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send Inquiry")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Inquiry")
                    }
                }
            }
        }

        // Inquiry Dialog
        if (uiState.showInquiryDialog) {
            InquiryDialog(
                message = uiState.inquiryMessage,
                onMessageChange = viewModel::onMessageChange,
                onDismiss = viewModel::hideInquiryDialog,
                onSend = {
                    viewModel.sendInquiry {
                        // Show success message
                    }
                },
                isLoading = uiState.isSendingInquiry,
                error = uiState.inquiryError
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(120.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun InquiryDialog(
    message: String,
    onMessageChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Send Inquiry") },
        text = {
            Column {
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    label = { Text("Your message") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5,
                    enabled = !isLoading
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSend,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}
