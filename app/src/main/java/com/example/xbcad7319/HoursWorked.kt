package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HoursWorked : AppCompatActivity() {
    //Error handling for spinners and user inputs was adapted from:
    //Title: Spinner in Android with Example
    //Author: GeeksforGeeks
    //Link: https://www.geeksforgeeks.org/spinner-in-android-using-java-with-example/
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var taskId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hours_worked)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        taskId = intent.getStringExtra("taskId")
        val taskNameTextView: TextView = findViewById(R.id.taskNameTextView)
        val hoursEditText: EditText = findViewById(R.id.hoursEditText)
        val logHoursButton: Button = findViewById(R.id.logHoursButton)
        val backButton: Button = findViewById(R.id.back_button)

        if (taskId == null) {
            handleTaskNotFound()
            return
        }

        loadTaskName(taskId!!, taskNameTextView)

        logHoursButton.setOnClickListener {
            val hours = hoursEditText.text.toString().trim()
            if (hours.isNotEmpty()) {
                try {
                    val hoursValue = hours.toDouble()
                    logHours(hoursValue)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Please enter a valid number for hours.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter hours worked.", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadTaskName(taskId: String, taskNameTextView: TextView) {
        db.collection("tasks").document(taskId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val taskName = document.getString("taskName") ?: "Unnamed Task"
                    taskNameTextView.text = "Task: $taskName"
                } else {
                    handleTaskNotFound()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading task details: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("HoursWorked", "Error loading task details: ${e.message}")
                handleTaskNotFound()
            }
    }

    private fun handleTaskNotFound() {
        Toast.makeText(this, "Task not found. Please go back to 'Your Tasks' to select a task.", Toast.LENGTH_LONG).show()
        // Navigate back to YourTasks activity
        val intent = Intent(this, YourTasks::class.java)
        startActivity(intent)
        finish() // Closes HoursWorked activity and returns to YourTasks
    }

    private fun logHours(hours: Double) {
        val employeeId = auth.currentUser?.uid
        if (employeeId == null || taskId == null) {
            Toast.makeText(this, "Task or user details missing. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }

        val data = hashMapOf(
            "taskId" to taskId,
            "employeeId" to employeeId,
            "hours" to hours,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("hoursWorked").add(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Hours logged successfully!", Toast.LENGTH_SHORT).show()
                updateTaskTotalHours(hours)
            }
            .addOnFailureListener { e ->
                Log.e("HoursWorked", "Error logging hours: ${e.message}")
                Toast.makeText(this, "Error logging hours. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateTaskTotalHours(hours: Double) {
        taskId?.let { id ->
            db.collection("tasks").document(id)
                .get()
                .addOnSuccessListener { document ->
                    val currentTotal = document.getDouble("totalHours") ?: 0.0
                    val newTotal = currentTotal + hours

                    db.collection("tasks").document(id)
                        .update("totalHours", newTotal)
                        .addOnSuccessListener {
                            Log.d("HoursWorked", "Total hours updated successfully.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("HoursWorked", "Error updating total hours: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("HoursWorked", "Error fetching current total hours: ${e.message}")
                }
        }
    }
}
