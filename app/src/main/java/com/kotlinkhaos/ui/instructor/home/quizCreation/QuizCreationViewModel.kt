package com.kotlinkhaos.ui.instructor.home.quizCreation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinkhaos.classes.quiz.InstructorQuiz
import com.kotlinkhaos.classes.services.InstructorQuizCreateReq
import kotlinx.coroutines.launch

class QuizCreationViewModel : ViewModel() {
    private var _practiceQuiz = MutableLiveData<InstructorQuiz?>()
    val practiceQuiz: LiveData<InstructorQuiz?> = _practiceQuiz

    private var _quizError = MutableLiveData<Exception?>()
    val quizError: LiveData<Exception?> = _quizError

    private var _quizQuestions =
        MutableLiveData<List<String>>(emptyList())
    val quizQuestions: LiveData<List<String>> = _quizQuestions

    private var _quizStarted = MutableLiveData(false)
    val quizStarted: LiveData<Boolean> = _quizStarted

    fun createNewQuiz(options: InstructorQuizCreateReq.Options) {
        viewModelScope.launch {
            try {
                val quiz = InstructorQuiz.createQuiz(options)
                _practiceQuiz.value = quiz
                _quizQuestions.value = quiz.getQuestions()
            } catch (e: Exception) {
                _quizError.value = e
            }
        }
    }

    fun nextQuizQuestion() {
        viewModelScope.launch {
            try {
                val quiz = _practiceQuiz.value
                if (quiz != null) {
                    quiz.nextQuestion()
                    _quizQuestions.value = quiz.getQuestions()
                }
            } catch (e: Exception) {
                _quizError.value = e
            }
        }
    }

    fun startQuiz(currentDataSetQuestions: List<String>) {
        viewModelScope.launch {
            try {
                val quiz = _practiceQuiz.value
                if (quiz != null) {
                    if (currentDataSetQuestions != quiz.getQuestions()) {
                        quiz.editQuestions(currentDataSetQuestions)
                    }
                    quiz.start()
                    _quizStarted.value = true
                }
            } catch (e: Exception) {
                _quizError.value = e
            }
        }
    }

    fun resetQuiz() {
        _practiceQuiz.value = null
        _quizError.value = null
        _quizQuestions.value = emptyList()
        _quizStarted.value = false
    }

    fun clearErrors() {
        _quizError.value = null
    }
}
