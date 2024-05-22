package com.kotlinkhaos.ui.instructor.home.quizCreation

import SpaceItemDecorationBottom
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlinkhaos.R
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.errors.InstructorQuizError
import com.kotlinkhaos.databinding.FragmentInstructorStartQuizPreviewBinding

class InstructorStartQuizPreviewFragment : Fragment() {
    private var _binding: FragmentInstructorStartQuizPreviewBinding? = null
    private val quizCreationViewModel: QuizCreationViewModel by activityViewModels()
    private lateinit var quizQuestionsListAdapter: QuizQuestionsListAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructorStartQuizPreviewBinding.inflate(inflater, container, false)
        setLoadingState(true)
        val root: View = binding.root

        quizCreationViewModel.clearErrors()
        quizQuestionsListAdapter = QuizQuestionsListAdapter(emptyList<String>().toMutableList())
        binding.quizQuestionsList.layoutManager = LinearLayoutManager(requireContext())
        binding.quizQuestionsList.adapter = quizQuestionsListAdapter

        quizCreationViewModel.quizQuestions.observe(viewLifecycleOwner) { quizQuestions ->
            val quiz = quizCreationViewModel.practiceQuiz.value
            if (quiz !== null && quizQuestions.isNotEmpty()) {
                setLoadingState(false)
                if (quizQuestions.size != quiz.getQuestionLimit()) {
                    binding.startQuizButton.text = getString(R.string.next_question_button)
                    binding.startQuizButton.setOnClickListener {
                        handleNextQuizQuestion()
                    }
                } else {
                    binding.startQuizButton.text = getString(R.string.start_quiz_button)
                    binding.startQuizButton.setOnClickListener {
                        handleStartQuiz()
                    }
                }
                quizQuestionsListAdapter.appendToDataSet(quizQuestions.last())
                binding.quizQuestionsList.addItemDecoration(
                    SpaceItemDecorationBottom(
                        16
                    )
                )
            }
        }

        quizCreationViewModel.quizError.observe(viewLifecycleOwner) { err ->
            if (err != null) {
                if (err is FirebaseAuthError || err is InstructorQuizError) {
                    setLoadingState(false)
                    binding.errorMessage.text = err.message
                    return@observe
                }
                throw err
            }
        }

        quizCreationViewModel.quizStarted.observe(viewLifecycleOwner) { started ->
            if (started) {
                navigateBackToHome()
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val quiz = quizCreationViewModel.practiceQuiz.value
        if (quiz !== null) {
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                context?.getString(
                    R.string.quiz_creation_action_bar_title,
                    quiz.getName()
                )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleNextQuizQuestion() {
        setLoadingState(true)
        quizCreationViewModel.nextQuizQuestion()
    }

    private fun handleStartQuiz() {
        setLoadingState(true)
        val currentQuestions = quizQuestionsListAdapter.getDataSet()
        quizCreationViewModel.startQuiz(currentQuestions)
    }

    private fun setLoadingState(loading: Boolean) {
        if (isAdded) {
            binding.loadingQuizPreviewProgress.visibility = if (loading) View.VISIBLE else View.GONE
            binding.startQuizButton.isEnabled = !loading
        }
    }

    private fun navigateBackToHome() {
        val action =
            InstructorStartQuizPreviewFragmentDirections.startNavigationGoBackToInstructorHome()
        findNavController().navigate(action)
    }
}