package com.example.car_shop.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.car_shop.data.model.Car
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = hiltViewModel(),
    onAddCar: () -> Unit,
    onEditCar: (String) -> Unit,
    onViewInquiries: () -> Unit,
    onLogout: () -> Unit
) {
    val cars by viewModel.cars.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var carToDelete by remember { mutableStateOf<Car?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menu")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Inquiries") },
                            onClick = {
                                showMenu = false
                                onViewInquiries()
                            },
                            leadingIcon = { Icon(Icons.Default.Email, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMenu = false
                                viewModel.logout()
                                onLogout()
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCar) {
                Icon(Icons.Default.Add, "Add Car")
            }
        }
    ) { padding ->
        if (cars.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No cars yet. Add your first car!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(cars) { car ->
                    AdminCarItem(
                        car = car,
                        onEdit = { onEditCar(car.id) },
                        onDelete = { carToDelete = car }
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        carToDelete?.let { car ->
            AlertDialog(
                onDismissRequest = { carToDelete = null },
                title = { Text("Delete Car") },
                text = { Text("Are you sure you want to delete ${car.name}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCar(car.id)
                            carToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { carToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun AdminCarItem(
    car: Car,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = car.imageUrl.ifBlank { "https://via.placeholder.com/100" },
                contentDescription = car.name,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = car.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$${String.format(Locale.US, "%,.0f", car.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${car.year} • ${car.mileage} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit")
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
