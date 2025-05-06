package com.example.guitarapp.presentation.ui.tutorial

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.UiContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.R
import com.example.guitarapp.data.model.SongBeat
import com.google.android.flexbox.FlexboxLayout
import kotlin.text.toInt

class BeatAdapter(
    private val beats: List<SongBeat>
) : RecyclerView.Adapter<BeatAdapter.BeatViewHolder>() {

    inner class BeatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val flexChords: FlexboxLayout = view.findViewById(R.id.flexChords)
        val tvText: TextView = view.findViewById(R.id.tvText)
        val rootLayout: LinearLayout = view.findViewById(R.id.rootLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song_beat, parent, false)
        return BeatViewHolder(view)
    }

    override fun onBindViewHolder(holder: BeatViewHolder, position: Int) {
        val beat = beats[position]
        val context = holder.itemView.context

        holder.flexChords.removeAllViews()

        setLayoutMinWidth(holder, beat.beatChords.size)



        for (chord in beat.beatChords) {
            val chordView = TextView(context).apply {
                text = chord.chord.name
                textSize = 14f
                setTextColor(ContextCompat.getColor(context, R.color.darkest_color))
                background = ContextCompat.getDrawable(context, R.drawable.chord_background)
                setPadding(8, 8, 8, 8)
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 16, 8)
                }
            }
            holder.flexChords.addView(chordView)
        }

        holder.tvText.text = beat.text
        holder.tvText.setTextColor(ContextCompat.getColor(context, R.color.darker3_color))
    }

    private fun setLayoutMinWidth(holder: BeatViewHolder, chordsNum : Int) {
        val minWidthDp = chordsNum * 60

        val context = holder.itemView.context
        holder.rootLayout.minimumWidth = minWidthDp * context.resources.displayMetrics.density.toInt()
    }

    override fun getItemCount(): Int = beats.size
}
