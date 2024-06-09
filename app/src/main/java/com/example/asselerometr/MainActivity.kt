package com.example.asselerometr

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.audiofx.BassBoost
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var textView: TextView
    private lateinit var chart: LineChart
    private lateinit var stopButton: Button
    //private lateinit var saveButton: Button
    private lateinit var startButton: Button

    private val xValues = mutableListOf<Entry>()
    private val yValues = mutableListOf<Entry>()
    private val zValues = mutableListOf<Entry>()
    private var timestamp = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        textView = findViewById(R.id.textView)
        chart = findViewById(R.id.chart)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        //saveButton = findViewById(R.id.saveButton)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setupChart()



        stopButton.setOnClickListener {
            stopMeasurements()
        }

//        saveButton.setOnClickListener {
//            saveDataAndChart()
//        }

        startButton.setOnClickListener{
            resetChartAndData()
            startMeasurements()
        }
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

            textView.text = "x: %.2f y: %.2f z: %.2f".format(x, y, z)

            // Add the values to the dataset and update the chart
            xValues.add(Entry(timestamp, x))
            yValues.add(Entry(timestamp, y))
            zValues.add(Entry(timestamp, z))
            timestamp += 0.1f

            updateChart()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun setupChart() {
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

    private fun stopMeasurements() {
        sensorManager.unregisterListener(this)
    }

//    private fun saveDataAndChart() {
//        // Save the chart as an image
//        val bitmap = Bitmap.createBitmap(chart.width, chart.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        chart.draw(canvas)
//
//        val chartFile = File(getExternalFilesDir(null), "chart.png")
//        try {
//            val fileOutputStream = FileOutputStream(chartFile)
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
//            fileOutputStream.flush()
//            fileOutputStream.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        // Save the data as a CSV file
//        val csvData = StringBuilder()
//        csvData.append("Timestamp,X,Y,Z\n")
//        for (i in xValues.indices) {
//            csvData.append("${xValues[i].x},${xValues[i].y},${yValues[i].y},${zValues[i].y}\n")
//        }
//
//        val csvFile = File(getExternalFilesDir(null), "data.csv")
//        try {
//            val fileOutputStream = FileOutputStream(csvFile)
//            fileOutputStream.write(csvData.toString().toByteArray())
//            fileOutputStream.flush()
//            fileOutputStream.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }



    private fun resetChartAndData() {
        // Clear previous data and reset timestamp
        xValues.clear()
        yValues.clear()
        zValues.clear()
        timestamp = 0f

        // Clear the chart
        chart.clear()
        chart.invalidate()
    }

    private fun startMeasurements() {
        resetChartAndData()

        // Register the listener for the accelerometer
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
}