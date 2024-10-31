package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDash : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dash)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val activeTasksButton = findViewById<Button>(R.id.active_tasks_button)
        val completedTasksButton = findViewById<Button>(R.id.completed_tasks_button)
        val yourTasksButton = findViewById<Button>(R.id.your_tasks_button)
        val hoursWorkedButton = findViewById<Button>(R.id.hours_worked_button)
        val addEmployeeButton = findViewById<Button>(R.id.add_employee_button)
        val logTaskButton = findViewById<Button>(R.id.log_task_button)
        val logoutButton = findViewById<Button>(R.id.logout_button)

        // Button listeners with debugging
        activeTasksButton.setOnClickListener {
            startActivity(Intent(this, ActiveTasks::class.java))
        }

        completedTasksButton.setOnClickListener {
            startActivity(Intent(this, CompletedTasks::class.java))
        }

        yourTasksButton.setOnClickListener {
            startActivity(Intent(this, YourTasks::class.java))
        }

        hoursWorkedButton.setOnClickListener {
            navigateBasedOnRole()
        }

        addEmployeeButton.setOnClickListener {
            startActivity(Intent(this, CreateEmp::class.java))
        }

        logTaskButton.setOnClickListener {
            startActivity(Intent(this, CreateTask::class.java))
        }

        logoutButton.setOnClickListener {
            finish() // Perform logout
        }
    }

    private fun navigateBasedOnRole() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("Users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role == "Administrator") {
                        startActivity(Intent(this, AdminTaskDetails::class.java))
                    } else {
                        startActivity(Intent(this, HoursWorked::class.java))
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error checking user role: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}
