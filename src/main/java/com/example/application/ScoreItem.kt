package com.example.application

data class ScoreItem(
    val score: Int = 0,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val date: Long = System.currentTimeMillis()
)