package com.kotlinkhaos.ui.instructor.home.quizCreation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.errors.InstructorQuizError
import com.kotlinkhaos.classes.services.InstructorQuizCreateReq
import com.kotlinkhaos.databinding.FragmentInstructorCreateQuizBinding

class InstructorCreateQuizFragment : Fragment() {
    private var _binding: FragmentInstructorCreateQuizBinding? = null
    private val quizCreationViewModel: QuizCreationViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructorCreateQuizBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.generateQuizButton.setOnClickListener {
            handleGenerateQuiz()
        }

        quizCreationViewModel.resetQuiz()

        quizCreationViewModel.practiceQuiz.observe(viewLifecycleOwner) { quiz ->
            if (quiz != null) {
                navigateToQuizPreview()
            }
        }

        quizCreationViewModel.quizError.observe(viewLifecycleOwner) { err ->
            if (err != null) {
                setLoadingState(false)
                if (err is FirebaseAuthError || err is InstructorQuizError) {
                    binding.errorMessage.text = err.message
                    return@observe
                }
                throw err
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleGenerateQuiz() {
        setLoadingState(true)
        val quizName = binding.inputQuizName.text.toString().trim()
        val quizPrompt = binding.inputQuizPrompt.text.toString().trim()
        val quizQuestionLimit = binding.quizQuestionLimitSlider.value.toInt()
        val quizCreateOptions =
            InstructorQuizCreateReq.Options(quizName, quizQuestionLimit, quizPrompt)
        quizCreationViewModel.createNewQuiz(quizCreateOptions)
    }

    private fun setLoadingState(loading: Boolean) {
        if (isAdded) {
            binding.loadingQuizCreationProgress.visibility =
                if (loading) View.VISIBLE else View.GONE
            binding.generateQuizButton.isEnabled = !loading
        }
    }

    private fun navigateToQuizPreview() {
        val action = InstructorCreateQuizFragmentDirections.startNavigationStartQuizPreview()
        findNavController().navigate(action)
    }
}