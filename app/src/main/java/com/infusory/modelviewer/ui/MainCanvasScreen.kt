package com.infusory.modelviewer.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infusory.modelviewer.domain.ModelItem
import com.infusory.modelviewer.ui.components.AddModelBottomSheet
import com.infusory.modelviewer.ui.components.ModelContainerCard
import com.infusory.modelviewer.ui.theme.AccentCyan
import com.infusory.modelviewer.ui.theme.Slate400
import com.infusory.modelviewer.ui.theme.Slate800
import com.infusory.modelviewer.ui.theme.Slate900
import com.infusory.modelviewer.ui.theme.Slate950


@Composable
fun MainCanvasScreen(
    viewModel: ModelViewerViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val activeModels by viewModel.activeModels.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF131B2E),
                        Slate950
                    )
                )
            )
    ) {
        // ==========================================
        // ACTIVE 3D MODEL CONTAINERS
        // ==========================================
        for (container in activeModels) {
            androidx.compose.runtime.key(container.instanceId) {
                ModelContainerCard(
                    container = container,
                    onMove = { dx, dy -> viewModel.updateContainerPosition(container.instanceId, dx, dy) },
                    onResize = { scale -> viewModel.updateContainerSize(container.instanceId, scale) },
                    onToggleInteraction = { viewModel.toggleInteractionMode(container.instanceId) },
                    onToggleLabels = { viewModel.toggleLabels(container.instanceId) },
                    onClose = { viewModel.removeModel(container.instanceId) },
                    onBringToFront = { viewModel.bringToFront(container.instanceId) },
                    onUpdateLabels = { labels -> viewModel.updateProjectedLabels(container.instanceId, labels) }
                )
            }
        }

        // ==========================================
        // EMPTY STATE (when all models are closed)
        // ==========================================
        if (activeModels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Slate900, CircleShape)
                            .border(1.dp, Slate800, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Canvas is Empty",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tap '+ Add Model' below to load a 3D model",
                        color = Slate400,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ==========================================
        // TOP HUD BAR: Title & Multi-Model Stats
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "3D Model Viewer",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Single-Canvas Multi-Viewport",
                    color = Slate400,
                    fontSize = 11.sp
                )
            }

            // Live model count badge
            Box(
                modifier = Modifier
                    .background(Slate900.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .border(1.dp, AccentCyan.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(AccentCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${activeModels.size} Active Models",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ==========================================
        // FLOATING ACTION BUTTON: "+ Add Model"
        // ==========================================
        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add 3D Model",
                    tint = Color.Black
                )
            },
            text = {
                Text(
                    text = "Add Model",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            containerColor = AccentCyan,
            contentColor = Color.Black,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )

        // ==========================================
        // ADD MODEL BOTTOM SHEET
        // ==========================================
        if (showAddSheet) {
            AddModelBottomSheet(
                models = ModelItem.BUNDLED_MODELS,
                onSelectModel = { model ->
                    viewModel.addModel(model)
                },
                onDismiss = { showAddSheet = false }
            )
        }
    }
}
