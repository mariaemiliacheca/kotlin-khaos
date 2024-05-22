package com.kotlinkhaos.classes.course

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.kotlinkhaos.classes.errors.CourseCreationError
import com.kotlinkhaos.classes.errors.CourseDbError
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.user.InstructorNameCourseIndex
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.classes.user.UserDetails
import com.kotlinkhaos.classes.user.UserType
import com.kotlinkhaos.classes.user.viewmodel.UserViewModel
import kotlinx.coroutines.tasks.await
import java.util.UUID

enum class EducationLevelType {
    UNIVERSITY,
    ELEMENTARY,
    HIGH_SCHOOL,
    NONE
}

// No-argument constructor, initialized empty strings
data class CourseDetails(
    val id: String = "",
    val instructorId: String = "",
    val name: String = "",
    val educationLevel: EducationLevelType = EducationLevelType.NONE,
    val description: String = "",
    val studentIds: MutableList<String> = emptyList<String>().toMutableList(),
    val quizIds: MutableList<String> = emptyList<String>().toMutableList(),
)

class CourseInstructor private constructor(
    private val id: String,
    private val instructorId: String,
    private val name: String,
    private val educationLevel: EducationLevelType,
    private val description: String,
    private val studentIds: MutableList<String>,
    private val quizIds: MutableList<String>,
) {
    companion object {

        private fun validateCourseParameters(courseDetails: CourseDetails) {
            if (courseDetails.name.isEmpty() || courseDetails.description.isEmpty() || courseDetails.educationLevel == EducationLevelType.NONE) {
                throw FirebaseAuthError("Course name, description and education level must not be empty")
            }
        }

        suspend fun createCourseDetails(courseDetails: CourseDetails) {
            try {
                val databaseReference =
                    FirebaseDatabase.getInstance().getReference("courses/${courseDetails.id}")
                databaseReference.setValue(courseDetails).await()
            } catch (e: Exception) {
                throw FirebaseAuthError("Failed to create course details: ${e.message}")
            }
        }

        suspend fun getCourseDetails(courseId: String): CourseDetails {
            val databaseReference =
                FirebaseDatabase.getInstance().getReference("courses/${courseId}")
            val dataSnapshot = databaseReference.get().await()
            return dataSnapshot.getValue<CourseDetails>()
                ?: throw CourseDbError("Course details not found")
        }

        suspend fun create(
            userViewModel: UserViewModel,
            instructor: User,
            name: String,
            educationLevel: EducationLevelType,
            description: String
        ): CourseInstructor {

            if (instructor.getType() != UserType.INSTRUCTOR) {
                throw CourseCreationError("You do not have the authorization level to create a class")
            }
            // When a user is first created, they don't have a courseId
            val courseDetails = CourseDetails(
                id = UUID.randomUUID().toString(),
                instructor.getUserId(),
                name,
                educationLevel,
                description,
                studentIds = emptyList<String>().toMutableList(),
                quizIds = emptyList<String>().toMutableList()
            )
            validateCourseParameters(courseDetails)
            createCourseDetails(courseDetails)

            val userDetails =
                UserDetails(courseDetails.id, instructor.getName(), instructor.getType())
            User.createUserDetails(instructor.getUserId(), userDetails)
            val instructorNameCourseIndex =
                InstructorNameCourseIndex(instructor.getUserId(), courseDetails.id)
            User.createInstructorNameCourseIndex(instructor.getName(), instructorNameCourseIndex)
            // Updates userDetails in userViewModel cache
            userViewModel.saveDetails(userDetails.courseId, userDetails.type)
            return CourseInstructor(
                courseDetails.id,
                courseDetails.instructorId,
                courseDetails.name,
                courseDetails.educationLevel,
                courseDetails.description,
                courseDetails.studentIds,
                courseDetails.quizIds
            )

        }
    }
}