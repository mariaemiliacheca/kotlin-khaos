package com.kotlinkhaos.ui.student.joinCourse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kotlinkhaos.classes.course.CourseStudent
import com.kotlinkhaos.classes.errors.CourseError
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.classes.user.viewmodel.UserStore
import com.kotlinkhaos.classes.user.viewmodel.UserViewModel
import com.kotlinkhaos.classes.user.viewmodel.UserViewModelFactory
import com.kotlinkhaos.databinding.FragmentStudentJoinCourseBinding
import kotlinx.coroutines.launch

class StudentJoinCourseFragment : Fragment() {
    private var _binding: FragmentStudentJoinCourseBinding? = null
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(UserStore(requireContext()))
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentJoinCourseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.joinCourseButton.setOnClickListener {
            handleJoinClick()
        }


        // binding.
        return root
    }

    private fun handleJoinClick() {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val userName = binding.editTextSearch.text.toString().trim()
                val courseDetails = User.findCourseByInstructorUserName(userName)
                val user = User.getUser()
                if (user != null) {
                    CourseStudent.joinCourse(userViewModel, courseDetails, user)
                    val action =
                        StudentJoinCourseFragmentDirections.startNavigationGoBackToStudentHome()
                    findNavController().navigate(action)

                }
            } catch (e: Exception) {
                if (e is FirebaseAuthError || e is CourseError) {
                    binding.errorMessage.text = e.message
                    return@launch
                }
                throw e
            } finally {
                setLoadingState(false)
            }

        }

    }

    private fun setLoadingState(loading: Boolean) {
        if (isAdded) {
            binding.joinCourseProgess.visibility = if (loading) View.VISIBLE else View.GONE
            binding.joinCourseButton.isEnabled = !loading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}