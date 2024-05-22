package com.kotlinkhaos.classes.utils

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.errors.UserError
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.classes.user.viewmodel.UserAvatarViewModel
import com.kotlinkhaos.databinding.FragmentInstructorCourseBinding
import com.kotlinkhaos.databinding.FragmentStudentProfileBinding
import com.kotlinkhaos.databinding.ProfilePictureLayoutBinding
import kotlinx.coroutines.launch

fun uploadProfilePicture(
    lifecycleOwner: LifecycleOwner,
    context: Context,
    profilePictureLayoutBinding: ProfilePictureLayoutBinding,
    binding: ViewBinding,
    userAvatarViewModel: UserAvatarViewModel,
    selectedImageUri: Uri
) {
    lifecycleOwner.lifecycleScope.launch {
        try {
            // Set profile picture as loading during upload
            profilePictureLayoutBinding.profilePictureLoading.visibility = View.VISIBLE
            val userId = User.getUserId()
            val avatarHash = User.uploadProfilePicture(context, selectedImageUri)
            userAvatarViewModel.updateUserAvatarUrl(userId, avatarHash)
            loadProfilePicture(
                lifecycleOwner,
                profilePictureLayoutBinding,
                binding,
                userAvatarViewModel
            )
        } catch (err: Exception) {
            if (err is FirebaseAuthError || err is UserError) {
                when (binding) {
                    is FragmentStudentProfileBinding -> binding.errorMessage.text = err.message
                    is FragmentInstructorCourseBinding -> binding.errorMessage.text = err.message
                }
                return@launch
            }
            throw err
        } finally {
            // Finish loading
            if (profilePictureLayoutBinding.root.isAttachedToWindow) {
                profilePictureLayoutBinding.profilePictureLoading.visibility = View.GONE
            }
        }
    }
}

fun loadProfilePicture(
    lifecycleOwner: LifecycleOwner,
    profilePictureLayoutBinding: ProfilePictureLayoutBinding,
    binding: ViewBinding,
    userAvatarViewModel: UserAvatarViewModel,
) {
    if (userAvatarViewModel.updatedUserAvatarUrl.value != null) {
        profilePictureLayoutBinding.profilePicture.loadImage(
            userAvatarViewModel.updatedUserAvatarUrl.value!!,
            profilePictureLayoutBinding.profilePictureLoading
        )
        return
    }

    userAvatarViewModel.loadAvatarHash()
    userAvatarViewModel.avatarUrl.observe(lifecycleOwner) { avatarUrl ->
        profilePictureLayoutBinding.profilePicture.loadImage(
            avatarUrl,
            profilePictureLayoutBinding.profilePictureLoading
        )
    }

    userAvatarViewModel.userAvatarError.observe(lifecycleOwner) { err ->
        if (err is FirebaseAuthError || err is UserError) {
            when (binding) {
                is FragmentStudentProfileBinding -> binding.errorMessage.text = err.message
                is FragmentInstructorCourseBinding -> binding.errorMessage.text = err.message
            }
            return@observe
        }
        throw err
    }
}