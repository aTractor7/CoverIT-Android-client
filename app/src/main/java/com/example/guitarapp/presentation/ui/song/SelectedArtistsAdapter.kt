package com.example.guitarapp.presentation.ui.song

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.R
import com.example.guitarapp.data.model.ArtistShort
import com.google.android.material.button.MaterialButton

class SelectedArtistsAdapter(
    private val artists: List<ArtistShort>,
    private val onRemoveClick: (ArtistShort) -> Unit
) : RecyclerView.Adapter<SelectedArtistsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvArtistName: TextView = view.findViewById(R.id.tvArtistName)
        val btnRemove: ImageView = view.findViewById(R.id.btnRemoveArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_artist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = artists[position]
        holder.tvArtistName.text = artist.name
        holder.btnRemove.setOnClickListener {
            onRemoveClick(artist)
        }
    }

    override fun getItemCount() = artists.size
}