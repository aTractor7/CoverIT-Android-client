package com.example.guitarapp.presentation.ui.fragments

import BeatGroupEditAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guitarapp.MainActivity
import com.example.guitarapp.R
import com.example.guitarapp.data.model.BeatChord
import com.example.guitarapp.data.model.Chord
import com.example.guitarapp.data.model.SongBeat
import com.example.guitarapp.data.model.SongBeatCreate
import com.example.guitarapp.data.model.SongShort
import com.example.guitarapp.data.model.SongTutorial
import com.example.guitarapp.data.model.SongTutorialCreate
import com.example.guitarapp.databinding.FragmentTutorialCreateBinding
import com.example.guitarapp.view_model.ChordViewModel
import com.example.guitarapp.presentation.ui.adapters.ChordsSearchAdapter
import com.example.guitarapp.presentation.ui.adapters.SelectedChordsAdapter
import com.example.guitarapp.presentation.ui.adapters.SongSearchAdapter
import com.example.guitarapp.view_model.SongViewModel
import com.example.guitarapp.view_model.TutorialViewModel
import com.example.guitarapp.utils.Resource
import com.example.guitarapp.view_model.CommentViewModel
import com.example.guitarapp.view_model.PersonalLibraryViewModel
import com.example.guitarapp.view_model.factory.ChordViewModelFactory
import com.example.guitarapp.view_model.factory.SongViewModelFactory
import com.example.guitarapp.view_model.factory.TutorialViewModelFactory
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.flow.collectLatest
import kotlin.String
import kotlin.getValue

class CreateTutorialFragment : Fragment() {
    private val tutorialViewModel: TutorialViewModel by viewModels {
        TutorialViewModelFactory(requireActivity().application)
    }
    private val songViewModel: SongViewModel by viewModels {
        SongViewModelFactory(requireActivity().application)
    }
    private val chordViewModel: ChordViewModel by viewModels {
        ChordViewModelFactory(requireActivity().application)
    }

    private var _binding: FragmentTutorialCreateBinding? = null
    private val binding get() = _binding!!

    private lateinit var songSearchAdapter: SongSearchAdapter
    private lateinit var chordSearchAdapter: ChordsSearchAdapter
    private lateinit var selectedChordsAdapter: SelectedChordsAdapter
    private lateinit var beatGroupAdapter: BeatGroupEditAdapter

    private var selectedSong: SongShort? = null
    private val selectedChords = mutableListOf<Chord>()
    private val beatGroups = mutableListOf<MutableList<SongBeatCreate>>()
    private var existingTutorial: SongTutorial? = null
    private var isUpdateMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            existingTutorial = it.getParcelable<SongTutorial>("songTutorial")
            isUpdateMode = existingTutorial != null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBeatEditor()
        setupAdapters()
        setupClickListeners()
        observeViewModels()

        if (isUpdateMode) {
            setupUpdateMode()
        } else {
            binding.etSongSearch.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && selectedSong == null) {
                    showSearchViews()
                }
            }
        }
    }

    private fun setupUpdateMode() {
        existingTutorial?.let { tutorial ->
            // Змінюємо заголовок на "Update Tutorial"
            binding.tvCreateTutorialTitle.text = getString(R.string.update_tutorial_title)

            // Set song info (block selection)
            selectedSong = tutorial.song

            // Оновлюємо відображення обраної пісні (без кнопки видалення)
            binding.tvSongSearchLabel.text = getString(R.string.tutorial_covered_song)
            binding.llSelectedSong.visibility = View.VISIBLE
            binding.tvSelectedSongTitle.text = tutorial.song.title
            binding.btnRemoveSelectedSong.visibility = View.GONE // Ховаємо кнопку видалення
            binding.etSongSearch.visibility = View.GONE
            binding.btnSearchSong.visibility = View.GONE
            val params = binding.tvDifficultyLabel.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = binding.llSelectedSong.id
            binding.tvDifficultyLabel.layoutParams = params

            // Set basic info
            binding.etDifficulty.setText(tutorial.difficulty)
            binding.etDescription.setText(tutorial.description ?: "")
            binding.etStrumming.setText(tutorial.recommendedStrumming ?: "")

            // Set chords - тільки унікальні акорди
            selectedChords.clear()
            selectedChords.addAll(
                tutorial.beats.flatMap { beat ->
                    beat.beatChords.map { it.chord }
                }.distinctBy { it.id } // Беремо тільки унікальні акорди по id
            )
            selectedChordsAdapter.notifyDataSetChanged()

            // Set beats - групуюємо по наявності \n в тексті
            beatGroups.clear()
            var currentGroup = mutableListOf<SongBeatCreate>()

            tutorial.beats.sortedBy { it.beat }.forEach { beat ->
                currentGroup.add(
                    SongBeatCreate(
                        text = beat.text,
                        beat = beat.beat,
                        comment = beat.comment,
                        beatChords = beat.beatChords.map { beatChord ->
                            BeatChord(
                                id = beatChord.id,
                                songBeatId = beatChord.songBeatId,
                                chord = beatChord.chord.copy(),
                                recommendedFingering = beatChord.recommendedFingering?.copy()
                            )
                        }.toMutableList()
                    )
                )

                // Якщо текст містить \n - починаємо нову групу
                if (beat.text?.contains("\n") == true) {
                    beatGroups.add(currentGroup)
                    currentGroup = mutableListOf()
                }
            }

            // Додаємо останню групу, якщо вона не пуста
            if (currentGroup.isNotEmpty()) {
                beatGroups.add(currentGroup)
            }

            beatGroupAdapter.notifyDataSetChanged()

            // Change button text
            binding.btnCreateTutorial.text = getString(R.string.update_button)
        }
    }



    private fun setupBeatEditor() {
        beatGroups.add(mutableListOf(SongBeatCreate(null, 0, null, mutableListOf())))

        beatGroupAdapter = BeatGroupEditAdapter(
            beatGroups = beatGroups,
            selectedChords = selectedChords,
            onAddBeat = { groupIndex ->
                beatGroupAdapter.addBeat(groupIndex)
            },
            onRemoveBeat = { groupIndex, beatIndex ->
                beatGroupAdapter.removeBeat(groupIndex, beatIndex)
            },
            onAddGroup = {
                beatGroupAdapter.addGroup()
            },
            onRemoveGroup = { groupIndex ->
                beatGroupAdapter.removeGroup(groupIndex)
            },
            binding = binding
        )

        binding.rvBeatsContainer.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBeatsContainer.adapter = beatGroupAdapter

        binding.btnAddBeatGroup.setOnClickListener {
            val lastGroup = beatGroups.lastOrNull()

            if (lastGroup != null && lastGroup.isNotEmpty()) {
                var nextBeat = lastGroup.last().beat + 1
                val copiedGroup = lastGroup.map { beat ->
                    SongBeatCreate(
                        text = beat.text,
                        beat = nextBeat++,
                        comment = null,
                        beatChords = beat.beatChords.map { beatChord ->
                            BeatChord(
                                id = beatChord.id,
                                songBeatId = beatChord.songBeatId,
                                chord = beatChord.chord.copy(),
                                recommendedFingering = beatChord.recommendedFingering?.copy()
                            )
                        }.toMutableList()
                    )
                }.toMutableList()

                beatGroups.add(copiedGroup)
                beatGroupAdapter.notifyItemInserted(beatGroups.size - 1)
            } else {
                beatGroups.add(mutableListOf(SongBeatCreate(null, 0, null, mutableListOf())))
                beatGroupAdapter.notifyItemInserted(beatGroups.size - 1)
            }
        }

    }

    private fun setupAdapters() {
        songSearchAdapter = SongSearchAdapter(emptyList()) { song ->
            selectedSong = song

            binding.tvSongSearchLabel.text = getString(R.string.tutorial_covered_song)
            binding.llSelectedSong.visibility = View.VISIBLE
            binding.tvSelectedSongTitle.text = song.title

            hideSearchSongViews()
        }

        chordSearchAdapter = ChordsSearchAdapter { chord ->
            if (!selectedChords.any { it.id == chord.id }) {
                selectedChords.add(chord)
                selectedChordsAdapter.notifyItemInserted(selectedChords.size - 1)
            }
            clearSearchChords()
        }

        selectedChordsAdapter = SelectedChordsAdapter(selectedChords) { chord ->
            val index = selectedChords.indexOf(chord)
            selectedChords.remove(chord)
            selectedChordsAdapter.notifyItemRemoved(index)
        }

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songSearchAdapter
        }

        binding.rvSearchChords.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            adapter = chordSearchAdapter
        }

        binding.rvSelectedChords.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            adapter = selectedChordsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSearchSong.setOnClickListener {
            val query = binding.etSongSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                songViewModel.fetchSongs(query)
            }
        }

        binding.btnCreateTutorial.setOnClickListener {
            selectedSong?.let { song ->
                if (validateInput()) {
                    if (isUpdateMode) {
                        updateTutorial(song)
                    } else {
                        createTutorial(song)
                    }
                }
            } ?: run {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_select_song),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnSearchChords.setOnClickListener {
            val query = binding.etChordSearch.text.toString().trim()
            if(query.isNotEmpty()) {
                chordViewModel.fetchChords(query)
            }
        }

        binding.btnRemoveSelectedSong.setOnClickListener {
            binding.tvSongSearchLabel.text = getString(R.string.song_search_label)
            clearSelectedSong()
        }

        binding.tvNoSongFound.setOnClickListener {
            findNavController().navigate(R.id.action_createTutorialFragment_to_songCreateFragment)
        }
    }

    private fun observeViewModels() {
        lifecycleScope.launchWhenStarted {
            songViewModel.songState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> showSongSearchLoading(true)
                    is Resource.Success -> handleSongSearchResults(state.data)
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showSongSearchError(state.message)
                    else -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            tutorialViewModel.tutorialCreateState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> showTutorialCreationLoading(true)
                    is Resource.Success -> handleTutorialCreated(state.data)
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showTutorialCreationError(state.message)
                    else -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            chordViewModel.chordState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> showChordSearchLoading(true)
                    is Resource.Success -> handleChordsSearchResults(state.data)
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showTutorialCreationError(state.message)
                    else -> {}
                }
            }
        }
    }

    private fun handleSongSearchResults(songs: List<SongShort>) {
        showSongSearchLoading(false)
        if (songs.isEmpty()) {
            binding.tvNoSongFound.visibility = View.VISIBLE
            binding.rvSearchResults.visibility = View.GONE
        } else {
            binding.tvNoSongFound.visibility = View.GONE
            binding.rvSearchResults.visibility = View.VISIBLE
            songSearchAdapter.submitList(songs)
        }
    }

    private fun handleTutorialCreated(id: Int) {
        showTutorialCreationLoading(false)
        Toast.makeText(
            requireContext(),
            getString(R.string.tutorial_created_successfully),
            Toast.LENGTH_SHORT
        ).show()

        val args = Bundle().apply {
            putInt("tutorialId", id)
        }
        findNavController().navigate(
            R.id.action_createTutorialFragment_to_tutorialFragment,
            args
        )
    }

    private fun validateInput(): Boolean {
        if (binding.etDifficulty.text.toString().isBlank()) {
            binding.etDifficulty.error = getString(R.string.difficulty_required)
            return false
        }
        return true
    }

    private fun handleChordsSearchResults(chords: List<Chord>) {
        showChordSearchLoading(false)
        if (chords.isEmpty()) {
            binding.tvNoChordsFound.visibility = View.VISIBLE
            binding.rvSearchChords.visibility = View.GONE
        } else {
            binding.tvNoChordsFound.visibility = View.GONE
            binding.rvSearchChords.visibility = View.VISIBLE
            chordSearchAdapter.submitList(chords)
        }
    }

    private fun createTutorial(song: SongShort) {
        for (beats in beatGroups) {
            val lastBeat = beats.lastOrNull()
            lastBeat?.text = (lastBeat.text ?: "") + " \n"
        }
        val allBeats = beatGroups.flatten()

        val tutorial = SongTutorialCreate(
            difficulty = binding.etDifficulty.text.toString(),
            description = binding.etDescription.text.toString().takeIf { it.isNotBlank() },
            backtrack = null,
            recommendedStrumming = binding.etStrumming.text.toString().takeIf { it.isNotBlank() },
            song = song,
            beats = allBeats
        )
        tutorialViewModel.createSongTutorial(tutorial)
    }

    private fun updateTutorial(song: SongShort) {
        for (beats in beatGroups) {
            val lastBeat = beats.lastOrNull()
            lastBeat?.text = (lastBeat.text ?: "") + " \n"
        }
        val allBeats = beatGroups.flatten()

        existingTutorial?.let { originalTutorial ->
            val updatedTutorial = originalTutorial.copy(
                difficulty = binding.etDifficulty.text.toString(),
                description = binding.etDescription.text.toString().takeIf { it.isNotBlank() },
                recommendedStrumming = binding.etStrumming.text.toString().takeIf { it.isNotBlank() },
                beats = allBeats.mapIndexed { index, beatCreate ->
                    // Знаходимо оригінальний beat за beat або індексом
                    val originalBeat = originalTutorial.beats.find { it.beat == beatCreate.beat }
                        ?: originalTutorial.beats.getOrNull(index)

                    SongBeat(
                        id = originalBeat?.id ?: 0, // Беремо id з оригінального tutorial
                        text = beatCreate.text,
                        beat = beatCreate.beat,
                        comment = beatCreate.comment,
                        songTutorialId = originalTutorial.id,
                        beatChords = beatCreate.beatChords.map { beatChordCreate ->
                            // Знаходимо оригінальний beatChord за chord.id
                            val originalBeatChord = originalBeat?.beatChords?.find {
                                it.chord.id == beatChordCreate.chord.id
                            }

                            BeatChord(
                                id = originalBeatChord?.id ?: 0, // Беремо id з оригінального tutorial
                                songBeatId = originalBeat?.id ?: 0,
                                chord = beatChordCreate.chord.copy(),
                                recommendedFingering = beatChordCreate.recommendedFingering?.copy()
                            )
                        }
                    )
                }
            )
            tutorialViewModel.updateSongTutorial(updatedTutorial)
        }
    }


    private fun clearSelectedSong() {
        selectedSong = null
        binding.llSelectedSong.visibility = View.INVISIBLE
        showSearchViews()
        binding.etSongSearch.text.clear()
    }

    private fun hideSearchSongViews() {
        binding.apply {
            etSongSearch.visibility = View.GONE
            btnSearchSong.visibility = View.GONE
            rvSearchResults.visibility = View.GONE
            pbSearch.visibility = View.GONE
            tvNoSongFound.visibility = View.INVISIBLE
        }
        binding.etSongSearch.clearFocus()
    }

    private fun clearSearchChords() {
        binding.etChordSearch.text.clear()
        binding.rvSearchChords.visibility = View.GONE
        binding.tvNoChordsFound.visibility = View.GONE
    }

    private fun showSearchViews() {
        binding.apply {
            etSongSearch.visibility = View.VISIBLE
            btnSearchSong.visibility = View.VISIBLE
        }
    }

    private fun showSongSearchLoading(isLoading: Boolean) {
        binding.pbSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showTutorialCreationLoading(isLoading: Boolean) {
        binding.btnCreateTutorial.isEnabled = !isLoading
    }

    private fun showChordSearchLoading(isLoading: Boolean) {
        binding.pbChordsSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showSongSearchError(message: String?) {
        showSongSearchLoading(false)
        Toast.makeText(
            requireContext(),
            message ?: getString(R.string.song_search_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showTutorialCreationError(message: String?) {
        showTutorialCreationLoading(false)
        Toast.makeText(
            requireContext(),
            message ?: getString(R.string.tutorial_creation_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleAuthenticationError() {
        startLoginActivity()
        Toast.makeText(
            requireContext(),
            getString(R.string.error_session_expired),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun startLoginActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(tutorial: SongTutorial? = null) = CreateTutorialFragment().apply {
            arguments = Bundle().apply {
                putParcelable("songTutorial", tutorial)
            }
        }
    }
}