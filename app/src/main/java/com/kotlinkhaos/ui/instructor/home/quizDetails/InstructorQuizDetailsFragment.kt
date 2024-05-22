package com.kotlinkhaos.ui.instructor.home.quizDetails

import SpaceItemDecorationHeight
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.errors.InstructorQuizDetailsError
import com.kotlinkhaos.classes.errors.InstructorQuizError
import com.kotlinkhaos.classes.quiz.InstructorQuiz
import com.kotlinkhaos.databinding.FragmentInstructorQuizDetailsBinding
import com.kotlinkhaos.ui.instructor.home.QuizsForCourseViewModel
import kotlinx.coroutines.launch

class InstructorQuizDetailsFragment : Fragment() {
    private var _binding: FragmentInstructorQuizDetailsBinding? = null
    private val quizsForCourseViewModel: QuizsForCourseViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructorQuizDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadQuizDetails()

        return root
    }

    private fun loadQuizDetails() {
        try {
            // Use Safe Args to get the passed argument
            val args: InstructorQuizDetailsFragmentArgs by navArgs()

            val quiz = quizsForCourseViewModel.quizs.value?.find { quiz ->
                quiz.id == args.quizId
            } ?: throw InstructorQuizDetailsError("Specified quiz not found")

            binding.quizName.text = quiz.name
            val userAttempts = quiz.finishedUserAttempts.values.toList()

            val userAttemptListAdapter =
                UserAttemptListAdapter(userAttempts) { userId ->
                    val action =
                        InstructorQuizDetailsFragmentDirections.startNavigationViewUserAttempt(
                            quiz.id, userId
                        )
                    findNavController().navigate(action)
                }
            binding.quizAttempts.adapter = userAttemptListAdapter
            binding.quizAttempts.layoutManager = LinearLayoutManager(requireContext())
            binding.quizAttempts.addItemDecoration(
                SpaceItemDecorationHeight(
                    4
                )
            )
            binding.quizAttempts.setHasFixedSize(true) // fixed list performance optimization

            if (!quiz.finished) {
                binding.endQuizButton.visibility = View.VISIBLE
                binding.endQuizButton.setOnClickListener {
                    handleEndQuiz(quiz.id)
                }
            }

        } catch (err: Exception) {
            if (err is FirebaseAuthError || err is InstructorQuizError) {
                binding.errorMessage.text = err.message
            }
            throw err
        }

    }

    private fun handleEndQuiz(quizId: String) {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                InstructorQuiz.finish(quizId)
                binding.endQuizButton.visibility = View.GONE
            } catch (err: Exception) {
                if (err is FirebaseAuthError || err is InstructorQuizError) {
                    binding.errorMessage.text = err.message
                }
                throw err
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(loading: Boolean) {
        if (isAdded) {
            binding.loadingEndQuizProgress.visibility = if (loading) View.VISIBLE else View.GONE
            binding.endQuizButton.isEnabled = !loading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}