package com.infusory.modelviewer.engine

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.TextureView
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.infusory.modelviewer.data.GlbMetadataParser
import com.infusory.modelviewer.domain.PartLabel
import com.infusory.modelviewer.domain.ProjectedLabel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.sin


class FilamentModelSession(
    private val context: Context,
    private val textureView: TextureView,
    private val assetPath: String,
    private val onLabelsProjected: (List<ProjectedLabel>) -> Unit
) : Choreographer.FrameCallback {

    private val engine: Engine = FilamentEngineManager.getEngine(context)
    private val scene: Scene = engine.createScene()
    private val cameraEntity: Int = EntityManager.get().create()
    private val camera: Camera = engine.createCamera(cameraEntity)
    private val view: View = engine.createView()
    private val renderer: Renderer = engine.createRenderer()

    private var uiHelper: UiHelper? = null
    private var swapChain: SwapChain? = null

    private var assetLoader: AssetLoader? = null
    private var resourceLoader: ResourceLoader? = null
    private var asset: FilamentAsset? = null

    private var lightEntity: Int = 0

    // Camera Orbit & Zoom State
    private var yaw: Float = 25f
    private var pitch: Float = 15f
    private var cameraDistance: Float = 3.5f

    private var viewportWidth: Int = 300
    private var viewportHeight: Int = 320

    // Labels & Projection
    private var labelsMetadata: List<PartLabel> = emptyList()
    private var labeledEntities: List<Pair<PartLabel, Int>> = emptyList()
    private var labelsEnabled: Boolean = false

    private val viewMatrixDouble = DoubleArray(16)
    private val projMatrixDouble = DoubleArray(16)
    private val entityWorldMatrix = FloatArray(16)

    // Performance & Dirty Rendering Cadence
    @Volatile
    private var isDirty: Boolean = true
    private var isDestroyed: Boolean = false
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        FilamentEngineManager.registerSession()

        // Setup View configuration
        view.scene = scene
        view.camera = camera
        view.blendMode = View.BlendMode.TRANSLUCENT

        // Configure Clear Options
        val clearOptions = renderer.clearOptions
        clearOptions.clear = true
        clearOptions.clearColor = floatArrayOf(0.04f, 0.06f, 0.10f, 1.0f) // Sleek dark slate
        renderer.clearOptions = clearOptions

        setupLighting()
        setupUiHelper()
        loadModelAsync()

        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun setupLighting() {
        // Create a directional sunlight for clear 3D definition and part visibility
        lightEntity = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 0.98f, 0.95f)
            .intensity(90_000f)
            .direction(0.4f, -0.8f, -0.6f)
            .castShadows(false) // Disabled for low-end GPU performance
            .build(engine, lightEntity)
        scene.addEntity(lightEntity)
    }

    private fun setupUiHelper() {
        uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
        uiHelper?.renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: android.view.Surface) {
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = engine.createSwapChain(surface)
                isDirty = true
            }

            override fun onDetachedFromSurface() {
                swapChain?.let { engine.destroySwapChain(it) }
                swapChain = null
            }

            override fun onResized(width: Int, height: Int) {
                viewportWidth = maxOf(width, 1)
                viewportHeight = maxOf(height, 1)
                view.viewport = Viewport(0, 0, viewportWidth, viewportHeight)
                updateCameraProjection()
                isDirty = true
            }
        }
        uiHelper?.attachTo(textureView)
    }

    private fun loadModelAsync() {
        Thread {
            try {
                // 1. Parse labels metadata from GLB header chunk 0
                context.assets.open(assetPath).use { stream ->
                    labelsMetadata = GlbMetadataParser.parseLabels(stream)
                }

                // 2. Read full GLB buffer
                val bytes = context.assets.open(assetPath).use { it.readBytes() }
                val buffer = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.LITTLE_ENDIAN)
                buffer.put(bytes)
                buffer.rewind()

                mainHandler.post {
                    if (isDestroyed) return@post

                    // 3. Load asset into Filament
                    val loader = FilamentEngineManager.createAssetLoader(context)
                    assetLoader = loader
                    val resLoader = FilamentEngineManager.createResourceLoader(context)
                    resourceLoader = resLoader

                    val loadedAsset = loader.createAsset(buffer)
                    if (loadedAsset != null) {
                        asset = loadedAsset
                        resLoader.loadResources(loadedAsset)
                        loadedAsset.releaseSourceData()

                        // Normalize scale to unit cube
                        normalizeModelScale(loadedAsset)

                        // Add to scene
                        scene.addEntities(loadedAsset.entities)

                        // Map labeled parts to Filament entities
                        mapLabeledEntities(loadedAsset)

                        updateCameraTransform()
                        isDirty = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }


    private fun normalizeModelScale(asset: FilamentAsset) {
        val tm = engine.transformManager
        val rootInstance = tm.getInstance(asset.root)
        if (rootInstance != 0) {
            val box = asset.boundingBox
            val center = box.center
            val halfExtent = box.halfExtent
            val maxExtent = maxOf(halfExtent[0], halfExtent[1], halfExtent[2]) * 2.0f
            val scale = if (maxExtent > 0f) 2.2f / maxExtent else 1.0f

            val transform = FloatArray(16)
            // Initialize with identity
            android.opengl.Matrix.setIdentityM(transform, 0)
            // Uniform scale
            android.opengl.Matrix.scaleM(transform, 0, scale, scale, scale)
            // Center model at origin
            android.opengl.Matrix.translateM(
                transform,
                0,
                -center[0],
                -center[1],
                -center[2]
            )
            tm.setTransform(rootInstance, transform)
        }
    }


    private fun mapLabeledEntities(asset: FilamentAsset) {
        val matched = mutableListOf<Pair<PartLabel, Int>>()
        for (label in labelsMetadata) {
            val entity = asset.getFirstEntityByName(label.nodeName)
            if (entity != 0) {
                matched.add(label to entity)
            } else {
                // Fallback: search all entities if name prefix matches
                val all = asset.entities
                val candidate = all.firstOrNull { asset.getName(it) == label.nodeName }
                if (candidate != null) {
                    matched.add(label to candidate)
                }
            }
        }
        labeledEntities = matched
    }

    private fun updateCameraProjection() {
        val aspect = viewportWidth.toDouble() / viewportHeight.toDouble()
        // 45 degree FOV vertical perspective projection
        camera.setProjection(45.0, aspect, 0.1, 50.0, Camera.Fov.VERTICAL)
    }

    private fun updateCameraTransform() {
        val radPitch = Math.toRadians(pitch.toDouble())
        val radYaw = Math.toRadians(yaw.toDouble())

        val eyeX = cameraDistance * cos(radPitch) * sin(radYaw)
        val eyeY = cameraDistance * sin(radPitch)
        val eyeZ = cameraDistance * cos(radPitch) * cos(radYaw)

        camera.lookAt(
            eyeX, eyeY, eyeZ,  // Eye position
            0.0, 0.0, 0.0,     // Target (center of unit cube)
            0.0, 1.0, 0.0      // Up vector
        )
        isDirty = true
    }

    // ==========================================
    // Public Interaction & Gesture APIs
    // ==========================================

    fun rotate(deltaX: Float, deltaY: Float) {
        yaw += deltaX * 0.4f
        pitch = (pitch - deltaY * 0.4f).coerceIn(-85f, 85f)
        updateCameraTransform()
    }

    fun zoom(scaleFactor: Float) {
        cameraDistance = (cameraDistance / scaleFactor).coerceIn(1.2f, 9.0f)
        updateCameraTransform()
    }

    fun setLabelsEnabled(enabled: Boolean) {
        labelsEnabled = enabled
        isDirty = true
        if (!enabled) {
            onLabelsProjected(emptyList())
        }
    }

    // ==========================================
    // Choreographer Render Loop & Projection
    // ==========================================

    override fun doFrame(frameTimeNanos: Long) {
        if (isDestroyed) return

        val currentSwapChain = swapChain
        if (currentSwapChain != null) {
            // Render 3D Frame
            if (renderer.beginFrame(currentSwapChain, frameTimeNanos)) {
                renderer.render(view)
                renderer.endFrame()
            }

            // Project 2D Part Labels if enabled
            if (labelsEnabled && labeledEntities.isNotEmpty()) {
                projectLabels()
            }
        }

        Choreographer.getInstance().postFrameCallback(this)
    }

    private fun projectLabels() {
        camera.getViewMatrix(viewMatrixDouble)
        camera.getProjectionMatrix(projMatrixDouble)

        val tm = engine.transformManager
        val projected = mutableListOf<ProjectedLabel>()

        for ((label, entity) in labeledEntities) {
            val instance = tm.getInstance(entity)
            if (instance != 0) {
                tm.getWorldTransform(instance, entityWorldMatrix)
                // In column-major 4x4 matrix, translation is at index 12, 13, 14
                val worldX = entityWorldMatrix[12]
                val worldY = entityWorldMatrix[13]
                val worldZ = entityWorldMatrix[14]

                val result = LabelProjector.projectPoint(
                    labelText = label.labelText,
                    worldX = worldX,
                    worldY = worldY,
                    worldZ = worldZ,
                    viewMatrix = viewMatrixDouble,
                    projectionMatrix = projMatrixDouble,
                    viewportWidth = viewportWidth.toFloat(),
                    viewportHeight = viewportHeight.toFloat()
                )
                projected.add(result)
            }
        }

        onLabelsProjected(projected)
    }

    // ==========================================
    // Native Resource Disposal
    // ==========================================

    fun destroy() {
        if (isDestroyed) return
        isDestroyed = true

        Choreographer.getInstance().removeFrameCallback(this)
        onLabelsProjected(emptyList())

        // Destroy 3D model asset
        asset?.let {
            scene.removeEntities(it.entities)
            assetLoader?.destroyAsset(it)
        }
        asset = null

        resourceLoader?.destroy()
        resourceLoader = null

        assetLoader?.destroy()
        assetLoader = null

        // Destroy light
        if (lightEntity != 0) {
            scene.removeEntity(lightEntity)
            engine.destroyEntity(lightEntity)
            EntityManager.get().destroy(lightEntity)
            lightEntity = 0
        }

        // Destroy swapchain & helper
        swapChain?.let { engine.destroySwapChain(it) }
        swapChain = null

        uiHelper?.detach()
        uiHelper = null

        // Destroy camera, view, scene, renderer
        engine.destroyCameraComponent(cameraEntity)
        EntityManager.get().destroy(cameraEntity)

        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyRenderer(renderer)

        FilamentEngineManager.unregisterSession()
    }
}
