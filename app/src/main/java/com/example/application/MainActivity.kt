package com.example.application

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.application.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameManager: GameManager

    private val handler = Handler(Looper.getMainLooper())
    private val gameDelay: Long = 550

    private val gameRunnable = object : Runnable {
        override fun run() {
            // אם המשחק נעול, מפסיקים את הלופ
            if (gameManager.isGameOver) return

            val finished = gameManager.moveArmyStep {
                val livesLeft = gameManager.decreaseLife()
                // הערה: אם תרצה שהמשחק יתחיל מחדש אוטומטית, קרא ל-gameManager.resetGame() כאן
            }

            if (finished) {
                gameManager.startNewArmy()
            }
            handler.postDelayed(this, gameDelay)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initGame()
        initButtons()

        gameManager.resetGame()
        gameManager.startNewArmy()
        handler.postDelayed(gameRunnable, gameDelay)
    }

    private fun initGame() {
        // מיפוי עמודות: כל רשימה פנימית מייצגת Lane (עמודה)
        val armyMatrix = listOf(
            listOf(binding.army00, binding.army10, binding.army20, binding.army30),
            listOf(binding.army01, binding.army11, binding.army21, binding.army31),
            listOf(binding.army02, binding.army12, binding.army22, binding.army32)
        )

        val playerLanes = listOf(binding.player40, binding.player41, binding.player42)
        val lifeImages = listOf(binding.heart1, binding.heart2, binding.heart3)

        gameManager = GameManager(
            armyViewsMatrix = armyMatrix,
            playerViewsLanes = playerLanes,
            lifeImages = lifeImages,
            onGameStart = { Toast.makeText(this, "המשחק התחיל! בהצלחה", Toast.LENGTH_SHORT).show() },
            onCollision = { Toast.makeText(this, "בום! נפגעת!", Toast.LENGTH_SHORT).show() },
            onGameOver = { Toast.makeText(this, "המשחק נגמר!", Toast.LENGTH_LONG).show() }
        )
    }

    private fun initButtons() {
        binding.btnArrowLeft.setOnClickListener { gameManager.moveLeft() }
        binding.btnArrowRight.setOnClickListener { gameManager.moveRight() }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameRunnable)
    }
}