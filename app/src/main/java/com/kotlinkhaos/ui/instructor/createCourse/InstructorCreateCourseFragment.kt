package com.kotlinkhaos.ui.instructor.createCourse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kotlinkhaos.classes.course.CourseInstructor
import com.kotlinkhaos.classes.course.EducationLevelType
import com.kotlinkhaos.classes.errors.CourseError
import com.kotlinkhaos.classes.errors.FirebaseAuthError
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.classes.user.viewmodel.UserStore
import com.kotlinkhaos.classes.user.viewmodel.UserViewModel
import com.kotlinkhaos.classes.user.viewmodel.UserViewModelFactory
import com.kotlinkhaos.databinding.FragmentInstructorCreateCourseBinding
import kotlinx.coroutines.launch


class InstructorCreateCourseFragment : Fragment() {
    private var _binding: FragmentInstructorCreateCourseBinding? = null
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
        _binding = FragmentInstructorCreateCourseBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val items = arrayOf(
            EducationLevelType.HIGH_SCHOOL,
            EducationLevelType.ELEMENTARY,
            EducationLevelType.UNIVERSITY
        )

        val educationLevelListAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            items.map { it.name }
        )
        binding.educationLevelOptions.setAdapter(educationLevelListAdapter)
        var educationLevel: EducationLevelType = EducationLevelType.NONE
        binding.educationLevelOptions.setOnItemClickListener { parent, _, position, _ ->
            // Get the selected item as a String
            val selectedName = parent.getItemAtPosition(position) as String
            // Find the corresponding enum value
            educationLevel = EducationLevelType.valueOf(selectedName)
            // Now you have the selected option, you can do whatever you want with it
            // For example, you can log it or use it in further processing
        }

        binding.createCourseButton.setOnClickListener {
            handleCreateCourse(educationLevel)
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleCreateCourse(educationLevelSelected: EducationLevelType) {
        setLoadingState(true)
        lifecycleScope.launch {
            try {
                val user = User.getUser()
                val courseName = binding.inputCourseName.text.toString().trim()
                val courseDesc = binding.inputCourseDesc.text.toString().trim()
                if (user != null) {
                    CourseInstructor.create(
                        userViewModel,
                        user,
                        courseName,
                        educationLevelSelected,
                        courseDesc
                    )
                    val action =
                        InstructorCreateCourseFragmentDirections.startNavigationGoBackToInstructorHome()
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
            binding.courseCreationProgess.visibility = if (loading) View.VISIBLE else View.GONE
            binding.createCourseButton.isEnabled = !loading
        }
    }

}