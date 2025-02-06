package com.example.hobo_collab_app

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.example.hobo_collab_app.PostDetails_ApplyScreen
import com.example.hobo_collab_app.ui.theme.Home_Screen
import com.example.hobo_collab_app.ui.theme.UserProfile
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
class PostDetails_WithdrawScreen : ComponentActivity() {

    private lateinit var titleView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var platformLogoContainer: FrameLayout
    private lateinit var requiredFollower: TextView
    private lateinit var profileImage : ImageView
    private lateinit var userName : TextView
    private lateinit var userFollowerCount : TextView
    private lateinit var userPostCount : TextView
    private lateinit var userBio : TextView
    private lateinit var userLocation : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_details_withdraw_option)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("PostWithdraw", "PostWithdraw JWT Token $jwtToken")

        // Extract userId from intent variable
        val userID = intent.getStringExtra("userID")
        Log.d("PostWithdraw", "Extracted userId: $userID")

        // Initialize views
        titleView = findViewById(R.id.title_name)
        descriptionView = findViewById(R.id.description_body)
        platformLogoContainer = findViewById(R.id.plat_logos)
        requiredFollower =  findViewById(R.id.required_fo_count)
        profileImage = findViewById(R.id.person_profile)
        userName = findViewById(R.id.person_name)
        userFollowerCount = findViewById(R.id.person_follower_count)
        userPostCount = findViewById(R.id.person_post_count)
        userBio = findViewById(R.id.person_bio)
        userLocation = findViewById(R.id.person_location)

        // Fetch data
        fetchData(userID)

        // after clicking back arrow return to home screen
        val backArrow : View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@PostDetails_WithdrawScreen, PostDetails_ApplyScreen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }

        // navigate to public profile screen
        val personName : TextView = findViewById(R.id.person_name)
        personName.setOnClickListener{
            val intent = Intent(this@PostDetails_WithdrawScreen, Public_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }
        val personBio : TextView = findViewById(R.id.person_bio)
        personBio.setOnClickListener{
            val intent = Intent(this@PostDetails_WithdrawScreen, Public_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }
        val personLocation : TextView = findViewById(R.id. person_location)
        personLocation.setOnClickListener {
            val intent = Intent(this@PostDetails_WithdrawScreen, Public_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }

        val personProfile : View = findViewById(R.id.person_profile)
        personProfile.setOnClickListener{
            val intent = Intent(this@PostDetails_WithdrawScreen, Public_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }


        // navigate back to apply screen
        val withdraw : FrameLayout = findViewById(R.id.withdraw_button)
        withdraw.setOnClickListener{
            // toast a message
            Toast.makeText(this@PostDetails_WithdrawScreen,"Application withdraw successfully!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@PostDetails_WithdrawScreen, PostDetails_ApplyScreen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }


        // navigate to home screen
        val homeScreen : View = findViewById(R.id.home_button)
        homeScreen.setOnClickListener{
            val intent = Intent(this@PostDetails_WithdrawScreen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        // navigate to my collab screen
        val myCollabScreen : View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@PostDetails_WithdrawScreen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to user profile screen
        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@PostDetails_WithdrawScreen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
    }
    private fun extractUserIdFromJwt(jwtToken: String?): String? {
        if (jwtToken.isNullOrEmpty()) {
            Log.e("PostWithdraw Screen", "JWT Token is null or empty")
            return null
        }

        return try {
            val jwt = JWT(jwtToken)
            val userId = jwt.getClaim("id").asString()
            if (userId.isNullOrEmpty()) {
                Log.e("PostWithdraw Screen", "User ID claim is missing in the JWT Token")
                null
            } else {
                userId
            }
        } catch (e: Exception) {
            Log.e("PostWithdraw Screen", "Error decoding JWT Token: ${e.message}", e)
            null
        }
    }

    private fun fetchData(userId: String?) {
        val client = OkHttpClient()
        val request1 = Request.Builder()
            .url("https://collab-api.hobo.video/api/post-collab/$userId")
            .build()

        // Second URL request
        val request2 = Request.Builder()
            .url("https://collab-api.hobo.video/api/getProfile/$userId") // Ensure this is the correct endpoint
            .build()

        client.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("API_RESPONSE", "Response request1: $responseBody") // Log the response
                    responseBody?.let {
                        val moshi = Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .build()
                        val listType = Types.newParameterizedType(List::class.java, Collab::class.java)
                        val collabAdapter = moshi.adapter<List<Collab>>(listType)
                        val collabs = collabAdapter.fromJson(it)

                        collabs?.let { collabList ->
                            runOnUiThread {
                                populateCollabs(collabList)
                            }
                        } ?: Log.e("API_ERROR", "Failed to parse collab data")
                    } ?: Log.e("API_ERROR", "Empty response body")
                } else {
                    Log.e("API_ERROR", "Unsuccessful response: ${response.code}")
                }
            }
        })

        // fetching data from second URL
        client.newCall(request2).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch data from first URL: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        try {
                            val json = responseBody.string()
                            Log.d("JSON Response", json)
                            val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .build()
//                            val jsonAdapter = moshi.adapter(UserProfile::class.java)
                            val listType = Types.newParameterizedType(List::class.java, com.example.hobo_collab_app.ui.theme.UserProfile::class.java)
                            val jsonAdapter = moshi.adapter<List<UserProfile>>(listType)
                            val userProfileList  = jsonAdapter.fromJson(json)

                            if (userProfileList  != null) {
                                Log.i("profileData", "User profile $userProfileList ")
                                runOnUiThread {
                                    userProfileList.forEach { profile ->
                                        displayUserProfileData(profile)
                                    }
                                }
                            } else {
                                Log.e("JSON Parsing Error", "Parsed response is null")
                                runOnUiThread {
                                    populateProfiles(emptyList()) // Return an empty list if parsing results in null
                                }
                            }
                        } catch (e: IOException) {
                            Log.e("IO Error", "Error reading response body: ${e.message}")
                            runOnUiThread {
                                populateProfiles(emptyList()) // Return an empty list on IO error
                            }
                        } catch (e: Exception) {
                            Log.e("JSON Parsing Error", "Error parsing JSON: ${e.message}")
                            runOnUiThread {
                                populateProfiles(emptyList()) // Return an empty list on parsing error
                            }
                        }
                    } ?: run {
                        Log.e("API_ERROR", "Response body is null")
                        runOnUiThread {
                            populateProfiles(emptyList()) // Return an empty list if response body is null
                        }
                    }
                } else {
                    Log.e("API_ERROR", "Unsuccessful response for user profile: ${response.code}")
                    runOnUiThread {
                        populateProfiles(emptyList()) // Return an empty list on unsuccessful response
                    }
                }
            }
        })
    }
    private fun populateCollabs(collabs: List<Collab>) {
        collabs.forEach { collab ->
            Log.d("CollabData", "Title: ${collab.title}, Description: ${collab.description}, Required Followers: ${collab.requiredFollowers}")
            displayCollabData(collab)
        }
    }


    private fun displayCollabData(collabData: Collab) {
        titleView.text = collabData.title
        descriptionView.text = collabData.description
        requiredFollower.text = collabData.requiredFollowers

        // Clear any existing views in the container
        platformLogoContainer.removeAllViews()

        // Create a new LinearLayout
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 16, 0, 0)
            }
        }

        // Add ImageViews for each platform
        collabData.platforms.forEach { platform ->
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(64, 64).apply {
                    setMargins(16, 16, 0, 0)
                }
                setImageDrawable(getPlatformDrawable(platform))
            }
            linearLayout.addView(imageView)
        }

        // Add the LinearLayout to the FrameLayout
        platformLogoContainer.addView(linearLayout)
    }

    private fun populateProfiles(profiles: List<UserProfile>) {
        profiles.forEach { profile ->
            Log.d("ProfileData", "Name: ${profile.name}, Followers: ${profile.followersCount}")
            displayUserProfileData(profile)
        }
    }
    private fun displayUserProfileData(profile: UserProfile) {
        userName.text = profile.name
        userFollowerCount.text = profile.followersCount
        userBio.text = profile.niche
        userLocation.text = profile.about

        Glide.with(this@PostDetails_WithdrawScreen)
            .load(profile.profilePic.takeIf { !it.isNullOrEmpty() } ?: R.drawable.creator_image)
            .into(profileImage)
    }

    private fun getPlatformDrawable(platform: String): Drawable? {
        return when (platform) {
            "Instagram" -> getDrawable(R.drawable.insta_logo) // Replace with your drawable resource
            "Facebook" -> getDrawable(R.drawable.facebook_logo) // Replace with your drawable resource
            "Snapchat" -> getDrawable(R.drawable.snapchat_logo) // Replace with your drawable resource
            else -> getDrawable(R.drawable.logo1) // Replace with a default logo
        }
    }
}