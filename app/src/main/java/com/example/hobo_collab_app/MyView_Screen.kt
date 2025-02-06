package com.example.hobo_collab_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.ComponentActivity

class MyView_Screen:ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_view)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("MyProfile", "MyProfile JWT Token $jwtToken")


        // navigate back to my profile
        val goBack : LinearLayout = findViewById(R.id.more_platform_option)
        goBack.setOnClickListener{
            val intent = Intent(this@MyView_Screen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
    }

}
