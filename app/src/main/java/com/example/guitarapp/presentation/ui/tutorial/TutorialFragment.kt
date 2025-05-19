package com.example.guitarapp.presentation.ui.tutorial

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestOptions
import com.example.guitarapp.R
import com.example.guitarapp.data.model.BeatChord
import com.example.guitarapp.data.model.Comment
import com.example.guitarapp.data.model.CommentCreate
import com.example.guitarapp.data.model.Fingering
import com.example.guitarapp.data.model.PersonalLibraryCreate
import com.example.guitarapp.data.model.SongBeat
import com.example.guitarapp.data.model.SongTutorial
import com.example.guitarapp.presentation.ui.comment.CommentViewModel
import com.example.guitarapp.presentation.ui.song.SongSearchAdapter
import com.example.guitarapp.presentation.ui.tutorial.PersonalLibraryViewModel
import com.example.guitarapp.utils.Constants
import com.example.guitarapp.utils.SessionInterceptor
import com.example.guitarapp.utils.SessionManager
import okhttp3.OkHttpClient


class TutorialFragment : Fragment(){
    private val viewModel: TutorialViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TutorialViewModel(requireActivity().application) as T
            }
        }
    }
    private val commentViewModel: CommentViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CommentViewModel(requireActivity().application) as T
            }
        }
    }
    private val personalLibraryViewModel: PersonalLibraryViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return PersonalLibraryViewModel(requireActivity().application) as T
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

    private lateinit var btnAddToLibrary: ImageButton
    private lateinit var btnRemoveFromLibrary: ImageButton

    private var isInLibrary = false
    private var replyingToCommentId: Int = 0

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

        setupClickListeners()
        setupLibraryButtons()
        setupEditButton()
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

    private fun setupClickListeners() {
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

        binding.btnAddComment.setOnClickListener {
            val commentText = binding.etCommentInput.text.toString()
            if (commentText.isBlank()) return@setOnClickListener

            binding.etCommentInput.text.clear()
            binding.etCommentInput.hint = getString(R.string.tutorial_comments_add_hint)

            val tutorialId = currentTutorial?.id!!
            val commentCreate = CommentCreate(
                text = commentText,
                idAnswerOn = replyingToCommentId,
                songTutorialId = tutorialId
            )

            observeCommentsState()
            commentViewModel.createComment(commentCreate)
            replyingToCommentId = 0
        }

        commentAdapter.setOnAuthorClickListener { comment ->
            val args = Bundle().apply {
                putInt("userId", comment.author.id)
            }
            findNavController().navigate(R.id.action_tutorialFragment_to_profileFragment, args)
        }

        commentAdapter.setOnReplyClickListener { comment ->
            if (comment.id == 0) {
                binding.etCommentInput.hint = getString(R.string.tutorial_comments_add_hint)
                replyingToCommentId = 0
            } else {
                binding.etCommentInput.hint = getString(R.string.tutorial_comments_replying_to_comment, comment.author.username)
                replyingToCommentId = comment.id
                binding.etCommentInput.requestFocus()
            }
        }
    }

    private fun setupLibraryButtons() {
        btnAddToLibrary = binding.btnAddToLibrary
        btnRemoveFromLibrary = binding.btnRemoveFromLibrary

        btnAddToLibrary.setOnClickListener {
            currentTutorial?.id?.let { tutorialId ->
                personalLibraryViewModel.createPersonalLibrary(PersonalLibraryCreate(tutorialId))
                observeLibraryState()
            }
        }

        btnRemoveFromLibrary.setOnClickListener {
            currentTutorial?.id?.let { tutorialId ->
                personalLibraryViewModel.deletePersonalLibrary(tutorialId)
                observeLibraryState()
            }
        }

        checkLibraryStatus()
    }

    private fun setupEditButton() {
        val tvEdit = binding.tvEditTutorial
        tvEdit.visibility = View.GONE

        tvEdit.setOnClickListener {
            currentTutorial?.let { tutorial ->
                findNavController().navigate(
                    R.id.action_tutorialFragment_to_createTutorialFragment,
                    bundleOf("songTutorial" to tutorial)
                )
            }
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
        val adapter = BeatGroupAdapter(groupedBeats) {beatChord, view ->
            showChordPopup(beatChord, view)
        }
        binding.rvBeatsContainer.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBeatsContainer.adapter = adapter

        if (tutorial.comments.isNotEmpty()) {
            handleCommentLoaded(tutorial.comments)
        } else {
            binding.pbComments.visibility = View.GONE
            binding.rvComments.visibility = View.GONE
            binding.tvCommentsEmpty.visibility = View.VISIBLE
        }

        binding.tvEditTutorial.visibility = if (SessionManager.getUserId(requireContext()) == tutorial.tutorialAuthor.id) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun observeLibraryState() {
        lifecycleScope.launchWhenStarted {
            personalLibraryViewModel.personalLibraryPageState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        // Show loading if needed
                    }
                    is Resource.Success -> {
                        val tutorialId = arguments?.getInt(ARG_TUTORIAL_ID, -1) ?: -1
                        isInLibrary = state.data.any { it.songTutorial.id == tutorialId }
                        updateLibraryButtons()
                    }
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showError(state.message)
                    else -> {}
                }
            }
        }
    }

    private fun checkLibraryStatus() {
        val tutorialId = arguments?.getInt(ARG_TUTORIAL_ID, -1) ?: -1
        if (tutorialId != -1) {
            personalLibraryViewModel.fetchPersonalLibrary(userId = SessionManager.getUserId(requireContext()))
            observeLibraryState()
        }
    }

    private fun showChordPopup(chord: BeatChord, anchorView: View) {
        val fingerings = chord.chord.fingerings ?: run {
            Toast.makeText(requireContext(), "No image available for this chord", Toast.LENGTH_SHORT).show()
            return
        }

        if (fingerings.isEmpty()) {
            Toast.makeText(requireContext(), "No image available for this chord", Toast.LENGTH_SHORT).show()
            return
        }

        val fingeringsList: List<Fingering> = buildList {
            chord.recommendedFingering?.let { add(it) }
            addAll(fingerings)
        }.distinctBy { it.id }

        var currentIndex = 0
        val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.chord_popup, null)

        val popupWindow = PopupWindow(
            popupView,
            400,
            300,
            true
        ).apply {
            elevation = 10f
            isOutsideTouchable = true
        }

        val imageView = popupView.findViewById<ImageView>(R.id.ivChordImage)
        val closeButton = popupView.findViewById<ImageButton>(R.id.btnCloseChordImage)
        val prevButton = popupView.findViewById<ImageButton>(R.id.btnPrevChord)
        val nextButton = popupView.findViewById<ImageButton>(R.id.btnNextChord)

        fun loadImage(index: Int) {
            val imgPath = fingeringsList[index].imgPath

            val chordImageUrl = Constants.BASE_URL + imgPath.substringAfter("/")

            Glide.with(requireContext())
                .load(GlideUrl(chordImageUrl, LazyHeaders.Builder().build()))
                .into(imageView)

            // Update arrow visibility
            prevButton.visibility = if (index == 0) View.INVISIBLE else View.VISIBLE
            nextButton.visibility = if (index == fingeringsList.size - 1) View.INVISIBLE else View.VISIBLE
        }

        // Initial load
        loadImage(currentIndex)

        closeButton.setOnClickListener { popupWindow.dismiss() }

        prevButton.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                loadImage(currentIndex)
            }
        }

        nextButton.setOnClickListener {
            if (currentIndex < fingerings.size - 1) {
                currentIndex++
                loadImage(currentIndex)
            }
        }

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        popupWindow.showAtLocation(
            binding.root,
            Gravity.NO_GRAVITY,
            location[0] - (popupWindow.width / 2) + (anchorView.width / 2),
            location[1] - popupWindow.height - 16.dpToPx()
        )
    }

    private fun updateLibraryButtons() {
        if (isInLibrary) {
            btnAddToLibrary.visibility = View.GONE
            btnRemoveFromLibrary.visibility = View.VISIBLE
        } else {
            btnAddToLibrary.visibility = View.VISIBLE
            btnRemoveFromLibrary.visibility = View.GONE
        }
    }

    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun observeCommentsState() {
        lifecycleScope.launchWhenStarted {
            commentViewModel.commentState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        // Показати лоадер (можна винести окремо за бажанням)
                    }
                    is Resource.Success -> handleCommentLoaded(state.data)
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showError(state.message)
                    else -> {}
                }
            }
        }
    }

    private fun handleCommentLoaded(comments: List<Comment>){
        val sortedComments = comments.sortedByDescending { it.comments.size }
        binding.pbComments.visibility = View.GONE
        binding.rvComments.visibility = View.VISIBLE
        binding.tvCommentsEmpty.visibility = View.GONE
        commentAdapter.submitList(sortedComments)
    }

    private fun handleAuthenticationError() {
        startLoginActivity()
        Toast.makeText(requireContext(), getString(R.string.session_expired), Toast.LENGTH_LONG).show()
    }

    private fun showError(message: String?) {
        Toast.makeText(requireContext(), message ?: getString(R.string.unknown_error), Toast.LENGTH_SHORT).show()
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

            if(beat.text != null) {
                if (beat.text != "" && !beat.text.endsWith(" ") && !beat.text.endsWith("\n") && !isLast) {
                    val updatedBeat = beat.copy(text = beat.text + "-")
                    val nextOriginal = sortedBeats[i + 1]
                    nextBeat = nextOriginal.copy(text = "-" + nextOriginal.text)

                    currentGroup.add(updatedBeat)
                } else {
                    currentGroup.add(beat)
                }

                //Todo: змінити тут константу на залежний від ширини екрану розмір
                if (beat.text.endsWith("\n") || currentGroup.sumOf { it.text?.length ?: 0 } > 40) {
                    result.add(currentGroup.toList())
                    currentGroup.clear()
                }
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