package com.example.xbcad7319

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateTaskActivity  : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        // Initialize Firestore and Firebase Authentication
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val taskNameEditText: EditText = findViewById(R.id.taskNameEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        val dueDateEditText: EditText = findViewById(R.id.dueDateEditText)
        val logTaskButton: Button = findViewById(R.id.logTaskButton)

        logTaskButton.setOnClickListener {
            val taskName = taskNameEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val dueDate = dueDateEditText.text.toString().trim()

            if (taskName.isEmpty() || description.isEmpty() || dueDate.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get the current user ID
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                saveTaskToFirestore(userId, taskName, description, dueDate)
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTaskToFirestore(userId: String, taskName: String, description: String, dueDate: String) {
        val task = hashMapOf(
            "taskName" to taskName,
            "description" to description,
            "dueDate" to dueDate,
            "createdBy" to userId,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("tasks").add(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task successfully logged", Toast.LENGTH_SHORT).show()
                finish() // Close the activity after successful logging
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error logging task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
