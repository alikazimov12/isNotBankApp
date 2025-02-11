package com.example.bankapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var cardNumberEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fullNameEditText = findViewById(R.id.full_name)
        emailEditText = findViewById(R.id.email)
        cardNumberEditText = findViewById(R.id.cardnumber)
        passwordEditText = findViewById(R.id.password)
        confirmPasswordEditText = findViewById(R.id.confirm_password)
        registerButton = findViewById(R.id.create_account_button)

        registerButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val cardNumber = cardNumberEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            val balance = 0.00

            if (fullName.isEmpty() || email.isEmpty() || cardNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addUser(this, email, password, cardNumber, fullName, balance)

            Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun addUser(context: Context, email: String, password: String, cardNumber: String, fullname: String, balance: Double) {
        val users = getUsers(context).toMutableList()
        val newUser = "$email:$password:$cardNumber:$fullname:$balance"
        users.add(newUser)
        saveUsers(context, users)
    }

    private fun getUsers(context: Context): List<String> {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val usersString = sharedPreferences.getString("users", "") ?: ""

        return if (usersString.isNotEmpty()) {
            usersString.split(",")
        } else {
            emptyList()
        }
    }

    private fun saveUsers(context: Context, users: List<String>) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val usersString = users.joinToString(",")
        sharedPreferences.edit().putString("users", usersString).apply()
    }
}
