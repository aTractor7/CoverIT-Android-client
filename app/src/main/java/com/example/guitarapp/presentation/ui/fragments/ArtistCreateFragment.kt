package com.example.guitarapp.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.guitarapp.R
import com.example.guitarapp.data.model.Artist
import com.example.guitarapp.databinding.FragmentArtistCreateBinding
import com.example.guitarapp.utils.Resource
import com.example.guitarapp.view_model.ArtistViewModel
import kotlinx.coroutines.flow.collectLatest

class ArtistCreateFragment : Fragment() {
    private val viewModel: ArtistViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ArtistViewModel(requireActivity().application) as T
            }
        }
    }

    private var _binding: FragmentArtistCreateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeArtistState()
    }

    private fun setupClickListeners() {
        binding.btnCreateArtist.setOnClickListener {
            val name = binding.etArtistName.text.toString().trim()
            val bio = binding.etArtistBio.text.toString().trim()

            if (name.isEmpty()) {
                binding.etArtistName.error = getString(R.string.artist_name_required)
                return@setOnClickListener
            }

            val artist = Artist(
                id = 0,
                name = name,
                bio = if (bio.isEmpty()) null else bio
            )

            viewModel.createArtist(artist)
        }
    }

    private fun observeArtistState() {
        lifecycleScope.launchWhenStarted {
            viewModel.artistState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> showLoading(true)
                    is Resource.Success -> handleArtistCreated(state.data)
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showError(state.message)
                    else -> {}
                }
            }
        }
    }

    private fun handleArtistCreated(artists: List<Any>) {
        showLoading(false)
        Toast.makeText(
            requireContext(),
            getString(R.string.artist_created_successfully),
            Toast.LENGTH_SHORT
        ).show()
        findNavController().popBackStack()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreateArtist.isEnabled = !isLoading
    }

    private fun handleAuthenticationError() {
        Toast.makeText(
            requireContext(),
            getString(R.string.session_expired),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showError(message: String?) {
        showLoading(false)
        Toast.makeText(
            requireContext(),
            message ?: getString(R.string.unknown_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}