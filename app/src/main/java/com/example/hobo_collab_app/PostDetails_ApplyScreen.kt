package com.example.hobo_collab_app

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.example.hobo_collab_app.ui.theme.Home_Screen
import com.example.hobo_collab_app.ui.theme.UserProfile
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

data class AppliedUserData (
    val userName: String,
    val followersCount: String?,
    val profilePic: String?,
    val title: String?,
    val descriptions: String?,
    val platform: List<String?>,
    val loginUserId: String?,
    val userID: String?,
    val status: String?,
    val isApplied: Boolean
)

class PostDetails_ApplyScreen : ComponentActivity() {

    private lateinit var titleView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var platformLogoContainer: FrameLayout
    private lateinit var requiredFollower: TextView
    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userFollowerCount: TextView
    private lateinit var userPostCount: TextView
    private lateinit var userBio: TextView
    private lateinit var userLocation: TextView
    private lateinit var applyButton: FrameLayout
    private var isApplied: Boolean = false

    private var collabData: Collab? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_details_apply_option)


        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("PostApply", "PostApply JWT Token $jwtToken")

        val loginUserId = extractUserIdFromJwt(jwtToken)

        // Extract userId from intent variable
        val userID = intent.getStringExtra("userID")
        Log.d("PostApply", "Extracted userId: $userID")

        // Initialize views
        titleView = findViewById(R.id.title_name)
        descriptionView = findViewById(R.id.description_body)
        platformLogoContainer = findViewById(R.id.plat_logos)
        requiredFollower = findViewById(R.id.required_fo_count)
        profileImage = findViewById(R.id.person_profile)
        userName = findViewById(R.id.person_name)
        userFollowerCount = findViewById(R.id.person_follower_count)
        userPostCount = findViewById(R.id.person_post_count)
        userBio = findViewById(R.id.person_bio)
        userLocation = findViewById(R.id.person_location)
        applyButton = findViewById(R.id.apply_button)

        // Fetch data
        fetchData(userID, loginUserId)

        // after clicking back arrow return to home screen
        val backArrow: View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@PostDetails_ApplyScreen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to public profile screen
        val personName: TextView = findViewById(R.id.person_name)
        personName.setOnClickListener {
            val intent = Intent(this@PostDetails_ApplyScreen, Public_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }
        val personBio: TextView = findViewById(R.id.person_bio)
        personBio.setOnClickListener {
            val intent = Intent(this@PostDetails_ApplyScreen, Public_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }
        val personLocation: TextView = findViewById(R.id.person_location)
        personLocation.setOnClickListener {
            val intent = Intent(this@PostDetails_ApplyScreen, Public_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }

        val personProfile: View = findViewById(R.id.person_profile)
        personProfile.setOnClickListener {
            val intent = Intent(this@PostDetails_ApplyScreen, Public_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }


        // click on apply button navigate to post details withdraw screen
        applyButton.setOnClickListener {
            collabData?.let {
                saveUserData(jwtToken, userID, loginUserId, it)
            }
        }

        // navigate to home screen
        val homeScreen: View = findViewById(R.id.home_button)
        homeScreen.setOnClickListener {
            val intent = Intent(this@PostDetails_ApplyScreen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        // navigate to my collab screen
        val myCollabScreen: View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@PostDetails_ApplyScreen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to user profile screen
        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@PostDetails_ApplyScreen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }
    }

    private fun extractUserIdFromJwt(jwtToken: String?): String? {
        if (jwtToken.isNullOrEmpty()) {
            Log.e("PostApply Screen", "JWT Token is null or empty")
            return null
        }

        return try {
            val jwt = JWT(jwtToken)
            val userId = jwt.getClaim("id").asString()
            if (userId.isNullOrEmpty()) {
                Log.e("PostApply Screen", "User ID claim is missing in the JWT Token")
                null
            } else {
                userId
            }
        } catch (e: Exception) {
            Log.e("PostApply Screen", "Error decoding JWT Token: ${e.message}", e)
            null
        }
    }

    private fun extractUserIdFromResponse(responseBody: String): String? {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter: JsonAdapter<AppliedUserData> = moshi.adapter(AppliedUserData::class.java)

        return try {
            val appliedUserData = adapter.fromJson(responseBody)
            appliedUserData?.userID
        } catch (e: Exception) {
            Log.e("PostDetails_ApplyScreen", "Error parsing JSON: ${e.message}", e)
            null
        }
    }

    private fun navigateToWithdrawScreen(jwtToken: String?, userID: String?) {
        val intent = Intent(this@PostDetails_ApplyScreen, PostDetails_WithdrawScreen::class.java)
        intent.putExtra("jwt_token", jwtToken)
        intent.putExtra("userID", userID)
        startActivity(intent)
        finish()
    }

    private fun saveUserData(jwtToken: String?, userID: String?, loginUserId: String?, collabData: Collab) {
        val client = OkHttpClient()


        // URLs for checking existing data
        val loginUserIdExistingUrl = "https://collab-api.hobo.video/api/applied-users/getUser/$loginUserId"
        Log.d("PostApply", "LoginUserId URL: $loginUserIdExistingUrl")
        val userIdExistingUrl = "https://collab-api.hobo.video/api/applied-users/getAppliedUser/$userID"
        Log.d("PostApply", "User ID URL: $userIdExistingUrl")

        // Function to handle the save operation
        fun performSaveOperation() {
            val url = "https://collab-api.hobo.video/api/applied-users/saveUser"
            val userData = mapOf(
                "userName" to userName.text.toString(),
                "followerCount" to userFollowerCount.text.toString(),
                "profilePic" to (profileImage.drawable as? String ?: ""),
                "title" to collabData.title,
                "descriptions" to collabData.description,
                "platforms" to collabData.platforms,
                "loginUserId" to loginUserId.orEmpty(),
                "userId" to userID.orEmpty(),
                "isApplied" to true
            )

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val jsonAdapter = moshi.adapter(Map::class.java)
            val json = jsonAdapter.toJson(userData)

            val mediaType = "application/json".toMediaType()
            val requestBody = json.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("API_ERROR", "Failed to save user data: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        Log.d("API_RESPONSE", "User data saved successfully")
                        runOnUiThread {
                            Toast.makeText(
                                this@PostDetails_ApplyScreen,
                                "Application submitted successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@PostDetails_ApplyScreen, PostDetails_WithdrawScreen::class.java)
                            intent.putExtra("jwt_token", jwtToken)
                            intent.putExtra("userID", userID)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Log.e("API_ERROR", "Failed to save user data: ${response.code}")
                    }
                }
            })
        }

        // Function to check existing data
        fun checkExistingData() {
            val requestLoginUserId = Request.Builder().url(loginUserIdExistingUrl).build()
            val requestUserId = Request.Builder().url(userIdExistingUrl).build()

            client.newCall(requestLoginUserId).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("API_ERROR", "ExitinguserId Failed to fetch loginUserId data: ${e.message}")

                }
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody.isNullOrEmpty()) {
                            // No existing data for loginUserId, check userId
                            client.newCall(requestUserId).enqueue(object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    Log.e("API_ERROR", "Failed to fetch UserId data: ${e.message}")
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    if (response.isSuccessful) {
                                        val responseBody = response.body?.string()
                                        if (responseBody.isNullOrEmpty()) {
                                            Log.d("DEBUG", "No Data for loginUserId & userID, proceeding to save")
                                            performSaveOperation()
                                        } else {
                                            val userIdInResponse = extractUserIdFromResponse(responseBody)
                                            if (userIdInResponse == userID) {
                                                runOnUiThread {
                                                    Log.d("DEBUG", "Navigating to PostDetails_WithdrawScreen")
                                                    Toast.makeText(
                                                        this@PostDetails_ApplyScreen,
                                                        "Already applied for the post.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navigateToWithdrawScreen(jwtToken, userID)
                                                }
                                            } else {
                                                performSaveOperation()
                                            }
                                        }
                                    } else {
                                        Log.e("API_ERROR", "Failed to fetch userId data: ${response.code}")
                                    }
                                }
                            })
                        } else {
                            val userIdInResponse = extractUserIdFromResponse(responseBody)
                            if (userIdInResponse == userID) {
                                runOnUiThread {
                                    Log.d("DEBUG", "Navigating to PostDetails_WithdrawScreen")
                                    Toast.makeText(
                                        this@PostDetails_ApplyScreen,
                                        "Already applied for the post.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navigateToWithdrawScreen(jwtToken, userID)
                                }
                            } else {
                                performSaveOperation()
                            }
                        }
                    }
                }
            })
        }


        // Start the check
        checkExistingData()
    }

    private fun fetchData(userId: String?, loginUserId: String?) {
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
                                if (collabList.isNotEmpty()) {
                                    collabData = collabList[0] // Assuming you want the first collab
                                    populateCollabs(collabList)
                                }
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
                            val listType = Types.newParameterizedType(List::class.java, UserProfile::class.java)
                            val jsonAdapter = moshi.adapter<List<UserProfile>>(listType)
                            val userProfileList = jsonAdapter.fromJson(json)

                            if (userProfileList != null) {
                                Log.i("profileData", "User profile $userProfileList ")
                                runOnUiThread {
                                    userProfileList.forEach { profile ->
                                        displayUserProfileData(profile)
                                    }
                                    applyButton.visibility = (if (userId == loginUserId) View.GONE
                                    else {
                                        if (isApplied) {
                                            startActivity(
                                                Intent(
                                                    this@PostDetails_ApplyScreen,
                                                    PostDetails_WithdrawScreen::class.java
                                                )
                                            )
                                            View.GONE
                                        } else {
                                            View.VISIBLE
                                        }
                                    }
                                            )
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
            Log.d(
                "CollabData",
                "Title: ${collab.title}, Description: ${collab.description}, Required Followers: ${collab.requiredFollowers}, Platforms: ${collab.platforms}"
            )
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

        Glide.with(this@PostDetails_ApplyScreen)
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