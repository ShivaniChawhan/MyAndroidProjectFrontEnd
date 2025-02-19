package com.example.hobo_collab_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hobo_collab_app.ui.theme.Home_Screen
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


data class Post(
    val title: String,
    val description: String,
    val platformLogo: List<String>, // URL for the platform logo
    var status: String,
    val userName: String,
    val followerCount: String,
    var profilePic: String?,
    val userId: String,
    var niche: String?,
    var about: String?
)

data class UserProfileAppliedScreen(
    val userID: String,
    val profilePic: String?,
    val name: String,
    val followersCount: String,
    val niche: String,
    val about: String
)

class Applied_Collab_Screen: ComponentActivity() {

    private lateinit var postsRecyclerView: RecyclerView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.applied_mycollab_screen)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("AppliedCollab", "AppliedCollab JWT Token $jwtToken")

        val loginUserId = extractUserIdFromJwt(jwtToken)

        // after clicking back arrow return to home screen
        val backArrow : View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@Applied_Collab_Screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate back to my posted collab screen
        val mypostBack: FrameLayout = findViewById(R.id.my_postcollab_btn)
        mypostBack.setOnClickListener {
            Log.d("AppliedCollab", "My Post Back button clicked")
            val intent = Intent(this@Applied_Collab_Screen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to home screen
        val homeScreen : View = findViewById(R.id.home_button)
        homeScreen.setOnClickListener{
            val intent = Intent(this@Applied_Collab_Screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        // navigate to my collab screen
        val myCollabScreen : View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@Applied_Collab_Screen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to user profile screen
        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@Applied_Collab_Screen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        // Initialize RecyclerView
        postsRecyclerView = findViewById(R.id.recycler_view_posts)
        postsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch data from backend
        fetchPostsFromBackend(loginUserId)
    }

    private fun extractUserIdFromJwt(jwtToken: String?): String? {
        if (jwtToken.isNullOrEmpty()) {
            Log.e("AppliedCollab Screen", "JWT Token is null or empty")
            return null
        }

        return try {
            val jwt = JWT(jwtToken)
            val userId = jwt.getClaim("id").asString()
            if (userId.isNullOrEmpty()) {
                Log.e("AppliedCollab Screen", "User ID claim is missing in the JWT Token")
                null
            } else {
                userId
            }
        } catch (e: Exception) {
            Log.e("AppliedCollab Screen", "Error decoding JWT Token: ${e.message}", e)
            null
        }
    }

    private fun fetchPostsFromBackend(loginUserId: String?) {
        val url = "http://10.0.2.2:9001/api/applied-users/getUser/$loginUserId"; // Ensure this endpoint returns platform data
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@Applied_Collab_Screen,
                        "Failed to load posts: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("AppliedCollab", "Error fetching posts", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(
                                this@Applied_Collab_Screen,
                                "Error: ${response.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return
                    }

                    val responseBody = response.body?.string()
                    Log.d("AppliedCollab", "Response Body: $responseBody")
                    if (responseBody != null) {
                        val posts = parsePostsJson(responseBody)
                        Log.d("AppliedCollab", "Parsed Posts: $posts")
                        runOnUiThread {
                            val postAdapter = AppliedPostAdapter(posts)
                            postsRecyclerView.adapter = postAdapter
                        }
                    }
                }
            }
        })
    }

    private fun parsePostsJson(json: String): List<Post> {
        val posts = mutableListOf<Post>()
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            Log.d("AppliedCollab", "Post: $jsonObject")

            val platformLogoArray = jsonObject.optJSONArray("platforms")
            Log.d("AppliedCollab", "Platform Logo Array: $platformLogoArray")

            val platformLogos = mutableListOf<String>()

            if (platformLogoArray != null) {
                for (j in 0 until platformLogoArray.length()) {
                    platformLogos.add(platformLogoArray.getString(j))
                }
            }

            val post = Post(
                title = jsonObject.getString("title"),
                description = jsonObject.getString("descriptions"),
                platformLogo = platformLogos,
                status = jsonObject.optString("status"),
                followerCount = jsonObject.optString("followerCount", "N/A"),
                userName = jsonObject.optString("userName"),
                profilePic = null,
                userId = jsonObject.optString("userId"),
                niche = jsonObject.optString("bio"),
                about = jsonObject.optString("location")
            )

            // Fetch the user profile and update the post's profilePic
            fetchProfileData(post.userId) { userProfile ->
                userProfile?.let {
                    post.profilePic = userProfile.profilePic
                    post.about = userProfile.about
                    post.niche = userProfile.niche
                }
                Log.d("AppliedCollab", "After fetching Updated Post: $userProfile")
            }

            posts.add(post)
        }
        return posts
    }

    private fun fetchProfileData(userId: String?, callback: (UserProfileAppliedScreen?) -> Unit) {
        if (userId == null) {
            callback(null)
            return
        }

        val request = Request.Builder()
            .url("http://10.0.2.2:9001/api/getProfile/$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch user profile data: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val json = responseBody.string()
                        Log.d("JSON Response", json)

                        val moshi = Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .build()
                        val listType = Types.newParameterizedType(List::class.java, UserProfileAppliedScreen::class.java)
                        val jsonAdapter: JsonAdapter<List<UserProfileAppliedScreen>> = moshi.adapter(listType)

                        try {
                            val userProfiles = jsonAdapter.fromJson(json)
                            Log.d("APPLIED_PROFILE", "User Profiles: $userProfiles")
                            val userProfile = userProfiles?.firstOrNull() // Assuming you want the first profile
                            if (userProfile?.userID == null) {
                                Log.w("API_WARNING", "User ID is missing in the response")
                            }
                            callback(userProfile)
                        } catch (e: Exception) {
                            Log.e("JSON_PARSE_ERROR", "Failed to parse JSON: ${e.message}", e)
                            callback(null)
                        }
                    } ?: run {
                        Log.e("API_ERROR", "Response body is null")
                        callback(null)
                    }
                } else {
                    Log.e("API_ERROR", "Unsuccessful response: ${response.code}")
                    callback(null)
                }
            }
        })
    }
}

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.need_fellow)
        val description: TextView = itemView.findViewById(R.id.need_fellow_description)
        val platformLogoContainer: LinearLayout = itemView.findViewById(R.id.platform_logo_container)
        val statusButton: Button = itemView.findViewById(R.id.status_btn)
        val followerCount : TextView = itemView.findViewById(R.id.creator_follower_count)
        val userName: TextView = itemView.findViewById(R.id.creators_name)
        var profileImageView: ImageView = itemView.findViewById(R.id.creators_profile_image) // Add ImageView for profile picture
        val bio: TextView = itemView.findViewById(R.id.creators_bio)
        val location: TextView = itemView.findViewById(R.id.location_details)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.applied_items_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        Log.d("AppliedCollab", "Post: $post")
        holder.title.text = post.title
        holder.description.text = post.description
        holder.followerCount.text = post.followerCount
        holder.userName.text = post.userName
        holder.bio.text = post.niche
        holder.location.text = post.about

        // Load profile picture if available
        post.profilePic?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .into(holder.profileImageView)
        }

        // Clear any existing views in the container
        holder.platformLogoContainer.removeAllViews()

        // Create a new LinearLayout
        val linearLayout = LinearLayout(holder.itemView.context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 10, 0, 0)
            }
        }

        // Add ImageViews for each platform logo
        post.platformLogo.forEach { platform ->
            val imageView = ImageView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(64, 64).apply {
                    setMargins(16, 10, 0, 0)
                }
                setImageDrawable(getPlatformDrawable(holder.itemView.context, platform))
            }
            linearLayout.addView(imageView)
        }

        // Add the LinearLayout to the platformLogoContainer
        holder.platformLogoContainer.addView(linearLayout)

        // Set the status button text and background based on status
        when (post.status) {
//            "Pending" -> {
//                holder.statusButton.text = "Pending"
//                holder.statusButton.setBackgroundResource(R.drawable.blue_status_btn)
//            }
            "Selected" -> {
                holder.statusButton.text = "Selected"
                holder.statusButton.setBackgroundResource(R.drawable.green_status_btn)
            }
            "Rejected" -> {
                holder.statusButton.text = "Rejected"
                holder.statusButton.setBackgroundResource(R.drawable.red_status_btn)
            }
            "On Hold" -> {
                holder.statusButton.text = "On Hold"
                holder.statusButton.setBackgroundResource(R.drawable.orange_status_btn)
            }
            else -> {
                holder.statusButton.text = "Pending"
                holder.statusButton.setBackgroundResource(R.drawable.blue_status_btn)
            }
        }


        // Set the background drawable based on status
        val backgroundResource = when (post.status) {
            "Pending" -> R.drawable.blue_status_btn
            "Selected" -> R.drawable.green_status_btn
            "Rejected" -> R.drawable.red_status_btn
            "On Hold" -> R.drawable.orange_status_btn
            else -> null
        }
        // Set the background resource if it's not null, otherwise hide the button
        if (backgroundResource != null) {
            holder.statusButton.setBackgroundResource(backgroundResource)
            holder.statusButton.visibility = View.VISIBLE
        }
        // Example of handling button click to update status
        holder.statusButton.setOnClickListener {
            // Logic to update status, e.g., show a dialog to select new status
            updatePostStatus(position, "New Status") // Replace "New Status" with actual logic
        }
    }

    private fun getPlatformDrawable(context: Context, platform: String): Drawable? {
        return when (platform) {
            "Instagram" -> ContextCompat.getDrawable(context, R.drawable.insta_logo) // Replace with your drawable resource
            "Facebook" -> ContextCompat.getDrawable(context, R.drawable.facebook_logo) // Replace with your drawable resource
            "Snapchat" -> ContextCompat.getDrawable(context, R.drawable.snapchat_logo) // Replace with your drawable resource
            else -> ContextCompat.getDrawable(context, R.drawable.logo1) // Replace with a default logo
        }
    }

    override fun getItemCount(): Int = posts.size

    fun updatePostStatus(position: Int, newStatus: String) {
        if (position in posts.indices) {
            posts[position].status = newStatus
            notifyItemChanged(position)
        }
    }
}



