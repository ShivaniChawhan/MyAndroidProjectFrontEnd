package com.example.hobo_collab_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.hobo_collab_app.ui.theme.Home_Screen
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class Login : ComponentActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val emailInput : EditText = findViewById(R.id.email_input)
        val passwordInput : EditText = findViewById(R.id.password_input)

        val signIn : Button = findViewById(R.id.login_button)
        signIn.setOnClickListener {

            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            performLogin(email, password)
        }

        val signUp: TextView = findViewById(R.id.sign_up)
        signUp.setOnClickListener {
            val intent = Intent(this@Login, SignUp::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun performLogin(email: String, password: String) {
        val url = "https://collab-api.hobo.video/api/user/login"

        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }


        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@Login, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    val responseBody = response.body?.string()
                    val responseMessage = response.message
                    if (response.isSuccessful && responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        val message = jsonResponse.getString("message")
                        val token = jsonResponse.getString("token")
                        Log.d("Token", "Token Value: $token")
                        Toast.makeText(this@Login, message, Toast.LENGTH_SHORT).show()
                        // Handle successful login, e.g., navigate to another screen
                        // Store the token securely, e.g., in SharedPreferences
                        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("jwt_token", token)
                            apply()
                        }
                        // Pass the token to Home_Screen
                        val intent = Intent(this@Login, Home_Screen::class.java)
                        intent.putExtra("jwt_token", token)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Login, "Login failed: $responseMessage", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

}