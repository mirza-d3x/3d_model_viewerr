package com.infusory.modelviewer.domain

/**
 * Represents a labelled part parsed from the GLB nodes metadata (`extras.prop`).
 *
 * @property nodeIndex Index of the node in the glTF JSON "nodes" array.
 * @property nodeName glTF name of the node (e.g. "Empty.004", "Empty.001").
 * @property labelText The label text defined in `extras.prop` (e.g. "Filament", "Piston head").
 * @property localTranslation 3-element array [x, y, z] representing local translation from glTF node.
 */
data class PartLabel(
    val nodeIndex: Int,
    val nodeName: String,
    val labelText: String,
    val localTranslation: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PartLabel
        return nodeIndex == other.nodeIndex &&
                nodeName == other.nodeName &&
                labelText == other.labelText &&
                localTranslation.contentEquals(other.localTranslation)
    }

    override fun hashCode(): Int {
        var result = nodeIndex
        result = 31 * result + nodeName.hashCode()
        result = 31 * result + labelText.hashCode()
        result = 31 * result + localTranslation.contentHashCode()
        return result
    }
}

/**
 * Screen-space projected coordinates for drawing a 2D label and connector line.
 *
 * @property labelText The text to display in the 2D pill badge.
 * @property screenX Projected X coordinate in the container's coordinate system.
 * @property screenY Projected Y coordinate in the container's coordinate system.
 * @property isVisible True if the 3D point is in front of the camera frustum and within bounds.
 */
data class ProjectedLabel(
    val labelText: String,
    val screenX: Float,
    val screenY: Float,
    val isVisible: Boolean
)
