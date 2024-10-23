package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class AdminDash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up buttons
        val activeTasksButton = findViewById<Button>(R.id.active_tasks_button)
        val completedTasksButton = findViewById<Button>(R.id.completed_tasks_button)
        val yourTasksButton = findViewById<Button>(R.id.your_tasks_button)
        val hoursWorkedButton = findViewById<Button>(R.id.hours_worked_button)
        val addEmployeeButton = findViewById<Button>(R.id.add_employee_button)
        val logTaskButton = findViewById<Button>(R.id.log_task_button)
        val logoutButton = findViewById<Button>(R.id.logout_button)

        // Set click listeners for each button
        activeTasksButton.setOnClickListener {
            // Navigate to ActiveTasks activity
            val intent = Intent(this, ActiveTasks::class.java)
            startActivity(intent)
            finish()
        }

        completedTasksButton.setOnClickListener {
            // Navigate to CompletedTasks activity
            val intent = Intent(this, CompletedTasks::class.java)
            startActivity(intent)
            finish()
        }

        yourTasksButton.setOnClickListener {
            // Navigate to YourTasks activity
            val intent = Intent(this, YourTasks::class.java)
            startActivity(intent)
            finish()
        }

        hoursWorkedButton.setOnClickListener {
            // Navigate to HoursWorked activity
            val intent = Intent(this, HoursWorked::class.java)
            startActivity(intent)
            finish()
        }

        addEmployeeButton.setOnClickListener {
            // Navigate to CreateEmp activity to add a new employee
            val intent = Intent(this, CreateEmp::class.java)
            startActivity(intent)
            finish()
        }

        logTaskButton.setOnClickListener {
            // Navigate to CreateTask activity to log a new task
            val intent = Intent(this, CreateTask::class.java)
            startActivity(intent)
            finish()
        }

        logoutButton.setOnClickListener {
            // Perform logout operation
            finish() // Finish activity and return to login
        }
    }
}