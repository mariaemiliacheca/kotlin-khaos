package com.kotlinkhaos.ui.student.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kotlinkhaos.databinding.FragmentStudentPracticeBinding

class StudentPracticeFragment : Fragment() {
    private var _binding: FragmentStudentPracticeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentPracticeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.studentPracticeButton.setOnClickListener {
            studentPracticeAttemptsNav()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun studentPracticeAttemptsNav() {
        val prompt = binding.inputQuizPrompt.text.toString().trim()
        val action =
            StudentPracticeFragmentDirections.startNavigationPracticeAttempt(prompt)
        findNavController().navigate(action)
    }

}