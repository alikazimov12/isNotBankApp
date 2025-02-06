package com.example.isnotbankapp

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var accountHolderTextView: TextView
    private lateinit var accountNumberTextView: TextView
    private lateinit var currentBalanceTextView: TextView
    private lateinit var amountInput: EditText
    private lateinit var depositButton: MaterialButton
    private lateinit var withdrawButton: MaterialButton
    private lateinit var transferButton: MaterialButton
    private lateinit var selectTransactionButton: MaterialButton

    private var currentBalance = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accountHolderTextView = findViewById(R.id.account_holder)
        accountNumberTextView = findViewById(R.id.account_number)
        currentBalanceTextView = findViewById(R.id.current_balance)
        amountInput = findViewById(R.id.amount_input)
        depositButton = findViewById(R.id.deposit_button)
        withdrawButton = findViewById(R.id.withdraw_button)
        transferButton = findViewById(R.id.transfer_button)
        selectTransactionButton = findViewById(R.id.select_transaction_button)

        val cardNumber = intent.getStringExtra("cardNumber")

        if (cardNumber != null) {
            val user = getUserByCardNumber(this, cardNumber)
            if (user != null) {
                accountHolderTextView.text = user["fullName"]
                accountNumberTextView.text = user["cardNumber"]
                currentBalance = user["balance"]?.toDouble() ?: 0.00
                currentBalanceTextView.text = "$$currentBalance"
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            }
        }

        depositButton.setOnClickListener {
            handleTransaction("deposit")
        }

        withdrawButton.setOnClickListener {
            handleTransaction("withdraw")
        }

        transferButton.setOnClickListener {
            handleTransaction("transfer")
        }

        selectTransactionButton.setOnClickListener {
            Toast.makeText(this, "Transaction selection clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleTransaction(transactionType: String) {
        val amountText = amountInput.text.toString().trim()

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDouble()

        when (transactionType) {
            "deposit" -> {
                currentBalance += amount
                Toast.makeText(this, "Deposited $$amount", Toast.LENGTH_SHORT).show()
            }
            "withdraw" -> {
                if (amount <= currentBalance) {
                    currentBalance -= amount
                    Toast.makeText(this, "Withdrew $$amount", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Insufficient funds", Toast.LENGTH_SHORT).show()
                }
            }
            "transfer" -> {
                // Implement transfer logic (e.g., to another account)
                Toast.makeText(this, "Transferred $$amount", Toast.LENGTH_SHORT).show()
            }
        }

        currentBalanceTextView.text = "$$currentBalance"
        amountInput.text?.clear()
    }

    private fun getUserByCardNumber(context: Context, cardNumber: String): Map<String, String>? {
        val users = getUsers(context)
        return users.find { it["cardNumber"] == cardNumber }
    }

    private fun getUsers(context: Context): List<Map<String, String>> {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val usersString = sharedPreferences.getString("users", "") ?: ""

        return if (usersString.isNotEmpty()) {
            usersString.split(",").map { user ->
                val userDetails = user.split(":")
                if (userDetails.size == 4) {
                    mapOf(
                        "email" to userDetails[0],
                        "password" to userDetails[1],
                        "cardNumber" to userDetails[2],
                        "fullName" to userDetails[3],
                        "balance" to userDetails.getOrElse(4) { "0.00" }
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
