package com.example.car_shop.feature.profile

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    initialLocation: String = "",
    viewModel: MapPickerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Parse initial location
    val initialLatLng = remember {
        if (initialLocation.isNotBlank() && initialLocation.contains(",")) {
            try {
                val parts = initialLocation.split(",")
                LatLng(
                    parts[0].trim().toDouble(),
                    parts[1].trim().toDouble()
                )
            } catch (_: Exception) {
                LatLng(0.0, 0.0)
            }
        } else {
            LatLng(0.0, 0.0)
        }
    }

    var selectedLocation by remember {
        mutableStateOf(if (initialLocation.isNotBlank()) initialLatLng else null)
    }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 10f)
    }

    val mapProperties = remember {
        MapProperties(isMyLocationEnabled = false)
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false
        )
    }

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (hasLocationPermission) {
            // Get current location after permission granted
            isLoadingLocation = true
            coroutineScope.launch {
                try {
                    val location = fusedLocationClient.lastLocation.await()
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        selectedLocation = currentLatLng
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
                        )
                    }
                } catch (_: Exception) {
                    // Handle error silently
                } finally {
                    isLoadingLocation = false
                }
            }
        }
    }

    // Function to get current location
    fun getCurrentLocation() {
        if (hasLocationPermission) {
            isLoadingLocation = true
            coroutineScope.launch {
                try {
                    val location = fusedLocationClient.lastLocation.await()
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        selectedLocation = currentLatLng
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)
                        )
                    }
                } catch (_: Exception) {
                    // Handle error silently
                } finally {
                    isLoadingLocation = false
                }
            }
        } else {
            // Request permission
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pick Location") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                }
            ) {
                selectedLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Selected Location",
                        snippet = String.format(
                            Locale.US,
                            "%.6f, %.6f",
                            location.latitude,
                            location.longitude
                        )
                    )
                }
            }

            // My Location FAB
            FloatingActionButton(
                onClick = { getCurrentLocation() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                if (isLoadingLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "My Location"
                    )
                }
            }

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedLocation != null) {
                        Text(
                            text = "Selected Location",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(
                                Locale.US,
                                "%.6f, %.6f",
                                selectedLocation!!.latitude,
                                selectedLocation!!.longitude
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Tap on the map to select a location",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            selectedLocation?.let { location ->
                                isSaving = true
                                val locationString = String.format(
                                    Locale.US,
                                    "%f,%f",
                                    location.latitude,
                                    location.longitude
                                )
                                coroutineScope.launch {
                                    viewModel.saveLocation(locationString)
                                    onNavigateBack()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedLocation != null && !isSaving,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Confirm")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm Location")
                        }
                    }
                }
            }
        }
    }
}
