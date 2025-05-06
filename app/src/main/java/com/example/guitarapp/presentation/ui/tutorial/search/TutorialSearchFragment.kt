package com.example.guitarapp.presentation.ui.tutorial.search

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guitarapp.MainActivity
import com.example.guitarapp.R
import com.example.guitarapp.databinding.FragmentTutorialSearchBinding
import com.example.guitarapp.presentation.ui.profile.ProfileFragment
import com.example.guitarapp.utils.Resource
import kotlinx.coroutines.flow.collectLatest

class TutorialSearchFragment : Fragment() {
    private val viewModel: TutorialSearchViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return TutorialSearchViewModel(requireActivity().application) as T
            }
        }
    }
    private var _binding: FragmentTutorialSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TutorialShortAdapter

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

        setupRecyclerView()
        setupSearch()
        observeTutorials()

        viewModel.fetchSongTutorials()
    }

    private fun setupRecyclerView() {
        adapter = TutorialShortAdapter { tutorial ->
            val args = Bundle().apply {
                putInt("tutorialId", tutorial.id)
            }
            findNavController().navigate(
                R.id.action_tutorialSearchFragment_to_tutorialFragment,
                args
            )
        }

        binding.rvTutorials.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TutorialSearchFragment.adapter
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.fetchSongTutorials(songTitle = it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Опціонально: реалізація пошуку під час введення
                return false
            }
        })
    }

    private fun observeTutorials() {
        lifecycleScope.launchWhenStarted {
            viewModel.tutorialPageState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        // Показати лоадер, якщо потрібно
                    }
                    is Resource.Success -> {
                        state.data.let { tutorials ->
                            adapter.submitList(tutorials)
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

