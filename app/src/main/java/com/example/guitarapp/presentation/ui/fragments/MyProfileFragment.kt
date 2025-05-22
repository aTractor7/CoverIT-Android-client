package com.example.guitarapp.presentation.ui.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.guitarapp.MainActivity
import com.example.guitarapp.R
import com.example.guitarapp.data.model.UserDto
import com.example.guitarapp.databinding.FragmentProfileBinding
import com.example.guitarapp.utils.Resource
import com.example.guitarapp.view_model.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlin.getValue

class MyProfileFragment: Fragment() {
    private val viewModel: ProfileViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(requireActivity().application) as T
            }
        }
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var currentUser: UserDto? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.fetchAuthenticatedUserProfile()

        val tvEdit = binding.tvEditProfile
        tvEdit.setOnClickListener {
            currentUser?.let { user ->
                findNavController().navigate(
                    R.id.action_myProfileFragment_to_editProfileFragment,
                    bundleOf("user" to user)
                )
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.profileState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        // Показати лоадер
                    }
                    is Resource.Success -> {
                        val user = state.data
                        currentUser = user
                        showUserData(user)
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

    private fun showUserData(user: UserDto) {
        binding.tvUsername.text = user.username
        binding.tvEmail.text = user.email
        binding.tvJoinDate.text = user.joinDate
        binding.tvRole.text = user.role
        binding.tvSkill.text = user.skill
        binding.tvInstrument.text = user.instrument ?: ""
        binding.tvBio.text = user.bio ?: ""

        user.profileImg?.let { imgBytes ->
            val bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.size)
            binding.ivProfileImg.setImageBitmap(bmp)
        }

        binding.tvEditProfile.visibility = View.VISIBLE
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