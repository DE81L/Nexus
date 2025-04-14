package com.example.yourappname

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var editTextLoginEmail: EditText
    private lateinit var editTextLoginPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewRegister: TextView
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextLoginEmail = findViewById(R.id.editTextLoginEmail)
        editTextLoginPassword = findViewById(R.id.editTextLoginPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewRegister = findViewById(R.id.textViewRegister)

        buttonLogin.setOnClickListener {
            login()
        }

        textViewRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val email = editTextLoginEmail.text.toString()
        val password = editTextLoginPassword.text.toString()

        val users = loadUsers()
        val user = users.find { it.email == email && it.password == password }

        if (user != null) {
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
            // Navigate to the main app screen here
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUsers(): List<User> {
        val file = File(filesDir, "users.json")
        if (!file.exists()) return emptyList()
        val json = file.readText()
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}