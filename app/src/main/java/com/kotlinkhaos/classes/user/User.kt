package com.kotlinkhaos.classes.user

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import com.kotlinkhaos.classes.course.CourseDetails
import com.kotlinkhaos.classes.course.CourseInstructor
import com.kotlinkhaos.classes.errors.CourseDbError
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.errors.UserCreateStreamError
import com.kotlinkhaos.classes.errors.UserNetworkError
import com.kotlinkhaos.classes.services.KotlinKhaosUserApi
import com.kotlinkhaos.classes.user.viewmodel.UserViewModel
import com.kotlinkhaos.classes.utils.calculateSha256Hash
import kotlinx.coroutines.tasks.await

enum class UserType {
    STUDENT,
    INSTRUCTOR,
    NONE
}

// No-argument constructor, initialized empty strings
data class UserDetails(
    val courseId: String = "",
    val name: String = "",
    val type: UserType = UserType.NONE
)

data class InstructorNameCourseIndex(
    val userId: String = "",
    val courseId: String = "",
)

class User private constructor(
    private val userId: String,
    private val courseId: String,
    private val name: String,
    private val type: UserType
) {
    companion object {
        private fun validateLoginParameters(email: String, pass: String) {
            if (email.isEmpty() || pass.isEmpty()) {
                throw FirebaseAuthError("Email and password must not be empty")
            }
        }

        private suspend fun validateRegisterParameters(
            email: String,
            pass: String,
            userDetails: UserDetails
        ) {
            validateLoginParameters(email, pass)
            if (userDetails.name.isEmpty()) {
                throw FirebaseAuthError("Name must not be empty")
            }
            if (userDetails.type == UserType.INSTRUCTOR) {
                // Validate if a instructor index already exists with this name
                val databaseReference =
                    FirebaseDatabase.getInstance()
                        .getReference("instructorsNameCourseIndex/${userDetails.name}")
                val dataSnapshot = databaseReference.get().await()
                val result = dataSnapshot.getValue<InstructorNameCourseIndex>()
                if (result != null) {
                    throw FirebaseAuthError("Name has been taken by another instructor")
                }
            }
        }

        private suspend fun fetchUserDetails(userId: String): UserDetails {
            val databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId")
            val dataSnapshot = databaseReference.get().await()
            return dataSnapshot.getValue<UserDetails>()
                ?: throw FirebaseAuthError("User details not found")
        }

        suspend fun createUserDetails(userId: String, userDetails: UserDetails) {
            try {
                val databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId")
                databaseReference.setValue(userDetails).await()
            } catch (e: Exception) {
                throw FirebaseAuthError("Failed to create user details: ${e.message}")
            }
        }

        suspend fun createInstructorNameCourseIndex(
            instructorName: String,
            instructorNameCourseIndex: InstructorNameCourseIndex
        ) {
            try {
                val databaseReference =
                    FirebaseDatabase.getInstance()
                        .getReference("instructorsNameCourseIndex/$instructorName")
                databaseReference.setValue(instructorNameCourseIndex).await()
            } catch (e: Exception) {
                throw FirebaseAuthError("Failed to create instructor name course index: ${e.message}")
            }
        }

        suspend fun findCourseByInstructorUserName(instructorName: String): CourseDetails {
            if (instructorName.isEmpty()) {
                throw CourseDbError("No instructor name specified!")
            }
            val databaseReference =
                FirebaseDatabase.getInstance()
                    .getReference("instructorsNameCourseIndex/$instructorName")
            val dataSnapshot = databaseReference.get().await()
            val result = dataSnapshot.getValue<InstructorNameCourseIndex>()
                ?: throw CourseDbError("Course details not found")
            return CourseInstructor.getCourseDetails(result.courseId)
        }

        suspend fun login(userViewModel: UserViewModel, email: String, pass: String): User? {
            try {
                validateLoginParameters(email, pass)
                val mAuth = FirebaseAuth.getInstance()
                val result = mAuth.signInWithEmailAndPassword(email, pass).await() ?: return null
                val userDetails = fetchUserDetails(result.user!!.uid)
                // Caches userDetails in userViewModel cache
                userViewModel.saveDetails(userDetails.courseId, userDetails.type)
                return User(
                    result.user!!.uid,
                    userDetails.courseId,
                    userDetails.name,
                    userDetails.type
                )
            } catch (err: Exception) {
                if (err is FirebaseAuthInvalidCredentialsException && err.message != null) {
                    throw FirebaseAuthError(err.message!!)
                }
                if (err is FirebaseException) {
                    if (err.message == "An internal error has occurred. [ INVALID_LOGIN_CREDENTIALS ]") {
                        throw FirebaseAuthError("Invalid password")
                    }
                    throw FirebaseAuthError("An error occurred when logging in")
                }
                throw err
            }
        }

        suspend fun register(
            userViewModel: UserViewModel,
            email: String,
            pass: String,
            name: String,
            type: UserType
        ): User? {
            try {
                // When a user is first created, they don't have a courseId
                val userDetails = UserDetails(courseId = "", name, type)
                validateRegisterParameters(email, pass, userDetails)
                val mAuth = FirebaseAuth.getInstance()
                val result =
                    mAuth.createUserWithEmailAndPassword(email, pass).await() ?: return null

                if (userDetails.type == UserType.INSTRUCTOR) {
                    val instructorNameCourseIndex =
                        InstructorNameCourseIndex(result.user!!.uid, courseId = "")
                    createInstructorNameCourseIndex(userDetails.name, instructorNameCourseIndex)
                }
                createUserDetails(result.user!!.uid, userDetails)
                // Caches userDetails in userViewModel cache
                userViewModel.saveDetails(userDetails.courseId, userDetails.type)
                return User(
                    result.user!!.uid,
                    userDetails.courseId,
                    userDetails.name,
                    userDetails.type
                )
            } catch (err: Exception) {
                if (err is FirebaseAuthException) {
                    throw FirebaseAuthError(err.message!!)
                }
                throw err
            }
        }

        suspend fun sendForgotPasswordEmail(email: String) {
            if (email.isEmpty()) {
                throw FirebaseAuthError("Email must not be empty")
            }
            val mAuth = FirebaseAuth.getInstance()
            mAuth.sendPasswordResetEmail(email).await()
        }

        suspend fun getUser(): User? {
            try {
                val mAuth = FirebaseAuth.getInstance()
                val loadedFirebaseUser = mAuth.currentUser ?: return null
                loadedFirebaseUser.reload()
                val userDetails = fetchUserDetails(loadedFirebaseUser.uid)
                return User(
                    loadedFirebaseUser.uid,
                    userDetails.courseId,
                    userDetails.name,
                    userDetails.type
                )
            } catch (err: Exception) {
                if (err is FirebaseAuthInvalidUserException) {
                    if (err.message != null) {
                        Log.i("Firebase", err.message!!)
                    }
                    return null
                }
                throw err;
            }
        }

        fun getProfilePicture(userId: String, hash: String): String {
            return "https://images.maximoguk.com/kotlin-khaos/profile/picture/${userId}/${hash}"
        }

        /**
         * Get profile picture for currently logged in user
         */
        suspend fun getProfilePicture(): String {
            val userId = getUserId()
            val token = getJwt()
            val kotlinKhaosApi = KotlinKhaosUserApi()
            val avatarHash =
                kotlinKhaosApi.getProfilePictureHash(token).sha256
            return "https://images.maximoguk.com/kotlin-khaos/profile/picture/${userId}/${avatarHash}"
        }

        suspend fun uploadProfilePicture(context: Context, imageUri: Uri): String {
            try {
                val token = getJwt()
                // Calculate SHA-256 hash
                val sha256Hash =
                    context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                        calculateSha256Hash(inputStream)
                    } ?: throw UserCreateStreamError()

                val kotlinKhaosApi = KotlinKhaosUserApi()
                val res =
                    kotlinKhaosApi.getPresignedProfilePictureUploadUrl(token, sha256Hash)
                val s3Api = KotlinKhaosUserApi()

                // Upload the image
                context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    s3Api.uploadImageToS3(inputStream, res.uploadUrl)
                } ?: throw UserCreateStreamError()

                return res.sha256
            } catch (err: Exception) {
                if (err is FirebaseNetworkException) {
                    throw UserNetworkError()
                }
                throw err
            }
        }

        fun getUserId(): String {
            val mAuth = FirebaseAuth.getInstance()
            val loadedFirebaseUser =
                mAuth.currentUser ?: throw FirebaseAuthError("User is not logged in!")
            return loadedFirebaseUser.uid
        }

        suspend fun getJwt(): String {
            // Since we're using the firebase sdk, it should manage token refreshes automatically for us
            val mAuth = FirebaseAuth.getInstance()
            val loadedFirebaseUser =
                mAuth.currentUser ?: throw FirebaseAuthError("User is not logged in!")
            return loadedFirebaseUser.getIdToken(false).await().token
                ?: throw Exception("Error getting token!")
        }

        /**
         * Logs out the current user from FirebaseAuth.
         *
         * This method takes in a userViewModel to ensure that the UserTypeStore
         * does not get out of sync with the FirebaseAuth state.
         */
        fun logout(userViewModel: UserViewModel) {
            FirebaseAuth.getInstance().signOut()
            // Clears userViewModel cache
            userViewModel.clear()
        }
    }

    fun getUserId(): String {
        return this.userId
    }

    fun getCourseId(): String {
        return this.courseId;
    }

    fun getName(): String {
        return this.name
    }

    fun getType(): UserType {
        return this.type
    }
}