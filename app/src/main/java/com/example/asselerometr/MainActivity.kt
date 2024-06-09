package com.example.asselerometr

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.asselerometr.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var textView: TextView
    private lateinit var chart: LineChart

    private val xValues = mutableListOf<Entry>()
    private val yValues = mutableListOf<Entry>()
    private val zValues = mutableListOf<Entry>()
    private var timestamp = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        textView = findViewById(R.id.textView)
        chart = findViewById(R.id.chart)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setupChart()
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            textView.text = "x: %.2f\n y: %.2f\n z: %.2f".format(x, y, z)

            // Add the values to the dataset and update the chart
            xValues.add(Entry(timestamp, x))
            yValues.add(Entry(timestamp, y))
            zValues.add(Entry(timestamp, z))
            timestamp += 0.1f

            updateChart()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(true)
    }

    private fun updateChart() {
        val xDataSet = LineDataSet(xValues, "X Axis")
        xDataSet.color = resources.getColor(R.color.red)

        val yDataSet = LineDataSet(yValues, "Y Axis")
        yDataSet.color = resources.getColor(R.color.green)

        val zDataSet = LineDataSet(zValues, "Z Axis")
        zDataSet.color = resources.getColor(R.color.blue)

        val data = LineData(xDataSet, yDataSet, zDataSet)
        chart.data = data
        chart.invalidate()
    }
}