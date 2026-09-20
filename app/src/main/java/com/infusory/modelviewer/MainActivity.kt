package com.infusory.modelviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.infusory.modelviewer.engine.FilamentEngineManager
import com.infusory.modelviewer.ui.MainCanvasScreen
import com.infusory.modelviewer.ui.theme.ModelViewerTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ModelViewerTheme {
                MainCanvasScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure shared 3D engine releases native pipeline resources
        if (isFinishing) {
            FilamentEngineManager.destroy()
        }
    }
}
