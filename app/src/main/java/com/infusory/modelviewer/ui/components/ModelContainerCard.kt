package com.infusory.modelviewer.ui.components

import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.LabelOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.infusory.modelviewer.domain.ActiveModelContainer
import com.infusory.modelviewer.domain.ProjectedLabel
import com.infusory.modelviewer.engine.FilamentModelSession
import com.infusory.modelviewer.ui.theme.AccentAmber
import com.infusory.modelviewer.ui.theme.AccentCyan
import com.infusory.modelviewer.ui.theme.AccentRose
import com.infusory.modelviewer.ui.theme.CardBackground
import com.infusory.modelviewer.ui.theme.CardBorder
import com.infusory.modelviewer.ui.theme.CardBorderNormal
import com.infusory.modelviewer.ui.theme.Slate400
import com.infusory.modelviewer.ui.theme.Slate800
import kotlin.math.roundToInt


@Composable
fun ModelContainerCard(
    container: ActiveModelContainer,
    onMove: (deltaX: Float, deltaY: Float) -> Unit,
    onResize: (scaleFactor: Float) -> Unit,
    onToggleInteraction: () -> Unit,
    onToggleLabels: () -> Unit,
    onClose: () -> Unit,
    onBringToFront: () -> Unit,
    onUpdateLabels: (List<ProjectedLabel>) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Reference to the active 3D Filament session
    val sessionState = remember { mutableStateOf<FilamentModelSession?>(null) }

    // Sync labels visibility with the 3D session
    LaunchedEffect(container.showLabels) {
        sessionState.value?.setLabelsEnabled(container.showLabels)
    }

    val widthDp = with(density) { container.width.toDp() }
    val heightDp = with(density) { container.height.toDp() }

    Box(
        modifier = modifier
            .offset { IntOffset(container.positionX.roundToInt(), container.positionY.roundToInt()) }
            .size(width = widthDp, height = heightDp)
            .zIndex(container.zIndex)
            .shadow(
                elevation = if (container.isInteractionMode) 16.dp else 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (container.isInteractionMode) AccentCyan.copy(alpha = 0.5f) else Color.Black
            )
            .background(
                color = CardBackground,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = if (container.isInteractionMode) 2.dp else 1.dp,
                color = if (container.isInteractionMode) CardBorder else CardBorderNormal,
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(container.isInteractionMode) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onBringToFront()
                    if (container.isInteractionMode) {
                        // INTERACTION MODE:
                        // One-finger drag -> Rotates 3D model
                        // Two-finger pinch -> Zooms 3D content
                        sessionState.value?.rotate(pan.x, pan.y)
                        if (zoom != 1.0f) {
                            sessionState.value?.zoom(zoom)
                        }
                    } else {
                        // NORMAL MODE:
                        // One-finger drag -> Moves container
                        // Two-finger pinch -> Resizes container
                        onMove(pan.x, pan.y)
                        if (zoom != 1.0f) {
                            onResize(zoom)
                        }
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onBringToFront
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ==========================================
            // HEADER BAR: Title & 3 Always-Visible Buttons
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        color = if (container.isInteractionMode) Color(0x3338BDF8) else Slate800.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Model Title & Mode Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (container.isInteractionMode) AccentCyan else Slate400,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = container.modelItem.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // The Three Mandatory Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 1. Interaction Toggle Button
                    IconButton(
                        onClick = onToggleInteraction,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (container.isInteractionMode) Icons.Default.Sync else Icons.Default.OpenWith,
                            contentDescription = if (container.isInteractionMode) "Switch to Normal Mode" else "Switch to Interaction Mode",
                            tint = if (container.isInteractionMode) AccentCyan else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 2. Label Toggle Button
                    IconButton(
                        onClick = onToggleLabels,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (container.showLabels) Icons.AutoMirrored.Filled.Label else Icons.AutoMirrored.Filled.LabelOff,
                            contentDescription = if (container.showLabels) "Hide Labels" else "Show Labels",
                            tint = if (container.showLabels) AccentAmber else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 3. Close Button
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Model Container",
                            tint = AccentRose,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ==========================================
            // 3D VIEWPORT & 2D LABEL OVERLAY
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            ) {
                // Native Filament TextureView
                AndroidView(
                    factory = { ctx ->
                        TextureView(ctx).apply {
                            isOpaque = false
                            val session = FilamentModelSession(
                                context = ctx,
                                textureView = this,
                                assetPath = container.modelItem.assetPath,
                                onLabelsProjected = onUpdateLabels
                            )
                            sessionState.value = session
                            session.setLabelsEnabled(container.showLabels)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Lifecycle management for native 3D engine session
                DisposableEffect(container.instanceId) {
                    onDispose {
                        sessionState.value?.destroy()
                        sessionState.value = null
                    }
                }

                // 2D Labels & Connector Lines Overlay
                if (container.showLabels) {
                    PartLabelOverlay(
                        labels = container.labels,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Visual gesture hint badge when in interaction mode
                if (container.isInteractionMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                            .background(Color(0xCC000000), RoundedCornerShape(12.dp))
                            .border(0.5.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "3D Orbit: 1 finger | Zoom: 2 fingers",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
