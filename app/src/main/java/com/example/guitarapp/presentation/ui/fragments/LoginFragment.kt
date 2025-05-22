package com.example.guitarapp.presentation.ui.fragments

import android.content.Intent
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
import com.example.guitarapp.databinding.FragmentLoginBinding
import com.example.guitarapp.presentation.ui.BaseActivity
import com.example.guitarapp.utils.Resource
import com.example.guitarapp.utils.SessionManager
import com.example.guitarapp.view_model.LoginViewModel
import com.example.guitarapp.view_model.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest

class LoginFragment : Fragment() {

    //TODO: Правильно булоб зробить окрему актівіті для логіну

    private val viewModel: LoginViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(requireActivity().application) as T
            }
        }
    }
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkAuthentication()

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.login(username, password)
        }

        binding.tvCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loginState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        // показати лоадер
                    }
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "Login success!", Toast.LENGTH_SHORT).show()

                        SessionManager.saveSessionId(requireContext(), state.data.first)
                        SessionManager.saveUserId(requireContext(), state.data.second)

                        startBaseActivity()
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), state.message ?: "Unknown error", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun checkAuthentication() {
        val sessionId = SessionManager.getSessionId(requireContext())

        if (sessionId != null && sessionId.isNotBlank()) {
            profileViewModel.fetchAuthenticatedUserProfile()
            lifecycleScope.launchWhenStarted {
                profileViewModel.profileState.collectLatest { state ->
                    when (state) {
                        is Resource.Loading -> {
                            // Показати лоадер
                        }
                        is Resource.Success -> {
                            startBaseActivity()
                        }
                        is Resource.NotAuthenticated -> {
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
    }


    private fun startBaseActivity(){
        val intent = Intent(requireContext(), BaseActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}