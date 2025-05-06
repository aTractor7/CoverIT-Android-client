package com.example.guitarapp.presentation.ui.tutorial

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.R
import com.example.guitarapp.data.model.SongBeat

class BeatGroupAdapter(
    private val beatGroups: List<List<SongBeat>>
) : RecyclerView.Adapter<BeatGroupAdapter.BeatGroupViewHolder>() {

    inner class BeatGroupViewHolder(val recyclerView: RecyclerView) : RecyclerView.ViewHolder(recyclerView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeatGroupViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song_beat_group, parent, false) as RecyclerView
        view.layoutManager = LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)
        return BeatGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: BeatGroupViewHolder, position: Int) {
        val beatGroup = beatGroups[position]
        holder.recyclerView.adapter = BeatAdapter(beatGroup)
    }

    override fun getItemCount(): Int = beatGroups.size
}
