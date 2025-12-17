package com.example.application

import android.view.View
import android.widget.ImageView
import kotlin.random.Random

private const val ARMY_ROWS = 4
private const val ARMY_COLS = 3

data class ArmyObject(var row: Int, val lane: Int)

class GameManager(
    private val armyViewsMatrix: List<List<ImageView>>,
    private val playerViewsLanes: List<ImageView>,
    private val lifeImages: List<ImageView>,
    private val onGameStart: () -> Unit,
    private val onCollision: () -> Unit,
    private val onGameOver: () -> Unit
) {
    private var lives = 3
    private var playerLanePosition = 1
    private var activeArmyObject: ArmyObject? = null
    private var firstStart = true
    var isGameOver = false // משתנה חדש לניהול מצב המשחק
        private set

    fun resetGame() {
        if (lives <= 0 || firstStart) {
            onGameStart()
            firstStart = false
        }
        lives = 3
        isGameOver = false // איפוס הנעילה
        playerLanePosition = 1
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
        if (isGameOver) return // נעילה
        if (playerLanePosition > 0) {
            playerLanePosition--
            showPlayerAt(playerLanePosition)
        }
    }

    fun moveRight() {
        if (isGameOver) return // נעילה
        if (playerLanePosition < ARMY_COLS - 1) {
            playerLanePosition++
            showPlayerAt(playerLanePosition)
        }
    }

    fun startNewArmy() {
        if (isGameOver || activeArmyObject != null) return
        val newLane = Random.nextInt(ARMY_COLS)
        activeArmyObject = ArmyObject(0, newLane)
        armyViewsMatrix[newLane][0].visibility = View.VISIBLE
    }

    fun moveArmyStep(onCollisionLogic: () -> Unit): Boolean {
        if (isGameOver) return false

        val army = activeArmyObject ?: return false
        armyViewsMatrix[army.lane][army.row].visibility = View.INVISIBLE

        if (army.row == ARMY_ROWS - 1) {
            if (army.lane == playerLanePosition) {
                onCollision()
                onCollisionLogic()
            }
            activeArmyObject = null
            return true
        } else {
            army.row++
            armyViewsMatrix[army.lane][army.row].visibility = View.VISIBLE
            return false
        }
    }

    fun decreaseLife(): Int {
        if (lives > 0) {
            lives--
            updateLifeUI()
        }
        if (lives == 0) {
            isGameOver = true // נעילת המשחק
            onGameOver()
        }
        return lives
    }

    private fun updateLifeUI() {
        lifeImages.forEachIndexed { index, img ->
            img.visibility = if (index < lives) View.VISIBLE else View.INVISIBLE
        }
    }
}