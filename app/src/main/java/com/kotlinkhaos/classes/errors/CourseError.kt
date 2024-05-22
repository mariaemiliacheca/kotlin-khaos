package com.kotlinkhaos.classes.errors;

abstract class CourseError(message: String) : Exception(message)

class CourseCreationError(message: String) : CourseError(message)

class CourseDbError(message: String) : CourseError(message)

class CourseJoinError(message: String) : CourseError(message)