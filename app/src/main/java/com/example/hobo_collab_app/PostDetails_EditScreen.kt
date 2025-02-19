package com.example.hobo_collab_app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.hobo_collab_app.ui.theme.Home_Screen
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class PostDetails_EditScreen : ComponentActivity() {

    private lateinit var titleView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var platformLogoContainer: FrameLayout
    private lateinit var requiredFollower: TextView


    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_details_delete_option)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("PostApply", "PostApply JWT Token $jwtToken")

        // Extract userId from intent variable
        val userID = intent.getStringExtra("userID")
        Log.d("PostApply", "Extracted userId: $userID")

        // Initialize views
        titleView = findViewById(R.id.title_name)
        descriptionView = findViewById(R.id.description_body)
        platformLogoContainer = findViewById(R.id.plat_logos)
        requiredFollower =  findViewById(R.id.required_fo_count)

        // Fetch data
        fetchData()

        // after clicking back arrow return to home screen
        val backArrow : View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@PostDetails_EditScreen, MyCollabs_Screen::class.java)
            startActivity(intent)
            finish()
        }
        // navigate to my posted collabs page
        val delete : FrameLayout = findViewById(R.id.delete_btn)
        delete.setOnClickListener {
            val intent = Intent(this@PostDetails_EditScreen, MyCollabs_Screen::class.java)
            startActivity(intent)
            finish()
        }

        // navigate to all applications screen
        val viewApplication : FrameLayout = findViewById(R.id.view_application_button)
        viewApplication.setOnClickListener{
            val intent = Intent(this@PostDetails_EditScreen,App_Management_Screen::class.java)
            startActivity(intent)
            finish()
        }

        // navigate to home screen
        val homeScreen : View = findViewById(R.id.home_button)
        homeScreen.setOnClickListener{
            val intent = Intent(this@PostDetails_EditScreen, Home_Screen::class.java)
            startActivity(intent)
            finish()
        }

        // navigate to my collab screen
        val myCollabScreen : View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@PostDetails_EditScreen, MyCollabs_Screen::class.java)
            startActivity(intent)
            finish()
        }

        // navigate to user profile screen
        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@PostDetails_EditScreen, My_Profile_Screen::class.java)
            startActivity(intent)
            finish()
        }

        val edit: FrameLayout = findViewById(R.id.edit_btn)
        edit.setOnClickListener {
            val intent = Intent(this@PostDetails_EditScreen, Post_Collabartion_Screen::class.java)
            startActivity(intent)
            finish()
        }

        val viewApplicationBtn :FrameLayout = findViewById(R.id.view_application_button)
        viewApplicationBtn.setOnClickListener {
            val intent = Intent(this@PostDetails_EditScreen, App_Management_Screen::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun fetchData() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:9001/api/post-collab")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("API_RESPONSE", "Response: $responseBody") // Log the response
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


    private fun getPlatformDrawable(platform: String): Drawable? {
        return when (platform) {
            "Instagram" -> getDrawable(R.drawable.insta_logo) // Replace with your drawable resource
            "Facebook" -> getDrawable(R.drawable.facebook_logo) // Replace with your drawable resource
            "Snapchat" -> getDrawable(R.drawable.snapchat_logo) // Replace with your drawable resource
            else -> getDrawable(R.drawable.logo1) // Replace with a default logo
        }
    }

}