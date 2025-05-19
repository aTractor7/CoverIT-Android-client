package com.example.guitarapp.presentation.ui.tutorial.create

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.guitarapp.R
import com.example.guitarapp.data.model.BeatChord
import com.example.guitarapp.data.model.Chord
import com.example.guitarapp.data.model.Fingering
import com.example.guitarapp.data.model.SongBeatCreate
import com.example.guitarapp.databinding.ChordPopupBinding
import com.example.guitarapp.databinding.FragmentTutorialCreateBinding
import com.example.guitarapp.databinding.ItemSongBeatEditBinding
import com.example.guitarapp.utils.Constants
import com.example.guitarapp.utils.DoubleClickListener
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

        // Add existing chords
        for ((index, beatChord) in beat.beatChords.withIndex()) {
            addChordView(holder, beat, beatChord, index)
        }

        // Add chord button
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

            setOnClickListener(DoubleClickListener(
                onDoubleClick = {
                    beat.beatChords.removeAt(index)
                    notifyItemChanged(holder.adapterPosition)
                },
                onSingleClick = {
                    showChordPopup(beat.beatChords[index], this)
                }
            ))
        }
        holder.binding.flexChords.addView(chordView)
    }

    private fun showChordPopup(beatChord: BeatChord, anchorView: View) {
        val fingerings = beatChord.chord.fingerings ?: run {
            Toast.makeText(anchorView.context, "No image available for this chord", Toast.LENGTH_SHORT).show()
            return
        }

        if (fingerings.isEmpty()) {
            Toast.makeText(anchorView.context, "No image available for this chord", Toast.LENGTH_SHORT).show()
            return
        }

        var currentIndex = 0
        val popupBinding = ChordPopupBinding.inflate(LayoutInflater.from(anchorView.context))
        val popupView = popupBinding.root

        val popupWindow = PopupWindow(
            popupView,
            400,
            300,
            true
        ).apply {
            elevation = 10f
            isOutsideTouchable = true
        }

        fun loadImage(index: Int) {
            val imgPath = fingerings[index].imgPath
            val chordImageUrl = Constants.BASE_URL + imgPath.substringAfter("/")

            Glide.with(anchorView.context)
                .load(GlideUrl(chordImageUrl, LazyHeaders.Builder().build()))
                .into(popupBinding.ivChordImage)

            popupBinding.btnPrevChord.visibility = if (index == 0) View.INVISIBLE else View.VISIBLE
            popupBinding.btnNextChord.visibility = if (index == fingerings.size - 1) View.INVISIBLE else View.VISIBLE
        }

        loadImage(currentIndex)

        popupBinding.btnCloseChordImage.setOnClickListener { popupWindow.dismiss() }

        popupBinding.btnPrevChord.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                loadImage(currentIndex)
            }
        }

        popupBinding.btnNextChord.setOnClickListener {
            if (currentIndex < fingerings.size - 1) {
                currentIndex++
                loadImage(currentIndex)
            }
        }

        popupBinding.ivChordImage.setOnClickListener {
            beatChord.recommendedFingering = fingerings[currentIndex]
            popupWindow.dismiss()
            Toast.makeText(anchorView.context, "Fingering selected", Toast.LENGTH_SHORT).show()
        }

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        popupWindow.showAtLocation(
            popupView,
            Gravity.NO_GRAVITY,
            location[0] - (popupWindow.width / 2) + (anchorView.width / 2),
            location[1] - popupWindow.height - 16.dpToPx(anchorView.context)
        )
    }

    private fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

    private fun showChordSelectionDialog(context: Context, beat: SongBeatCreate, onChordSelected: (Chord) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Chord")

        val chordsArray = selectedChords.map { it.name }.toTypedArray()
        builder.setItems(chordsArray) { _, which ->
            val selectedChord = selectedChords[which]
            // Створюємо новий об'єкт BeatChord для кожного використання
            val newBeatChord = BeatChord(
                id = 0,
                songBeatId = 0,
                chord = selectedChord.copy(), // Копіюємо акорд
                recommendedFingering = null
            )
            beat.beatChords.add(newBeatChord)
            notifyItemChanged(beats.indexOf(beat))
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