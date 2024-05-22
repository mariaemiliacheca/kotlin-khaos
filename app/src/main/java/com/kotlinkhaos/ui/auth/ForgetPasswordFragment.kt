package com.kotlinkhaos.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.databinding.FragmentForgetPasswordBinding
import kotlinx.coroutines.launch


class ForgetPasswordFragment : Fragment() {
    private var _binding: FragmentForgetPasswordBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.forgetPasswordButton.setOnClickListener {
            handleForgetPassword()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleForgetPassword() {
        lifecycleScope.launch {
            try {
                val email = binding.inputEmailAddress.text.toString().trim()
                User.sendForgotPasswordEmail(email)
                Toast.makeText(
                    requireContext(),
                    "Password has been reseted successfully! Please check your email.",
                    Toast.LENGTH_SHORT
                ).show()
                handleGoToLogin()
            } catch (err: Exception) {
                if (err is FirebaseAuthError) {
                    binding.errorMessage.text = err.message
                    return@launch
                }
                throw err
            }
        }

    }

    private fun handleGoToLogin() {
        val action = ForgetPasswordFragmentDirections.actionGoToLogin()
        findNavController().navigate(action)
    }
}