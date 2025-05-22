package com.example.guitarapp.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.data.model.SongTutorialShort
import com.example.guitarapp.databinding.ItemTutorialShortBinding

class TutorialShortAdapter(
    private val onItemClick: (SongTutorialShort) -> Unit
) : ListAdapter<SongTutorialShort, TutorialShortAdapter.TutorialViewHolder>(
    DiffCallback()
) {
    class TutorialViewHolder(
        private val binding: ItemTutorialShortBinding,
        private val onItemClick: (SongTutorialShort) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tutorial: SongTutorialShort) {
            binding.tvSongTitle.text = tutorial.song.title
            binding.tvAuthor.text = tutorial.tutorialAuthor.username

            binding.root.setOnClickListener {
                onItemClick(tutorial)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val binding = ItemTutorialShortBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TutorialViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<SongTutorialShort>() {
        override fun areItemsTheSame(oldItem: SongTutorialShort, newItem: SongTutorialShort): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SongTutorialShort, newItem: SongTutorialShort): Boolean {
            return oldItem == newItem
        }
    }
}