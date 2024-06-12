@file:Suppress("DEPRECATION")

package com.example.asselerometr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
//    private var gyroscope: Sensor? = null
    private lateinit var textView1: TextView
    private lateinit var textView2: TextView
    private lateinit var chart: LineChart
    private lateinit var stopButton: Button
    private lateinit var saveButton: Button
    private lateinit var startButton: Button

    private val xValues = mutableListOf<Entry>()
    private val yValues = mutableListOf<Entry>()
    private val zValues = mutableListOf<Entry>()

//    private val gyroXValues = mutableListOf<Entry>()
//    private val gyroYValues = mutableListOf<Entry>()
//    private val gyroZValues = mutableListOf<Entry>()

    private var timestamp = 0f

    companion object {
        private const val REQUEST_WRITE_STORAGE = 112
        private const val REQUEST_MANAGE_EXTERNAL_STORAGE = 113
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        textView1 = findViewById(R.id.textView1)
        textView2 = findViewById(R.id.textView2)
        chart = findViewById(R.id.chart)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        saveButton = findViewById(R.id.saveButton)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        setupChart()

        stopButton.setOnClickListener {
            stopMeasurements()
        }

        saveButton.setOnClickListener {
            checkManageExternalStoragePermission()
        }

        startButton.setOnClickListener {
            resetChartAndData()
            startMeasurements()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
//        gyroscope?.also { gyro ->
//            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL)
//        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun checkManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:" + applicationContext.packageName)
                startActivityForResult(intent, REQUEST_MANAGE_EXTERNAL_STORAGE)
            } else {
                saveDataAndChart()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                saveDataAndChart()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                saveDataAndChart()
            }
        }
    }

    private fun saveDataAndChart() {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val dataDir = File(downloadDir, "Данные")
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }

        val currentDateTime =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val bitmap = Bitmap.createBitmap(chart.width, chart.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        chart.draw(canvas)

        val chartFile = File(dataDir, "Диаграмма_$currentDateTime.png")
        var fileOutputStream: FileOutputStream? = null

        try {
            fileOutputStream = FileOutputStream(chartFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        fileOutputStream?.flush()
        fileOutputStream?.close()

    }


    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                textView1.text = "Acc x: %.2f y: %.2f z: %.2f".format(x, y, z)

                xValues.add(Entry(timestamp, x))
                yValues.add(Entry(timestamp, y))
                zValues.add(Entry(timestamp, z))
            }
//            Sensor.TYPE_GYROSCOPE -> {
//                val x = event.values[0]
//                val y = event.values[1]
//                val z = event.values[2]
//
//                textView2.text = "Gyro x: %.2f y: %.2f z: %.2f".format(x, y, z)
//
//                gyroXValues.add(Entry(timestamp, x))
//                gyroYValues.add(Entry(timestamp, y))
//                gyroZValues.add(Entry(timestamp, z))
//            }
        }

        timestamp += 0.1f

        updateChart()
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
        val xDataSet = LineDataSet(xValues, "Acc X").apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.red)
            setDrawCircles(false)  
        }

        val yDataSet = LineDataSet(yValues, "Acc Y").apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.green)
            setDrawCircles(false)  
        }

        val zDataSet = LineDataSet(zValues, "Acc Z").apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.blue)
            setDrawCircles(false)  
        }

//        val gyroXDataSet = LineDataSet(gyroXValues, "Gyro X").apply {
//            color = ContextCompat.getColor(this@MainActivity, R.color.yellow)
//            setDrawCircles(false)
//        }
//
//        val gyroYDataSet = LineDataSet(gyroYValues, "Gyro Y").apply {
//            color = ContextCompat.getColor(this@MainActivity, R.color.teal_200)
//            setDrawCircles(false)
//        }
//
//        val gyroZDataSet = LineDataSet(gyroZValues, "Gyro Z").apply {
//            color = ContextCompat.getColor(this@MainActivity, R.color.purple)
//            setDrawCircles(false)
//        }

        val lineData = LineData(xDataSet, yDataSet, zDataSet)//, gyroXDataSet, gyroYDataSet, gyroZDataSet)

        chart.data = lineData
        chart.invalidate()
    }


    private fun resetChartAndData() {
        xValues.clear()
        yValues.clear()
        zValues.clear()

//        gyroXValues.clear()
//        gyroYValues.clear()
//        gyroZValues.clear()

        timestamp = 0f

        chart.clear()
    }

    private fun stopMeasurements() {
        sensorManager.unregisterListener(this)
    }

    private fun startMeasurements() {
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
//        gyroscope?.also { gyro ->
//            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL)
//        }
    }
}