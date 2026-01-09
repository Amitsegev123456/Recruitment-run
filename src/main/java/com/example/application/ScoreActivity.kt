package com.example.application

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ScoreActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    // משתני מיקום שמתחילים כ"ריקים" כדי שלא יהיה מיקום ברירת מחדל שקרי
    private var lastLat: Double = 0.0
    private var lastLng: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // פתרון למסך השחור - רקע לבן מיידי
        window.setBackgroundDrawableResource(android.R.color.white)
        setContentView(R.layout.activity_score)

        val nameView = findViewById<TextView>(R.id.score_LBL_name)
        val highScoresView = findViewById<TextView>(R.id.score_LBL_high_scores)
        val btnBack = findViewById<Button>(R.id.score_BTN_back)

        // קבלת נתונים מה-Intent
        val name = intent.getStringExtra("NAME") ?: "אנונימי"
        val score = intent.getIntExtra("SCORE", 0)

        // כאן אנחנו מושכים את המיקום האמיתי שה-MainActivity שלח
        lastLat = intent.getDoubleExtra("LATITUDE", 0.0)
        lastLng = intent.getDoubleExtra("LONGITUDE", 0.0)

        nameView.text = "שחקן: $name | תוצאה: ${score}m"

        // טעינת השיאים (בודק אם הגענו ממשחק כדי לשמור)
        val isFromGame = intent.hasExtra("SCORE")
        loadTopTen(name, score, highScoresView, isFromGame)

        // טעינת המפה
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.score_MAP_location) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        btnBack.setOnClickListener {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // הגדרות תצוגה למפה
        mMap?.uiSettings?.isZoomControlsEnabled = true

        // הצגת המיקום רק אם הוא באמת התקבל מה-GPS (לא 0.0)
        if (lastLat != 0.0 && lastLng != 0.0) {
            val userLocation = LatLng(lastLat, lastLng)
            mMap?.clear()
            mMap?.addMarker(MarkerOptions()
                .position(userLocation)
                .title("כאן נקבע השיא שלך!"))

            // זום קרוב (16f) כדי לראות את הרחוב המדויק
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16f))
        }

        // הצגת הנקודה הכחולה של "איפה אני עכשיו" אם יש הרשאות
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap?.isMyLocationEnabled = true
        }
    }

    private fun loadTopTen(name: String, score: Int, display: TextView, shouldAdd: Boolean) {
        val sp = getSharedPreferences("MyGamePrefs", Context.MODE_PRIVATE)
        val rawData = sp.getString("HIGH_SCORES_LIST", "") ?: ""

        // שימוש במפריד נקודתיים (:) כדי לשמור על היסטוריית השיאים שלך
        val scoreList = if (rawData.isEmpty()) mutableListOf() else rawData.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) parts[0] to parts[1].toInt() else null
        }.toMutableList()

        if (shouldAdd && score > 0) {
            scoreList.add(name to score)
        }

        // מיון, הסרת כפילויות ושמירת הטופ 10
        val topScores = scoreList.sortedByDescending { it.second }.distinct().take(10)
        val savedString = topScores.joinToString(",") { "${it.first}:${it.second}" }
        sp.edit().putString("HIGH_SCORES_LIST", savedString).apply()

        // הצגת הרשימה למסך
        val displayText = StringBuilder("--- 10 השיאים הגדולים ---\n\n")
        topScores.forEachIndexed { index, pair ->
            displayText.append("${index + 1}. ${pair.first}: ${pair.second}m\n")
        }
        display.text = displayText.toString()
    }
}