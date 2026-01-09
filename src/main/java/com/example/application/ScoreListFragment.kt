package com.example.application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScoreListFragment : Fragment() {

    private lateinit var scoreRecyclerView: RecyclerView
    private var callBackListClick: CallBack_ListClick? = null

    // פונקציה לחיבור ה-Callback מה-Activity
    fun setCallBackListClick(callBack: CallBack_ListClick) {
        this.callBackListClick = callBack
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_score_list, container, false)

        scoreRecyclerView = view.findViewById(R.id.score_LST_scores)

        // טעינת הנתונים מהשמירה
        val spManager = SharedPreferencesManager(requireContext())
        val scores = spManager.getScores()

        // מיון הרשימה מהגבוה לנמוך (למקרה שלא נשמר ממוין)
        scores.sortByDescending { it.score }

        // חיבור ה-Adapter לרשימה
        val adapter = ScoreAdapter(scores, object : CallBack_ListClick {
            override fun onRecordClicked(lat: Double, lng: Double) {
                // העברת הלחיצה הלאה ל-Activity
                callBackListClick?.onRecordClicked(lat, lng)
            }
        })

        scoreRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        scoreRecyclerView.adapter = adapter

        return view
    }
}