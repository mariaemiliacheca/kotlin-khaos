package com.kotlinkhaos.ui.instructor.home.quizCreation

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kotlinkhaos.R

class QuizQuestionsListAdapter(
    private val dataSet: MutableList<String>
) :
    RecyclerView.Adapter<QuizQuestionsListAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val quizQuestionNumber: TextView
        val quizQuestion: TextView
        var textWatcher: TextWatcher? = null

        init {
            quizQuestionNumber = view.findViewById(R.id.quizQuestionNumber)
            quizQuestion = view.findViewById(R.id.quizQuestion)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.quiz_question_card_editable_layout, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val question = dataSet[position]
        viewHolder.quizQuestionNumber.text =
            viewHolder.quizQuestionNumber.context.getString(
                R.string.quiz_question_card_question_number,
                (position + 1).toString()
            )
        viewHolder.quizQuestion.text = question

        // Remove existing TextWatcher
        viewHolder.quizQuestion.removeTextChangedListener(viewHolder.textWatcher)

        // Create new TextWatcher
        viewHolder.textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val adapterPos = viewHolder.adapterPosition
                // Make sure that adapterPosition is valid
                if (adapterPos != RecyclerView.NO_POSITION) {
                    dataSet[adapterPos] = s.toString()
                }
            }

            // Empty implementations for other required methods of TextWatcher
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        // Add new TextWatcher
        viewHolder.quizQuestion.addTextChangedListener(viewHolder.textWatcher)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun appendToDataSet(question: String) {
        dataSet.add(question)
        notifyItemInserted(dataSet.size - 1)
    }

    fun getDataSet(): List<String> {
        return this.dataSet
    }
}