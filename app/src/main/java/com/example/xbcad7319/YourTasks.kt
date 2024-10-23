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
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_your_tasks)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firestore and Firebase Authentication
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadYourTasks()
    }

    private fun loadYourTasks() {
        val currentUser = auth.currentUser
        val yourTasksLayout: LinearLayout = findViewById(R.id.yourTasksLayout)
        if (currentUser != null) {
            db.collection("tasks")
                .whereEqualTo("assignedEmployee", currentUser.displayName)
                .get()
                .addOnSuccessListener { tasks ->
                    for (task in tasks) {
                        val taskView = layoutInflater.inflate(R.layout.task_item, null)
                        val taskNameTextView: TextView =
                            taskView.findViewById(R.id.taskNameTextView)
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
                            showUpdateConfirmationDialog(taskId.toString())
                        }

                        workButton.setOnClickListener {
                            showWorkDialog()
                        }

                        yourTasksLayout.addView(taskView)
                    }
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

    // Update the Task Status to "Completed" in Firestore
    private fun updateTaskStatusToCompleted(taskId: String) {
        db.collection("tasks").document(taskId)
            .update("status", "Completed")
            .addOnSuccessListener {
                Toast.makeText(this, "Task marked as completed!", Toast.LENGTH_SHORT).show()
                recreate()  // Reload the activity to reflect changes
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update task.", Toast.LENGTH_SHORT).show()
            }
    }

    // Show the Work Hours Dialog
    private fun showWorkDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Log Work Hours")
        builder.setMessage("Please log work hours in the appropriate window.")
        builder.setPositiveButton("Ok") { _, _ ->
            val intent = Intent(this, HoursWorked::class.java)
            startActivity(intent)
        }
        val dialog = builder.create()
        dialog.show()
    }
}