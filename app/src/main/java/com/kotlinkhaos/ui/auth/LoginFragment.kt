package com.kotlinkhaos.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kotlinkhaos.MainActivity
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.classes.user.viewmodel.UserStore
import com.kotlinkhaos.classes.user.viewmodel.UserViewModel
import com.kotlinkhaos.classes.user.viewmodel.UserViewModelFactory
import com.kotlinkhaos.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserStore(requireContext()))
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.loginButton.setOnClickListener {
            handleLogin()
        }
        binding.goToRegister.setOnClickListener {
            handleGoToRegister()
        }
        binding.goToForgetPassword.setOnClickListener {
            handleGoToForgetPassword()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleLogin() {
        lifecycleScope.launch {
            try {
                val email = binding.inputEmailAddress.text.toString().trim()
                val password = binding.inputPassword.text.toString().trim()
                val user = User.login(userViewModel, email, password)
                if (user != null) {
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            } catch (err: Exception) {
                if (err is FirebaseAuthError) {
                    binding.errorMessage.text = err.message
                    return@launch
                }
                throw err;
            }

        }
    }

    private fun handleGoToRegister() {
        val action = LoginFragmentDirections.actionGoToRegister()
        findNavController().navigate(action)
    }

    private fun handleGoToForgetPassword() {
        val action = LoginFragmentDirections.actionGoToForgetPassword()
        findNavController().navigate(action)
    }
}