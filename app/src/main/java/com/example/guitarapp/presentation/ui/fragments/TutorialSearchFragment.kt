package com.example.guitarapp.presentation.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guitarapp.MainActivity
import com.example.guitarapp.R
import com.example.guitarapp.data.model.SongTutorialShort
import com.example.guitarapp.databinding.FragmentTutorialSearchBinding
import com.example.guitarapp.presentation.ui.adapters.TutorialShortAdapter
import com.example.guitarapp.view_model.PersonalLibraryViewModel
import com.example.guitarapp.utils.Resource
import com.example.guitarapp.utils.SessionManager
import com.example.guitarapp.view_model.TutorialSearchViewModel
import kotlinx.coroutines.flow.collectLatest

class TutorialSearchFragment : Fragment() {
    private val viewModel: TutorialSearchViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TutorialSearchViewModel(requireActivity().application) as T
            }
        }
    }
    private val personalLibraryViewModel: PersonalLibraryViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PersonalLibraryViewModel(requireActivity().application) as T
            }
        }
    }
    private var _binding: FragmentTutorialSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TutorialShortAdapter
    private lateinit var personalLibraryAdapter: TutorialShortAdapter
    private lateinit var popularTutorialsAdapter: TutorialShortAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupSearch()
        observeTutorials()
        observePersonalLibraries()

        personalLibraryViewModel.fetchPersonalLibrary(userId = SessionManager.getUserId(requireContext()))
        viewModel.fetchSongTutorials()
    }

    private fun setupRecyclerViews() {
        fun createAdapterAndSetToRecyclerView(recyclerView: RecyclerView): TutorialShortAdapter {
            return TutorialShortAdapter { tutorial ->
                navigateToTutorial(tutorial)
            }.also {
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = it
                }
            }
        }

        adapter = createAdapterAndSetToRecyclerView(binding.rvSearchResults)
        personalLibraryAdapter = createAdapterAndSetToRecyclerView(binding.rvPersonalLibrary)
        popularTutorialsAdapter = createAdapterAndSetToRecyclerView(binding.rvTutorials)
    }

    private fun navigateToTutorial(tutorial: SongTutorialShort) {
        val args = Bundle().apply {
            putInt("tutorialId", tutorial.id)
        }
        findNavController().navigate(
            R.id.action_tutorialSearchFragment_to_tutorialFragment,
            args
        )
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.fetchSongTutorials(songTitle = it)
                    showSearchResults(true)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    viewModel.fetchSongTutorials()
                    showSearchResults(false)
                }
                return false
            }
        })
    }

    private fun showSearchResults(show: Boolean) {
        if (show) {
            binding.rvSearchResults.visibility = View.VISIBLE
            binding.popularViewContainer.visibility = View.GONE
            binding.libraryViewContainer.visibility = View.GONE
        } else {
            binding.rvSearchResults.visibility = View.GONE
            binding.popularViewContainer.visibility = View.VISIBLE
            binding.libraryViewContainer.visibility = View.VISIBLE
        }
    }

    private fun observeTutorials() {
        lifecycleScope.launchWhenStarted {
            viewModel.tutorialPageState.collectLatest { state ->
                when (state) {
                    is Resource.Success -> {
                        state.data.let { tutorials ->
                            if (binding.searchView.query.isNotEmpty()) {
                                adapter.submitList(tutorials)
                            } else {
                                popularTutorialsAdapter.submitList(tutorials)
                            }
                        }
                    }
                    is Resource.NotAuthenticated -> {
                        startLoginActivity()
                        Toast.makeText(requireContext(), "Your session has expired.", Toast.LENGTH_LONG).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), state.message ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observePersonalLibraries() {
        lifecycleScope.launchWhenStarted {
            personalLibraryViewModel.personalLibraryPageState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        // Show loader if needed
                    }
                    is Resource.Success -> {
                        state.data.let { personalLibraries ->
                            if (personalLibraries.isNotEmpty()) {
                                personalLibraryAdapter.submitList(personalLibraries.map { it.songTutorial })
                                binding.tvPersonalLibraryTitle.visibility = View.VISIBLE
                                binding.libraryViewContainer.visibility = View.VISIBLE
                            } else {
                                binding.tvPersonalLibraryTitle.visibility = View.GONE
                                binding.libraryViewContainer.visibility = View.GONE
                            }
                        }
                    }
                    is Resource.NotAuthenticated -> {
                        startLoginActivity()
                        Toast.makeText(requireContext(), "Your session has expired.", Toast.LENGTH_LONG).show()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), state.message ?: "Error", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
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
}