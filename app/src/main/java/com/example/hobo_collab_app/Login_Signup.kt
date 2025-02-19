package com.example.hobo_collab_app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.example.hobo_collab_app.ui.theme.Home_Screen

class Login_Signup : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instagram_login_activity)

        val intent = intent
        val callbackUrl = intent.data.toString() // Get the URL of the callback

        // Check if the callback URL contains the access token or any other data
        if (callbackUrl.contains("access_token")) {
            // Handle the access token
            val token = extractTokenFromUrl(callbackUrl)
            // Use the token for further API calls or actions
        }

        // In Login_Signup activity
        val sharedPreferences = getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false) // Set login status to true
        editor.apply()


        // continue with instagram on click
        val loginBtn : FrameLayout = findViewById(R.id.continue_with_insta_button)
        loginBtn.setOnClickListener{
            // Define the REST API URL for the Instagram login
            val loginUrl = "http://10.0.2.2:9001/auth/facebook" // Use your backend URL

            // Create an intent to open the URL in the browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(loginUrl))

            startActivity(intent)
            finish()
        }
        // after clicking on cross button move back on home screen
        val closeButton: View = findViewById(R.id.cross_vector)
        closeButton.setOnClickListener {
            val intent = Intent(this@Login_Signup, Home_Screen::class.java)
            startActivity(intent)
            finish() // Close Login_Signup screen
        }


    }

    private fun extractTokenFromUrl(url: String): String {
        // Parse the URL to extract the token (this is an example and might vary)
        val tokenPattern = "access_token=([^&]+)"
        val regex = Regex(tokenPattern)
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: ""
    }
}