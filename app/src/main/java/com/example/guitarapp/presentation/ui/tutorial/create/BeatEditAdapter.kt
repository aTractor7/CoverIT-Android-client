package com.example.guitarapp.presentation.ui.tutorial.create

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.R
import com.example.guitarapp.data.model.BeatChord
import com.example.guitarapp.data.model.Chord
import com.example.guitarapp.data.model.Fingering
import com.example.guitarapp.data.model.SongBeatCreate
import com.example.guitarapp.databinding.FragmentTutorialCreateBinding
import com.example.guitarapp.databinding.ItemSongBeatEditBinding
import com.google.android.flexbox.FlexboxLayout
import kotlin.math.max

class BeatEditAdapter(
    private val beats: MutableList<SongBeatCreate>,
    private val selectedChords: List<Chord>,
    private val onAddBeat: () -> Unit,
    private val onRemoveBeat: (beatIndex: Int) -> Unit,
    private val binding: FragmentTutorialCreateBinding
) : RecyclerView.Adapter<BeatEditAdapter.BeatViewHolder>() {

    inner class BeatViewHolder(val binding: ItemSongBeatEditBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeatViewHolder {
        val itemBinding = ItemSongBeatEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BeatViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: BeatViewHolder, position: Int) {
        val beat = beats[position]

        holder.binding.etText.setText(beat.text)
        holder.binding.etText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                beat.text = holder.binding.etText.text.toString()
            }
        }

        holder.binding.flexChords.removeAllViews()

        // Додаємо існуючі акорди
        for ((index, beatChord) in beat.beatChords.withIndex()) {
            addChordView(holder, beat, beatChord, index)
        }

        // Кнопка додавання акорду
        holder.binding.btnAddChord.setOnClickListener {
            if (selectedChords.isNotEmpty()) {
                showChordSelectionDialog(holder.itemView.context, beat) { selectedChord ->
                    val newBeatChord = BeatChord(
                        id = 0,
                        songBeatId = 0,
                        chord = selectedChord,
                        recommendedFingering = null
                    )
                    beat.beatChords.add(newBeatChord)
                    notifyItemChanged(position)
                }
            } else {
                Toast.makeText(holder.itemView.context, "No chords selected", Toast.LENGTH_SHORT).show()
            }
        }

        holder.binding.btnRemoveBeat.setOnClickListener {
            onRemoveBeat(position)
        }

        setLayoutMinWidth(holder, beat.beatChords.size)
    }

    private fun addChordView(holder: BeatViewHolder, beat: SongBeatCreate, beatChord: BeatChord, index: Int) {
        val context = holder.itemView.context
        val chordView = TextView(context).apply {
            text = beatChord.chord.name
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
            setOnClickListener {
                beat.beatChords.removeAt(index)
                notifyItemChanged(holder.adapterPosition)
            }
        }
        holder.binding.flexChords.addView(chordView)
    }

    private fun showChordSelectionDialog(context: Context, beat: SongBeatCreate, onChordSelected: (Chord) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Chord")

        val chordsArray = selectedChords.map { it.name }.toTypedArray()
        builder.setItems(chordsArray) { _, which ->
            onChordSelected(selectedChords[which])
        }

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun setLayoutMinWidth(holder: BeatViewHolder, chordsNum: Int) {
        val minWidthDp = max(60, chordsNum * 60)
        val context = holder.itemView.context
        holder.binding.rootLayout.minimumWidth = (minWidthDp * context.resources.displayMetrics.density).toInt()
    }

    override fun getItemCount(): Int = beats.size
}