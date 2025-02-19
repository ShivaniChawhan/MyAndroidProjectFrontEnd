package com.example.hobo_collab_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.hobo_collab_app.com.example.hobo_collab_app.OTP_Screen
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


class SignUp: ComponentActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        val nameInput : EditText = findViewById(R.id.name_input)
        val mobileInput: EditText = findViewById(R.id.mobile_input)
        val emailInput: EditText = findViewById(R.id.email_input)
        val passwordInput: EditText = findViewById(R.id.password_input)

        val signUp : Button = findViewById(R.id.signup_button)
        signUp.setOnClickListener {

            val name = nameInput.text.toString()
            val mobile = mobileInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            performSignUp(name, mobile, email, password)

            val intent = Intent(this@SignUp, OTP_Screen::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
            finish()
        }

        val signIn: TextView = findViewById(R.id.sign_in)
        signIn.setOnClickListener {
            val intent = Intent(this@SignUp, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun performSignUp(name: String, mobile: String, email: String, password: String) {
        val url = "http://10.0.2.2:9001/api/user/signup"
        Log.d("SignUp", "URL: $url")

        val json = JSONObject().apply {
            put("name", name)
            put("mobile", mobile)
            put("email", email)
            put("password", password)
        }
        Log.d("SignUp", "Request JSON: $json")

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SignUp", "Network request failed", e)
                runOnUiThread {
                    Toast.makeText(this@SignUp, "Sign up failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("SignUp", "Response Code: ${response.code}")
                Log.d("SignUp", "Response Body: $responseBody")

                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val message = jsonResponse.getString("message")
                            Toast.makeText(this@SignUp, message, Toast.LENGTH_SHORT).show()
                            // Handle successful signup, e.g., navigate to another screen
                        } catch (e: Exception) {
                            Log.e("SignUp", "Failed to parse response", e)
                            Toast.makeText(this@SignUp, "Sign up failed: Invalid response", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignUp, "Sign up failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}