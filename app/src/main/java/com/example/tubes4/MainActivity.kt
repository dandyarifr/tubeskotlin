package com.example.tubes4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import io.github.controlwear.virtual.joystick.android.JoystickView
import kotlin.math.cos
import kotlin.math.sin
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    private lateinit var socketClient: SocketClient
    private var currentZ: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the app has the INTERNET permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            // Request the INTERNET permission at runtime
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 0)
        }

        socketClient = SocketClient("192.168.4.1", 80)

        socketClient.connect(
            onConnected = {
                runOnUiThread {
                    Toast.makeText(this, "Connected to ESP32", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { exception ->
                runOnUiThread {
                    Toast.makeText(this, "Connection failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        val joystick = findViewById<JoystickView>(R.id.joystick)
        joystick.setOnMoveListener { angle, strength ->
            // Calculate x and y values based on joystick input
            val angleRadians = Math.toRadians(angle.toDouble())
            val x = (strength * cos(angleRadians) / 100).toFloat()
            val y = (strength * sin(angleRadians) / 100).toFloat()

            socketClient.sendXYZ(x.toInt(), y.toInt(), currentZ.toInt())
        }

        val slider = findViewById<SeekBar>(R.id.slider)
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentZ = progress.toFloat()
                // Assuming x and y values are at their initial/default positions
                socketClient.sendXYZ(0, 0, currentZ.toInt())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        socketClient.disconnect()
    }
}
