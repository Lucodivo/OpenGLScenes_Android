package com.inasweaterpoorlyknit.learnopengl_androidport.scenes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView
import android.widget.Toast
import android.app.ActivityManager
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.view.MotionEvent
import glm_.vec2.Vec2
import android.hardware.*
import android.hardware.SensorManager.SENSOR_DELAY_GAME
import com.inasweaterpoorlyknit.learnopengl_androidport.utils.MAT_4x4_SIZE
import com.inasweaterpoorlyknit.learnopengl_androidport.utils.systemTimeInSeconds
import glm_.mat4x4.Mat4


@SuppressLint("ViewConstructor")
class InfiniteCubeScene(context: Activity) : GLSurfaceView(context), SensorEventListener {

    private val renderer: InfiniteCubeRenderer = InfiniteCubeRenderer(context)
    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val rotationSensorMatrix: FloatArray = FloatArray(MAT_4x4_SIZE)
    private var orientation: Int = context.resources.configuration.orientation

    private val touchScaleFactor: Float = 180.0f / 320f
    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var actionDownTime: Float = 0f

    init {
        val activity: Activity = context
        val activityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        val configurationInfo = activityManager!!.deviceConfigurationInfo
        val openGLESVersion = java.lang.Double.parseDouble(configurationInfo.glEsVersion)

        if (openGLESVersion >= 3.0) {
            // We have at least ES 3.0
            setEGLContextClientVersion(3)
        } else {
            val openGLVersionToast: Toast = Toast.makeText(context, "Device only supports OpenGL $openGLESVersion. \n Scene requires at least OpenGL 3.0", Toast.LENGTH_LONG)
            openGLVersionToast.show()
            activity.finish()
        }

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        // set rotation matrix to identity matrix
        rotationSensorMatrix[0] = 1.0f
        rotationSensorMatrix[4] = 1.0f
        rotationSensorMatrix[8] = 1.0f
        rotationSensorMatrix[12] = 1.0f
    }

    private fun onClick() {
        renderer.action()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val x: Float = event.x
        val y: Float = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                previousX = x
                previousY = y
                actionDownTime = systemTimeInSeconds()
                return true
            }
            MotionEvent.ACTION_MOVE -> {

                var dx: Float = x - previousX
                var dy: Float = y - previousY

                renderer.pan(Vec2(dx, dy) * touchScaleFactor)

                previousX = x
                previousY = y
                return true
            }
            MotionEvent.ACTION_UP -> {
                if((systemTimeInSeconds() - actionDownTime) <= 0.3f) {
                    onClick()
                }
                return true
            }
            else -> {
                return super.onTouchEvent(event)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // enable our sensor when attached
        sensorManager.registerListener(this, sensor, SENSOR_DELAY_GAME)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        // Turn our sensor off on detached
        sensorManager.unregisterListener(this)
    }

    fun orientationChange(orientation: Int) {
        this.orientation = orientation
    }

    override fun onSensorChanged(event: SensorEvent) {
        when(event.sensor.type){
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationSensorMatrix, event.values)
                if(orientation == ORIENTATION_LANDSCAPE) {
                    SensorManager.remapCoordinateSystem(rotationSensorMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotationSensorMatrix)
                }
                renderer.processRotationSensor(Mat4(rotationSensorMatrix))
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
