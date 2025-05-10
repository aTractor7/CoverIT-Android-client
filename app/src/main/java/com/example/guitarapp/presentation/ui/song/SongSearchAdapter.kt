package com.example.guitarapp.presentation.ui.song

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.data.model.SongShort
import com.example.guitarapp.databinding.ItemSomeNameSearchResultBinding

class SongSearchAdapter(
    private var songs: List<SongShort>,
    private val onItemClick: (SongShort) -> Unit
) : RecyclerView.Adapter<SongSearchAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSomeNameSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(song: SongShort) {
            binding.tvName.text = song.title
            binding.root.setOnClickListener { onItemClick(song) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSomeNameSearchResultBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(songs[position])
    }

    override fun getItemCount() = songs.size

    fun submitList(newSongs: List<SongShort>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}
