package com.kotlinkhaos.ui.instructor.home

import SpaceItemDecorationHeight
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.errors.InstructorQuizError
import com.kotlinkhaos.classes.user.viewmodel.UserAvatarViewModel
import com.kotlinkhaos.databinding.FragmentInstructorHomeBinding

class InstructorHomeFragment : Fragment() {
    private var _binding: FragmentInstructorHomeBinding? = null
    private lateinit var quizsForCourseListAdapter: QuizsForCourseListAdapter
    private val quizsForCourseViewModel: QuizsForCourseViewModel by activityViewModels()
    private val userAvatarViewModel: UserAvatarViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructorHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.addQuizFAB.setOnClickListener {
            handleAddQuiz()
        }
        binding.refreshQuizForCourseList.setOnRefreshListener {
            loadQuizListForCourse()
        }
        quizsForCourseListAdapter = QuizsForCourseListAdapter(emptyList(), { quizId ->
            val action = InstructorHomeFragmentDirections.startNavigationQuizDetails(quizId)
            findNavController().navigate(action)
        }, userAvatarViewModel)
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
        quizsForCourseViewModel.loadQuizList()

        quizsForCourseViewModel.quizs.observe(viewLifecycleOwner) { quizs ->
            if (quizs != null) {
                quizsForCourseListAdapter.updateData(quizs)
                binding.quizsForCourseList.setHasFixedSize(true) // fixed list performance optimization
                setLoadingState(false)
            }
        }

        quizsForCourseViewModel.courseQuizListError.observe(viewLifecycleOwner) { err ->
            if (err != null) {
                setLoadingState(false)
                if (err is FirebaseAuthError || err is InstructorQuizError) {
                    binding.errorMessage.text = err.message
                    return@observe
                }
                throw err
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

    private fun handleAddQuiz() {
        val action = InstructorHomeFragmentDirections.startNavigationCreateQuiz()
        findNavController().navigate(action)
    }
}