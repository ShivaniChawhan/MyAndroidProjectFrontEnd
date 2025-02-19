package com.example.hobo_collab_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squareup.moshi.JsonAdapter
import com.example.hobo_collab_app.ui.theme.Home_Screen
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

data class UserData(
    val userID: String?, // Required field
    val name: String?,  // Optional field
    val profilePic: String? // Optional field
)


class Public_Profile_Screen :ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.public_profile)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("PublicProfile", "Public JWT Token $jwtToken")

        // Extract userId from jwtToken
        val userID = intent.getStringExtra("userID")
        Log.d("PublicProfile", "Extracted userId: $userID")

        // Fetch user profile data
        fetchUserProfileData(userID)

        // after clicking back arrow return to home screen
        val backArrow : View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@Public_Profile_Screen, PostDetails_ApplyScreen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }

        // Navigate to submit report screen
        val flag : View = findViewById(R.id.flag)
        flag.setOnClickListener{
            val intent = Intent(this@Public_Profile_Screen, Submit_report::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }

        // navigate to public view screen
        val more_button : TextView = findViewById(R.id.more_btn)
        more_button.setOnClickListener{
            val intent = Intent(this@Public_Profile_Screen, Public_ViewScreen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }


        // after clicking home button return to home screen
        val homeButton : View = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this@Public_Profile_Screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        // navigate to my collab screen
        val myCollabScreen : View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@Public_Profile_Screen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to user profile screen
        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@Public_Profile_Screen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
    }


    private fun extractUserIdFromJwt(jwtToken: String?): String? {
        if (jwtToken.isNullOrEmpty()) {
            Log.e("PublicProfile Screen", "JWT Token is null or empty")
            return null
        }

        return try {
            val jwt = JWT(jwtToken)
            val userId = jwt.getClaim("id").asString()
            if (userId.isNullOrEmpty()) {
                Log.e("PublicProfile Screen", "User ID claim is missing in the JWT Token")
                null
            } else {
                userId
            }
        } catch (e: Exception) {
            Log.e("PublicProfile Screen", "Error decoding JWT Token: ${e.message}", e)
            null
        }
    }

    private fun fetchUserProfileData(userId: String?) {
        if (userId == null) {
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show()
            return
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:9001/api/getProfile/$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch user profile data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val json = responseBody.string()
                        Log.d("JSON Response", json)

                        val moshi = Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .build()
                        val listType = Types.newParameterizedType(List::class.java, UserData::class.java)
                        val jsonAdapter: JsonAdapter<List<UserData>> = moshi.adapter(listType)

                        try {
                            val userProfileList = jsonAdapter.fromJson(json)
                            userProfileList?.forEach { userProfile ->
                                if (userProfile.userID.toString().isBlank()) {
                                    Log.e("API_ERROR", "User ID is missing in the response")
                                } else {
                                    runOnUiThread {
                                        updateUIWithUserProfile(userProfile)
                                    }
                                }
                           /* userProfileList?.firstOrNull()?.let {
                                if (it.userId.isNotBlank()) {
                                    runOnUiThread {
                                        updateUIWithUserProfile(it)
                                    }
                                } else {
                                    Log.e("API_ERROR", "User ID is missing in the response")
                                }*/
                            } ?: Log.e("API_ERROR", "User profile list is empty or null")
                        } catch (e: Exception) {
                            Log.e("JSON_PARSE_ERROR", "Failed to parse JSON: ${e.message}", e)
                        }
                    } ?: Log.e("API_ERROR", "Response body is null")
                } else {
                    Log.e("API_ERROR", "Unsuccessful response: ${response.code}")
                }
            }

        })
    }

    private fun updateUIWithUserProfile(userProfile: UserData) {
        val userProfileView = findViewById<ImageView>(R.id.user_profile)
        val userNameTextView = findViewById<TextView>(R.id.user_name)

        // Update the TextView with the user's name
        userNameTextView.text = userProfile.name

        // Load the profile picture if available, otherwise set a default image from drawable
        if (userProfile.profilePic != null) {
            Glide.with(this)
                .load(userProfile.profilePic)
                .apply(RequestOptions.circleCropTransform())
                .into(userProfileView)
        } else {
            userProfileView.setImageResource(R.drawable.creator_image) // Replace with your actual drawable resource name
        }
    }
}