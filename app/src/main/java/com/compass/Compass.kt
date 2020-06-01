package com.compass

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class Compass : View, SensorEventListener {

    private val centerPoint: PointF = PointF()
    private val paint: Paint = Paint()

    private val sensorManager: SensorManager
    private val mSensor: Sensor
    private val gSensor: Sensor

    private var mGeomagnetic = FloatArray(3)
    private var mGravity = FloatArray(3)
    private val rotMatrix = FloatArray(9) // rotation matrix
    private val iMatrix = FloatArray(9) // inclination matrix
     var azimuth = 0f

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values
        }

        if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values
        }

        val success = SensorManager.getRotationMatrix(rotMatrix, iMatrix, mGravity, mGeomagnetic)
        if (success) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotMatrix, orientation)
//        Azimuth - angle of rotation about the -z axis.
//        This value represents the angle between the device's y axis and the magnetic north pole.
            azimuth = orientation[0]
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // set the center point to the bottom of the screen
        centerPoint.set(width.toFloat() / 2, height.toFloat() / 2)

        // apply rotation to North
        canvas?.rotate(-azimuth, centerPoint.x, centerPoint.y)

        paint.color = ContextCompat.getColor(context, R.color.colorAccent)
        canvas?.drawCircle(centerPoint.x, centerPoint.y, getRadiusForRing(0), paint)
        paint.color = ContextCompat.getColor(context, R.color.subBorderCircleBlue)
        canvas?.drawCircle(centerPoint.x, centerPoint.y, getRadiusForRing(1), paint)

        paint.color = ContextCompat.getColor(context, R.color.colorPrimary)
        // draw the line pointing North
        val startX = centerPoint.x + sin(azimuth) * getRadiusForRing(2)
        val startY = centerPoint.y + cos(azimuth) * getRadiusForRing(2)
        val stopX = centerPoint.x + sin(azimuth) * getRadiusForRing(0)
        val stopY = centerPoint.y + cos(azimuth) * getRadiusForRing(0)
        paint.strokeWidth = 10f
        canvas?.drawLine(startX, startY, stopX, stopY, paint)
    }

    private fun getRadiusForRing(i: Int): Float {
        return (height - i * (height.toFloat() / 2)) / 4
    }
}