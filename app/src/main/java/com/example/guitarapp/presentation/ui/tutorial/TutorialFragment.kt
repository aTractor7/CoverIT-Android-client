package com.example.guitarapp.presentation.ui.tutorial

import android.content.Intent
import android.graphics.Color
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guitarapp.MainActivity
import com.example.guitarapp.databinding.FragmentTutorialBinding
import com.example.guitarapp.utils.Resource
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.flow.collectLatest
import kotlin.getValue
import androidx.core.graphics.toColorInt
import androidx.navigation.fragment.findNavController
import com.example.guitarapp.R
import com.example.guitarapp.data.model.SongBeat

class TutorialFragment : Fragment(){
    private val viewModel: TutorialViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TutorialViewModel(requireActivity().application) as T
            }
        }
    }
    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.fetchSongTutorial(9)

        binding.tvTutorialAuthor.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.profileState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        // Показати лоадер
                    }
                    is Resource.Success -> {
                        val tutorial = state.data

                        binding.tvSongTitle.text = tutorial.song.title
                        if (tutorial.description != null) {
                            binding.tvTutorialDescription.text = tutorial.description
                            binding.tvTutorialDescriptionLabel.visibility = View.VISIBLE
                        }
                        if (tutorial.recommendedStrumming != null) {
                            binding.tvTutorialStrumming.text = tutorial.recommendedStrumming
                            binding.tvTutorialStrummingLabel.visibility = View.VISIBLE
                        }
                        binding.tvTutorialAuthor.text = tutorial.tutorialAuthor.username
                        binding.tvTutorialCreatedAt.text = tutorial.createdAt.toString()

                        val sortedBeats = tutorial.beats.sortedBy { it.beat }


//                        binding.rvBeats.layoutManager = LinearLayoutManager(requireContext())
//                        binding.rvBeats.adapter = SongBeatAdapter(sortedBeats)

                        val groupedBeats = groupBeatsByNewline(sortedBeats)

                        groupedBeats.forEach { beatGroup ->

                            // Контейнер для одного рядка (одного підсписку бітів)
                            val rowLayout = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding(0, 16, 0, 16)
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                            }

                            // Ітеруємо біти в середині підсписку
                            beatGroup.forEach { beat ->
                                val beatLayout = LinearLayout(requireContext()).apply {
                                    orientation = LinearLayout.VERTICAL
                                    setPadding(5, 0, 0, 0) // відступи між бітами
                                }

                                // Контейнер для акордів
                                val chordsRow = FlexboxLayout(requireContext()).apply {
                                    flexWrap = FlexWrap.WRAP
                                    justifyContent = JustifyContent.FLEX_START
                                }

                                beat.beatChords.forEach { beatChord ->
                                    val chordView = TextView(requireContext()).apply {
                                        text = beatChord.chord.name
                                        textSize = 14f
                                        setTextColor(ContextCompat.getColor(context, R.color.darkest_color))
                                        background = ContextCompat.getDrawable(context, R.drawable.chord_background)
                                        setPadding(8, 8, 8, 8)

                                        // Додаємо маржін для відступу між акордами
                                        layoutParams = FlexboxLayout.LayoutParams(
                                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                            FlexboxLayout.LayoutParams.WRAP_CONTENT
                                        ).apply {
                                            setMargins(0, 0, 16, 8)
                                        }
                                    }

                                    chordsRow.addView(chordView)
                                }

                                val textView = TextView(requireContext()).apply {
                                    text = beat.text
                                    setTextColor(ContextCompat.getColor(context, R.color.darker3_color))
                                    textSize = 16f
                                    setPadding(4, 4, 4, 4)
                                }

                                beatLayout.addView(chordsRow)
                                beatLayout.addView(textView)

                                // Додаємо блок біта в горизонтальний рядок
                                rowLayout.addView(beatLayout)
                            }

                            // Додаємо рядок у головний контейнер
                            binding.llBeatsContainer.addView(rowLayout)
                        }
                    }
                    is Resource.NotAuthenticated -> {
                        startLoginActivity()
                        Toast.makeText(requireContext(),"Your session has expired.", Toast.LENGTH_LONG).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), state.message ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun groupBeatsByNewline(sortedBeats: List<SongBeat>): List<List<SongBeat>> {
        val result = mutableListOf<List<SongBeat>>()
        val currentGroup = mutableListOf<SongBeat>()

        for (beat in sortedBeats) {
            currentGroup.add(beat)
            if (beat.text.endsWith("\n")) {
                result.add(currentGroup.toList())
                currentGroup.clear()
            }
        }

        if (currentGroup.isNotEmpty()) {
            result.add(currentGroup.toList())
        }

        return result
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