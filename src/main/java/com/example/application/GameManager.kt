package com.example.application

import android.view.View
import android.widget.ImageView
import kotlin.random.Random

private const val ARMY_ROWS = 4
private const val ARMY_COLS = 5

data class ArmyObject(var row: Int, val lane: Int, val isCoin: Boolean)

class GameManager(
    private val armyViewsMatrix: List<List<ImageView>>,
    private val playerViewsLanes: List<ImageView>,
    private val lifeImages: List<ImageView>,
    private val onGameStart: () -> Unit,
    private val onCollision: () -> Unit,
    private val onGameOver: () -> Unit
) {
    private var lives = 3
    private var playerLanePosition = 2 // שמתי 2 כדי שיתחיל באמצע (מתוך 5 טורים)
    private var distance = 0
    private var activeArmyObject: ArmyObject? = null
    private var firstStart = true
    var isGameOver = false
        private set

    fun resetGame() {
        if (lives <= 0 || firstStart) {
            onGameStart()
            firstStart = false
        }
        lives = 3
        distance = 0
        isGameOver = false
        playerLanePosition = 2
        activeArmyObject = null
        resetVisuals()
        showPlayerAt(playerLanePosition)
    }

    private fun resetVisuals() {
        armyViewsMatrix.flatten().forEach { it.visibility = View.INVISIBLE }
        updateLifeUI()
    }

    private fun showPlayerAt(pos: Int) {
        if (isGameOver) return
        playerViewsLanes.forEachIndexed { index, img ->
            img.visibility = if (index == pos) View.VISIBLE else View.INVISIBLE
        }
    }

    fun moveLeft() {
        if (isGameOver) return
        if (playerLanePosition > 0) {
            playerLanePosition--
            showPlayerAt(playerLanePosition)
        }
    }

    fun moveRight() {
        if (isGameOver) return
        if (playerLanePosition < ARMY_COLS - 1) {
            playerLanePosition++
            showPlayerAt(playerLanePosition)
        }
    }

    fun startNewArmy() {
        if (isGameOver || activeArmyObject != null) return

        // הגרלת טור אקראי מתוך 5 (0 עד 4)
        val newLane = Random.nextInt(ARMY_COLS)
        val isCoin = Random.nextInt(5) == 0

        activeArmyObject = ArmyObject(0, newLane, isCoin)

        // תיקון גישה למטריצה: ודאי שהמערך בנוי לפי [lane][row]
        // אם זה קורס כאן, סימן שב-XML יש לך פחות מ-5 טורים או 4 שורות
        try {
            val view = armyViewsMatrix[newLane][0]
            if (isCoin) {
                view.setImageResource(R.drawable.img_coin)
            } else {
                view.setImageResource(R.drawable.img_army)
            }
            view.visibility = View.VISIBLE
        } catch (e: Exception) {
            activeArmyObject = null // ביטול אם יש שגיאת אינדקס
        }
    }

    fun moveArmyStep(onCollisionLogic: () -> Unit): Boolean {
        if (isGameOver) return false

        distance++
        val army = activeArmyObject ?: return false

        // העלמת האובייקט מהמיקום הנוכחי
        armyViewsMatrix[army.lane][army.row].visibility = View.INVISIBLE

        // בדיקה אם האובייקט הגיע לשורה האחרונה (איפה שהשחקן נמצא)
        if (army.row == ARMY_ROWS - 1) {
            if (army.lane == playerLanePosition) {
                if (army.isCoin) {
                    distance += 10
                } else {
                    onCollision()
                    onCollisionLogic()
                }
            }
            activeArmyObject = null // האובייקט סיים את תפקידו
            return true
        } else {
            // הזזה למטה
            army.row++
            if (army.row < ARMY_ROWS) {
                val view = armyViewsMatrix[army.lane][army.row]
                if (army.isCoin) {
                    view.setImageResource(R.drawable.img_coin)
                } else {
                    view.setImageResource(R.drawable.img_army)
                }
                view.visibility = View.VISIBLE
            }
            return false
        }
    }

    fun decreaseLife(): Int {
        if (lives > 0) {
            lives--
            updateLifeUI()
        }
        if (lives == 0) {
            isGameOver = true
            onGameOver()
        }
        return lives
    }

    fun getDistance(): Int = distance

    private fun updateLifeUI() {
        lifeImages.forEachIndexed { index, img ->
            img.visibility = if (index < lives) View.VISIBLE else View.INVISIBLE
        }
    }
}