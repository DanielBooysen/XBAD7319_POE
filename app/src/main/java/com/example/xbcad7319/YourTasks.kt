package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class YourTasks : AppCompatActivity() {
    //The Firebase Firestore query logic for fetching and updating tasks was adapted from:
    //Title: Querying Firestore Collections in Android
    //Author: Firebase Documentation
    //Link: https://firebase.google.com/docs/firestore/query-data/listen

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_your_tasks)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.yourTasks)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadYourTasks()

        // Set up the Back button
        val backButton: Button = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun loadYourTasks() {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        val yourTasksLayout: LinearLayout = findViewById(R.id.yourTasksLayout)

        if (uid != null) {
            db.collection("Users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        username = document.getString("name").toString()

                        db.collection("tasks")
                            .whereEqualTo("assignedEmployee", username)
                            .get()
                            .addOnSuccessListener { tasks ->
                                for (task in tasks) {
                                    val taskView = layoutInflater.inflate(R.layout.task_item, null)
                                    val taskNameTextView: TextView = taskView.findViewById(R.id.taskNameTextView)
                                    val statusTextView: TextView = taskView.findViewById(R.id.statusTextView)
                                    val updateButton: Button = taskView.findViewById(R.id.updateButton)
                                    val workButton: Button = taskView.findViewById(R.id.workButton)

                                    val taskName = task.getString("taskName") ?: "Unknown"
                                    val taskStatus = task.getString("status") ?: "In-Progress"

                                    taskNameTextView.text = "Task: $taskName"
                                    statusTextView.text = "Status: $taskStatus"

                                    updateButton.visibility = View.VISIBLE
                                    workButton.visibility = View.VISIBLE

                                    updateButton.setOnClickListener {
                                        showUpdateConfirmationDialog(task.id)
                                    }

                                    workButton.setOnClickListener {
                                        val intent = Intent(this@YourTasks, HoursWorked::class.java)
                                        intent.putExtra("taskId", task.id) // Pass task ID to HoursWorked
                                        startActivity(intent)
                                    }

                                    yourTasksLayout.addView(taskView)
                                }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error getting document: $exception")
                }
        }
    }

    private fun showUpdateConfirmationDialog(taskId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Complete Task")
        builder.setMessage("Are you sure you want to mark this task as completed?")
        builder.setPositiveButton("Yes") { _, _ ->
            updateTaskStatusToCompleted(taskId)
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun updateTaskStatusToCompleted(taskId: String) {
        println("Updating task with ID: $taskId")

        db.collection("tasks").document(taskId)
            .update("status", "Completed")
            .addOnSuccessListener {
                Toast.makeText(this, "Task marked as completed!", Toast.LENGTH_SHORT).show()
                recreate()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update task: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }

    private fun navigateToDashboard() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        if (userId != null) {
            db.collection("Users").document(userId).get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role == "Administrator") {
                        val intent = Intent(this, AdminDash::class.java)
                        startActivity(intent)
                        finish()
                    } else if (role == "Employee") {
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
