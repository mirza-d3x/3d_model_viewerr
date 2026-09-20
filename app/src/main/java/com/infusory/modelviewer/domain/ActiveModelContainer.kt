package com.infusory.modelviewer.domain

import java.util.UUID

/**
 * State representing an active 3D model container rendered on the canvas.
 *
 * @property instanceId Unique instance identifier allowing multiple instances of the same model.
 * @property modelItem The definition of the bundled model.
 * @property positionX Horizontal canvas coordinate in pixels.
 * @property positionY Vertical canvas coordinate in pixels.
 * @property width Container width in pixels.
 * @property height Container height in pixels.
 * @property zIndex Stacking order on the canvas (highest is on top).
 * @property isInteractionMode When true, drag rotates the 3D model and pinch zooms the 3D mesh.
 *                             When false (Normal Mode), drag moves the container and pinch resizes the container.
 * @property showLabels Whether part labels are visible (defaults to false per specification).
 * @property rotationX Pitch rotation in degrees for 3D model interaction.
 * @property rotationY Yaw rotation in degrees for 3D model interaction.
 * @property zoom Zoom scale multiplier for 3D model interaction.
 * @property labels List of part labels with projected 2D coordinates.
 */
data class ActiveModelContainer(
    val instanceId: String = UUID.randomUUID().toString(),
    val modelItem: ModelItem,
    val positionX: Float = 60f,
    val positionY: Float = 120f,
    val width: Float = 300f,
    val height: Float = 320f,
    val zIndex: Float = 0f,
    val isInteractionMode: Boolean = false,
    val showLabels: Boolean = false,
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val zoom: Float = 1.0f,
    val labels: List<ProjectedLabel> = emptyList()
) {
    companion object {
        const val MIN_WIDTH = 180f
        const val MIN_HEIGHT = 200f
        const val MAX_WIDTH = 700f
        const val MAX_HEIGHT = 750f
    }
}
