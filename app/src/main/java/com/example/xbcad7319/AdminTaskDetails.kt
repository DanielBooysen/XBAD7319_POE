package com.example.xbcad7319

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdminTaskDetails : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var taskSpinner: Spinner
    private lateinit var taskInfoTextView: TextView
    private lateinit var backButton: Button
    private val taskIds = mutableListOf<String>() // To keep track of task IDs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_task_details)

        db = FirebaseFirestore.getInstance()
        taskSpinner = findViewById(R.id.taskSpinner)
        taskInfoTextView = findViewById(R.id.taskInfoTextView)
        backButton = findViewById(R.id.back_button) // Initialize back button

        loadTasksIntoSpinner()

        taskSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0) { // Skipping the first item ("Select a task")
                    val selectedTaskId = taskIds[position - 1] // Get the task ID from the list
                    displayTaskDetails(selectedTaskId)
                } else {
                    taskInfoTextView.text = "" // Clear the details if no valid task is selected
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing when no item is selected
            }
        }

        // Set up the back button to finish the activity
        backButton.setOnClickListener {
            finish() // Closes the current activity and returns to the previous screen
        }
    }

    private fun loadTasksIntoSpinner() {
        db.collection("tasks").get()
            .addOnSuccessListener { documents ->
                val taskList = mutableListOf("Select a task")
                taskIds.clear() // Clear previous task IDs
                for (document in documents) {
                    val taskName = document.getString("taskName") ?: "Unknown Task"
                    taskList.add(taskName)
                    taskIds.add(document.id) // Store the document ID
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, taskList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                taskSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayTaskDetails(taskId: String) {
        db.collection("tasks").document(taskId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val assignedEmployee = document.getString("assignedEmployee") ?: "Unknown"
                    val description = document.getString("description") ?: "No description"
                    val dueDate = document.getString("dueDate") ?: "No due date"
                    val status = document.getString("status") ?: "Unknown"
                    val taskName = document.getString("taskName") ?: "Unknown"
                    val totalHours = document.getDouble("totalHours") ?: 0.0

                    val taskDetails = """
                        Assigned Employee: $assignedEmployee
                        Description: $description
                        Due Date: $dueDate
                        Status: $status
                        Task Name: $taskName
                        Total Hours: $totalHours
                    """.trimIndent()

                    taskInfoTextView.text = taskDetails
                } else {
                    Toast.makeText(this, "Task details not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching task details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
