package com.example.hobo_collab_app.com.example.hobo_collab_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.ComponentActivity
import com.example.hobo_collab_app.Login
import com.example.hobo_collab_app.R
import com.example.hobo_collab_app.SignUp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class OTP_Screen: ComponentActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otp_screen)

        val email = intent.getStringExtra("email") ?: ""

        val cross: View = findViewById(R.id.cross_vector)
        cross.setOnClickListener {
            val intent = Intent(this@OTP_Screen, SignUp::class.java)
            startActivity(intent)
            finish()
        }

        val otpDigits = listOf(
            findViewById<EditText>(R.id.otpDigit1),
            findViewById<EditText>(R.id.otpDigit2),
            findViewById<EditText>(R.id.otpDigit3),
            findViewById<EditText>(R.id.otpDigit4),
            findViewById<EditText>(R.id.otpDigit5),
            findViewById<EditText>(R.id.otpDigit6)
        )
        otpDigits.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < otpDigits.size - 1) {
                        otpDigits[index + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        val verifyButton: Button = findViewById(R.id.btnVerify)
        verifyButton.setOnClickListener {
            val otp = otpDigits.joinToString("") { it.text.toString() }
//            val email = "shiv14.chawhan@gmail.com"
            verifyOtp(email, otp, verifyButton)

            val intent = Intent(this@OTP_Screen, Login::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun verifyOtp(email: String, otp: String, verifyButton: Button) {
        val url = "http://10.0.2.2:9001/api/user/verifyotp"
        val json = JSONObject().apply {
            put("email", email)
            put("otp", otp)
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        Log.d("OTP_Screen", "Sending OTP verification request: $json")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OTP_Screen", "Network request failed", e)
                runOnUiThread {
                    Toast.makeText(this@OTP_Screen, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("OTP_Screen", "Response Code: ${response.code}")
                Log.d("OTP_Screen", "Response Body: $responseBody")
                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val message = jsonResponse.getString("message")
                            Toast.makeText(this@OTP_Screen, message, Toast.LENGTH_SHORT).show()
                            // Handle successful verification, e.g., navigate to another screen
                            verifyButton.setBackgroundColor(resources.getColor(R.color.verified))

                            verifyButton.text = "Verified"
                        } catch (e: Exception) {
                            Log.e("OTP_Screen", "Failed to parse response", e)
                            Toast.makeText(this@OTP_Screen, "Verification failed: Invalid response", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(this@OTP_Screen, "Verification failed: ${response.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
    }
}
