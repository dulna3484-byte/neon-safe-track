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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.SafeTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    viewModel: SafeTrackViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("privacy_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
                    Column {
                        Text(
                            text = "Explicit Consent Only",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "SafeTrack is engineered from the ground up to prevent unauthorized or covert tracking.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            PolicySection(
                title = "1. No Hidden or Covert Tracking",
                content = "SafeTrack strictly prohibits covert tracking. We do NOT provide phone number tracking, SIM tracking, IMEI tracking, or background surveillance. A person's location can only be viewed if they actively opened the app, verified their consent, and initiated a timed sharing session."
            )

            PolicySection(
                title = "2. Operating System Location Permissions",
                content = "We always prompt the Android operating system for explicit location permissions. You can inspect, modify, or revoke this permission at any time in Android system settings or within the SafeTrack settings screen."
            )

            PolicySection(
                title = "3. Auto-Expiring Sharing Codes",
                content = "Every sharing link or code (e.g. SAFE-XXXX) is temporary. Sessions automatically expire after your chosen duration (15 minutes, 1 hour, or 8 hours). Furthermore, you have a one-tap 'Stop Sharing' button that instantly revokes access."
            )

            PolicySection(
                title = "4. Data Minimization & Security",
                content = "Passwords are cryptographically hashed using SHA-256 and salt before storage. Plaintext passwords are never kept. We do not sell your personal data or location coordinates to advertisers, third parties, or brokers."
            )

            PolicySection(
                title = "5. Local-Only Location History",
                content = "Location history is completely optional. If turned on, records are stored locally in your encrypted device database. You can permanently wipe your entire history with one tap at any time."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    viewModel: SafeTrackViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("terms_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            PolicySection(
                title = "1. Acceptance of Terms",
                content = "By creating an account or using SafeTrack, you agree to these Terms of Service. SafeTrack is designed exclusively for consensual location sharing among trusted friends and family."
            )

            PolicySection(
                title = "2. Prohibited Uses",
                content = "You agree NOT to use SafeTrack to: (a) stalk, harass, or intimidate any person; (b) install or configure on a device without the explicit consent and continuous awareness of the device owner; (c) attempt unauthorized reverse engineering; (d) misrepresent your identity."
            )

            PolicySection(
                title = "3. User Control & Immediate Revocation",
                content = "Users maintain 100% control over their data. Any session can be stopped instantaneously by tapping 'Stop Sharing' in the app. No secondary party can force a location update once sharing has ceased."
            )

            PolicySection(
                title = "4. Emergency & Real-Time Disclaimer",
                content = "While SafeTrack strives for high accuracy, GPS accuracy depends on device hardware, cellular networks, and satellite line-of-sight. SafeTrack should not be solely relied upon as a primary emergency life-safety response tool."
            )

            PolicySection(
                title = "5. Account Termination",
                content = "You may delete your account and all associated records permanently via the Settings screen at any time."
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
