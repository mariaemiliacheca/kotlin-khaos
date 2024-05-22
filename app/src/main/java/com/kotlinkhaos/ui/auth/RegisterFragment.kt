package com.kotlinkhaos.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kotlinkhaos.MainActivity
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.classes.user.UserType
import com.kotlinkhaos.classes.user.viewmodel.UserStore
import com.kotlinkhaos.classes.user.viewmodel.UserViewModel
import com.kotlinkhaos.classes.user.viewmodel.UserViewModelFactory
import com.kotlinkhaos.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
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
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.registerButton.setOnClickListener {
            handleRegister()
        }
        return root
    }

    private fun handleRegister() {
        lifecycleScope.launch {
            try {
                val name = binding.inputName.text.toString().trim()
                val email = binding.inputEmailAddress.text.toString().trim()
                val password = binding.inputPassword.text.toString().trim()

                val userType = if (!binding.switchChoice.isChecked) {
                    UserType.STUDENT
                } else {
                    UserType.INSTRUCTOR
                }
                val user = User.register(userViewModel, email, password, name, userType)
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
                throw err
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}