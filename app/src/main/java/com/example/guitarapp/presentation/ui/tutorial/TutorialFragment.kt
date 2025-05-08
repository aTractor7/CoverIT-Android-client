package com.example.guitarapp.presentation.ui.tutorial

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guitarapp.MainActivity
import com.example.guitarapp.databinding.FragmentTutorialBinding
import com.example.guitarapp.utils.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlin.getValue
import androidx.navigation.fragment.findNavController
import com.example.guitarapp.R
import com.example.guitarapp.data.model.Comment
import com.example.guitarapp.data.model.SongBeat
import com.example.guitarapp.data.model.SongTutorial


class TutorialFragment : Fragment(){
    private val viewModel: TutorialViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TutorialViewModel(requireActivity().application) as T
            }
        }
    }
    companion object {
        private const val ARG_TUTORIAL_ID = "tutorialId"

        fun newInstance(userId: Int = -1): TutorialFragment {
            val fragment = TutorialFragment()
            val args = Bundle().apply {
                putInt(ARG_TUTORIAL_ID, userId)
            }
            fragment.arguments = args
            return fragment
        }
    }
    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!

    private var currentTutorial: SongTutorial? = null

    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)

        commentAdapter = CommentAdapter()
        binding.rvComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tutorialId = arguments?.getInt(ARG_TUTORIAL_ID, -1) ?: -1
        if (!handleInvalidTutorialId(tutorialId)) return

        setupAuthorClickListener()
        observeTutorialState()
        viewModel.fetchSongTutorial(tutorialId)
    }

    private fun handleInvalidTutorialId(tutorialId: Int): Boolean {
        if (tutorialId == -1) {
            Toast.makeText(requireContext(), "Tutorial not found", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return false
        }
        return true
    }

    private fun setupAuthorClickListener() {
        binding.tvTutorialAuthor.setOnClickListener {
            currentTutorial?.tutorialAuthor?.id?.let { authorId ->
                val args = Bundle().apply {
                    putInt("userId", authorId)
                }
                findNavController().navigate(
                    R.id.action_tutorialFragment_to_profileFragment,
                    args
                )
            }
        }

        commentAdapter.setOnAuthorClickListener { comment ->
            val args = Bundle().apply {
                putInt("userId", comment.author.id)
            }
            findNavController().navigate(R.id.action_tutorialFragment_to_profileFragment, args)
        }
    }

    private fun observeTutorialState() {
        lifecycleScope.launchWhenStarted {
            viewModel.tutorialState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        // Показати лоадер (можна винести окремо за бажанням)
                    }
                    is Resource.Success -> handleTutorialLoaded(state.data)
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showError(state.message)
                    else -> {}
                }
            }
        }
    }

    private fun handleTutorialLoaded(tutorial: SongTutorial) {
        currentTutorial = tutorial
        binding.tvSongTitle.text = tutorial.song.title

        tutorial.description?.let {
            binding.tvTutorialDescription.text = it
            binding.tvTutorialDescriptionLabel.visibility = View.VISIBLE
        }

        tutorial.recommendedStrumming?.let {
            binding.tvTutorialStrumming.text = it
            binding.tvTutorialStrummingLabel.visibility = View.VISIBLE
        }

        binding.tvTutorialAuthor.text = tutorial.tutorialAuthor.username
        binding.tvTutorialCreatedAt.text = tutorial.createdAt.toString()

        val groupedBeats = groupBeatsByNewline(tutorial.beats.sortedBy { it.beat })
        val adapter = BeatGroupAdapter(groupedBeats)
        binding.rvBeatsContainer.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBeatsContainer.adapter = adapter

        if (tutorial.comments.isNotEmpty()) {
            binding.pbComments.visibility = View.GONE
            binding.rvComments.visibility = View.VISIBLE
            commentAdapter.submitList(tutorial.comments)
        } else {
            binding.pbComments.visibility = View.GONE
            binding.rvComments.visibility = View.GONE
            Toast.makeText(requireContext(), "Немає коментарів", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleAuthenticationError() {
        startLoginActivity()
        Toast.makeText(requireContext(), "Your session has expired.", Toast.LENGTH_LONG).show()
    }

    private fun showError(message: String?) {
        Toast.makeText(requireContext(), message ?: "Error", Toast.LENGTH_SHORT).show()
    }


    private fun groupBeatsByNewline(sortedBeats: List<SongBeat>): List<List<SongBeat>> {
        val result = mutableListOf<List<SongBeat>>()
        val currentGroup = mutableListOf<SongBeat>()

        var nextBeat: SongBeat? = null
        for (i in sortedBeats.indices) {
            var beat = sortedBeats[i]

            if (nextBeat != null) {
                beat = nextBeat
                nextBeat = null
            }

            val isLast = i == sortedBeats.lastIndex

            if (beat.text != "" && !beat.text.endsWith(" ") && !beat.text.endsWith("\n") && !isLast) {
                val updatedBeat = beat.copy(text = beat.text + "-")
                val nextOriginal = sortedBeats[i + 1]
                nextBeat = nextOriginal.copy(text = "-" + nextOriginal.text)

                currentGroup.add(updatedBeat)
            } else {
                currentGroup.add(beat)
            }

            //Todo: змінити тут константу на залежний від ширини екрану розмір
            if (beat.text.endsWith("\n") || currentGroup.sumOf { it.text.length } > 40) {
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