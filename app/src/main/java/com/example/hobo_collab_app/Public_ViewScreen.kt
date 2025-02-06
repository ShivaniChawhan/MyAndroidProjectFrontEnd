package com.example.hobo_collab_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class Public_ViewScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.public_view)

        // Extract userId from jwtToken
        val userID = intent.getStringExtra("userID")
        Log.d("PublicProfile", "Extracted userId: $userID")

        // navigate back to public profile
        val goBack : LinearLayout = findViewById(R.id.more_platform_option)
        goBack.setOnClickListener{
            val intent = Intent(this@Public_ViewScreen, Public_Profile_Screen::class.java)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }

    }
}