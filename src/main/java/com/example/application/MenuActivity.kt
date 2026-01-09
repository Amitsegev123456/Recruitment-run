package com.example.application

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)

        // הגדרת שוליים למסך
        val mainView = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- חיבור הכפתורים מה-XML החדש ---
        val btnSlow = findViewById<Button>(R.id.menu_BTN_buttons_slow)
        val btnFast = findViewById<Button>(R.id.menu_BTN_buttons_fast)
        val btnSensors = findViewById<Button>(R.id.menu_BTN_sensors)
        val btnTop10 = findViewById<Button>(R.id.menu_BTN_top10)

        // לחיצה על כפתור "Buttons - Slow"
        btnSlow.setOnClickListener {
            startGame(isSensorMode = false, speedType = "SLOW")
        }

        // לחיצה על כפתור "Buttons - Fast"
        btnFast.setOnClickListener {
            startGame(isSensorMode = false, speedType = "FAST")
        }

        // לחיצה על כפתור "Sensors"
        btnSensors.setOnClickListener {
            // במצב חיישנים נתחיל במהירות רגילה, והבונוס של ההטיה ישנה אותה
            startGame(isSensorMode = true, speedType = "SLOW")
        }

        // לחיצה על כפתור "Top 10 Scores"
        btnTop10.setOnClickListener {
            openScoreScreen()
        }
    }

    /**
     * פונקציה שעוברת למסך המשחק ושולחת את סוג השליטה והמהירות
     */
    private fun startGame(isSensorMode: Boolean, speedType: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("SENSOR_MODE", isSensorMode)
        intent.putExtra("SPEED_TYPE", speedType) // שליחת המהירות ל-MainActivity
        startActivity(intent)
        finish()
    }

    private fun openScoreScreen() {
        val intent = Intent(this, ScoreActivity::class.java)
        startActivity(intent)
    }
}