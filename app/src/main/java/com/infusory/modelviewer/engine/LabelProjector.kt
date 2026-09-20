package com.infusory.modelviewer.engine

import com.infusory.modelviewer.domain.ProjectedLabel


object LabelProjector {

    /**
     * Projects a 3D point in world space into 2D viewport coordinates.
     *
     * @param worldX 3D X coordinate in world space.
     * @param worldY 3D Y coordinate in world space.
     * @param worldZ 3D Z coordinate in world space.
     * @param viewMatrix 16-element FloatArray or DoubleArray representing camera view matrix.
     * @param projectionMatrix 16-element DoubleArray representing camera projection matrix.
     * @param viewportWidth Width of the 3D viewport in pixels.
     * @param viewportHeight Height of the 3D viewport in pixels.
     * @return [ProjectedLabel] containing screen coordinates and visibility flag.
     */
    fun projectPoint(
        labelText: String,
        worldX: Float,
        worldY: Float,
        worldZ: Float,
        viewMatrix: DoubleArray,
        projectionMatrix: DoubleArray,
        viewportWidth: Float,
        viewportHeight: Float
    ): ProjectedLabel {
        // 1. Transform World to View space: P_view = ViewMatrix * P_world
        // Column-major: index = col * 4 + row
        val vx = viewMatrix[0] * worldX + viewMatrix[4] * worldY + viewMatrix[8] * worldZ + viewMatrix[12]
        val vy = viewMatrix[1] * worldX + viewMatrix[5] * worldY + viewMatrix[9] * worldZ + viewMatrix[13]
        val vz = viewMatrix[2] * worldX + viewMatrix[6] * worldY + viewMatrix[10] * worldZ + viewMatrix[14]
        val vw = viewMatrix[3] * worldX + viewMatrix[7] * worldY + viewMatrix[11] * worldZ + viewMatrix[15]

        // 2. Transform View to Clip space: P_clip = ProjMatrix * P_view
        val cx = projectionMatrix[0] * vx + projectionMatrix[4] * vy + projectionMatrix[8] * vz + projectionMatrix[12] * vw
        val cy = projectionMatrix[1] * vx + projectionMatrix[5] * vy + projectionMatrix[9] * vz + projectionMatrix[13] * vw
        val cz = projectionMatrix[2] * vx + projectionMatrix[6] * vy + projectionMatrix[10] * vz + projectionMatrix[14] * vw
        val cw = projectionMatrix[3] * vx + projectionMatrix[7] * vy + projectionMatrix[11] * vz + projectionMatrix[15] * vw

        // If cw <= 0, the point is behind the camera plane
        if (cw <= 0.0001) {
            return ProjectedLabel(
                labelText = labelText,
                screenX = 0f,
                screenY = 0f,
                isVisible = false
            )
        }

        // 3. Perspective divide to Normalized Device Coordinates (NDC)
        val ndcX = (cx / cw).toFloat()
        val ndcY = (cy / cw).toFloat()
        val ndcZ = (cz / cw).toFloat()

        // Check if inside visible frustum (with a small margin so labels don't clip harshly at edges)
        val isInsideFrustum = ndcZ in -1.2f..1.2f && ndcX in -1.1f..1.1f && ndcY in -1.1f..1.1f

        // 4. Map NDC [-1, 1] to Viewport screen coordinates [0, width], [0, height]
        // Note: Y is flipped because in NDC +1 is top, but in Android View coords +1 is down
        val screenX = (ndcX + 1.0f) * 0.5f * viewportWidth
        val screenY = (1.0f - ndcY) * 0.5f * viewportHeight

        return ProjectedLabel(
            labelText = labelText,
            screenX = screenX,
            screenY = screenY,
            isVisible = isInsideFrustum
        )
    }


    fun toDoubleArray(floats: FloatArray): DoubleArray {
        val out = DoubleArray(floats.size)
        for (i in floats.indices) {
            out[i] = floats[i].toDouble()
        }
        return out
    }
}
