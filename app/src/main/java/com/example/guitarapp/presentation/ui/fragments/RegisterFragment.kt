package com.example.guitarapp.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.guitarapp.R
import com.example.guitarapp.databinding.FragmentRegisterBinding
import com.example.guitarapp.utils.Resource
import com.example.guitarapp.view_model.RegisterViewModel
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels()
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSpinner(view)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val skillLevel = binding.spinnerSkill.selectedItem.toString()
                .uppercase(Locale.ROOT)
            val instrument = binding.etInstrument.text.toString()
            val bio = binding.etBio.text.toString()

            viewModel.register(username, email, password, skillLevel, instrument, bio)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.registerState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> {
                        // показати лоадер
                    }
                    is Resource.Success -> {
                        Toast.makeText(requireContext(), "Registration success!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), state.message ?: "Unknown error", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setupSpinner(view: View) {
        val spinnerSkill = view.findViewById<Spinner>(R.id.spinnerSkill)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.skill_levels_array,
            R.layout.dropdown_menu_popup_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSkill.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}