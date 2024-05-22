package com.kotlinkhaos.ui.instructor.home.quizDetails.userAttempt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.kotlinkhaos.R
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.errors.InstructorQuizDetailsError
import com.kotlinkhaos.classes.errors.InstructorQuizError
import com.kotlinkhaos.classes.errors.StudentQuizError
import com.kotlinkhaos.classes.quiz.StudentQuizAttempt
import com.kotlinkhaos.classes.services.StudentQuizAttemptRes
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.classes.utils.loadImage
import com.kotlinkhaos.databinding.FragmentInstructorViewUserAttemptBinding
import com.kotlinkhaos.ui.instructor.home.QuizsForCourseViewModel
import kotlinx.coroutines.launch

class InstructorViewUserAttemptFragment : Fragment() {
    private var _binding: FragmentInstructorViewUserAttemptBinding? = null
    private val quizsForCourseViewModel: QuizsForCourseViewModel by activityViewModels()
    private lateinit var studentQuizAttempt: StudentQuizAttemptRes.QuizAttempt
    private var currentQuestion = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentInstructorViewUserAttemptBinding.inflate(inflater, container, false)
        val root: View = binding.root

        loadUserAttempt()
        binding.buttonPrevUserAnswer.setOnClickListener {
            previousQuestion()
        }
        binding.buttonNextUserAnswer.setOnClickListener {
            nextQuestion()
        }

        return root
    }

    private fun loadUserAttempt() {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                // Use Safe Args to get the passed argument
                val args: InstructorViewUserAttemptFragmentArgs by navArgs()

                val quiz = quizsForCourseViewModel.quizs.value?.find { quiz ->
                    quiz.id == args.quizId
                }

                val attempt = quiz?.finishedUserAttempts?.get(args.userId)
                    ?: throw InstructorQuizDetailsError("Specified attempt not found")

                if (attempt.studentAvatarHash != null) {
                    binding.userAttemptAvatarIcon.loadImage(
                        User.getProfilePicture(
                            attempt.studentId,
                            attempt.studentAvatarHash
                        )
                    )
                }

                studentQuizAttempt =
                    StudentQuizAttempt.getStudentQuizAttempt(attempt.attemptId)
                binding.userAttemptQuestionNumber.text = getString(
                    R.string.quiz_question_number,
                    currentQuestion + 1
                )
                binding.userAttemptQuestion.text =
                    studentQuizAttempt.questions[currentQuestion]
                binding.userAttemptAnswer.setText(studentQuizAttempt.answers[currentQuestion])
                setupActionBarUserAttemptTitle(attempt.name)
            } catch (err: Exception) {
                if (err is FirebaseAuthError || err is InstructorQuizError || err is StudentQuizError) {
                    binding.errorMessage.text = err.message
                    return@launch
                }
                throw err
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun previousQuestion() {
        if (currentQuestion > 0) {
            currentQuestion--
        }
        binding.userAttemptQuestionNumber.text = getString(
            R.string.quiz_question_number,
            currentQuestion + 1
        )
        binding.userAttemptQuestion.text =
            studentQuizAttempt.questions[currentQuestion]
        binding.userAttemptAnswer.setText(studentQuizAttempt.answers[currentQuestion])
    }

    private fun nextQuestion() {
        if (currentQuestion < studentQuizAttempt.questions.size - 1) {
            currentQuestion++
        }
        binding.userAttemptQuestionNumber.text = getString(
            R.string.quiz_question_number,
            currentQuestion + 1
        )
        binding.userAttemptQuestion.text =
            studentQuizAttempt.questions[currentQuestion]
        binding.userAttemptAnswer.setText(studentQuizAttempt.answers[currentQuestion])
    }

    private fun setupActionBarUserAttemptTitle(name: String) {
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            context?.getString(
                R.string.user_attempt_action_bar_title,
                name
            )
    }

    private fun setLoadingState(loading: Boolean) {
        if (isAdded) {
            binding.userAttemptQuestionLoading.visibility = if (loading) View.VISIBLE else View.GONE
            binding.buttonNextUserAnswer.visibility = if (loading) View.GONE else View.VISIBLE
            binding.buttonPrevUserAnswer.visibility = if (loading) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}