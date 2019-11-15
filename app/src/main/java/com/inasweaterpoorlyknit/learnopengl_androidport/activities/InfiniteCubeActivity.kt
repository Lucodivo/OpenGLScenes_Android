package com.inasweaterpoorlyknit.learnopengl_androidport.activities

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.inasweaterpoorlyknit.learnopengl_androidport.scenes.InfiniteCubeScene
import com.inasweaterpoorlyknit.learnopengl_androidport.scenes.SceneSurfaceView

class InfiniteCubeActivity : AppCompatActivity() {

    private lateinit var scene: SceneSurfaceView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        scene = SceneSurfaceView(this, InfiniteCubeScene(this))
        setContentView(scene)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        newConfig?.orientation?.let {
            scene.orientationChange(it)
        }
    }
}
