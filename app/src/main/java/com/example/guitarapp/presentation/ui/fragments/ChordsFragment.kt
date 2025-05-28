package com.example.guitarapp.presentation.ui.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.target.Target
import com.example.guitarapp.MainActivity
import com.example.guitarapp.R
import com.example.guitarapp.data.model.Chord
import com.example.guitarapp.data.model.Fingering
import com.example.guitarapp.databinding.FragmentChordsBinding
import com.example.guitarapp.utils.Constants
import com.example.guitarapp.utils.Resource
import com.example.guitarapp.view_model.ChordViewModel
import com.example.guitarapp.view_model.CommentViewModel
import com.example.guitarapp.view_model.PersonalLibraryViewModel
import com.example.guitarapp.view_model.factory.ChordViewModelFactory
import kotlinx.coroutines.flow.collectLatest

class ChordsFragment : Fragment() {

    private val chordViewModel: ChordViewModel by viewModels {
        ChordViewModelFactory(requireActivity().application)
    }

    private var _binding: FragmentChordsBinding? = null
    private val binding get() = _binding!!

    private val baseChordNames = listOf("A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChordNavigation()
        observeChordState()
        chordViewModel.fetchChords("A")
    }

    private fun setupChordNavigation() {
        val baseChordsContainer = binding.root.findViewById<LinearLayout>(R.id.baseChordsLinearLayout)
        baseChordsContainer.removeAllViews()

        baseChordNames.forEach { chordName ->
            TextView(requireContext()).apply {
                text = chordName
                setTextColor(ContextCompat.getColor(requireContext(), R.color.darker2_color))
                textSize = 25f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(32, 16, 32, 16)
                setOnClickListener { chordViewModel.fetchChords(chordName) }
                baseChordsContainer.addView(this)
            }
        }
    }

    private fun observeChordState() {
        lifecycleScope.launchWhenStarted {
            chordViewModel.chordState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                    }
                    is Resource.Success -> handleCommentLoaded(state.data)
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showError(state.message)
                    else -> {}
                }
            }
        }
    }

    private fun handleCommentLoaded(chords: List<Chord>){
        displayChordVariations(chords)
        if (chords.isNotEmpty()) {
            loadChordImage(chords[0].fingerings)
        }
    }

    private fun displayChordVariations(chord: List<Chord>) {
        val modificationsContainer = binding.root.findViewById<LinearLayout>(R.id.modificationsLinearLayout)
        modificationsContainer.removeAllViews()

        chord.forEach { modification ->
            TextView(requireContext()).apply {
                text = modification.name
                setTextColor(ContextCompat.getColor(requireContext(), R.color.darker1_color))
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(32, 16, 32, 16)
                setOnClickListener { loadChordImage(modification.fingerings) }
                modificationsContainer.addView(this)
            }
        }
    }

    private fun loadChordImage(fingerings: List<Fingering>?) {
        binding.chordImageContainer.visibility = View.VISIBLE

        if (fingerings == null || fingerings.isEmpty()) {

            binding.ivChordImage.setImageResource(R.drawable.icon_question_mark)

            binding.btnPrevChord.visibility = View.INVISIBLE
            binding.btnNextChord.visibility = View.INVISIBLE

            Toast.makeText(requireContext(), "No image available for this chord", Toast.LENGTH_SHORT).show()
            return
        }

        val fingeringsList = fingerings.distinctBy { it.id }
        var currentIndex = 0


        binding.btnPrevChord.visibility = View.VISIBLE
        binding.btnNextChord.visibility = View.VISIBLE

        loadFingeringImage(fingeringsList[currentIndex])

        binding.btnPrevChord.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                loadFingeringImage(fingeringsList[currentIndex])
                updateNavigationButtons(currentIndex, fingeringsList.size)
            }
        }

        binding.btnNextChord.setOnClickListener {
            if (currentIndex < fingeringsList.size - 1) {
                currentIndex++
                loadFingeringImage(fingeringsList[currentIndex])
                updateNavigationButtons(currentIndex, fingeringsList.size)
            }
        }

        updateNavigationButtons(currentIndex, fingeringsList.size)
    }

    private fun loadFingeringImage(fingering: Fingering) {
        val imgPath = fingering.imgPath
        val chordImageUrl = Constants.BASE_URL + imgPath.substringAfter("/")

        Glide.with(requireContext())
            .load(GlideUrl(chordImageUrl, LazyHeaders.Builder().build()))
            .override(Target.SIZE_ORIGINAL)
            .fitCenter()
            .into(binding.ivChordImage)
    }

    private fun handleAuthenticationError() {
        startLoginActivity()
        Toast.makeText(requireContext(), getString(R.string.session_expired), Toast.LENGTH_LONG).show()
    }

    private fun showError(message: String?) {
        Toast.makeText(requireContext(), message ?: getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
    }

    private fun updateNavigationButtons(currentIndex: Int, totalCount: Int) {
        binding.btnPrevChord.visibility = if (currentIndex == 0) View.INVISIBLE else View.VISIBLE
        binding.btnNextChord.visibility = if (currentIndex == totalCount - 1) View.INVISIBLE else View.VISIBLE

        // anim
        binding.btnPrevChord.animate().alpha(if (currentIndex == 0) 0f else 1f).start()
        binding.btnNextChord.animate().alpha(if (currentIndex == totalCount - 1) 0f else 1f).start()
    }

    private fun startLoginActivity(){
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}