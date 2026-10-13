package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.SafeTrackViewModel
import com.example.ui.components.InteractiveMapView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: SafeTrackViewModel,
    modifier: Modifier = Modifier
) {
    val currentLocation by viewModel.currentLocation.collectAsState()
    val activeSession by viewModel.activeSharingSession.collectAsState()
    val joinedSession by viewModel.joinedSession.collectAsState()

    // Active session could be either the one user is sharing or one user is viewing
    val displaySession = joinedSession ?: if (activeSession?.sharingStatus == "ACTIVE") activeSession else null
    val isViewingOther = joinedSession != null
    val isSharingMine = activeSession != null && activeSession?.sharingStatus == "ACTIVE"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isViewingOther) "Viewing ${joinedSession?.ownerName}" else "Live SafeMap",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = when {
                                isViewingOther -> "Code: ${joinedSession?.sessionId} • Verified Consent"
                                isSharingMine -> "Sharing Active • Code: ${activeSession?.sessionId}"
                                else -> "Private • Your location only"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSharingMine || isViewingOther) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        modifier = Modifier.testTag("map_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshLocation() },
                        modifier = Modifier.testTag("map_refresh_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh GPS Location")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Vector Map
            InteractiveMapView(
                userLocation = currentLocation,
                sharedSession = displaySession,
                onRecenterUser = { viewModel.refreshLocation() },
                onRefresh = { viewModel.refreshLocation() }
            )

            // Bottom Information Card
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Status row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSharingMine -> Color(0xFF10B981)
                                            isViewingOther -> Color(0xFF06B6D4)
                                            else -> Color(0xFF94A3B8)
                                        }
                                    )
                            )
                            Text(
                                text = when {
                                    isViewingOther -> "Tracking Authorized Session"
                                    isSharingMine -> "Sharing Active with ${activeSession?.invitedName ?: "code"}"
                                    else -> "Private GPS Mode"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                        Text(
                            text = "Updated: ${timeFormat.format(Date())}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Address and GPS info
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (isViewingOther && joinedSession != null) joinedSession?.address ?: "" else currentLocation.address,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        val lat = if (isViewingOther && joinedSession != null) joinedSession?.lastLatitude else currentLocation.latitude
                        val lng = if (isViewingOther && joinedSession != null) joinedSession?.lastLongitude else currentLocation.longitude
                        Text(
                            text = "Lat: %.5f • Lng: %.5f • Accuracy: ±%dm".format(lat ?: 0.0, lng ?: 0.0, currentLocation.accuracy.toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isSharingMine) {
                            Button(
                                onClick = { viewModel.stopSharing() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop Sharing")
                            }
                        } else if (isViewingOther) {
                            Button(
                                onClick = { viewModel.stopViewingSharedSession() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop Viewing")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.SHARE) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share My Location")
                            }

                            OutlinedButton(
                                onClick = { viewModel.navigateTo(AppScreen.JOIN) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Enter Code")
                            }
                        }
                    }
                }
            }
        }
    }
}
