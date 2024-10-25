package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateEmp : AppCompatActivity() {
    private lateinit var firestoreDatabase: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_emp)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firestore and Firebase Authentication
        firestoreDatabase = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        val empNameInput: EditText = findViewById(R.id.emp_name_input)
        val empEmailInput: EditText = findViewById(R.id.emp_email_input)
        val empRoleInput: EditText = findViewById(R.id.emp_role_input)
        val empPasswordInput: EditText = findViewById(R.id.emp_password_input)
        val addEmployeeButton: Button = findViewById(R.id.add_employee_button)
        val backButton: Button = findViewById(R.id.back_button)

        // Add Employee Button logic
        addEmployeeButton.setOnClickListener {
            val empName = empNameInput.text.toString().trim()
            val empEmail = empEmailInput.text.toString().trim()
            val empRole = empRoleInput.text.toString().trim()
            val empPassword = empPasswordInput.text.toString().trim()

            if (empName.isEmpty() || empEmail.isEmpty() || empRole.isEmpty() || empPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (empPassword.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create the employee in Firebase Authentication
            auth.createUserWithEmailAndPassword(empEmail, empPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Get the user ID from Firebase Authentication
                        val userId = auth.currentUser?.uid

                        // Store the additional employee information in Firestore
                        if (userId != null) {
                            val employee = hashMapOf(
                                "name" to empName,
                                "email" to empEmail,
                                "role" to empRole
                            )

                            firestoreDatabase.collection("Users").document(userId)
                                .set(employee)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Employee added successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error adding employee: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        // Handle error during user creation in Firebase Authentication
                        Toast.makeText(this, "Failed to create employee: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Back Button logic
        backButton.setOnClickListener {
            val intent = Intent(this, AdminDash::class.java)
            startActivity(intent)
            finish()
        }
    }
}