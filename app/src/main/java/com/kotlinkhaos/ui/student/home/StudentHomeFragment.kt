package com.kotlinkhaos.ui.student.home

import SpaceItemDecorationHeight
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.errors.StudentQuizError
import com.kotlinkhaos.classes.quiz.StudentQuizAttempt
import com.kotlinkhaos.databinding.FragmentStudentHomeBinding
import kotlinx.coroutines.launch

class StudentHomeFragment : Fragment() {
    private var _binding: FragmentStudentHomeBinding? = null
    private lateinit var quizsForCourseListAdapter: QuizsForCourseListAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.refreshQuizForCourseList.setOnRefreshListener {
            loadQuizListForCourse()
        }
        quizsForCourseListAdapter = QuizsForCourseListAdapter(emptyList()) { quizId ->
            val action = StudentHomeFragmentDirections.startNavigationStudentQuizAttempt(quizId)
            findNavController().navigate(action)
        }
        binding.quizsForCourseList.adapter = quizsForCourseListAdapter
        binding.quizsForCourseList.layoutManager = LinearLayoutManager(requireContext())
        binding.quizsForCourseList.addItemDecoration(
            SpaceItemDecorationHeight(
                4
            )
        )

        loadQuizListForCourse()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadQuizListForCourse() {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val quizs = StudentQuizAttempt.getQuizsForCourse()
                if (isAdded) {
                    quizsForCourseListAdapter.updateData(quizs)
                    binding.quizsForCourseList.setHasFixedSize(true) // fixed list performance optimization
                }
            } catch (err: Exception) {
                if (err is FirebaseAuthError || err is StudentQuizError) {
                    binding.errorMessage.text = err.message
                    return@launch
                }
                throw err
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(loading: Boolean) {
        if (isAdded) {
            binding.refreshQuizForCourseList.post {
                binding.refreshQuizForCourseList.isRefreshing = loading
            }
        }
    }

}