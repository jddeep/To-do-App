package com.jddeep.todoapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jddeep.todoapp.model.TodoModel

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1  // Database Version
        private const val DATABASE_NAME = "todos_db" // Database Name
    }

    // Database Table Name
    private val TABLE_NAME = "todoTable"
    // Attributes for Tables
    private val COLUMN_ID = "id"
    private val COLUMN_TITLE = "title"
    private val COLUMN_PRIORITY = "priority"
    private val COLUMN_TIMESTAMP = "timestamp"

    private val COL_TODO_STATUS = "todo_status"
    private val COL_DEFAULT_STATUS = "pending"
    private val COL_STATUS_COMPLETED = "completed"


    // Create table SQL query
    private val CREATE_TABLE = (
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_PRIORITY + " TEXT,"
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + COL_TODO_STATUS + " TEXT NOT NULL DEFAULT "
                    + COL_DEFAULT_STATUS
                    + ")")

    // 1) Select All Query, 2)looping through all rows and adding to list 3) close db connection, 4) return todos list
    val todoList: ArrayList<TodoModel>
        get() {
            val todos = ArrayList<TodoModel>()
            val selectQuery = "SELECT  * FROM $TABLE_NAME ORDER BY $COLUMN_TIMESTAMP DESC"

            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val todo = TodoModel(cursor!!.getInt(cursor.getColumnIndex(COLUMN_ID)), cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                            cursor.getString(cursor.getColumnIndex(COLUMN_PRIORITY)), cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                            cursor.getString(cursor.getColumnIndex(COL_TODO_STATUS)))
                    todo.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID))
                    todo.title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
                    todo.priorityTag = cursor.getString(cursor.getColumnIndex(COLUMN_PRIORITY))
                    todo.timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP))
                    todo.taskStatus = cursor.getString(cursor.getColumnIndex(COL_TODO_STATUS))
                    todos.add(todo)
                } while (cursor.moveToNext())
            }
            db.close()
            cursor.close()
            return todos
        }

    // return count
    val todoCount: Int
        get() {
            val countQuery = "SELECT  * FROM $TABLE_NAME"
            val db = this.readableDatabase
            val cursor = db.rawQuery(countQuery, null)
            val count = cursor.count
            cursor.close()
            return count
        }

    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)  // create notes table
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME") // Drop older table if existed
        onCreate(db)  // Create tables again
    }

    fun insertTodo(todo: TodoModel): Long {
        val db = this.writableDatabase   // get writable database as we want to write data
        val values = ContentValues()
        // `id` and `timestamp` will be inserted automatically, no need to add them
        values.put(COLUMN_TITLE, todo.title)
        values.put(COLUMN_PRIORITY, todo.priorityTag)
        values.put(COL_TODO_STATUS, COL_DEFAULT_STATUS)
        val id = db.insert(TABLE_NAME, null, values)  // insert row
        db.close() // close db connection

        return id  // return newly inserted row id
    }

    fun getTodo(id: Long): TodoModel {
        val db = this.readableDatabase    // get readable database as we are not inserting anything
        val cursor = db.query(TABLE_NAME,
                arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_PRIORITY, COLUMN_TIMESTAMP), COLUMN_ID + "=?",
                arrayOf(id.toString()), null, null, null, null)

        cursor?.moveToFirst()
        // prepare todos object
        val todo = TodoModel(
                cursor!!.getInt(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(COLUMN_PRIORITY)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)),
                cursor.getString(cursor.getColumnIndex(COL_TODO_STATUS)))

        cursor.close()   // close the db connection
        return todo
    }

    fun completeTodo(todo: TodoModel): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_TODO_STATUS, COL_STATUS_COMPLETED)
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(todo.id.toString()))
        db.close()
        return true
    }

    fun undoTodo(todo: TodoModel): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_TODO_STATUS, COL_DEFAULT_STATUS)
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(todo.id.toString()))
        db.close()
        return true
    }

    fun deleteTodo(todo: TodoModel): Boolean {
        val db = writableDatabase   // Gets the data repository in write mode
        db.delete(TABLE_NAME, "$COLUMN_ID LIKE ?", arrayOf(todo.id.toString())) // Issue SQL statement.
        return true
    }
}