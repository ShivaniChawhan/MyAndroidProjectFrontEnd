package com.example.hobo_collab_app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.example.hobo_collab_app.ui.theme.Home_Screen
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


data class User(
    val name: String,
    var location: String = "Unknown",
    var bio: String = "No bio available",
    var profilePicUrl: String = "",
    var followerCount: String = "0"
)

data class AppliedUser(
    val id: String,
    var userName: String,
    val followerCount: Int,
    val profilePic: String,
    val title: String,
    val descriptions: String,
    val platforms: List<String>,
    val loginUserId: String,
    val userId: String,
    var status: String,
    val createdAt: String,
    val updatedAt: String,
    var location: String = "Unknown",
    var bio: String = "No bio available",
    var profilePicUrl: String = "",
    var followersCount: String = "0"
)

class App_Management_Screen: ComponentActivity() {

    private lateinit var adapter: UserAdapter
    private val client = OkHttpClient()
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.app_management_screen)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("AllApplicatons", "AllApplicatons JWT Token $jwtToken")

        val userId = extractUserIdFromJwt(jwtToken)
        Log.d("AllApplicatons", "Extracted userId: $userId")

        recyclerView = findViewById(R.id.recycler_application_card)
        val appliedUserList = mutableListOf<AppliedUser>()
        adapter = UserAdapter(this, appliedUserList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fetchUsersFromBackend(userId)

        val backArrow: View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@App_Management_Screen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        val homeScreen: View = findViewById(R.id.home_button)
        homeScreen.setOnClickListener {
            val intent = Intent(this@App_Management_Screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        val myCollabScreen: View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@App_Management_Screen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }

        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@App_Management_Screen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
    }

    private fun extractUserIdFromJwt(jwtToken: String?): String? {
        if (jwtToken.isNullOrEmpty()) {
            Log.e("AllApplicatons Screen", "JWT Token is null or empty")
            return null
        }

        return try {
            val jwt = JWT(jwtToken)
            val userId = jwt.getClaim("id").asString()
            if (userId.isNullOrEmpty()) {
                Log.e("AllApplicatons Screen", "User ID claim is missing in the JWT Token")
                null
            } else {
                userId
            }
        } catch (e: Exception) {
            Log.e("AllApplicatons Screen", "Error decoding JWT Token: ${e.message}", e)
            null
        }
    }

    private fun fetchUsersFromBackend(userId: String?) {
        val url = "https://collab-api.hobo.video/api/applied-users/getAppliedUser/$userId"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UserListActivity", "Error fetching users: ${e.message}")
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call, response: Response) {
                Log.d("API_CALL", "Received response for call: ${call.request().url}")
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val json = responseBody.string()
                        Log.d("API_RESPONSE", "Response body: $json")
                        val appliedUsers = parseAppliedUsersFromJson(json).distinctBy { it.loginUserId }
                        Log.d("App Management Screen", "Parsed applied users: ${appliedUsers.size} users")

                        appliedUsers.forEach { appliedUser ->
                            Log.d("App Management Screen", "Fetching profiles for user: ${appliedUser.loginUserId}")
                            fetchUserProfiles(appliedUser.loginUserId) { users ->
                                if (users.isNotEmpty()) {
                                    val user = users[0] // Assuming the response is a list with a single user
                                    Log.d("Profile Update", "Updating applied user with profile data: ${user.name}")
                                    appliedUser.userName = user.name
                                    appliedUser.location = user.location
                                    appliedUser.bio = user.bio
                                    appliedUser.profilePicUrl = user.profilePicUrl
                                    appliedUser.followersCount = user.followerCount
                                }
                                runOnUiThread {
                                    Log.d("UI_UPDATE", "Notifying adapter of data change")
                                    adapter.updateUsers(appliedUsers)
                                }
                            }
                        }
                    } ?: run {
                        Log.e("API_RESPONSE", "Response body is null")
                    }
                } else {
                    Log.e("UserListActivity", "Failed to fetch users: ${response.code}")
                }
            }
        })
    }

    private fun parseAppliedUsersFromJson(json: String): List<AppliedUser> {

        val appliedUserList = mutableListOf<AppliedUser>()
        try {
            val jsonArray = JSONArray(json)
            Log.d("PARSE_JSON", "Parsing JSON array with ${jsonArray.length()} elements")
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                Log.d("PARSE_JSON", "Parsing JSON object at index $i: $jsonObject")
                val appliedUser = AppliedUser(
                    id = jsonObject.getString("_id"),
                    userName = jsonObject.getString("userName"),
                    followerCount = jsonObject.getInt("followerCount"),
                    profilePic = jsonObject.getString("profilePic"),
                    title = jsonObject.getString("title"),
                    descriptions = jsonObject.getString("descriptions"),
                    platforms = jsonObject.getJSONArray("platforms").let { array ->
                        List(array.length()) { array.getString(it) }
                    },
                    loginUserId = jsonObject.getString("loginUserId"),
                    userId = jsonObject.getString("userId"),
                    status = jsonObject.getString("status"),
                    createdAt = jsonObject.getString("createdAt"),
                    updatedAt = jsonObject.getString("updatedAt")
                )
                Log.d("PARSE_JSON", "Created AppliedUser object: $appliedUser")
                appliedUserList.add(appliedUser)
            }
            Log.d("PARSE_JSON", "Successfully parsed ${appliedUserList.size} applied users")
        } catch (e: Exception) {
            Log.e("PARSE_ERROR", "Error parsing applied user data: ${e.message}")
        }
        return appliedUserList
    }

    private fun fetchUserProfiles(loginUserId: String, callback: (List<User>) -> Unit) {
        val url = "https://collab-api.hobo.video/api/getProfile/$loginUserId"
        Log.d("UserProfileFetch", "Fetching user profiles from URL: $url")

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UserProfileFetch", "Error fetching user profiles: ${e.message}")
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("UserProfileFetch", "Received response with code: ${response.code}")
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val json = responseBody.string()
                        Log.d("UserProfileFetch", "Response body: $json")
                        val users = parseUserProfilesFromJson(json)
                        Log.d("UserProfileFetch", "Parsed ${users.size} user profiles")
                        callback(users)
                    }
                } else {
                    Log.e("UserProfileFetch", "Failed to fetch user profiles: ${response.code}")
                    callback(emptyList())
                }
            }
        })
    }

    private fun parseUserProfilesFromJson(json: String): List<User> {
        val userList = mutableListOf<User>()
        try {
            val jsonArray = JSONArray(json)
            Log.d("PARSE_JSON", "Parsing JSON array with ${jsonArray.length()} elements")
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                Log.d("PARSE_JSON", "Parsing JSON object at index $i: $jsonObject")
                val user = User(
                    name = jsonObject.getString("name"),
                    location = jsonObject.optString("niche", "Unknown"),
                    bio = jsonObject.optString("about", "No bio available"),
                    profilePicUrl = jsonObject.optString("profilePic", "").replace("\\", "//"),
                    followerCount = jsonObject.optString("followersCount", "0")
                )
                Log.d("PARSE_JSON", "Created User object: $user")
                userList.add(user)
            }
            Log.d("PARSE_JSON", "Successfully parsed ${userList.size} user profiles")
        } catch (e: Exception) {
            Log.e("PARSE_ERROR", "Error parsing user profiles data: ${e.message}")
        }
        return userList
    }
}

class UserAdapter(
    private val context: Context,
    private val userList: MutableList<AppliedUser>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private val client = OkHttpClient()

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val influencerName: TextView = itemView.findViewById(R.id.influencer_name)
        val influencerLocation: TextView = itemView.findViewById(R.id.influencer_location_details)
        val influencerBio: TextView = itemView.findViewById(R.id.influencer_bio)
        val influencerPic: ImageView = itemView.findViewById(R.id.influencer_pic)
        val followerCount: TextView = itemView.findViewById(R.id.f_count)
        val statusButton: Button = itemView.findViewById(R.id.status_btn)
        val acceptButton: FrameLayout = itemView.findViewById(R.id.green_button)
        val rejectButton: FrameLayout = itemView.findViewById(R.id.red_button)
        val holdButton: FrameLayout = itemView.findViewById(R.id.orange_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.all_application_card, parent, false)
        return UserViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.influencerName.text = user.userName
        holder.influencerLocation.text = user.location
        holder.influencerBio.text = user.bio
        holder.followerCount.text = user.followersCount
        holder.statusButton.text = user.status

        Glide.with(context)
            .load(user.profilePicUrl)
            .circleCrop()
//            .placeholder(R.drawable.creator_image)
            .error(R.drawable.creator_image)
            .into(holder.influencerPic)

        when (user.status) {
            "Selected" -> {
                disableButtons(holder.acceptButton, holder.rejectButton, holder.holdButton)
                holder.statusButton.setBackgroundColor(context.getColor(R.color.selected_color))
            }
            "Rejected" -> {
                disableButtons(holder.acceptButton, holder.rejectButton, holder.holdButton)
                holder.statusButton.setBackgroundColor(context.getColor(R.color.rejected_color))
            }
            "On Hold" -> {
                disableButtons(holder.acceptButton, holder.rejectButton, holder.holdButton)
                holder.statusButton.setBackgroundColor(context.getColor(R.color.onhold_color))
            }
            else -> {
                // Enable buttons if status is not one of the above
                holder.acceptButton.isEnabled = true
                holder.rejectButton.isEnabled = true
                holder.holdButton.isEnabled = true
            }
        }

        holder.acceptButton.setOnClickListener {
            updateUserStatus(position, "Selected", holder)
        }

        holder.rejectButton.setOnClickListener {
            updateUserStatus(position, "Rejected", holder)
        }

        holder.holdButton.setOnClickListener {
            updateUserStatus(position, "On Hold", holder)
        }
    }

    override fun getItemCount(): Int {
        Log.d("UserAdapter", "getItemCount: Returning item count ${userList.size}")
        return userList.size
    }

    private fun disableButtons(vararg buttons: FrameLayout) {
        Log.d("UserAdapter", "disableButtons: Disabling buttons")
        buttons.forEach {
            it.isEnabled = false
            it.setBackgroundColor(context.getColor(R.color.disable_btn))
        }
    }

    private fun updateUserStatus(position: Int, status: String, holder: UserViewHolder) {
        val user = userList[position]
        user.status = status
        holder.statusButton.text = status
        holder.statusButton.setBackgroundColor(
            when (status) {
                "Selected" -> context.getColor(R.color.selected_color)
                "Rejected" -> context.getColor(R.color.rejected_color)
                "On Hold" -> context.getColor(R.color.onhold_color)
                else -> context.getColor(R.color.disable_btn)
            }
        )
        disableButtons(holder.acceptButton, holder.rejectButton, holder.holdButton)


        // Update the status in the database
        // Create JSON object for the request body
        val jsonObject = JSONObject()
        jsonObject.put("status", user.status)

        // Convert JSON object to RequestBody
        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(),
            jsonObject.toString()
        )
        val url = "https://collab-api.hobo.video/api/applied-users/users/status/${user.loginUserId}/${user.userId}"
        val request = Request.Builder()
            .url(url)
            .put(requestBody) // Use an empty body for POST request
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UserAdapter", "Failed to update status: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("UserAdapter", "Successfully updated status for user: ${user.loginUserId}")
                } else {
                    Log.e("UserAdapter", "Failed to update status: ${response.code}")
                }
            }
        })
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateUsers(users: List<AppliedUser>) {
        Log.d("UserAdapter", "updateUsers: Updating users in adapter")
        userList.clear()
        userList.addAll(users)
        notifyDataSetChanged()
    }
}