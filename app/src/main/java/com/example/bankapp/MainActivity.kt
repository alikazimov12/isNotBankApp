package com.example.bankapp

import android.content.Context
import android.os.Bundle
import android.view.View
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
    private lateinit var confirmTransferButton: MaterialButton
    private lateinit var recipientCardNumberInput: EditText

    private var currentBalance = 0.00
    private var cardNumber: String? = null

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
        confirmTransferButton = findViewById(R.id.confirm_transfer_button)
        recipientCardNumberInput = findViewById(R.id.recipient_card_number)

        recipientCardNumberInput.visibility = View.GONE
        confirmTransferButton.visibility = View.GONE

        cardNumber = intent.getStringExtra("cardNumber")

        if (cardNumber != null) {
            val user = getUserByCardNumber(this, cardNumber!!)
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
            recipientCardNumberInput.visibility = View.VISIBLE
            confirmTransferButton.visibility = View.VISIBLE
        }

        confirmTransferButton.setOnClickListener {
            handleTransaction("transfer")
        }
    }

    private fun handleTransaction(transactionType: String) {
        val amountText = amountInput.text.toString().trim()

        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDouble()
        val senderCardNumber = cardNumber ?: return

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
                    return
                }
            }
            "transfer" -> {
                val recipientCardNumber = recipientCardNumberInput.text.toString().trim()

                if (recipientCardNumber.isEmpty()) {
                    Toast.makeText(this, "Please enter the recipient's card number", Toast.LENGTH_SHORT).show()
                    return
                }

                if (recipientCardNumber == senderCardNumber) {
                    Toast.makeText(this, "You cannot transfer money to yourself", Toast.LENGTH_SHORT).show()
                    return
                }

                val recipientUser = getUserByCardNumber(this, recipientCardNumber)
                if (recipientUser != null) {
                    if (amount <= currentBalance) {
                        currentBalance -= amount
                        val recipientBalance = recipientUser["balance"]?.toDouble() ?: 0.00
                        val newRecipientBalance = recipientBalance + amount

                        updateUserBalance(this, senderCardNumber, currentBalance)
                        updateUserBalance(this, recipientCardNumber, newRecipientBalance)

                        Toast.makeText(this, "Transferred $$amount to ${recipientUser["fullName"]}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Insufficient funds for transfer", Toast.LENGTH_SHORT).show()
                        return
                    }
                } else {
                    Toast.makeText(this, "Recipient not found", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }

        updateUserBalance(this, senderCardNumber, currentBalance)
        currentBalanceTextView.text = "$$currentBalance"
        amountInput.text?.clear()
        recipientCardNumberInput.text?.clear()
        recipientCardNumberInput.visibility = View.GONE
        confirmTransferButton.visibility = View.GONE
    }

    private fun getUserByCardNumber(context: Context, cardNumber: String): Map<String, String>? {
        return getUsers(context).find { it["cardNumber"] == cardNumber }
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
                        "fullName" to userDetails[3],
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

    private fun updateUserBalance(context: Context, cardNumber: String, newBalance: Double) {
        val users = getUsers(context).toMutableList()
        val userToUpdate = users.find { it["cardNumber"] == cardNumber }

        if (userToUpdate != null) {
            users.remove(userToUpdate)

            val updatedUser = userToUpdate.toMutableMap()
            updatedUser["balance"] = newBalance.toString()

            users.add(updatedUser)
        }

        saveUsers(context, users)
    }

    private fun saveUsers(context: Context, users: List<Map<String, String>>) {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val usersString = users.joinToString(",") { user ->
            "${user["email"]}:${user["password"]}:${user["cardNumber"]}:${user["fullName"]}:${user["balance"]}"
        }

        sharedPreferences.edit().putString("users", usersString).apply()
    }
}
