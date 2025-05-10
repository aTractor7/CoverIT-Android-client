package com.example.guitarapp.presentation.ui.song

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.guitarapp.MainActivity
import com.example.guitarapp.R
import com.example.guitarapp.data.model.ArtistShort
import com.example.guitarapp.data.model.Song
import com.example.guitarapp.data.model.SongGenre
import com.example.guitarapp.databinding.FragmentSongCreateBinding
import com.example.guitarapp.presentation.ui.artist.ArtistViewModel
import com.example.guitarapp.presentation.ui.artist.ArtistsSearchAdapter
import com.example.guitarapp.utils.Resource
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.flow.collectLatest

class SongCreateFragment : Fragment() {
    private val viewModel: SongViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SongViewModel(requireActivity().application) as T
            }
        }
    }

    private val artistViewModel: ArtistViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ArtistViewModel(requireActivity().application) as T
            }
        }
    }

    private var _binding: FragmentSongCreateBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedArtistsAdapter: SelectedArtistsAdapter
    private lateinit var artistsSearchAdapter: ArtistsSearchAdapter

    private val selectedArtists = mutableListOf<ArtistShort>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGenreDropdown()
        setupAdapters()
        setupClickListeners()
        observeViewModels()
    }

    private fun setupGenreDropdown() {
        val genres = SongGenre.entries.map { it.displayName }.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, genres)
        binding.actvSongGenre.setAdapter(adapter)
    }

    private fun setupAdapters() {
        selectedArtistsAdapter = SelectedArtistsAdapter(selectedArtists) { artist ->
            val index = selectedArtists.indexOf(artist)
            selectedArtists.remove(artist)
            selectedArtistsAdapter.notifyItemRemoved(index)
        }

        artistsSearchAdapter = ArtistsSearchAdapter { artist ->
            if (!selectedArtists.any { it.id == artist.id }) {
                selectedArtists.add(artist)
                selectedArtistsAdapter.notifyItemInserted(selectedArtists.size - 1)
            }
            clearSearch()
        }

        binding.rvSelectedArtists.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
            adapter = selectedArtistsAdapter
        }

        binding.rvSearchArtists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = artistsSearchAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnCreateSong.setOnClickListener {
            val title = binding.etSongTitle.text.toString().trim()
            val genreDisplayName = binding.actvSongGenre.text.toString().trim()
            val releaseDate = binding.etReleaseDate.text.toString().trim()

            if (title.isEmpty()) {
                binding.etSongTitle.error = getString(R.string.song_title_required)
                return@setOnClickListener
            }

            val genre = SongGenre.fromDisplayName(genreDisplayName)
            if (genre == null) {
                binding.tilSongGenre.error = getString(R.string.song_genre_required)
                return@setOnClickListener
            } else {
                binding.tilSongGenre.error = null
            }

            if (selectedArtists.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.at_least_one_artist_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val song = Song(
                id = 0,
                title = title,
                genre = genre.name,
                releaseDate = releaseDate,
                creatorId = null,
                songAuthors = selectedArtists.toList()
            )

            viewModel.createSong(song)
        }

        binding.btnSearchArtist.setOnClickListener {
            val query = binding.etArtistSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                artistViewModel.fetchArtists(query)
            }
        }

        binding.tvNoArtistsFound.setOnClickListener {
            findNavController().navigate(R.id.action_songCreateFragment_to_artistCreateFragment)
        }
    }

    private fun observeViewModels() {
        lifecycleScope.launchWhenStarted {
            viewModel.songState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> showLoading(true)
                    is Resource.Success -> handleSongCreated(state.data)
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showError(state.message)
                    else -> {}
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            artistViewModel.artistState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> showArtistSearchLoading(true)
                    is Resource.Success -> handleArtistSearchResults(state.data)
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showArtistSearchError(state.message)
                    else -> {}
                }
            }
        }
    }

    private fun handleSongCreated(songs: List<Any>) {
        showLoading(false)
        Toast.makeText(
            requireContext(),
            getString(R.string.song_created_successfully),
            Toast.LENGTH_SHORT
        ).show()
        requireActivity().onBackPressed()
    }

    private fun handleArtistSearchResults(artists: List<ArtistShort>) {
        showArtistSearchLoading(false)
        if (artists.isEmpty()) {
            binding.tvNoArtistsFound.visibility = View.VISIBLE
            binding.rvSearchArtists.visibility = View.GONE
        } else {
            binding.tvNoArtistsFound.visibility = View.GONE
            binding.rvSearchArtists.visibility = View.VISIBLE
            artistsSearchAdapter.submitList(artists)
        }
    }

    private fun clearSearch() {
        binding.etArtistSearch.text.clear()
        binding.rvSearchArtists.visibility = View.GONE
        binding.tvNoArtistsFound.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreateSong.isEnabled = !isLoading
    }

    private fun showArtistSearchLoading(isLoading: Boolean) {
        binding.pbArtistSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showArtistSearchError(message: String?) {
        showArtistSearchLoading(false)
        Toast.makeText(
            requireContext(),
            message ?: getString(R.string.artist_search_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun handleAuthenticationError() {
        startLoginActivity()
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