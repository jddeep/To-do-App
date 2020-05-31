package com.jddeep.todoapp.adapter

import android.content.Context
import android.graphics.Paint
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jddeep.todoapp.R
import com.jddeep.todoapp.database.DBHelper
import com.jddeep.todoapp.model.TodoModel
import kotlinx.android.synthetic.main.todo_item_layout.view.*


class TodoAdapter(private val todoList: ArrayList<TodoModel>, private val context: Context) : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

    private lateinit var dbHelper: DBHelper
    private var itemDeleteListener: OnItemDeleteListener? = null

    fun setItemDeleteListener(deleteListener: OnItemDeleteListener) {
        this.itemDeleteListener = deleteListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.todo_item_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        dbHelper = DBHelper(context)
        val todo: TodoModel = todoList[position]

        holder.bindItems(todo)

        holder.itemView.doneTodo.setOnClickListener {
            if (dbHelper.completeTodo(todo)) {
                holder.itemView.doneTodo.visibility = View.GONE
                holder.itemView.undoTodo.visibility = View.VISIBLE
                holder.itemView.deleteTodo.visibility = View.VISIBLE
                holder.itemView.todo_title.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        holder.itemView.undoTodo.setOnClickListener {
            if (dbHelper.undoTodo(todo)) {
                holder.itemView.doneTodo.visibility = View.VISIBLE
                holder.itemView.undoTodo.visibility = View.GONE
                holder.itemView.deleteTodo.visibility = View.GONE
                holder.itemView.todo_title.paintFlags = holder.itemView.todo_title.paintFlags and
                        Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        holder.itemView.deleteTodo.setOnClickListener {
            if (dbHelper.deleteTodo(todo)) {
                itemDeleteListener?.onItemDelete(todo)
                Log.e("Todo Delete:", "done")
            }
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(todo: TodoModel) {
            val tvTitle = itemView.findViewById<TextView>(R.id.todo_title)
            val tvPriority = itemView.findViewById<TextView>(R.id.todo_priority_tv)
            val tvDate = itemView.findViewById<TextView>(R.id.todo_date)
            val doneTodoBtn = itemView.findViewById<ImageView>(R.id.doneTodo)
            val undoTodoBtn = itemView.findViewById<ImageView>(R.id.undoTodo)
            val deleteTodoBtn = itemView.findViewById<ImageView>(R.id.deleteTodo)
            if (todo.taskStatus != "pending") {
                tvTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                doneTodoBtn.visibility = View.GONE
                undoTodoBtn.visibility = View.VISIBLE
                deleteTodoBtn.visibility = View.VISIBLE
            }

            tvTitle.text = todo.title
            tvPriority.text = todo.priorityTag
            tvDate.text = todo.timestamp
        }
    }

    interface OnItemDeleteListener {
        fun onItemDelete(todo: TodoModel)
    }

}