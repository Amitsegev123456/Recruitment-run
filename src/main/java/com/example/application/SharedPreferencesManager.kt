package com.example.application

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager(context: Context) {
    private val sharedPref = context.getSharedPreferences("SCORES_DB", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveScores(scores: List<ScoreItem>) {
        val json = gson.toJson(scores)
        sharedPref.edit().putString("high_scores", json).apply()
    }

    fun getScores(): MutableList<ScoreItem> {
        val json = sharedPref.getString("high_scores", null)
        if (json == null) return mutableListOf()
        val type = object : TypeToken<MutableList<ScoreItem>>() {}.type
        return gson.fromJson(json, type)
    }
}