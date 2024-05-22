package com.kotlinkhaos.ui.student.practice.practiceQuiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.kotlinkhaos.R
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.errors.PracticeQuizError
import com.kotlinkhaos.classes.practiceQuiz.PracticeQuiz
import com.kotlinkhaos.databinding.FragmentPracticeAttemptBinding
import kotlinx.coroutines.launch

class PracticeAttemptFragment : Fragment() {
    private var _binding: FragmentPracticeAttemptBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var practiceQuiz: PracticeQuiz

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticeAttemptBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Use Safe Args to get the passed argument
        val args: PracticeAttemptFragmentArgs by navArgs()

        startPracticeQuiz(args.prompt)
        //converting the edit text to a string
        binding.buttonGetFeedback.setOnClickListener {
            handleGetFeedback()
        }
        binding.buttonNextQuestion.setOnClickListener {
            handleNextQuestion()
        }

        return root
    }

    private fun handleGetFeedback() {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val userAnswer = binding.practiceQuizAnswer.text.toString().trim()
                practiceQuiz.sendAnswer(userAnswer)
                binding.practiceQuizQuestionNumber.text =
                    getString(
                        R.string.practice_quiz_feedback_for_question,
                        practiceQuiz.getCurrentQuestionNumber()
                    )
                binding.practiceQuizQuestion.text = practiceQuiz.getFeedback()
                binding.buttonGetFeedback.visibility = View.GONE
                binding.buttonNextQuestion.visibility = View.VISIBLE
                binding.layoutPracticeQuizAnswer.visibility = View.GONE
            } catch (err: Exception) {
                if (err is FirebaseAuthError || err is PracticeQuizError) {
                    binding.errorMessage.text = err.message
                    return@launch
                }
                throw err
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun handleNextQuestion() {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val nextQuestion = practiceQuiz.continuePracticeQuiz()
                if (nextQuestion) {
                    binding.practiceQuizQuestion.text = practiceQuiz.getQuestion()
                    binding.practiceQuizQuestionNumber.text =
                        getString(
                            R.string.quiz_question_number,
                            practiceQuiz.getCurrentQuestionNumber()
                        )
                    binding.practiceQuizAnswer.text?.clear()
                    // Hide the Next button until next feedback
                    binding.buttonNextQuestion.visibility = View.GONE
                    binding.buttonGetFeedback.visibility = View.VISIBLE
                    binding.layoutPracticeQuizAnswer.visibility = View.VISIBLE
                } else {
                    // Handle the end of practice quiz scenario
                    binding.practiceQuizQuestion.text = getString(
                        R.string.quiz_question_number_final_score,
                        practiceQuiz.getFinalScore()
                    )
                    binding.practiceQuizQuestionNumber.text =
                        getString(R.string.quiz_final_score)
                    binding.buttonNextQuestion.visibility = View.GONE
                }
            } catch (err: Exception) {
                if (err is FirebaseAuthError || err is PracticeQuizError) {
                    binding.errorMessage.text = err.message
                    return@launch
                }
                throw err
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun startPracticeQuiz(prompt: String) {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                binding.practiceQuizQuestion.text = getString(R.string.question_loading)
                practiceQuiz = PracticeQuiz.start(prompt)
                binding.practiceQuizQuestion.text = practiceQuiz.getQuestion()
            } catch (err: Exception) {
                if (err is FirebaseAuthError || err is PracticeQuizError) {
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
            if (loading) {
                binding.errorMessage.text = ""
            }
            binding.practiceQuizQuestionLoading.visibility =
                if (loading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}