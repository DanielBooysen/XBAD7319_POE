package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EmpDash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emp_dash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up buttons
        val viewActiveTasksButton = findViewById<Button>(R.id.view_active_tasks_button)
        val viewCompletedTasksButton = findViewById<Button>(R.id.view_completed_tasks_button)
        val viewYourTasksButton = findViewById<Button>(R.id.view_your_tasks_button)
        val logHoursButton = findViewById<Button>(R.id.log_hours_button)
        val logoutButton = findViewById<Button>(R.id.logout_button)

        // Set click listeners for each button
        viewActiveTasksButton.setOnClickListener {
            // Navigate to ActiveTasks activity
            val intent = Intent(this, ActiveTasks::class.java)
            startActivity(intent)
            finish()
        }

        viewCompletedTasksButton.setOnClickListener {
            // Navigate to CompletedTasks activity
            val intent = Intent(this, CompletedTasks::class.java)
            startActivity(intent)
            finish()
        }

        viewYourTasksButton.setOnClickListener {
            // Navigate to YourTasks activity
            val intent = Intent(this, YourTasks::class.java)
            startActivity(intent)
            finish()
        }

        logHoursButton.setOnClickListener {
            // Navigate to HoursWorked activity
            val intent = Intent(this, HoursWorked::class.java)
            startActivity(intent)
            finish()
        }

        logoutButton.setOnClickListener {
            // Perform logout operation
            finish() // Finish activity and return to login
        }
    }
}