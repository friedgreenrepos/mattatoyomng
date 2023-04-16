package com.example.mattatoyomng.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mattatoyomng.R
import com.example.mattatoyomng.adapters.TodoRecyclerAdapter
import com.example.mattatoyomng.databinding.FragmentTodoListBinding
import com.example.mattatoyomng.firebase.FirestoreClass
import com.example.mattatoyomng.models.Todo
import com.example.mattatoyomng.utils.addChip
import com.example.mattatoyomng.utils.createDialog
import com.example.mattatoyomng.utils.dpToPx
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore


class TodoListFragment : BaseFragment(), FirestoreClass.CreateTodoCallback,
    FirestoreClass.GetTodoListCallback {

    private val TAG = "TodoListFragment"

    private lateinit var binding: FragmentTodoListBinding

    private var todoRecyclerView: RecyclerView? = null
    private var todoAdapter: TodoRecyclerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTodoListBinding.inflate(inflater, container, false)

        // initialize recycler view
        todoRecyclerView = binding.todoListRV

        binding.addTodoBTN.setOnClickListener {
            showAddTodoDialog()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // get todolist from db
        FirestoreClass().getTodoList(this)
    }

    // show dialog to add todo in list
    private fun showAddTodoDialog() {
        val dialog = this.requireContext().createDialog(R.layout.add_todo_dialog, true)
        val button = dialog.findViewById<MaterialButton>(R.id.todoDialogAdd)
        val editText = dialog.findViewById<EditText>(R.id.todoDialogET)
        button.setOnClickListener {
            if (editText.text.toString().isEmpty()) {
                showErrorSnackBar(resources.getString(R.string.enter_text))
            } else {
                val text = editText.text.toString()
                val dateAdded = Timestamp.now()
                val todo = Todo(text, dateAdded)
                FirestoreClass().createTodo(this, todo)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    override fun onCreateTodoSuccess() {
        showInfoSnackBar(resources.getString(R.string.todo_create_success))
        Log.d(TAG, "adapter = $todoAdapter")
        // reload todolist from db
        FirestoreClass().getTodoList(this)
    }

    override fun onCreateTodoError(e: Exception) {
        showErrorSnackBar(resources.getString(R.string.todo_create_fail) + ": " + e)
    }

    override fun onGetTodoListResult(todoList: MutableList<Todo>) {
        setupTodoRecyclerView(todoList)
    }

    private fun setupTodoRecyclerView(todoList: MutableList<Todo>) {
        // setup event adapter using event list from firestore
        todoRecyclerView?.setHasFixedSize(true)
        todoRecyclerView?.layoutManager = LinearLayoutManager(activity)
        todoAdapter = TodoRecyclerAdapter(this.requireContext(), todoList)
        todoRecyclerView?.adapter = todoAdapter
        todoAdapter!!.notifyDataSetChanged()
    }

}