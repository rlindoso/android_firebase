package com.example.android_firebase

class Task {
    companion object Factory {
        fun create(): Task = Task()
    }

    var objectId: String? = null
    var taskDesc: String? = null
    var done: Boolean? = false
}