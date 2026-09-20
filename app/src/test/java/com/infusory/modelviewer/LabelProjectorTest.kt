package com.infusory.modelviewer

import com.infusory.modelviewer.engine.LabelProjector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LabelProjectorTest {

    @Test
    fun projectCenterPoint() {
        // Identity view matrix (looking down -Z)
        // Camera at (0, 0, 4) looking at (0, 0, 0)
        // View matrix translates world by -4 in Z: [12]=0, [13]=0, [14]=-4, [15]=1
        val viewMatrix = doubleArrayOf(
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, -4.0, 1.0
        )

        // Simple symmetric perspective projection matrix (fov 60 deg, aspect 1, near 0.1, far 100)
        // P[0] = 1.0, P[5] = 1.0, P[10] = -1.0, P[11] = -1.0, P[14] = -0.2
        val projMatrix = doubleArrayOf(
            1.732, 0.0, 0.0, 0.0,
            0.0, 1.732, 0.0, 0.0,
            0.0, 0.0, -1.002, -1.0,
            0.0, 0.0, -0.2, 0.0
        )

        val result = LabelProjector.projectPoint(
            labelText = "Center Test",
            worldX = 0f,
            worldY = 0f,
            worldZ = 0f,
            viewMatrix = viewMatrix,
            projectionMatrix = projMatrix,
            viewportWidth = 300f,
            viewportHeight = 300f
        )

        assertTrue(result.isVisible)
        assertEquals(150f, result.screenX, 1.0f)
        assertEquals(150f, result.screenY, 1.0f)
    }

    @Test
    fun projectBehindCameraPoint() {
        // Point is at (0, 0, 10), but camera is at (0, 0, 4) looking along -Z
        // So point is BEHIND camera!
        val viewMatrix = doubleArrayOf(
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, -4.0, 1.0
        )
        val projMatrix = doubleArrayOf(
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, -1.0, -1.0,
            0.0, 0.0, -0.2, 0.0
        )

        val result = LabelProjector.projectPoint(
            labelText = "Behind Test",
            worldX = 0f,
            worldY = 0f,
            worldZ = 10f,
            viewMatrix = viewMatrix,
            projectionMatrix = projMatrix,
            viewportWidth = 300f,
            viewportHeight = 300f
        )

        assertFalse("Point behind camera must not be visible", result.isVisible)
    }
}
