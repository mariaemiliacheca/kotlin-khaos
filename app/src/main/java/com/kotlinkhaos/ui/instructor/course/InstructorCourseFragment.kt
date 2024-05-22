package com.kotlinkhaos.ui.instructor.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kotlinkhaos.classes.course.CourseInstructor
import com.kotlinkhaos.classes.errors.CourseError
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.classes.user.viewmodel.UserAvatarViewModel
import com.kotlinkhaos.classes.utils.loadProfilePicture
import com.kotlinkhaos.classes.utils.openPictureGallery
import com.kotlinkhaos.classes.utils.setupImagePickerCallbacks
import com.kotlinkhaos.classes.utils.uploadProfilePicture
import com.kotlinkhaos.databinding.FragmentInstructorCourseBinding
import kotlinx.coroutines.launch

class InstructorCourseFragment : Fragment() {
    private var _binding: FragmentInstructorCourseBinding? = null
    private val userAvatarViewModel: UserAvatarViewModel by activityViewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructorCourseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val (imagePicker, requestPermissionLauncher) = setupImagePickerCallbacks { selectedImageUri ->
            if (selectedImageUri != null) {
                uploadProfilePicture(
                    this,
                    requireContext(),
                    binding.profilePictureLayout,
                    binding,
                    userAvatarViewModel,
                    selectedImageUri
                )
            }
        }
        binding.profilePictureLayout.changeProfilePicture.setOnClickListener {
            openPictureGallery(imagePicker, requestPermissionLauncher)
        }
        loadCourseDetails()
        loadProfilePicture(this, binding.profilePictureLayout, binding, userAvatarViewModel)
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadCourseDetails() {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val user = User.getUser()
                if (user != null) {
                    val courseDetails = CourseInstructor.getCourseDetails(user.getCourseId())
                    binding.courseDesc.setText(courseDetails.description)
                    binding.educationLevelOptions.setText(courseDetails.educationLevel.name)
                }
            } catch (err: Exception) {
                if (err is FirebaseAuthError || err is CourseError) {
                    binding.errorMessage.text = err.message
                    return@launch
                }
                throw err
            } finally {
                setLoadingState(false)
            }
        }
    }

    private fun setLoadingState(loading: Boolean) {
        if (isAdded) {
            with(binding) {
                loadingCourseDetails.visibility = if (loading) View.VISIBLE else View.GONE
                layoutCourseDesc.visibility = if (loading) View.GONE else View.VISIBLE
                educationLevelMenu.visibility = if (loading) View.GONE else View.VISIBLE
            }
        }
    }
}