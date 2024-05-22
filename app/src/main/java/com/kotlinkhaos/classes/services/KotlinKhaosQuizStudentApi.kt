package com.kotlinkhaos.classes.services;

import android.util.Log
import com.kotlinkhaos.classes.errors.KotlinKhaosApiError
import com.kotlinkhaos.classes.errors.StudentQuizApiError
import com.kotlinkhaos.classes.errors.StudentQuizNetworkError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.channels.UnresolvedAddressException

class KotlinKhaosQuizStudentApi(private val token: String) {
    private val apiHost = "https://kotlin-khaos-api.maximoguk.com"
    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
            })
        }
    }

    suspend fun createStudentQuizAttempt(quizId: String): StudentQuizAttemptCreateRes {
        try {
            val res = client.post("$apiHost/student/quizs/$quizId/attempt") {
                addRequiredApiHeaders()
            }
            return parseResponseFromApi(res)
        } catch (err: Exception) {
            Log.e("KotlinKhaosApi", "Error in createStudentQuizAttempt", err)
            if (err is UnresolvedAddressException || err is HttpRequestTimeoutException) {
                throw StudentQuizNetworkError()
            }
            throw err
        } finally {
            client.close()
        }
    }

    suspend fun getStudentQuizAttempt(
        quizAttemptId: String,
    ): StudentQuizAttemptRes {
        try {
            val res = client.get("$apiHost/student/quiz-attempts/$quizAttemptId") {
                addRequiredApiHeaders()
            }
            return parseResponseFromApi(res)
        } catch (err: Exception) {
            Log.e("KotlinKhaosApi", "Error in getStudentQuizAttempt", err)
            if (err is UnresolvedAddressException || err is HttpRequestTimeoutException) {
                throw StudentQuizNetworkError()
            }
            throw err
        } finally {
            client.close()
        }
    }

    suspend fun submitStudentQuizAttempt(
        quizAttemptId: String,
        answers: List<String>
    ): StudentQuizAttemptSubmitRes {
        try {
            val res = client.post("$apiHost/student/quiz-attempts/$quizAttemptId/submit") {
                setBody(StudentQuizAttemptSubmitReq(answers))
                addRequiredApiHeaders()
            }
            return parseResponseFromApi(res)
        } catch (err: Exception) {
            Log.e("KotlinKhaosApi", "Error in finishStudentQuizAttempt", err)
            if (err is UnresolvedAddressException || err is HttpRequestTimeoutException) {
                throw StudentQuizNetworkError()
            }
            throw err
        } finally {
            client.close()
        }
    }

    suspend fun getCourseQuizsForStudent(): StudentQuizsForCourseRes {
        try {
            val res = client.get("$apiHost/student/course/quizs") {
                addRequiredApiHeaders()
            }
            return parseResponseFromApi(res)
        } catch (err: Exception) {
            Log.e("KotlinKhaosApi", "Error in getCourseQuizsForStudent", err)
            if (err is UnresolvedAddressException || err is HttpRequestTimeoutException) {
                throw StudentQuizNetworkError()
            }
            throw err
        } finally {
            client.close()
        }
    }

    suspend fun getWeeklySummaryForStudent(): StudentWeeklySummaryRes {
        try {
            val res = client.get("$apiHost/student/course/quizs/weekly-summary") {
                addRequiredApiHeaders()
            }
            return parseResponseFromApi(res)
        } catch (err: Exception) {
            Log.e("KotlinKhaosApi", "Error in getCourseQuizsForStudent", err)
            if (err is UnresolvedAddressException || err is HttpRequestTimeoutException) {
                throw StudentQuizNetworkError()
            }
            throw err
        } finally {
            client.close()
        }
    }

    private fun HttpRequestBuilder.addRequiredApiHeaders() {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }

    private suspend inline fun <reified T> parseResponseFromApi(res: HttpResponse): T {
        if (res.status.isSuccess()) {
            return res.body()
        }
        val apiError: KotlinKhaosApiError = res.body()
        throw StudentQuizApiError(apiError.status, apiError.error)
    }
}

@Serializable
data class StudentQuizAttemptCreateRes(
    val quizName: String,
    val quizAttemptId: String,
    val questions: List<String>
)

@Serializable
data class StudentQuizAttemptRes(val quizAttempt: QuizAttempt) {
    @Serializable
    data class QuizAttempt(
        val answers: List<String>,
        val questions: List<String>,
        val submitted: Boolean
    )
}

@Serializable
data class StudentQuizAttemptSubmitReq(val answers: List<String>)

@Serializable
data class StudentQuizAttemptSubmitRes(val score: Int)

@Serializable
data class StudentQuizsForCourseRes(
    val quizs: List<StudentQuizDetailsRes>
) {
    @Serializable
    data class StudentQuizDetailsRes(
        val id: String,
        val authorId: String,
        val authorAvatarHash: String? = null,
        val name: String,
        val started: Boolean,
        val finished: Boolean,
        val usersAttempt: UserAttempt? = null
    ) {
        @Serializable
        data class UserAttempt(
            val attemptId: String,
            val studentId: String,
            val score: Int,
            val submittedOn: Instant
        )
    }
}

@Serializable
data class StudentWeeklySummaryRes(
    val weeklySummary: WeeklySummary
) {
    @Serializable
    data class WeeklySummary(
        val sun: DaySummary? = null,
        val mon: DaySummary? = null,
        val tues: DaySummary? = null,
        val wed: DaySummary? = null,
        val thurs: DaySummary? = null,
        val fri: DaySummary? = null,
        val sat: DaySummary? = null
    )

    @Serializable
    data class DaySummary(
        val averageScore: Float,
        val quizs: List<QuizAttemptIdAndScore>
    )

    @Serializable
    data class QuizAttemptIdAndScore(
        val quizAttemptId: String,
        val score: Int
    )
}