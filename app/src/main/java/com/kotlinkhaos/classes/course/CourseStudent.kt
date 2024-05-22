package com.kotlinkhaos.classes.course

import com.kotlinkhaos.classes.errors.CourseJoinError
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.classes.user.UserDetails
import com.kotlinkhaos.classes.user.viewmodel.UserViewModel


class CourseStudent private constructor() {
    companion object {

        suspend fun joinCourse(
            userViewModel: UserViewModel,
            courseDetails: CourseDetails,
            user: User
        ) {
            if (user.getCourseId().isNotEmpty()) {
                throw CourseJoinError("User already enrolled in course")
            }
            courseDetails.studentIds.add(user.getUserId())
            val userDetails = UserDetails(courseDetails.id, user.getName(), user.getType())
            User.createUserDetails(user.getUserId(), userDetails)
            CourseInstructor.createCourseDetails(courseDetails)
            // Updates userDetails in userViewModel cache
            userViewModel.saveDetails(userDetails.courseId, userDetails.type)
        }
        
    }

}