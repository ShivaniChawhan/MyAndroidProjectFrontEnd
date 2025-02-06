package com.example.hobo_collab_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.example.hobo_collab_app.ui.theme.Home_Screen

class Choose_Collab_screen: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.choose_collab_layout)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("Choose Collab", "Choose Collab Screen JWT Token $jwtToken")

        // after clicking back arrow return to home screen
        val backArrow : View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@Choose_Collab_screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to post collabration screen
        val collabType1 : FrameLayout = findViewById(R.id.colab_frame1)
        collabType1.setOnClickListener {
            val intent = Intent(this@Choose_Collab_screen, Post_Collabartion_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        val collabType2 : FrameLayout = findViewById(R.id.colab_frame2)
        collabType2.setOnClickListener {
            val intent = Intent(this@Choose_Collab_screen, Post_Collabartion_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        val collabType3 : FrameLayout = findViewById(R.id.colab_frame3)
        collabType3.setOnClickListener {
            val intent = Intent(this@Choose_Collab_screen, Post_Collabartion_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        val collabType4 : FrameLayout = findViewById(R.id.colab_frame4)
        collabType4.setOnClickListener {
            val intent = Intent(this@Choose_Collab_screen, Post_Collabartion_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // after clicking home button return to home screen
        val homeButton : View = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this@Choose_Collab_screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to profile screen
        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@Choose_Collab_screen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to my collab screen
        val myCollabScreen : View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@Choose_Collab_screen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
    }
}