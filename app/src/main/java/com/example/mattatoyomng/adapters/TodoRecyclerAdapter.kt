package com.example.mattatoyomng.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mattatoyomng.R
import com.example.mattatoyomng.databinding.ItemTodoBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.fragments.TodoListFragment
import com.example.mattatoyomng.models.Todo

class TodoRecyclerAdapter(
    private val context: Context,
    private val todoList: MutableList<Todo>
) : RecyclerView.Adapter<TodoRecyclerAdapter.TodoViewHolder>(),
    FirestoreClass.DeleteTodoCallback,
    FirestoreClass.UpdateTodoCallback {

    private var TAG = "TodoRecyclerAdapter"

    lateinit var binding: ItemTodoBinding
    private var onClickListener: OnClickListener? = null

    inner class TodoViewHolder(binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: Todo) {
            binding.todo = todo
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TodoRecyclerAdapter.TodoViewHolder {
        binding = ItemTodoBinding.inflate(LayoutInflater.from(context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoRecyclerAdapter.TodoViewHolder, position: Int) {
        val todo: Todo = todoList[position]
        holder.bind(todo)
        binding.deleteTodoBTN.setOnClickListener {
            removeAt(position)
        }
        binding.todoCheckbox.setOnCheckedChangeListener { _, isChecked ->
            val documentId = todoList[position].documentId
            FirestoreClass().updateTodoStatus(this, documentId, isChecked)
        }
    }

    private fun removeAt(position: Int) {
        val documentId = todoList[position].documentId
        FirestoreClass().deleteTodo(this, documentId, position)

    }

    override fun getItemCount(): Int = todoList.size

    interface OnClickListener {
        fun onClick(position: Int, model: Todo)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onDeleteTodoSuccess(position: Int) {
        todoList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onDeleteTodoFail(e: Exception) {
        // TODO: avoid creating fragment
        Log.d(TAG, "TODO DELETE FAIL: $e")
        val fragment = TodoListFragment()
        fragment.showErrorSnackBar(fragment.resources.getString(R.string.delete_event_fail))
    }

    override fun onUpdateTodoSuccess() {
        Log.d(TAG, "Todo updated successfully")
    }

    override fun onUpdateTodoFail(e: Exception) {
        Log.d(TAG, "Todo update failed: $e")
    }
}