package com.example.hobo_collab_app

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.example.hobo_collab_app.ui.theme.AdditionalData
import com.example.hobo_collab_app.ui.theme.Home_Screen
import com.example.hobo_collab_app.ui.theme.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONArray

data class CollabData (
    val id: String?,
    val title: String,
    val description: String,
    val requiredFollowers: String?,
    val platforms: List<String>
)

class MyCollabs_Screen : ComponentActivity() {
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var collabAdapter: CollabAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.posted_mycollab_screen)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("MyCollab", "MyCollab JWT Token: $jwtToken")

        val userId = extractUserIdFromJwt(jwtToken)
        Log.d("MyCollab", "Extracted userId: $userId")

        postsRecyclerView = findViewById(R.id.recycler_view_posts)
        postsRecyclerView.layoutManager = LinearLayoutManager(this)
        collabAdapter = CollabAdapter(mutableListOf(), jwtToken)
        postsRecyclerView.adapter = collabAdapter


        fetchData(userId)

        setupNavigation(jwtToken)
    }

    private fun extractUserIdFromJwt(jwtToken: String?): String? {
        if (jwtToken.isNullOrEmpty()) {
            Log.e("MyCollab Screen", "JWT Token is null or empty")
            return null
        }

        return try {
            val jwt = JWT(jwtToken)
            val userId = jwt.getClaim("id").asString()
            if (userId.isNullOrEmpty()) {
                Log.e("MyCollab Screen", "User ID claim is missing in the JWT Token")
                null
            } else {
                Log.d("MyCollab Screen", "User ID extracted: $userId")
                userId
            }
        } catch (e: Exception) {
            Log.e("MyCollab Screen", "Error decoding JWT Token: ${e.message}", e)
            null
        }
    }

    private fun fetchData(userId: String?) {
        fetchAdditionalData(userId) { additionalDataList ->
            additionalDataList.forEach { additionalData ->
                fetchUserProfileData(additionalData.userId) { userProfile ->
                    userProfile?.let {
                        collabAdapter.addProfile(it, additionalData)
                    }
                }
            }
        }
    }

    private fun setupNavigation(jwtToken: String?) {
        Log.d("MyCollab Screen", "Setting up navigation")
        val backArrow = findViewById<View>(R.id.back_arrow)
        val myAppliedCollab = findViewById<FrameLayout>(R.id.my_applied_collab)
        val homeButton = findViewById<View>(R.id.home_button)
        val profileButton = findViewById<View>(R.id.profile_button)

        if (backArrow == null || myAppliedCollab == null ||
            homeButton == null || profileButton == null) {
            Log.e("MyCollabs_Screen", "One or more views are null")
            return
        }

        backArrow.setOnClickListener {
            Log.d("MyCollab Screen", "Navigating to Home Screen")
            navigateTo(Home_Screen::class.java, jwtToken)
        }
        myAppliedCollab.setOnClickListener {
            Log.d("MyCollab Screen", "Navigating to Applied_Collab_Screen")
            navigateTo(Applied_Collab_Screen::class.java, jwtToken)
        }
        homeButton.setOnClickListener {
            Log.d("MyCollab Screen", "Navigating to Home Screen")
            navigateTo(Home_Screen::class.java, jwtToken)
        }
        profileButton.setOnClickListener {
            Log.d("MyCollab Screen", "Navigating to My_Profile_Screen")
            navigateTo(My_Profile_Screen::class.java, jwtToken)
        }
    }

    private fun navigateTo(target: Class<*>, jwtToken: String?) {
        Log.d("MyCollab Screen", "Navigating to ${target.simpleName}")
        val intent = Intent(this, target)
        intent.putExtra("jwt_token", jwtToken)
        startActivity(intent)
        finish()
    }


    private fun fetchAdditionalData(userId: String?, callback: (List<AdditionalData>) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://10.0.2.2:9001/api/post-collab/$userId")
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
            .url("http://10.0.2.2:9001/api/getProfile/$userId")
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

class CollabAdapter(
    private var profiles: MutableList<Pair<UserProfile, AdditionalData>>,
    private val jwtToken: String?
) : RecyclerView.Adapter<CollabAdapter.CollabViewHolder>() {

    class CollabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView? = view.findViewById(R.id.creators_profile_image1)
        val profileName: TextView? = view.findViewById(R.id.creators_name1)
        val followerCount: TextView? = view.findViewById(R.id.creator_follower_count1)
        val bio: TextView? = view.findViewById(R.id.creators_bio1)
        val location: TextView? = view.findViewById(R.id.location_details1)
        val title: TextView? = view.findViewById(R.id.need_fellow1)
        val description: TextView? = view.findViewById(R.id.need_fellow_description1)
        val platformLogoContainer: FrameLayout? = view.findViewById(R.id.platform_logo)
        val viewApplication: Button? = view.findViewById(R.id.view_application_btn)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollabViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.postedcollab_recycler, parent, false)
        return CollabViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollabViewHolder, position: Int) {
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

        holder.viewApplication?.setOnClickListener {
            Log.d("MyCollab Screen", "Navigating to App_Management_Screen")
            val context = holder.viewApplication.context
            val intent = Intent(context, App_Management_Screen::class.java)
            if (jwtToken != null) {
                intent.putExtra("jwt_token", jwtToken)
                Log.d("MyCollab Screen", "JWT Token added to intent")
            } else {
                Log.e("MyCollab Screen", "JWT Token is null, proceeding without it")
            }

            context.startActivity(intent)
            Log.d("MyCollab Screen", "Started App_Management_Screen activity")
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
}