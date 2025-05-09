package com.example.guitarapp.presentation.ui.song

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.R
import com.example.guitarapp.data.model.ArtistShort

class SearchArtistsAdapter(
    private val onArtistClick: (ArtistShort) -> Unit
) : RecyclerView.Adapter<SearchArtistsAdapter.ViewHolder>() {

    private var artists = listOf<ArtistShort>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvArtistName: TextView = view.findViewById(R.id.tvArtistName)
    }

    fun submitList(newList: List<ArtistShort>) {
        artists = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = artists[position]
        holder.tvArtistName.text = artist.name
        holder.itemView.setOnClickListener {
            onArtistClick(artist)
        }
    }

    override fun getItemCount() = artists.size
}