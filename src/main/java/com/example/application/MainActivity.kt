package com.example.application

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.*
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.application.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameManager: GameManager
    private val handler = Handler(Looper.getMainLooper())

    // סאונד - הגדרות משופרות
    private lateinit var soundPool: SoundPool
    private var crashSoundId: Int = 0
    private var isSoundLoaded: Boolean = false

    private var gameDelay: Long = 500
    private val SLOW_SPEED: Long = 800
    private val FAST_SPEED: Long = 300
    private var playerName: String = "אנונימי"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLatitude: Double = 0.0
    private var lastLongitude: Double = 0.0

    private lateinit var sensorManager: SensorManager
    private var accSensor: Sensor? = null
    private var isSensorMode: Boolean = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            getLocation()
        }
    }

    private val gameRunnable = object : Runnable {
        override fun run() {
            if (gameManager.isGameOver) return
            val finished = gameManager.moveArmyStep { }
            binding.mainLBLDistance.text = "${gameManager.getDistance()} m"
            if (finished) { gameManager.startNewArmy() }
            handler.postDelayed(this, gameDelay)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // אתחול SoundPool עם הגדרות עוצמה ומהירות
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // מאפשר השמעה של כמה צלילים במקביל
            .setAudioAttributes(audioAttributes)
            .build()

        // טעינת הסאונד ומעקב אחרי סטטוס טעינה
        crashSoundId = soundPool.load(this, R.raw.crash_sound, 1)
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) isSoundLoaded = true
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermissions()

        isSensorMode = intent.getBooleanExtra("SENSOR_MODE", false)
        val speedType = intent.getStringExtra("SPEED_TYPE")
        gameDelay = if (speedType == "FAST") FAST_SPEED else SLOW_SPEED

        initGame()
        showNameDialog()
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            getLocation()
        }
    }

    private fun getLocation() {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        lastLatitude = location.latitude
                        lastLongitude = location.longitude
                    }
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun showNameDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("ברוך הבא!")
        builder.setMessage("הכנס את שמך כדי להישמר בטבלת השיאים:")
        val input = EditText(this)
        builder.setView(input)
        builder.setPositiveButton("התחל משחק") { _, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) playerName = name
            startGame()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun startGame() {
        if (isSensorMode) {
            initSensors()
            binding.btnArrowLeft.visibility = View.GONE
            binding.btnArrowRight.visibility = View.GONE
        } else {
            initButtons()
        }
        gameManager.resetGame()
        gameManager.startNewArmy()
        handler.postDelayed(gameRunnable, gameDelay)
    }

    private fun initGame() {
        val armyMatrix = listOf(
            listOf(binding.army00, binding.army10, binding.army20, binding.army30),
            listOf(binding.army01, binding.army11, binding.army21, binding.army31),
            listOf(binding.army02, binding.army12, binding.army22, binding.army32),
            listOf(binding.army03, binding.army13, binding.army23, binding.army33),
            listOf(binding.army04, binding.army14, binding.army24, binding.army34)
        )
        val playerLanes = listOf(binding.player40, binding.player41, binding.player42, binding.player43, binding.player44)
        val lifeImages = listOf(binding.heart1, binding.heart2, binding.heart3)

        gameManager = GameManager(
            armyViewsMatrix = armyMatrix,
            playerViewsLanes = playerLanes,
            lifeImages = lifeImages,
            onGameStart = { },
            onCollision = {
                vibrateDevice()
                playCrashSound() // הפעלת הסאונד בכל פגיעה
                gameManager.decreaseLife()
            },
            onGameOver = { handleGameOver() }
        )
    }

    private fun handleGameOver() {
        handler.removeCallbacks(gameRunnable)
        val finalScore = gameManager.getDistance()
        getLocation() // רענון מיקום אחרון

        val builder = AlertDialog.Builder(this)
        builder.setTitle("המשחק נגמר!")
        builder.setMessage("שחקן: $playerName\nהציון שצברת: ${finalScore}m")

        builder.setPositiveButton("לטבלת שיאים ומפה") { _, _ ->
            val intent = Intent(this, ScoreActivity::class.java).apply {
                putExtra("SCORE", finalScore)
                putExtra("NAME", playerName)
                putExtra("LATITUDE", lastLatitude)
                putExtra("LONGITUDE", lastLongitude)
            }
            startActivity(intent)
            finish()
        }

        builder.setNeutralButton("חזרה לתפריט") { _, _ -> finish() }

        builder.setNegativeButton("נסה שוב") { _, _ ->
            gameManager.resetGame()
            gameManager.startNewArmy()
            handler.postDelayed(gameRunnable, gameDelay)
        }

        builder.setCancelable(false)
        builder.show()
    }

    private fun initButtons() {
        binding.btnArrowLeft.setOnClickListener { gameManager.moveLeft() }
        binding.btnArrowRight.setOnClickListener { gameManager.moveRight() }
    }

    private fun initSensors() {
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accSensor != null) {
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun playCrashSound() {
        if (isSoundLoaded && crashSoundId != 0) {
            // השמעה בעוצמה מקסימלית (1.0f)
            soundPool.play(crashSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    private fun vibrateDevice() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isSensorMode) return
        if (event.values[0] < -3.0) gameManager.moveRight()
        else if (event.values[0] > 3.0) gameManager.moveLeft()

        if (event.values[1] > 3.0) gameDelay = SLOW_SPEED
        else if (event.values[1] < -3.0) gameDelay = FAST_SPEED
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        if (isSensorMode && accSensor != null) {
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}