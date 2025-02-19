package com.example.hobo_collab_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


class Submit_report :ComponentActivity() {
    private val client = OkHttpClient()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.report_profile_screen)

        // Extract userId from jwtToken
        val userID = intent.getStringExtra("userID")
        Log.d("PublicProfile", "Extracted userId: $userID")

        // navigate back to public profile
        val submitReport : Button = findViewById(R.id.submit_btn)
        val issueInput: EditText = findViewById(R.id.issue_input)

        submitReport.setOnClickListener {
            val issueText = issueInput.text.toString()
            intent.putExtra("userID", userID)
            if (issueText.isNotBlank()) {
                submitReportToServer(issueText)
            } else {
                Toast.makeText(this, "Please enter an issue", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitReportToServer(issueText: String) {
        val url = "http://10.0.2.2:9001/api/report/report_profile" // Replace with your backend URL

        // Define the media type for JSON
        val JSON = "application/json; charset=utf-8".toMediaType()

        // Create a JSON object with the issue text
        val jsonObject = JSONObject()
        jsonObject.put("issue", issueText)

       /* val requestBody = FormBody.Builder()
            .add("issue", issueText)// Add necessary form data here
            .build()*/

        // Create a request body with the JSON object
        val requestBody = jsonObject.toString().toRequestBody(JSON)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@Submit_report, "Failed to submit report", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@Submit_report, "Report submitted!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@Submit_report, Public_Profile_Screen::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Submit_report, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}