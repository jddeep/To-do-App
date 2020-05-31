package com.jddeep.todoapp.model

import java.io.Serializable

class TodoModel(var name: String) : Serializable {
    var id: Int? = null
    var title: String? = null
    var timestamp: String? = null
    var priorityTag: String? = null
    var taskStatus: String = "pending"

    constructor(id: Int, title: String, priorityTag: String, timestamp: String, status: String) : this(title) {
        this.id = id
        this.title = title
        this.timestamp = timestamp
        this.priorityTag = priorityTag
        this.taskStatus = status
    }

    constructor(title: String, priorityTag: String) : this(title) {
        this.title = title
        this.priorityTag = priorityTag
    }
}