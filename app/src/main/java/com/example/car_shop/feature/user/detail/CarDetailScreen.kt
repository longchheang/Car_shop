package com.example.car_shop.feature.user.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.car_shop.shared.components.InquiryDialog
import com.example.car_shop.shared.components.SpecBox
import com.example.car_shop.shared.utils.toLocaleString
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
    val isAdmin by viewModel.isAdmin.collectAsState()
    val shopLocation by viewModel.shopLocation.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(carId) {
        viewModel.loadCar(carId)
    }

    Scaffold(
        // Sticky Bottom Bar for the Action Button (only for non-admin users)
        bottomBar = {
            if (uiState.car != null && !isAdmin) {
                Surface(
                    tonalElevation = 3.dp,
                    shadowElevation = 8.dp
                ) {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)) {
                        Button(
                            onClick = { viewModel.showInquiryDialog() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState.car!!.isAvailable,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.car!!.isAvailable) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, "Send Inquiry")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (uiState.car!!.isAvailable) "Send Inquiry" else "Sold Out")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.error}")
                    }
                }
                uiState.car != null -> {
                    val car = uiState.car!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(bottom = padding.calculateBottomPadding())
                    ) {
                        // 1. Immersive Header Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            AsyncImage(
                                model = car.imageUrl.ifBlank { "https://via.placeholder.com/400" },
                                contentDescription = car.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Dark gradient at top so buttons are visible
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent)
                                        )
                                    )
                            )

                            if (!car.isAvailable) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "SOLD OUT",
                                        color = Color.White,
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 2. Content Body (Overlapping the image)
                        Column(
                            modifier = Modifier
                                .offset(y = (-24).dp) // Pull up over the image
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(24.dp)
                        ) {
                            // Header: Brand, Model, Price
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = car.brand,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = car.model,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "$${String.format(Locale.US, "%,.0f", car.price)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 3. Visual Specs Grid (No external icons required)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SpecBox(label = "Year", value = car.year.toString())
                                SpecBox(label = "Mileage", value = "${car.mileage.toLocaleString()} km")
                                // Shorten fuel/trans text if too long
                                SpecBox(label = "Fuel", value = car.fuelType)
                                SpecBox(label = "Gear", value = car.transmission)
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Simple Divider
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4. Description
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = car.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                            )
                        }
                    }

                    // 5. Floating Action Buttons (Top Bar)
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .padding(top = 24.dp), // Extra top padding for status bar area
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back Button
                        SmallFloatingActionButton(
                            onClick = onNavigateBack,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }

                        // Right side buttons (Location and Favorite - hidden for admin)
                        if (!isAdmin) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Location Button - uses shop location (same for all cars)
                                if (shopLocation.isNotBlank()) {
                                    SmallFloatingActionButton(
                                        onClick = {
                                            val gmmIntentUri = if (shopLocation.contains(",")) {
                                                // If it's coordinates (lat,lng)
                                                Uri.parse("geo:$shopLocation?q=$shopLocation")
                                            } else {
                                                // If it's a place name
                                                Uri.parse("geo:0,0?q=${Uri.encode(shopLocation)}")
                                            }
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                            mapIntent.setPackage("com.google.android.apps.maps")
                                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                                context.startActivity(mapIntent)
                                            } else {
                                                // Fallback to browser if Google Maps not installed
                                                val browserIntent = Intent(Intent.ACTION_VIEW,
                                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(shopLocation)}"))
                                                context.startActivity(browserIntent)
                                            }
                                        },
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        contentColor = MaterialTheme.colorScheme.primary,
                                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, "View Location")
                                    }
                                }

                                // Favorite Button
                                SmallFloatingActionButton(
                                    onClick = { viewModel.toggleFavorite() },
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    contentColor = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite"
                                    )
                                }
                            }
                        }
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
                        // Success action
                    }
                },
                isLoading = uiState.isSendingInquiry,
                error = uiState.inquiryError
            )
        }
    }
}
