package com.infusory.modelviewer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infusory.modelviewer.domain.ProjectedLabel
import com.infusory.modelviewer.ui.theme.AccentCyan
import kotlin.math.roundToInt

@Composable
fun PartLabelOverlay(
    labels: List<ProjectedLabel>,
    modifier: Modifier = Modifier
) {
    if (labels.isEmpty()) return

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Draw anchor dots and connector lines on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (label in labels) {
                if (!label.isVisible) continue

                val anchor = Offset(label.screenX, label.screenY)
                // Determine label badge target position with a gentle offset
                val targetOffset = computeBadgeOffset(label.screenX, label.screenY, size.width, size.height)
                val badgePos = anchor + targetOffset

                // Connector line
                drawLine(
                    color = AccentCyan.copy(alpha = 0.85f),
                    start = anchor,
                    end = badgePos,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )

                // Part Anchor dot (outer glow ring)
                drawCircle(
                    color = AccentCyan.copy(alpha = 0.35f),
                    radius = 6.dp.toPx(),
                    center = anchor
                )
                // Part Anchor dot (inner solid dot)
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = anchor
                )
            }
        }

        // 2. Render label pill badges as Compose UI widgets
        for (label in labels) {
            if (!label.isVisible) continue

            val targetOffset = computeBadgeOffset(label.screenX, label.screenY, 800f, 800f)
            val badgeX = (label.screenX + targetOffset.x).roundToInt()
            val badgeY = (label.screenY + targetOffset.y).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(badgeX, badgeY) }
                    .background(
                        color = Color(0xEB0A101D),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = AccentCyan.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = label.labelText,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}


private fun computeBadgeOffset(anchorX: Float, anchorY: Float, width: Float, height: Float): Offset {
    val offsetX = if (anchorX > width * 0.6f) -50f else 35f
    val offsetY = if (anchorY > height * 0.6f) -35f else 25f
    return Offset(offsetX, offsetY)
}
