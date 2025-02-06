package com.example.hobo_collab_app.ui.theme

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.activity.ComponentActivity
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.example.hobo_collab_app.*
import com.example.hobo_collab_app.PostDetails_ApplyScreen
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

data class UserProfile(
    val _id: String,
    val profilePic: String?,
    val name: String?,
    val followersCount: String?,
    val niche: String?,
    val about: String?,
    val userID: String
) {
    fun getFullProfilePicUrl(): String? {
        return profilePic?.let {
            "https://collab-api.hobo.video/$profilePic"
        }
    }
}

data class AdditionalData(
    val _id: String,
    val title: String,
    val description: String,
    val requiredFollowers: String,
    val platforms: List<String>,
    val userId: String
)

data class UserProfileResponse(
    val message: String,
    val profiles: List<UserProfile>
)

class Home_Screen : ComponentActivity() {

    private lateinit var titleView1: TextView
    private lateinit var descriptionView1: TextView
    private lateinit var platformLogoContainer1: FrameLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var profileAdapter: ProfileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_screen)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("HomeScreen", "Home JWT Token $jwtToken")


        val loginUserId = extractUserIdFromJwt(jwtToken)
        Log.d("HomeScreen", "Extracted userId: $loginUserId")

        val userID = intent.getStringExtra("userID")
        Log.d("HomeScreen", "Extracted userId: $userID")

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.recycler_postcard)
        recyclerView.layoutManager = LinearLayoutManager(this)
        profileAdapter = ProfileAdapter(mutableListOf(), jwtToken)
        recyclerView.adapter = profileAdapter

        // Initialize views
        titleView1 = findViewById(R.id.need_fellow1)
        descriptionView1 = findViewById(R.id.need_fellow_description1)
        platformLogoContainer1 = findViewById(R.id.platform_logo1)

        fetchData()

        // Check login status
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", true)

        // navigate to notification screen
        val notify: LinearLayout = findViewById(R.id.notificationlayout)
        notify.setOnClickListener {
            val intent = Intent(this@Home_Screen, Notification_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        // navigate to login with instagram screen
        val nicheFilter: View = findViewById(R.id.niche_rectangle)
        nicheFilter.setOnClickListener {
            handleButtonClick(isLoggedIn, Login_Signup::class.java, jwtToken)
        }
        // navigate to login with instagram screen
        val collabFilter: View = findViewById(R.id.collab_rectangle)
        collabFilter.setOnClickListener {
            handleButtonClick(isLoggedIn, Login_Signup::class.java, jwtToken)
        }
        // navigate to login with instagram screen
        val followersFilter: View = findViewById(R.id.followers_rectangle)
        followersFilter.setOnClickListener {
            handleButtonClick(isLoggedIn, Login_Signup::class.java, jwtToken)
        }
        // navigate to login with instagram screen
        val moreFilter: View = findViewById(R.id.more_filters_layout)
        moreFilter.setOnClickListener {
            handleButtonClick(isLoggedIn, Login_Signup::class.java, jwtToken)
        }

        // navigate to post details screen
        val creatorPostDetail1: FrameLayout = findViewById(R.id.creators_layout1)
//        creatorPostDetail1.setOnClickListener {
//            val intent = Intent(this@Home_Screen, PostDetails_ApplyScreen::class.java)
//            startActivity(intent)
//            finish()
//        }

        // navigate to choose collab type screen
        val chooseCollabType: ImageView = findViewById(R.id.add_vector1)
        chooseCollabType.setOnClickListener {
            val intent = Intent(this@Home_Screen, Choose_Collab_screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to saved post screen
        val savePost1: ImageView = findViewById(R.id.archive_button1)
        savePost1.setOnClickListener {
            val intent = Intent(this@Home_Screen, MyProfile_SavedScreen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        // navigate to my collab screen
        val myCollabScreen: View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@Home_Screen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            intent.putExtra("userID", userID)
            startActivity(intent)
            finish()
        }

        // navigate to profile screen
        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@Home_Screen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
    }

    private fun extractUserIdFromJwt(jwtToken: String?): String? {
        if (jwtToken.isNullOrEmpty()) {
            Log.e("Home Screen", "JWT Token is null or empty")
            return null
        }

        return try {
            val jwt = JWT(jwtToken)
            val userId = jwt.getClaim("id").asString()
            if (userId.isNullOrEmpty()) {
                Log.e("Home Screen", "User ID claim is missing in the JWT Token")
                null
            } else {
                userId
            }
        } catch (e: Exception) {
            Log.e("Home Screen", "Error decoding JWT Token: ${e.message}", e)
            null
        }
    }

    private fun handleButtonClick(isLoggedIn: Boolean, targetActivity: Class<*>, jwtToken: String?) {
        if (isLoggedIn) {
            // If logged in, navigate to the target screen
            val intent = Intent(this@Home_Screen, targetActivity)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
        } else {
            // If not logged in, navigate to the Login_Signup screen
            val intent = Intent(this@Home_Screen, Login_Signup::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish() // Optionally, close the Home_Screen if user needs to log in
        }

    }

    private fun fetchData() {
        fetchAdditionalData { additionalDataList ->
            additionalDataList.forEach { additionalData ->
                fetchUserProfileData(additionalData.userId) { userProfile ->
                    userProfile?.let {
                        profileAdapter.addProfile(it, additionalData)
                    }
                }
            }
        }
    }

    private fun fetchAdditionalData(callback: (List<AdditionalData>) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://collab-api.hobo.video/api/post-collab")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch additional data: ${e.message}")
                runOnUiThread { callback(emptyList()) }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val jsonArray = JSONArray(responseBody.string())
                        val additionalDataList = mutableListOf<AdditionalData>()
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val additionalData = AdditionalData(
                                _id = jsonObject.getString("_id"),
                                title = jsonObject.getString("title"),
                                description = jsonObject.getString("description"),
                                requiredFollowers = jsonObject.getString("requiredFollowers"),
                                platforms = jsonObject.getJSONArray("platforms").let { array ->
                                    List(array.length()) { array.getString(it) }
                                },
                                userId = jsonObject.getString("userId")
                            )
                            additionalDataList.add(additionalData)
                        }
                        runOnUiThread { callback(additionalDataList) }
                    } ?: run {
                        Log.e("API_ERROR", "Response body is null")
                        runOnUiThread { callback(emptyList()) }
                    }
                } else {
                    Log.e("API_ERROR", "Unsuccessful response: ${response.code}")
                    runOnUiThread { callback(emptyList()) }
                }
            }
        })
    }

    private fun fetchUserProfileData(userId: String, callback: (UserProfile?) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://collab-api.hobo.video/api/getProfile/$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API_ERROR", "Failed to fetch user profile data: ${e.message}")
                runOnUiThread { callback(null) }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val moshi = Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .build()
                        val jsonAdapter = moshi.adapter<List<UserProfile>>(Types.newParameterizedType(List::class.java, UserProfile::class.java))
                        val userProfiles = jsonAdapter.fromJson(responseBody.string())
                        runOnUiThread { callback(userProfiles?.firstOrNull()) }
                    } ?: run {
                        Log.e("API_ERROR", "Response body is null")
                        runOnUiThread { callback(null) }
                    }
                } else {
                    Log.e("API_ERROR", "Unsuccessful response: ${response.code}")
                    runOnUiThread { callback(null) }
                }
            }
        })
    }
}

class ProfileAdapter(
    private var profiles: MutableList<Pair<UserProfile, AdditionalData>>,
    private val jwtToken: String?
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    class ProfileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView? = view.findViewById(R.id.creators_profile_image)
        val profileName: TextView? = view.findViewById(R.id.creators_name)
        val followerCount: TextView? = view.findViewById(R.id.creator_follower_count)
        val bio: TextView? = view.findViewById(R.id.creators_bio)
        val location: TextView? = view.findViewById(R.id.location_details)
        val title: TextView? = view.findViewById(R.id.need_fellow)
        val description: TextView? = view.findViewById(R.id.need_fellow_description)
        val platformLogoContainer: FrameLayout? = view.findViewById(R.id.platform_logo)
        val archiveButton: ImageView? = view.findViewById(R.id.archive_button)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.postcard_homescreen, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val (profile, additionalData) = profiles[position]
        holder.profileName?.text = profile.name
        holder.followerCount?.text = profile.followersCount ?: "0"
        holder.bio?.text = profile.niche
        holder.location?.text = profile.about
        holder.title?.text = additionalData.title
        holder.description?.text = additionalData.description

        holder.profileImage?.let { imageView ->
            val profilePicUrl = profile.getFullProfilePicUrl()
            if (profilePicUrl != null) {
                Glide.with(holder.itemView.context)
                    .load(profilePicUrl)
                    .error(R.drawable.creator_image)
                    .into(imageView)
            }
        }

        holder.itemView.setOnClickListener {
            navigateToPostDetailsApplyScreen(holder.itemView.context, jwtToken, profile.userID)
            Log.d("Profile Click", "Profile clicked for user: ${profile.name}")
            Log.d("Profile Click UserID", "Profile clicked for user: ${profile.userID}")
        }

        holder.archiveButton?.setOnClickListener {
            archiveUser(jwtToken, profile.userID)
            Log.d("Archive Click", "Archive button clicked for user: ${profile.userID}")
        }

        holder.platformLogoContainer?.let { container ->
            container.removeAllViews()
            val linearLayout = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 10, 16, 0)
                }
            }
            additionalData.platforms.forEach { platform ->
                val imageView = ImageView(holder.itemView.context).apply {
                    layoutParams = LinearLayout.LayoutParams(64, 64).apply {
                        setMargins(16, 10, 16, 0)
                    }
                    setImageDrawable(getPlatformDrawable(holder.itemView.context, platform))
                }
                linearLayout.addView(imageView)
            }
            container.addView(linearLayout)
        }
    }

    override fun getItemCount(): Int = profiles.size

    fun addProfile(userProfile: UserProfile, additionalData: AdditionalData) {
        profiles.add(Pair(userProfile, additionalData))
        notifyDataSetChanged()
    }

    private fun getPlatformDrawable(context: Context, platform: String): Drawable? {
        return when (platform) {
            "Instagram" -> ContextCompat.getDrawable(context, R.drawable.insta_logo)
            "Facebook" -> ContextCompat.getDrawable(context, R.drawable.facebook_logo)
            "Snapchat" -> ContextCompat.getDrawable(context, R.drawable.snapchat_logo)
            else -> ContextCompat.getDrawable(context, R.drawable.logo1)
        }
    }

    private fun ProfileAdapter.archiveUser(jwtToken: String?, userId: String) {
    }

    private fun navigateToPostDetailsApplyScreen(context: Context, jwtToken: String?, userID: String) {
        val intent = Intent(context, PostDetails_ApplyScreen::class.java)
        intent.putExtra("jwt_token", jwtToken)
        intent.putExtra("userID", userID)
        Log.d("Navigation", "Navigating to PostDetails_ApplyScreen with jwtToken: $jwtToken")
        context.startActivity(intent)
    }
}
