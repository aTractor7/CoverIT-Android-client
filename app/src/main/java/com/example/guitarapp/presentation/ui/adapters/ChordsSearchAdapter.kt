package com.example.guitarapp.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.R
import com.example.guitarapp.data.model.Chord

class ChordsSearchAdapter(
    private val onChordClick: (Chord) -> Unit
) : RecyclerView.Adapter<ChordsSearchAdapter.ViewHolder>() {

    private var chords = listOf<Chord>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvChordName: TextView = view.findViewById(R.id.tvName)
    }

    fun submitList(newList: List<Chord>) {
        chords = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chords_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chord = chords[position]
        holder.tvChordName.text = chord.name
        holder.itemView.setOnClickListener {
            onChordClick(chord)
        }
    }

    override fun getItemCount() = chords.size
}