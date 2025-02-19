package com.example.hobo_collab_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import okhttp3.*
import androidx.activity.ComponentActivity
import com.auth0.android.jwt.JWT
import com.example.hobo_collab_app.ui.theme.Home_Screen
import com.google.gson.Gson
import okhttp3.MediaType.Companion.parse
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException
import org.bson.codecs.pojo.annotations.BsonId

data class CollaborationPost(
    val title: String,
    val description: String,
    val requiredFollowers: String,
    val platforms: List<String>,
    @BsonId val userId: String,
    var isApplied: Boolean = false
)


class Post_Collabartion_Screen : ComponentActivity() {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val selectedPlatforms = mutableListOf<String>()
    private var userId: String? = "" // Global variable for user ID

    private lateinit var instagramIcon: View
    private lateinit var snapchatIcon: View
    private lateinit var facebookIcon: View

    @SuppressLint("CutPasteId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_collabaration)



        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("Post Collab Screen", "Post Collab Screen JWT Token $jwtToken")
        if (jwtToken != null) {
            try {
                val jwt = JWT(jwtToken)
                userId = jwt.getClaim("id").asString()
                Log.d("Post Collab Screen", "User ID from JWT Token: $userId")
            } catch (e: Exception) {
                Log.e("Post Collab Screen", "Error decoding JWT Token", e)
            }
        } else {
            Log.e("Post Collab Screen", "JWT Token is null")
        }

        Log.d("PostCollaboration", "Post_Collabartion Screen created")

        val titleEditText: EditText = findViewById(R.id.title_box)
        val descriptionEditText: EditText = findViewById(R.id.description_box)
        val postButton: FrameLayout = findViewById(R.id.post_collab_btn)
        Log.d("PostCollabButton Tag", "Value of postButton: $postButton")
        val requiredFollowerFrame: FrameLayout = findViewById(R.id.required_follower_frame)
        val requiredFollowerText: TextView = findViewById(R.id.required_follower_text)
        val requiredFollowersSpinner: Spinner = findViewById(R.id.requiredFollowersSpinner)
        val downVector: ImageView = findViewById(R.id.down_vector)


        instagramIcon = findViewById(R.id.insta_outline)
        snapchatIcon = findViewById(R.id.snapchat)
        facebookIcon = findViewById(R.id.facebook)


        // after clicking back arrow return to home screen
        val backArrow: View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@Post_Collabartion_Screen, Choose_Collab_screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // drop down options
        val followersOptions = resources.getStringArray(R.array.followers_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, followersOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        requiredFollowersSpinner.adapter = adapter

        // post button integration
        postButton.setOnClickListener {
            try {
                Log.d("PostCollaboration", "Post button -> Clicked")
                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val requiredFollowers = requiredFollowersSpinner.selectedItem.toString()
                val userID = userId.toString()

                if (title.isNotEmpty() && description.isNotEmpty() && requiredFollowers.isNotEmpty()) {
                    val newPost = CollaborationPost(title, description, requiredFollowers, selectedPlatforms, userID)
                    Log.d("PostCollaboration", "Post button -> New post: $newPost")
                    // Execute network operation on a background thread
                    Thread {
                        savePostToDatabase(newPost)
                    }.start()
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
                if (selectedPlatforms.isEmpty()) {
                    Toast.makeText(this, "Please select at least one platform", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PostCollaboration", "Error in post button click: ${e.message}")
                Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            //after clicking post button navigate to home screen
            val intent = Intent(this@Post_Collabartion_Screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

       requiredFollowerFrame.setOnClickListener {
            requiredFollowersSpinner.performClick() // Open the dropdown
            downVector.setImageResource(R.drawable.up_arrow) // Change arrow to up
            downVector.visibility = View.VISIBLE
       }

        // Handle selected item from Spinner
        requiredFollowersSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                val selectedOption = followersOptions[position]
                val selectedOption = resources.getStringArray(R.array.followers_options)[position]
                requiredFollowerText.text = selectedOption // Update hint text
                downVector.setImageResource(R.drawable.down_arrow) // Reset arrow direction
                downVector.visibility = View.VISIBLE // Ensure down arrow is visible after selection
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        Log.d("PostCollaboration", "before post button")
        // Check if the button is initialized and visible
        if (postButton == null) {
            Log.e("PostCollaboration", "Post button is not initialized")
        } else {
            Log.d("PostCollaboration", "Post button is initialized $postButton")
        }


        // after clicking home button return to home screen
        val homeButton: View = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this@Post_Collabartion_Screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        // navigate to profle screen
        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@Post_Collabartion_Screen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        // navigate to my collab screen
        val myCollabScreen: View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@Post_Collabartion_Screen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // Initialize platform selection listeners
        setupPlatformSelection()

    }


    private fun savePostToDatabase(post: CollaborationPost) {
        val json = gson.toJson(post) // Serialize the data to JSON
        Log.d("PostCollaboration", "Serialized JSON: $json")

        val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), json)
        Log.d("PostCollaboration", "Request body: $requestBody")
        val request = Request.Builder()
            .url("http://10.0.2.2:9001/api/post-collab") // Replace with your actual base URL
            .post(requestBody)
            .build()

        Log.d("PostCollaboration", "Sending request to URL: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                    Log.d("PostCollaboration", "Response successful: ${response.body?.string()}")
                        runOnUiThread {
                        Toast.makeText(this@Post_Collabartion_Screen, "Post saved successfully", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("PostCollaboration", "Response failed with code: ${response.code}")
                        runOnUiThread {
                            Toast.makeText(this@Post_Collabartion_Screen, "Failed to save post", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("PostCollaboration", "Request failed: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@Post_Collabartion_Screen, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupPlatformSelection() {
        instagramIcon.setOnClickListener { togglePlatform("Instagram") }
        snapchatIcon.setOnClickListener { togglePlatform("Snapchat") }
        facebookIcon.setOnClickListener { togglePlatform("Facebook") }
    }

    private fun togglePlatform(platform: String) {
        if (!selectedPlatforms.contains(platform)) {
            selectedPlatforms.add(platform)
            Toast.makeText(this, "$platform selected", Toast.LENGTH_SHORT).show()
        } else {
            selectedPlatforms.remove(platform)
            Toast.makeText(this, "$platform deselected", Toast.LENGTH_SHORT).show()
        }
    }
}

