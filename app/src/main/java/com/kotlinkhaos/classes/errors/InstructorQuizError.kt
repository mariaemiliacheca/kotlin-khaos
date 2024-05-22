package com.kotlinkhaos.classes.errors;

abstract class InstructorQuizError(message: String) : Exception(message)

class InstructorQuizCreationError(message: String) : InstructorQuizError(message)

class InstructorQuizDetailsError(message: String) : InstructorQuizError(message)

class InstructorQuizNetworkError : InstructorQuizError("A network error has occurred.")

class InstructorQuizApiError(status: Int, message: String) : InstructorQuizError(message)