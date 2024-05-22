package com.kotlinkhaos.classes.practiceQuiz

import com.google.firebase.FirebaseNetworkException
import com.kotlinkhaos.classes.errors.PracticeQuizNetworkError
import com.kotlinkhaos.classes.services.KotlinKhaosPracticeQuizApi
import com.kotlinkhaos.classes.user.User

class PracticeQuiz private constructor(
    private val id: String,
    private var question: String,
    private var currentQuestionNumber: Int,
    private var feedback: String?,
    private var finalScore: Int?,
) {
    companion object {
        suspend fun start(prompt: String): PracticeQuiz {
            try {
                val token = User.getJwt()
                val kotlinKhaosApi = KotlinKhaosPracticeQuizApi(token)
                val res = kotlinKhaosApi.startPracticeQuiz(prompt)
                return PracticeQuiz(
                    res.practiceQuizId,
                    res.problem,
                    currentQuestionNumber = 1,
                    feedback = null,
                    finalScore = null
                )
            } catch (err: Exception) {
                if (err is FirebaseNetworkException) {
                    throw PracticeQuizNetworkError()
                }
                throw err
            }
        }
    }

    fun getId(): String {
        return this.id;
    }

    fun getQuestion(): String {
        return this.question;
    }

    fun getFeedback(): String? {
        return this.feedback
    }

    fun getCurrentQuestionNumber(): Int {
        return this.currentQuestionNumber;
    }

    private fun setFinalScore(finalScore: Int) {
        this.finalScore = finalScore
    }

    fun getFinalScore(): Int? {
        return this.finalScore
    }

    private fun setQuestion(question: String) {
        this.question = question
    }

    private fun incrementQuestionNumber() {
        this.currentQuestionNumber++
    }

    private fun setFeedback(feedback: String) {
        this.feedback = feedback
    }

    suspend fun sendAnswer(answer: String) {
        try {
            val token = User.getJwt()
            val kotlinKhaosApi = KotlinKhaosPracticeQuizApi(token)
            val res = kotlinKhaosApi.sendPracticeQuizAnswer(this.getId(), answer)
            setFeedback(res.feedback)
        } catch (err: Exception) {
            if (err is FirebaseNetworkException) {
                throw PracticeQuizNetworkError()
            }
            throw err
        }
    }

    suspend fun continuePracticeQuiz(): Boolean {
        try {
            val token = User.getJwt()
            val kotlinKhaosApi = KotlinKhaosPracticeQuizApi(token)
            val res = kotlinKhaosApi.continuePracticeQuiz(this.getId())
            if (res.problem != null) {
                setQuestion(res.problem)
                incrementQuestionNumber()
                return true
            }
            if (res.score != null) {
                setFinalScore(res.score)
            }
            return false
        } catch (err: Exception) {
            if (err is FirebaseNetworkException) {
                throw PracticeQuizNetworkError()
            }
            throw err
        }
    }
}