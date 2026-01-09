package com.example.application

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ScoreAdapter(
    private val scores: List<ScoreItem>,
    private val callBack: CallBack_ListClick
) : RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.score_item, parent, false)
        return ScoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) {
        val item = scores[position]
        holder.scoreLabel.text = "Score: ${item.score}"

        // עיצוב התאריך
        val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        holder.dateLabel.text = sdf.format(Date(item.date))

        // לחיצה על שורה מעבירה את הקואורדינטות למפה
        holder.itemView.setOnClickListener {
            callBack.onRecordClicked(item.lat, item.lng)
        }
    }

    override fun getItemCount(): Int = scores.size

    class ScoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val scoreLabel: TextView = itemView.findViewById(R.id.score_LBL_score)
        val dateLabel: TextView = itemView.findViewById(R.id.score_LBL_date)
    }
}