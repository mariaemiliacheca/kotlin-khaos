package com.kotlinkhaos.classes.quiz

import com.google.firebase.FirebaseNetworkException
import com.kotlinkhaos.classes.errors.InstructorQuizCreationError
import com.kotlinkhaos.classes.errors.InstructorQuizNetworkError
import com.kotlinkhaos.classes.services.InstructorQuizCreateReq
import com.kotlinkhaos.classes.services.InstructorQuizsForCourseRes
import com.kotlinkhaos.classes.services.KotlinKhaosQuizInstructorApi
import com.kotlinkhaos.classes.user.User

class InstructorQuiz private constructor(
    private val id: String,
    private var name: String,
    private val questionLimit: Int,
    private var questions: MutableList<String>
) {
    companion object {
        private fun validateQuizCreateOptions(quizCreateOptions: InstructorQuizCreateReq.Options) {
            if (quizCreateOptions.name.isEmpty() || quizCreateOptions.prompt.isEmpty()) {
                throw InstructorQuizCreationError("Name and prompt must not be empty")
            }
        }

        suspend fun createQuiz(
            quizCreateOptions: InstructorQuizCreateReq.Options
        ): InstructorQuiz {
            try {
                validateQuizCreateOptions(quizCreateOptions)
                val token = User.getJwt()
                val kotlinKhaosApi = KotlinKhaosQuizInstructorApi(token)
                val res = kotlinKhaosApi.createQuiz(quizCreateOptions)
                val questions = mutableListOf(res.firstQuestion)
                return InstructorQuiz(
                    res.quizId,
                    quizCreateOptions.name,
                    quizCreateOptions.questionLimit,
                    questions
                )
            } catch (err: Exception) {
                if (err is FirebaseNetworkException) {
                    throw InstructorQuizNetworkError()
                }
                throw err
            }
        }

        suspend fun getQuizsForCourse(): List<InstructorQuizsForCourseRes.InstructorQuizDetailsRes> {
            try {
                val kotlinKhaosApi = KotlinKhaosQuizInstructorApi(User.getJwt())
                return kotlinKhaosApi.getCourseQuizsForInstructor().quizs
            } catch (err: Exception) {
                if (err is FirebaseNetworkException) {
                    throw InstructorQuizNetworkError()
                }
                throw err
            }
        }

        suspend fun finish(quizId: String) {
            try {
                val token = User.getJwt()
                val kotlinKhaosApi = KotlinKhaosQuizInstructorApi(token)
                kotlinKhaosApi.finishQuiz(quizId)
            } catch (err: Exception) {
                if (err is FirebaseNetworkException) {
                    throw InstructorQuizNetworkError()
                }
                throw err
            }
        }
    }

    fun getQuizId(): String {
        return this.id;
    }

    fun getName(): String {
        return this.name;
    }

    fun getQuestionLimit(): Int {
        return this.questionLimit;
    }

    private fun setQuestions(questions: List<String>) {
        this.questions = questions.toMutableList()
    }

    fun getQuestions(): List<String> {
        return this.questions;
    }

    private fun appendQuestion(question: String) {
        this.questions.add(question)
    }

    suspend fun nextQuestion() {
        try {
            val token = User.getJwt()
            val kotlinKhaosApi = KotlinKhaosQuizInstructorApi(token)
            val res = kotlinKhaosApi.nextQuestion(this.getQuizId())
            appendQuestion(res.question)
        } catch (err: Exception) {
            if (err is FirebaseNetworkException) {
                throw InstructorQuizNetworkError()
            }
            throw err
        }
    }

    suspend fun editQuestions(questions: List<String>) {
        try {
            val token = User.getJwt()
            val kotlinKhaosApi = KotlinKhaosQuizInstructorApi(token)
            kotlinKhaosApi.editQuestions(this.getQuizId(), questions)
            setQuestions(questions)
        } catch (err: Exception) {
            if (err is FirebaseNetworkException) {
                throw InstructorQuizNetworkError()
            }
            throw err
        }
    }

    suspend fun start() {
        try {
            val token = User.getJwt()
            val kotlinKhaosApi = KotlinKhaosQuizInstructorApi(token)
            kotlinKhaosApi.startQuiz(this.getQuizId())
        } catch (err: Exception) {
            if (err is FirebaseNetworkException) {
                throw InstructorQuizNetworkError()
            }
            throw err
        }
    }
}