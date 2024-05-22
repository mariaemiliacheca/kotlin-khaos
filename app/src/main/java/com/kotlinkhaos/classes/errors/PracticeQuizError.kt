package com.kotlinkhaos.classes.errors;

abstract class PracticeQuizError(message: String) : Exception(message)

class PracticeQuizNetworkError : PracticeQuizError("A network error has occurred.")

class PracticeQuizApiError(status: Int, message: String) : PracticeQuizError(message)