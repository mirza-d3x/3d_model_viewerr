package com.infusory.modelviewer.engine

import android.content.Context
import com.google.android.filament.Engine
import com.google.android.filament.Filament
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.Gltfio
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.utils.Utils
import java.util.concurrent.atomic.AtomicInteger


object FilamentEngineManager {

    private var engine: Engine? = null
    private var materialProvider: UbershaderProvider? = null
    private var activeSessionsCount = AtomicInteger(0)

    @Synchronized
    fun getEngine(context: Context): Engine {
        if (engine == null) {
            // Initialize native libraries
            Gltfio.init()
            Utils.init()
            Filament.init()

            // Create shared engine
            engine = Engine.create()
            materialProvider = UbershaderProvider(engine!!)
        }
        return engine!!
    }

    @Synchronized
    fun getMaterialProvider(context: Context): UbershaderProvider {
        getEngine(context)
        return materialProvider!!
    }


    fun createAssetLoader(context: Context): AssetLoader {
        val eng = getEngine(context)
        val provider = getMaterialProvider(context)
        return AssetLoader(eng, provider, eng.entityManager)
    }


    fun createResourceLoader(context: Context): ResourceLoader {
        val eng = getEngine(context)
        return ResourceLoader(eng)
    }


    fun registerSession() {
        activeSessionsCount.incrementAndGet()
    }


    fun unregisterSession() {
        activeSessionsCount.decrementAndGet()
    }


    @Synchronized
    fun destroy() {
        materialProvider?.destroy()
        materialProvider = null

        engine?.destroy()
        engine = null
    }
}
