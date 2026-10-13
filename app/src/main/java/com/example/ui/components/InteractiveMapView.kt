package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeviceLocation
import com.example.data.SharingSessionEntity
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun InteractiveMapView(
    userLocation: DeviceLocation,
    sharedSession: SharingSessionEntity?,
    modifier: Modifier = Modifier,
    onRecenterUser: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    // Zoom and pan offsets
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Pulse animation for location indicators
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    // Calculate distance between user and shared contact if both exist
    val distanceMeters = remember(userLocation, sharedSession) {
        if (sharedSession != null && sharedSession.sharingStatus == "ACTIVE") {
            calculateDistance(
                userLocation.latitude, userLocation.longitude,
                sharedSession.lastLatitude, sharedSession.lastLongitude
            )
        } else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3.5f)
                    panOffset += pan
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f + panOffset.x, size.height / 2f + panOffset.y)

            // Draw stylized vector street grid and landscape
            drawMapBackground(center, scale, size)

            // User location coordinates in screen space
            val userScreenPos = center
            // Shared person screen space position relative to user location
            val sharedScreenPos = if (sharedSession != null && sharedSession.sharingStatus == "ACTIVE") {
                val dLat = (sharedSession.lastLatitude - userLocation.latitude) * 55000f * scale
                val dLng = (sharedSession.lastLongitude - userLocation.longitude) * 55000f * scale
                Offset(center.x + dLng.toFloat(), center.y - dLat.toFloat())
            } else null

            // If shared person is visible, draw connecting trajectory dash line
            if (sharedScreenPos != null) {
                drawLine(
                    color = Color(0xFF10B981).copy(alpha = 0.6f),
                    start = userScreenPos,
                    end = sharedScreenPos,
                    strokeWidth = 3f * scale,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )

                // Shared person marker
                drawSharedMarker(
                    position = sharedScreenPos,
                    name = sharedSession?.ownerName ?: "",
                    pulseRadius = pulseRadius * scale,
                    pulseAlpha = pulseAlpha
                )
            }

            // User marker
            drawUserMarker(
                position = userScreenPos,
                accuracyRadius = (userLocation.accuracy * 2.5f * scale).coerceIn(16f, 90f),
                pulseRadius = pulseRadius * scale,
                pulseAlpha = pulseAlpha
            )
        }

        // Distance Badge floating at top if sharing is active
        if (distanceMeters != null && sharedSession != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Text(
                        text = "${sharedSession.ownerName} is ${formatDistance(distanceMeters)} away",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Map Zoom & Recenter Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = { scale = (scale * 1.25f).coerceAtMost(3.5f) },
                modifier = Modifier
                    .size(42.dp)
                    .testTag("zoom_in_button"),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = { scale = (scale * 0.8f).coerceAtLeast(0.5f) },
                modifier = Modifier
                    .size(42.dp)
                    .testTag("zoom_out_button"),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = {
                    panOffset = Offset.Zero
                    scale = 1.0f
                    onRecenterUser()
                },
                modifier = Modifier
                    .size(46.dp)
                    .testTag("recenter_button"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.LocationSearching, contentDescription = "Recenter on My Location", modifier = Modifier.size(22.dp))
            }
        }
    }
}

private fun DrawScope.drawMapBackground(center: Offset, scale: Float, canvasSize: Size) {
    // Background fill
    drawRect(Color(0xFF131B2E))

    // Stylized river/waterway
    val waterPath = Path().apply {
        val startY = (center.y - 120f * scale).coerceIn(-100f, canvasSize.height + 100f)
        moveTo(0f, startY)
        cubicTo(
            canvasSize.width * 0.35f, startY - 60f * scale,
            canvasSize.width * 0.65f, startY + 90f * scale,
            canvasSize.width, startY + 20f * scale
        )
        lineTo(canvasSize.width, startY + 70f * scale)
        cubicTo(
            canvasSize.width * 0.65f, startY + 140f * scale,
            canvasSize.width * 0.35f, startY - 10f * scale,
            0f, startY + 50f * scale
        )
        close()
    }
    drawPath(waterPath, Color(0xFF1E3A5F).copy(alpha = 0.7f))

    // Stylized parks / green spaces
    val parkX = center.x - 180f * scale
    val parkY = center.y + 80f * scale
    drawRoundRect(
        color = Color(0xFF0F382E).copy(alpha = 0.5f),
        topLeft = Offset(parkX, parkY),
        size = Size(140f * scale, 100f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f * scale)
    )

    // Grid streets
    val streetColor = Color(0xFF243048)
    val mainRoadColor = Color(0xFF334466)
    val spacing = 75f * scale

    val startX = (center.x % spacing)
    var curX = startX
    while (curX < canvasSize.width) {
        val isMain = ((curX - center.x) / spacing).roundToInt() % 3 == 0
        drawLine(
            color = if (isMain) mainRoadColor else streetColor,
            start = Offset(curX, 0f),
            end = Offset(curX, canvasSize.height),
            strokeWidth = if (isMain) 3f * scale else 1.5f * scale
        )
        curX += spacing
    }

    val startY = (center.y % spacing)
    var curY = startY
    while (curY < canvasSize.height) {
        val isMain = ((curY - center.y) / spacing).roundToInt() % 3 == 0
        drawLine(
            color = if (isMain) mainRoadColor else streetColor,
            start = Offset(0f, curY),
            end = Offset(canvasSize.width, curY),
            strokeWidth = if (isMain) 3f * scale else 1.5f * scale
        )
        curY += spacing
    }
}

private fun DrawScope.drawUserMarker(
    position: Offset,
    accuracyRadius: Float,
    pulseRadius: Float,
    pulseAlpha: Float
) {
    // Accuracy outer circle
    drawCircle(
        color = Color(0xFF06B6D4).copy(alpha = 0.15f),
        radius = accuracyRadius,
        center = position
    )
    drawCircle(
        color = Color(0xFF06B6D4).copy(alpha = 0.35f),
        radius = accuracyRadius,
        center = position,
        style = Stroke(width = 1.5f)
    )

    // Animated pulse ring
    drawCircle(
        color = Color(0xFF06B6D4).copy(alpha = pulseAlpha),
        radius = pulseRadius,
        center = position
    )

    // White border ring
    drawCircle(
        color = Color.White,
        radius = 11f,
        center = position
    )

    // Inner blue core
    drawCircle(
        color = Color(0xFF0284C7),
        radius = 8f,
        center = position
    )
}

private fun DrawScope.drawSharedMarker(
    position: Offset,
    name: String,
    pulseRadius: Float,
    pulseAlpha: Float
) {
    // Pulse ring
    drawCircle(
        color = Color(0xFF10B981).copy(alpha = pulseAlpha),
        radius = pulseRadius + 6f,
        center = position
    )

    // Outer circle
    drawCircle(
        color = Color.White,
        radius = 16f,
        center = position
    )

    // Emerald circle
    drawCircle(
        color = Color(0xFF10B981),
        radius = 13f,
        center = position
    )

    // Pin indicator icon core
    drawCircle(
        color = Color(0xFF064E3B),
        radius = 5f,
        center = position
    )
}

// Distance formula
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

private fun formatDistance(meters: Double): String {
    return if (meters < 1000) {
        "${meters.roundToInt()} m"
    } else {
        "%.1f km".format(meters / 1000.0)
    }
}
