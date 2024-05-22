package com.kotlinkhaos.ui.student.home.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.kotlinkhaos.R
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.errors.StudentQuizError
import com.kotlinkhaos.classes.quiz.StudentQuizAttempt
import com.kotlinkhaos.databinding.FragmentStudentQuizAttemptBinding
import kotlinx.coroutines.launch

class StudentQuizAttemptFragment : Fragment() {
    private var _binding: FragmentStudentQuizAttemptBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var studentQuizAttempt: StudentQuizAttempt

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentQuizAttemptBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Use Safe Args to get the passed argument
        val args: StudentQuizAttemptFragmentArgs by navArgs()

        startQuizAttempt(args.quizId)
        binding.buttonNextQuestion.setOnClickListener {
            handleNextQuestion()
        }
        binding.buttonGoBackHome.setOnClickListener {
            handleGoBackHome()
        }

        return root
    }

    private fun handleNextQuestion() {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val userAnswer =
                    binding.quizAnswer.text.toString().trim()
                studentQuizAttempt.addUserAnswer(userAnswer)

                if (studentQuizAttempt.isFinished()) {
                    // If the student has finished answering questions, submit the attempt
                    studentQuizAttempt.submitAttempt()
                    binding.quizQuestion.text = getString(
                        R.string.quiz_question_number_final_score,
                        studentQuizAttempt.getFinalScore()
                    )
                    binding.quizQuestionNumber.text =
                        getString(R.string.quiz_final_score)
                    binding.buttonNextQuestion.visibility = View.GONE
                    binding.layoutQuizAnswer.visibility = View.GONE
                    binding.buttonGoBackHome.visibility = View.VISIBLE
                    return@launch
                }
                binding.quizAnswer.text?.clear()
                binding.quizQuestion.text = studentQuizAttempt.getCurrentQuestion()
                binding.quizQuestionNumber.text =
                    getString(
                        R.string.quiz_question_number,
                        studentQuizAttempt.getCurrentQuestionNumber()
                    )
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

    private fun startQuizAttempt(quizId: String) {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                binding.quizQuestion.text = getString(R.string.question_loading)
                studentQuizAttempt = StudentQuizAttempt.createQuizAttempt(quizId)
                binding.quizQuestion.text = studentQuizAttempt.getCurrentQuestion()
                binding.quizQuestionNumber.text =
                    getString(
                        R.string.quiz_question_number,
                        studentQuizAttempt.getCurrentQuestionNumber()
                    )
                setupActionBarQuizTitle()
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

    private fun setupActionBarQuizTitle() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            context?.getString(
                R.string.quiz_creation_action_bar_title,
                studentQuizAttempt.getQuizName()
            )
    }

    private fun handleGoBackHome() {
        val action = StudentQuizAttemptFragmentDirections.startNavigationGoBackToStudentHome()
        findNavController().navigate(action)
    }

    private fun setLoadingState(loading: Boolean) {
        if (isAdded) {
            if (loading) {
                binding.errorMessage.text = ""
            }
            binding.quizQuestionLoading.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}