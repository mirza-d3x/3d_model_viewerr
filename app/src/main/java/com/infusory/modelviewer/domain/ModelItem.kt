package com.infusory.modelviewer.domain

/**
 * Definition of a bundled 3D model asset.
 *
 * @property id Unique identifier for the model type.
 * @property title Human-readable display title.
 * @property subtitle Brief description or category.
 * @property assetPath Relative path within the app assets directory.
 * @property expectedLabels Expected count of labelled parts from the specification.
 */
data class ModelItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val assetPath: String,
    val expectedLabels: Int
) {
    companion object {
        val BUNDLED_MODELS = listOf(
            ModelItem(
                id = "bulb",
                title = "Light Bulb",
                subtitle = "Incandescent lamp anatomy (6 parts)",
                assetPath = "models/Bulb.glb",
                expectedLabels = 6
            ),
            ModelItem(
                id = "fiagena",
                title = "Bacterial Flagellum",
                subtitle = "Flagellar motor & filament (7 parts)",
                assetPath = "models/Fiagena.glb",
                expectedLabels = 7
            ),
            ModelItem(
                id = "lungs",
                title = "Respiratory System",
                subtitle = "Lungs, trachea & bronchi (5 parts)",
                assetPath = "models/Lungs.glb",
                expectedLabels = 5
            ),
            ModelItem(
                id = "microscope",
                title = "Compound Microscope",
                subtitle = "Optical microscope structure (12 parts)",
                assetPath = "models/Microscope.glb",
                expectedLabels = 12
            ),
            ModelItem(
                id = "solarsystem",
                title = "Solar System",
                subtitle = "Planetary orbits & sun (9 parts)",
                assetPath = "models/solarsystem.glb",
                expectedLabels = 9
            )
        )
    }
}
