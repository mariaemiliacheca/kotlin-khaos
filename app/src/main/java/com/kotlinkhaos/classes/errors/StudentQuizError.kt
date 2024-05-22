package com.kotlinkhaos.classes.errors;

abstract class StudentQuizError(message: String) : Exception(message)

class StudentQuizCreationError(message: String) : StudentQuizError(message)

class StudentQuizNetworkError : StudentQuizError("A network error has occurred.")

class StudentQuizApiError(status: Int, message: String) : StudentQuizError(message)