package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//The dynamic addition of task views using LinearLayout and custom layouts is inspired by:
//Title: Adding Views Dynamically in Android
//Author: Tutorialspoint
//Link: https://www.tutorialspoint.com/add-and-remove-views-in-android-dynamically

class ActiveTasks : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var username: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_active_tasks)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activeTasks)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firestore and Firebase Authentication
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadActiveTasks()

        // Set up the Back button
        val backButton: Button = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun loadActiveTasks() {
        val activeTasksLayout: LinearLayout = findViewById(R.id.activeTasksLayout)

        db.collection("tasks")

            // Only fetch tasks that are not completed
            .whereNotEqualTo("status", "Completed")

            .get()
            .addOnSuccessListener { tasks ->
                for (task in tasks) {
                    val taskView = layoutInflater.inflate(R.layout.task_item, null)
                    val taskNameTextView: TextView = taskView.findViewById(R.id.taskNameTextView)
                    val statusTextView: TextView = taskView.findViewById(R.id.statusTextView)
                    val acceptButton: Button = taskView.findViewById(R.id.acceptButton)

                    val taskName = task.getString("taskName") ?: "Unknown"
                    val assignedEmployee = task.getString("assignedEmployee") ?: "Unassigned"
                    val taskStatus = task.getString("status") ?: "In-Progress"

                    taskNameTextView.text = "Task: $taskName"
                    statusTextView.text = "Assigned: $assignedEmployee\nStatus: $taskStatus"

                    if (assignedEmployee == "Unassigned") {
                        acceptButton.visibility = View.VISIBLE
                        acceptButton.setOnClickListener {
                            acceptTask(task.id)
                        }
                    } else {
                        acceptButton.visibility = View.GONE
                    }

                    activeTasksLayout.addView(taskView)
                }
            }
    }

    private fun acceptTask(taskId: String) {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            val userRef = db.collection("Users").document(uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        username = document.getString("name").toString()

                        db.collection("tasks").document(taskId)
                            .update("assignedEmployee", username)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Task accepted!", Toast.LENGTH_SHORT).show()
                                recreate() // Reload the activity to reflect the changes
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to accept task.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error getting document: $exception")
                }
        }
    }

    private fun navigateToDashboard() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        if (userId != null) {
            // Fetch user role from Firestore
            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role == "Administrator") {
                        // Navigate back to Admin dashboard
                        val intent = Intent(this, AdminDash::class.java)
                        startActivity(intent)
                        finish()
                    } else if (role == "Employee") {
                        // Navigate back to Employee dashboard
                        val intent = Intent(this, EmpDash::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error fetching user role", Toast.LENGTH_SHORT).show()
                }
        }
    }
}