package com.example.hobo_collab_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.ComponentActivity
import com.example.hobo_collab_app.ui.theme.Home_Screen
import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.reflect.TypeToken


data class ProfileData(
   //val _id: String,
    val profilePic: String?,
    val name: String?,
    //val instagramHandle: String,
//    val followersCount: String,
//    val niche: String,
//    val about: String,
//    val portfolioLink: String
)

class My_Profile_Screen: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_profile)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("MyProfile", "MyProfile JWT Token $jwtToken")

        // Extract userId from jwtToken
        val userId = extractUserIdFromJwt(jwtToken)
        Log.d("MyProfile", "Extracted userId: $userId")

        fetchProfileData(userId)

        // after clicking back arrow return to home screen
        val backArrow : View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@My_Profile_Screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to setting screen
        val setting : View = findViewById(R.id.setting_icon)
        setting.setOnClickListener{
            val intent = Intent(this@My_Profile_Screen, Setting_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to my view screen
        val myView : TextView = findViewById(R.id.more_btn)
        myView.setOnClickListener{
            val intent = Intent(this@My_Profile_Screen, MyView_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to personal information screen
        val editProfile : FrameLayout = findViewById(R.id.edit_profile_btn)
        editProfile.setOnClickListener{
            val intent = Intent(this@My_Profile_Screen, Personal_Info_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to applied collab column
        val appliedColumn : FrameLayout = findViewById(R.id.applied_collab_btn)
        appliedColumn.setOnClickListener{
            val intent = Intent(this@My_Profile_Screen, MyProfile_AppliedScreen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to saved column
        val savedColumn : FrameLayout =  findViewById(R.id.saved_btn)
        savedColumn.setOnClickListener{
            val intent = Intent(this@My_Profile_Screen, MyProfile_SavedScreen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // after clicking home button return to home screen
        val homeButton : View = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this@My_Profile_Screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to my collab screen
        val myCollabScreen : View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@My_Profile_Screen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

    }

    private fun extractUserIdFromJwt(jwtToken: String?): String? {
        if (jwtToken.isNullOrEmpty()) {
            Log.e("MyProfile Screen", "JWT Token is null or empty")
            return null
        }

        return try {
            val jwt = JWT(jwtToken)
            val userId = jwt.getClaim("id").asString()
            if (userId.isNullOrEmpty()) {
                Log.e("MyProfile Screen", "User ID claim is missing in the JWT Token")
                null
            } else {
                userId
            }
        } catch (e: Exception) {
            Log.e("MyProfile Screen", "Error decoding JWT Token: ${e.message}", e)
            null
        }
    }

    private fun fetchProfileData(userID: String?) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:9001/api/getProfile/$userID") // Replace with your actual URL
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch profile data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let {
                        val gson = Gson()
                        val listType = object : TypeToken<List<ProfileData>>() {}.type
                        val profileDataList: List<ProfileData> = gson.fromJson(it, listType)
                        Log.d("ProfileData", "Profile Data List: $profileDataList")
                        runOnUiThread {
                            if (profileDataList.isNotEmpty()) {
                                updateUIWithProfileData(profileDataList[0]) // Assuming you want the first item
                            } else {
                                Log.e("API_ERROR", "Profile data list is empty")
                            }
                        }
                    } ?: Log.e("API_ERROR", "Empty response body")
                } else {
                    Log.e("API_ERROR", "Unsuccessful response: ${response.code}")
                }
            }
        })
    }

    private fun updateUIWithProfileData(profileData: ProfileData) {
        val userProfileView = findViewById<View>(R.id.user_profile)
        val userNameTextView = findViewById<TextView>(R.id.user_name)

        // Update the TextView with the user's name
        userNameTextView.text = profileData.name

        // Load the profile picture if available
        profileData.profilePic?.let {
            Glide.with(this)
                .load(it)
                .apply(RequestOptions.circleCropTransform())
                .into(userProfileView as ImageView) // Ensure user_profile is an ImageView
        }
    }
}