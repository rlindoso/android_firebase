package com.example.android_firebase

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TaskRowListener {
    override fun onTaskDelete(objectId: String) {
        val task = _db.child(Statics.FIREBASE_TASK).child(objectId)
        task.removeValue()
    }

    override fun onTaskChange(objectId: String, isDone: Boolean) {
        val task = _db.child(Statics.FIREBASE_TASK).child(objectId)
        task.child("done").setValue(isDone)
    }

    lateinit var _db: DatabaseReference

    lateinit var _adapter: TaskAdapter
    var _taskList: MutableList<Task>? = null

    var _taskListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            loadTaskList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        _db = FirebaseDatabase.getInstance().reference
        _taskList = mutableListOf()
        _adapter = TaskAdapter(this, _taskList!!)
        listviewTask!!.adapter = _adapter

        btnAdd.setOnClickListener{
            addTask()
        }

        _db.orderByKey().addValueEventListener(_taskListener)
    }

    fun addTask(){

        //Declare and Initialise the Task
        val task = Task.create()

        //Set Task Description and isDone Status
        task.taskDesc = txtNewTaskDesc.text.toString()
        task.done = false

        //Get the object id for the new task from the Firebase Database
        val newTask = _db.child(Statics.FIREBASE_TASK).push()
        task.objectId = newTask.key

        //Set the values for new task in the firebase using the footer form
        newTask.setValue(task)


        //Reset the new task description field for reuse.
        txtNewTaskDesc.setText("")

        Toast.makeText(this, "Task added to the list successfully" + task.objectId, Toast.LENGTH_SHORT).show()
    }

    private fun loadTaskList(dataSnapshot: DataSnapshot) {
        Log.d("MainActivity", "loadTaskList")

        val tasks = dataSnapshot.children.iterator()

        //Check if current database contains any collection
        if (tasks.hasNext()) {

            _taskList!!.clear()


            val listIndex = tasks.next()
            val itemsIterator = listIndex.children.iterator()

            //check if the collection has any task or not
            while (itemsIterator.hasNext()) {

                //get current task
                val currentItem = itemsIterator.next()
                val task = Task.create()

                //get current data in a map
                val map = currentItem.getValue() as HashMap<String, Any>

                //key will return the Firebase ID
                task.objectId = currentItem.key
                task.done = map.get("done") as Boolean?
                task.taskDesc = map.get("taskDesc") as String?
                _taskList!!.add(task)
            }
        }

        //alert adapter that has changed
        _adapter.notifyDataSetChanged()

    }
}
