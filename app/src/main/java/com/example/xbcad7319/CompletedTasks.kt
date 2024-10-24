package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
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

class CompletedTasks : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_completed_tasks)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Load completed tasks
        loadCompletedTasks()

        // Set up the Back button
        val backButton: Button = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun loadCompletedTasks() {
        val completedTasksLayout: LinearLayout = findViewById(R.id.completedTasksLayout)

        // Query Firestore for tasks that are marked as completed
        db.collection("tasks")
            .whereEqualTo("status", "Completed") // Fetch tasks with "Completed" status
            .get()
            .addOnSuccessListener { tasks ->
                for (task in tasks) {
                    val taskView = layoutInflater.inflate(R.layout.task_item, null)
                    val taskNameTextView: TextView = taskView.findViewById(R.id.taskNameTextView)
                    val statusTextView: TextView = taskView.findViewById(R.id.statusTextView)

                    val taskName = task.getString("taskName") ?: "Unknown"
                    val assignedEmployee = task.getString("assignedEmployee") ?: "Unassigned"
                    val taskStatus = task.getString("status") ?: "Completed"

                    taskNameTextView.text = "Task: $taskName"
                    statusTextView.text = "Assigned: $assignedEmployee\nStatus: $taskStatus"

                    completedTasksLayout.addView(taskView)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching completed tasks: ${e.message}", Toast.LENGTH_SHORT).show()
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