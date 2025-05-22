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
import com.example.guitarapp.MainActivity
import com.example.guitarapp.R
import com.example.guitarapp.data.model.UserDto
import com.example.guitarapp.databinding.FragmentProfileEditBinding
import com.example.guitarapp.presentation.ui.profile.ProfileViewModel
import com.example.guitarapp.utils.Resource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileEditBinding
    private var originalUser: UserDto? = null

    private val viewModel: ProfileViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(requireActivity().application) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            originalUser = it.getParcelable("user")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        originalUser?.let {
            populateUserData(it)
        } ?: run {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.profileState.collectLatest { state ->
                when (state) {
                    is Resource.Loading -> showLoading(true)
                    is Resource.Success -> handleUpdated(state.data)
                    is Resource.NotAuthenticated -> handleAuthenticationError()
                    is Resource.Error -> showUpdateError(state.message)
                    else -> {}
                }
            }
        }
    }

    private fun populateUserData(user: UserDto) {
        with(user) {
            binding.etUsername.setText(username)
            binding.etEmail.setText(email)
            binding.etSkill.setText(skill)
            binding.etInstrument.setText(instrument)
            binding.etBio.setText(bio)
        }
    }

    private fun updateUserProfile() {
        originalUser?.let { user ->
            val updatedUser = user.copy(
                username = binding.etUsername.text.toString(),
                email = binding.etEmail.text.toString(),
                skill = binding.etSkill.text.toString(),
                instrument = binding.etInstrument.text.toString(),
                bio = binding.etBio.text.toString()
            )

            viewModel.updateUserProfile(user.id, updatedUser)
        }
    }

    private fun handleUpdated(updatedUser: UserDto?) {
        showLoading(false)
        if (updatedUser != null) {
            findNavController().popBackStack()
        } else {
            // Handle update failure
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = getString(R.string.profile_edit_fail)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
    }

    private fun showUpdateError(message: String?) {
        showLoading(false)
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = message ?: getString(R.string.unknown_error)
    }

    private fun handleAuthenticationError() {
        startLoginActivity()
        Toast.makeText(requireContext(), getString(R.string.session_expired), Toast.LENGTH_LONG).show()
    }

    private fun startLoginActivity(){
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}