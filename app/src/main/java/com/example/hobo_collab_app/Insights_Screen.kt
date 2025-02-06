package com.example.hobo_collab_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.example.hobo_collab_app.ui.theme.Home_Screen

class Insights_Screen:ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.insights_screen)
        // after clicking back arrow return to home screen
        val backArrow : View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@Insights_Screen, App_Management_Screen::class.java)
            startActivity(intent)
            finish()
        }

        // navigate to home screen
        val homeScreen : View = findViewById(R.id.home_button)
        homeScreen.setOnClickListener{
            val intent = Intent(this@Insights_Screen, Home_Screen::class.java)
            startActivity(intent)
            finish()
        }
        // navigate to my collab screen
        val myCollabScreen : View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@Insights_Screen, MyCollabs_Screen::class.java)
            startActivity(intent)
            finish()
        }

        // navigate to user profile screen
        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@Insights_Screen, My_Profile_Screen::class.java)
            startActivity(intent)
            finish()
        }
    }
}