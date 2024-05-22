package com.kotlinkhaos.ui.instructor.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlinkhaos.classes.quiz.InstructorQuiz
import com.kotlinkhaos.classes.services.InstructorQuizsForCourseRes
import kotlinx.coroutines.launch

class QuizsForCourseViewModel : ViewModel() {
    private var _quizs =
        MutableLiveData<List<InstructorQuizsForCourseRes.InstructorQuizDetailsRes>?>()
    val quizs: LiveData<List<InstructorQuizsForCourseRes.InstructorQuizDetailsRes>?> = _quizs

    private var _courseQuizListError = MutableLiveData<Exception?>()
    val courseQuizListError: LiveData<Exception?> = _courseQuizListError

    fun loadQuizList() {
        _quizs.value = null
        _courseQuizListError.value = null

        viewModelScope.launch {
            try {
                _quizs.value = InstructorQuiz.getQuizsForCourse()
            } catch (e: Exception) {
                _courseQuizListError.value = e
            }
        }
    }
}
