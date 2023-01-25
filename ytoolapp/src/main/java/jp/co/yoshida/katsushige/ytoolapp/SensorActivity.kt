package jp.co.yoshida.katsushige.ytoolapp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import jp.co.yoshida.katsushige.ytoolapp.databinding.ActivitySensorBinding

class SensorActivity : AppCompatActivity(),SensorEventListener {

    val TAG = "SensorActivity"

    lateinit var binding: ActivitySensorBinding
    lateinit var constraintLayout: ConstraintLayout
    lateinit var tvAccel: TextView
    lateinit var tvGravity: TextView
    lateinit var tvLinearAccel: TextView
    lateinit var tvGyroscope: TextView
    lateinit var tvMagnetic: TextView
    lateinit var tvLight: TextView
    lateinit var tvTemperature: TextView
    lateinit var tvProximity: TextView
    lateinit var tvOrientation: TextView
    lateinit var tvPressure: TextView

    private lateinit var  mSensorManager: SensorManager
    private lateinit var  mAccelerometer: Sensor
    private lateinit var  mGravity: Sensor
    private lateinit var  mLinearAccelera: Sensor
    private lateinit var  mGyroscope: Sensor
    private lateinit var  mMagnetic: Sensor
    private lateinit var  mLight: Sensor
    private lateinit var  mTemperature: Sensor
    private lateinit var  mProximity: Sensor
    private lateinit var  mOrientation: Sensor
    private lateinit var  mPressure: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)
        Log.d(TAG,"onCreate")
        this.title = "センサー"

        binding = ActivitySensorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        constraintLayout = binding.constrainLayout
        tvAccel = binding.textView24
        tvGravity = binding.textView26
        tvLinearAccel = binding.textView28
        tvGyroscope = binding.textView30
        tvMagnetic = binding.textView32
        tvLight = binding.textView34
        tvTemperature = binding.textView36
        tvProximity = binding.textView38
        tvOrientation = binding.textView40
        tvPressure = binding.textView42

        mSensorManager  = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mGravity        = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        mLinearAccelera = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        mGyroscope      = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        mMagnetic       = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        mLight          = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
//        mTemperature    = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE)
        mProximity      = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        mOrientation    = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
//        mPressure       = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    }

    override fun onResume() {
        super.onResume()

        //  センサー値を取得するタイミングを指定
        val supportAccelmeter     = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        val supportGravity        = mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL)
        val supportLinearAccelera = mSensorManager.registerListener(this, mLinearAccelera, SensorManager.SENSOR_DELAY_NORMAL)
        val supportGyroscope      = mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        val supportMagnetic       = mSensorManager.registerListener(this, mMagnetic, SensorManager.SENSOR_DELAY_NORMAL)
        val supportLight          = mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL)
//        val supportTemperature    = mSensorManager.registerListener(this, mTemperature, SensorManager.SENSOR_DELAY_NORMAL)
        val supportProximity      = mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)
        val supportOrientation    = mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL)
//        val supportPressure       = mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent) {
//        TODO("Not yet implemented")
        if (event == null)
            return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            tvAccel.text = "X軸: %+.4f Y軸: %+.4f Z軸: %+.4f".format(event.values[0],event.values[1],event.values[2])
        } else if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            tvGravity.text = "X軸: %+.4f Y軸: %+.4f Z軸: %+.4f".format(event.values[0],event.values[1],event.values[2])
        } else if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            tvLinearAccel.text = "X軸: %+.4f Y軸: %+.4f Z軸: %+.4f".format(event.values[0],event.values[1],event.values[2])
        } else if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            tvGyroscope.text = "X軸: %+.4f Y軸: %+.4f Z軸: %+.4f".format(event.values[0],event.values[1],event.values[2])
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            tvMagnetic.text = "X軸: %+.4f Y軸: %+.4f Z軸: %+.4f".format(event.values[0],event.values[1],event.values[2])
        } else if (event.sensor.type == Sensor.TYPE_LIGHT) {
            tvLight.text = " %+.4f".format(event.values[0])
//        } else if (event.sensor.type == Sensor.TYPE_TEMPERATURE) {
//            tvTemperature.text = " %+.4f".format(event.values[0])
        } else if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            tvProximity.text = " %+.4f".format(event.values[0])
        } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
            tvOrientation.text = " %+.4f".format(event.values[0])
//        } else if (event.sensor.type == Sensor.TYPE_PRESSURE) {
//            tvPressure.text = " %+.4f".format(event.values[0])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        TODO("Not yet implemented")
    }


}