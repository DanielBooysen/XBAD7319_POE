package com.example.xbcad7319

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreDatabase: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance()
        firestoreDatabase = FirebaseFirestore.getInstance()

        // Sign in button click event
        findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            val email = findViewById<EditText>(R.id.email_input).text.toString().trim()
            val password = findViewById<EditText>(R.id.password_input).text.toString().trim()

            if (validateInputs(email, password)) {
                signInUser(email, password)
            }
        }
    }

    // Input validation function
    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // Function to sign in the user
    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        firestoreDatabase.collection("Users").document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    val role = document.getString("role")
                                    if (role == "Administrator") {
                                        // Redirect to Admin Home
                                        val intent = Intent(this, AdminDash::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else if (role == "Employee") {
                                        // Redirect to Employee Home
                                        val intent = Intent(this, EmpDash::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    Toast.makeText(this, "No such user found", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}