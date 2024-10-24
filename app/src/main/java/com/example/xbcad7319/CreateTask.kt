package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateTask : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_task)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize Firestore and Firebase Authentication
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val taskNameEditText: EditText = findViewById(R.id.taskNameEditText)
        val descriptionEditText: EditText = findViewById(R.id.descriptionEditText)
        val dueDateEditText: EditText = findViewById(R.id.dueDateEditText)
        val logTaskButton: Button = findViewById(R.id.logTaskButton)
        val employeeSpinner: Spinner = findViewById(R.id.employeeSpinner)

        // Fetch employees from Firestore
        db.collection("Users")
            .whereEqualTo("role", "Employee")
            .get()
            .addOnSuccessListener { documents ->
                val employeeList = mutableListOf("Unassigned")
                for (document in documents) {
                    val employeeName = document.getString("name") ?: "Unknown"
                    employeeList.add(employeeName)
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, employeeList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                employeeSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching employees: ${e.message}", Toast.LENGTH_SHORT).show()
            }


        logTaskButton.setOnClickListener {
            val taskName = taskNameEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val dueDate = dueDateEditText.text.toString().trim()
            val selectedEmployee = employeeSpinner.selectedItem.toString()

            if (taskName.isEmpty() || description.isEmpty() || dueDate.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val assignedEmployee = if (selectedEmployee == "Unassigned") "Unassigned" else selectedEmployee
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                saveTaskToFirestore(userId, taskName, description, dueDate, assignedEmployee)
            }
        }

        // Set up the Back button
        val backButton: Button = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            navigateToDashboard()
        }
    }

    private fun saveTaskToFirestore(userId: String, taskName: String, description: String, dueDate: String, assignedEmployee: String?) {
        val task = hashMapOf(
            "taskName" to taskName,
            "description" to description,
            "dueDate" to dueDate,
            "createdBy" to userId,
            "createdAt" to System.currentTimeMillis(),
            "assignedEmployee" to assignedEmployee,
            "status" to "In-Progress"
        )

        db.collection("tasks").add(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task successfully logged", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error logging task: ${e.message}", Toast.LENGTH_SHORT).show()
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