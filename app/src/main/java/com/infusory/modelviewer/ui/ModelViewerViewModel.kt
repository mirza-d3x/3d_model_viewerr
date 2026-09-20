package com.infusory.modelviewer.ui

import androidx.lifecycle.ViewModel
import com.infusory.modelviewer.domain.ActiveModelContainer
import com.infusory.modelviewer.domain.ModelItem
import com.infusory.modelviewer.domain.ProjectedLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class ModelViewerViewModel : ViewModel() {

    private val _activeModels = MutableStateFlow<List<ActiveModelContainer>>(emptyList())
    val activeModels: StateFlow<List<ActiveModelContainer>> = _activeModels.asStateFlow()

    private var highestZIndex = 1f
    private var modelSpawnIndex = 0

    init {
        // Auto-load the first model on startup so the canvas is immediately active
        addModel(ModelItem.BUNDLED_MODELS[0])
    }

    fun addModel(modelItem: ModelItem) {
        highestZIndex += 1f
        // Stagger spawn positions so new containers don't completely occlude previous ones
        val offsetX = 40f + (modelSpawnIndex % 4) * 50f
        val offsetY = 100f + (modelSpawnIndex % 4) * 60f
        modelSpawnIndex++

        val newContainer = ActiveModelContainer(
            modelItem = modelItem,
            positionX = offsetX,
            positionY = offsetY,
            width = 300f,
            height = 330f,
            zIndex = highestZIndex,
            isInteractionMode = false,
            showLabels = false
        )

        _activeModels.update { current ->
            current + newContainer
        }
    }


    fun removeModel(instanceId: String) {
        _activeModels.update { current ->
            current.filterNot { it.instanceId == instanceId }
        }
    }

    fun toggleInteractionMode(instanceId: String) {
        bringToFront(instanceId)
        _activeModels.update { list ->
            list.map { container ->
                if (container.instanceId == instanceId) {
                    container.copy(isInteractionMode = !container.isInteractionMode)
                } else {
                    container
                }
            }
        }
    }


    fun toggleLabels(instanceId: String) {
        _activeModels.update { list ->
            list.map { container ->
                if (container.instanceId == instanceId) {
                    container.copy(showLabels = !container.showLabels)
                } else {
                    container
                }
            }
        }
    }


    fun updateContainerPosition(instanceId: String, deltaX: Float, deltaY: Float) {
        _activeModels.update { list ->
            list.map { container ->
                if (container.instanceId == instanceId) {
                    container.copy(
                        positionX = container.positionX + deltaX,
                        positionY = container.positionY + deltaY
                    )
                } else {
                    container
                }
            }
        }
    }


    fun updateContainerSize(instanceId: String, scaleFactor: Float) {
        _activeModels.update { list ->
            list.map { container ->
                if (container.instanceId == instanceId) {
                    val newWidth = (container.width * scaleFactor).coerceIn(
                        ActiveModelContainer.MIN_WIDTH,
                        ActiveModelContainer.MAX_WIDTH
                    )
                    val newHeight = (container.height * scaleFactor).coerceIn(
                        ActiveModelContainer.MIN_HEIGHT,
                        ActiveModelContainer.MAX_HEIGHT
                    )
                    container.copy(width = newWidth, height = newHeight)
                } else {
                    container
                }
            }
        }
    }


    fun bringToFront(instanceId: String) {
        highestZIndex += 1f
        _activeModels.update { list ->
            list.map { container ->
                if (container.instanceId == instanceId) {
                    container.copy(zIndex = highestZIndex)
                } else {
                    container
                }
            }
        }
    }


    fun updateProjectedLabels(instanceId: String, labels: List<ProjectedLabel>) {
        _activeModels.update { list ->
            list.map { container ->
                if (container.instanceId == instanceId) {
                    container.copy(labels = labels)
                } else {
                    container
                }
            }
        }
    }
}
