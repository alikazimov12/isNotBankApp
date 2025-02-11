package com.example.bankapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var cardNumberEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        cardNumberEditText = findViewById(R.id.cardnumber)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.login_button)
        signUpTextView = findViewById(R.id.registerBtn)

        loginButton.setOnClickListener {
            val cardNumber = cardNumberEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (cardNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            } else {
                val user = authenticateUser(this, cardNumber, password)
                if (user != null) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("cardNumber", user["cardNumber"])
                    intent.putExtra("balance", user["balance"])
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        signUpTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun authenticateUser(context: Context, cardNumber: String, password: String): Map<String, String>? {
        val users = getUsers(context)
        return users.find { it["cardNumber"] == cardNumber && it["password"] == password }
    }

    private fun getUsers(context: Context): List<Map<String, String>> {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val usersString = sharedPreferences.getString("users", "") ?: ""

        return if (usersString.isNotEmpty()) {
            usersString.split(",").map { user ->
                val userDetails = user.split(":")
                if (userDetails.size == 5) {
                    mapOf(
                        "email" to userDetails[0],
                        "password" to userDetails[1],
                        "cardNumber" to userDetails[2],
                        "fullname" to userDetails[3],
                        "balance" to userDetails[4]
                    )
                } else {
                    emptyMap()
                }
            }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
    }
}
