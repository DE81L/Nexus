package com.example.yourappname

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class RegisterActivity : AppCompatActivity() {
    private lateinit var editTextRegisterEmail: EditText
    private lateinit var editTextRegisterPassword: EditText
    private lateinit var editTextRegisterConfirmPassword: EditText
    private lateinit var buttonRegister: Button
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail)
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword)
        editTextRegisterConfirmPassword = findViewById(R.id.editTextRegisterConfirmPassword)
        buttonRegister = findViewById(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val email = editTextRegisterEmail.text.toString()
        val password = editTextRegisterPassword.text.toString()
        val confirmPassword = editTextRegisterConfirmPassword.text.toString()

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        if(email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        val users = loadUsers()
        if (users.any { it.email == email }) {
            Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
            return
        }

        val newUser = User(email, password)
        saveUser(newUser)

        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
        finish()