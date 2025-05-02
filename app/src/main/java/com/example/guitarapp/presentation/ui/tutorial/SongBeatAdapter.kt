package com.example.guitarapp.presentation.ui.tutorial

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.data.model.SongBeat
import com.example.guitarapp.databinding.ItemBeatBinding
import com.google.android.flexbox.R

class SongBeatAdapter(private val beats: List<SongBeat>) :
    RecyclerView.Adapter<SongBeatAdapter.SongBeatViewHolder>() {

    inner class SongBeatViewHolder(val binding: ItemBeatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongBeatViewHolder {
        val binding = ItemBeatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongBeatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongBeatViewHolder, position: Int) {
        val beat = beats[position]

        holder.binding.beatText.text = beat.text

        holder.binding.chordContainer.removeAllViews()

        for (beatChord in beat.beatChords) {
            val context = holder.itemView.context
            val chordText = TextView(context).apply {
                text = beatChord.chord.name
                setPadding(16, 8, 16, 8)
//                setTextColor(ContextCompat.getColor(context, R.color.darkest_color))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }
            holder.binding.chordContainer.addView(chordText)
        }
    }

    override fun getItemCount(): Int = beats.size
}
