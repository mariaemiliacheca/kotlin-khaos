package com.kotlinkhaos.ui.instructor.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kotlinkhaos.R
import com.kotlinkhaos.classes.services.InstructorQuizsForCourseRes
import com.kotlinkhaos.classes.user.User
import com.kotlinkhaos.classes.user.viewmodel.UserAvatarViewModel
import com.kotlinkhaos.classes.utils.loadImage

class QuizsForCourseListAdapter(
    private var dataSet: List<InstructorQuizsForCourseRes.InstructorQuizDetailsRes>,
    private val clickListener: (String) -> Unit,
    private val userAvatarViewModel: UserAvatarViewModel
) :
    RecyclerView.Adapter<QuizsForCourseListAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quizName: TextView
        val avatarIcon: ImageView
        val viewButton: Button

        init {
            quizName = view.findViewById(R.id.quizName)
            avatarIcon = view.findViewById(R.id.avatarIcon)
            viewButton = view.findViewById(R.id.viewQuizDetailsButton)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.quiz_course_card_layout, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val quiz = dataSet[position]
        viewHolder.quizName.text = quiz.name
        if (quiz.authorAvatarHash !== null) {
            val updatedAvatarUrl = userAvatarViewModel.getUpdatedAvatarUrl(quiz.authorId)
            if (updatedAvatarUrl != null) {
                viewHolder.avatarIcon.loadImage(
                    updatedAvatarUrl
                )
            } else {
                viewHolder.avatarIcon.loadImage(
                    User.getProfilePicture(
                        quiz.authorId,
                        quiz.authorAvatarHash
                    )
                )
            }
        }
        viewHolder.viewButton.setOnClickListener {
            clickListener(quiz.id)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun updateData(newQuizList: List<InstructorQuizsForCourseRes.InstructorQuizDetailsRes>) {
        // We want the latest quiz to be the first quiz in the dataset
        dataSet = newQuizList.reversed()
        notifyDataSetChanged()
    }
}