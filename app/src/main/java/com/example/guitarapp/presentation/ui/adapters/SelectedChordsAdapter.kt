package com.example.guitarapp.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.R
import com.example.guitarapp.data.model.Chord

class SelectedChordsAdapter(
    private val chords: List<Chord>,
    private val onRemoveClick: (Chord) -> Unit
) : RecyclerView.Adapter<SelectedChordsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvChordName: TextView = view.findViewById(R.id.tvName)
        val btnRemove: ImageView = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_some_name, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chord = chords[position]
        holder.tvChordName.text = chord.name
        holder.btnRemove.setOnClickListener {
            onRemoveClick(chord)
        }
    }

    override fun getItemCount() = chords.size
}