package com.infusory.modelviewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infusory.modelviewer.domain.ModelItem
import com.infusory.modelviewer.ui.theme.AccentCyan
import com.infusory.modelviewer.ui.theme.Slate400
import com.infusory.modelviewer.ui.theme.Slate700
import com.infusory.modelviewer.ui.theme.Slate850
import com.infusory.modelviewer.ui.theme.Slate900


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddModelBottomSheet(
    models: List<ModelItem>,
    onSelectModel: (ModelItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Slate900,
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select 3D Model",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose from the 5 bundled GLB assets to load onto the canvas",
                fontSize = 13.sp,
                color = Slate400
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(models) { model ->
                    ModelSelectionItem(
                        model = model,
                        onClick = {
                            onSelectModel(model)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelSelectionItem(
    model: ModelItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate850, RoundedCornerShape(14.dp))
            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(AccentCyan.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ViewInAr,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = model.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = model.subtitle,
                    color = Slate400,
                    fontSize = 12.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .size(30.dp)
                .background(AccentCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
