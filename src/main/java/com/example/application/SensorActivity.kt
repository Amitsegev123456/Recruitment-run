package com.example.application

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.*
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.application.databinding.ActivitySensorBinding

class SensorActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivitySensorBinding
    private lateinit var gameManager: GameManager
    private var mp: MediaPlayer? = null

    private val handler = Handler(Looper.getMainLooper())
    private val gameDelay: Long = 550

    private lateinit var sensorManager: SensorManager
    private var accSensor: Sensor? = null

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (gameManager.isGameOver) return

            val finished = gameManager.moveArmyStep {
                gameManager.decreaseLife()
            }

            // עדכון המרחק בזמן אמת במסך הסנסורים
            binding.mainLBLDistance.text = "${gameManager.getDistance()} m"

            if (finished) {
                gameManager.startNewArmy()
            }
            handler.postDelayed(this, gameDelay)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySensorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnClose.setOnClickListener {
            finish()
        }

        initGame()
        initSensors()

        gameManager.resetGame()
        gameManager.startNewArmy()
        handler.postDelayed(gameRunnable, gameDelay)
    }

    private fun initGame() {
        // סידור המטריצה: 5 טורים (עמודות), בכל אחד 4 שורות
        val armyMatrix = listOf(
            listOf(binding.army00, binding.army01, binding.army02, binding.army03),
            listOf(binding.army10, binding.army11, binding.army12, binding.army13),
            listOf(binding.army20, binding.army21, binding.army22, binding.army23),
            listOf(binding.army30, binding.army31, binding.army32, binding.army33),
            //listOf(binding.army40, binding.army41, binding.army42, binding.army43)
        )

        val playerLanes = listOf(binding.player40, binding.player41, binding.player42, binding.player43, binding.player44)
        val lifeImages = listOf(binding.heart1, binding.heart2, binding.heart3)

        gameManager = GameManager(
            armyViewsMatrix = armyMatrix,
            playerViewsLanes = playerLanes,
            lifeImages = lifeImages,
            onGameStart = { Toast.makeText(this, "מצב חיישנים הופעל!", Toast.LENGTH_SHORT).show() },
            onCollision = {
                vibrateDevice()
                playCrashSound()
                Toast.makeText(this,"בום! נפסלת!", Toast.LENGTH_SHORT).show()
            },
            onGameOver = { Toast.makeText(this, "הפסדת במצב חיישנים!", Toast.LENGTH_LONG).show() }
        )
    }

    private fun playCrashSound() {
        mp?.release()
        mp = MediaPlayer.create(this, R.raw.crash_sound)
        mp?.start()
    }

    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun vibrateDevice() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val x = event.values[0]

        // רגישות התנועה (3.0 במקום 2.0 כדי למנוע רעידות מיותרות)
        if (x < -3.0) {
            gameManager.moveRight()
        } else if (x > 3.0) {
            gameManager.moveLeft()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameRunnable)
        mp?.release()
    }
}