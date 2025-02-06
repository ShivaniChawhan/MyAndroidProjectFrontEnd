package com.example.hobo_collab_app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import android.widget.ImageView
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.example.hobo_collab_app.ui.theme.Home_Screen
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import android.net.Uri
import android.provider.MediaStore
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import com.auth0.android.jwt.JWT
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.*


class Personal_Info_Screen:ComponentActivity() {

    private val client = OkHttpClient()
    private var imageUri: Uri? = null
    private lateinit var profileImageView: ImageView
    private lateinit var profileViewV1: View
    private lateinit var profileViewV2: View

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            // Handle the image URI (e.g., display it in an ImageView or upload it)
            imageUri?.let {
                Log.d("PersonalInfoScreen", "Image URI: $it")
//                profileImageView.setImageURI(it)

                // Use Glide to load the image with a circular crop
                Glide.with(this)
                    .load(it)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImageView)

                // Hide overlapping Views once the image is uploaded
                profileViewV1.visibility = View.GONE
                profileViewV2.visibility = View.GONE
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.personal_info_screen)

        val jwtToken = intent.getStringExtra("jwt_token")
        Log.d("PersonalInfo", "PersonalInfo JWT Token $jwtToken")

        // Extract userId from jwtToken
        val userId = extractUserIdFromJwt(jwtToken)
        Log.d("PersonalInfo", "Extracted userId: $userId")

        profileImageView = findViewById(R.id.profile_view)
        profileViewV1 = findViewById(R.id.profile_view_v1)
        profileViewV2 = findViewById(R.id.profile_view_v2)

        val profileButton: View = findViewById(R.id.profile_picture_bg)
        profileButton.setOnClickListener {
            openGallery()
        }

        // after clicking on save button return back to home screen
        val saveButton: FrameLayout = findViewById(R.id.save_btn)
        saveButton.setOnClickListener {
            val profile = collectProfileData(userId)
            if (profile != null) {
                sendProfileToServer(profile)
            }
            val intent = Intent(this@Personal_Info_Screen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish() // Close personal info screen
        }

        // after clicking back arrow return to home screen
        val backArrow : View = findViewById(R.id.back_arrow)
        backArrow.setOnClickListener {
            val intent = Intent(this@Personal_Info_Screen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        // after clicking home button return to home screen
        val homeButton : View = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            val intent = Intent(this@Personal_Info_Screen, Home_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        val userProfile: View = findViewById(R.id.profile_button)
        userProfile.setOnClickListener {
            val intent = Intent(this@Personal_Info_Screen, My_Profile_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
        // navigate to my collab screen
        val myCollabScreen : View = findViewById(R.id.collab_button)
        myCollabScreen.setOnClickListener {
            val intent = Intent(this@Personal_Info_Screen, MyCollabs_Screen::class.java)
            intent.putExtra("jwt_token", jwtToken)
            startActivity(intent)
            finish()
        }
    }

    private fun extractUserIdFromJwt(jwtToken: String?): String? {
        if (jwtToken.isNullOrEmpty()) {
            Log.e("PersonalInfo Screen", "JWT Token is null or empty")
            return null
        }

        return try {
            val jwt = JWT(jwtToken)
            val userId = jwt.getClaim("id").asString()
            if (userId.isNullOrEmpty()) {
                Log.e("PersonalInfo Screen", "User ID claim is missing in the JWT Token")
                null
            } else {
                userId
            }
        } catch (e: Exception) {
            Log.e("PersonalInfo Screen", "Error decoding JWT Token: ${e.message}", e)
            null
        }
    }
    // Add this function to save the image to the gallery
    fun saveImageToGallery(context: Context, file: File): File? {
        val uploadsDir = File(context.filesDir, "uploads")
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs()
        }

        val imageFile = File(uploadsDir, "image_${System.currentTimeMillis()}.jpg")
        return try {
            file.inputStream().use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("ImageSave", "Image saved to ${imageFile.absolutePath}")
            imageFile
        } catch (e: IOException) {
            Log.e("ImageSave", "Failed to save image", e)
            null
        }
    }

//    fun saveImageToGallery(context: Context, file: File) {
//        val contentResolver = context.contentResolver
//        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//
//        val imageDetails = ContentValues().apply {
//            put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.jpg")
//            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
//        }
//
//        val imageUri = contentResolver.insert(imageCollection, imageDetails)
//
//        imageUri?.let { uri ->
//            contentResolver.openOutputStream(uri)?.use { outputStream ->
//                file.inputStream().copyTo(outputStream)
//            }
//        }
//    }

    private fun collectProfileData(userId: String?): Profile_data? {
        val nameInput: EditText = findViewById(R.id.add_name_frame)
        val instagramHandleInput: EditText = findViewById(R.id.add_insta_handle_frame)
        val followersCountInput: EditText = findViewById(R.id.add_followers_count_frame)
        val nicheInput: EditText = findViewById(R.id.add_niche_frame)
        val aboutInput: EditText = findViewById(R.id.add_about_frame)
        val portfolioLinkInput: EditText = findViewById(R.id.add_portfolio_frame)

        val name = nameInput.text.toString().trim()
        val instagramHandle = instagramHandleInput.text.toString().trim()
        val followersCount = followersCountInput.text.toString().trim()
        val niche = nicheInput.text.toString().trim()
        val about = aboutInput.text.toString().trim()
        val portfolioLink = portfolioLinkInput.text.toString().trim()
        val userID = userId.toString()

        // Validate all input fields
        if (name.isEmpty() || instagramHandle.isEmpty() || followersCount.isEmpty() ||
            niche.isEmpty() || about.isEmpty() || portfolioLink.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return null
        }

       /* val profilePic = imageUri?.let { uri ->
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "profile_pic.jpeg")
            file.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            inputStream?.close()

            // Save the image to the gallery
            saveImageToGallery(this, file)

            file
        } */
        val profilePic = imageUri?.let { uri ->
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val file = File(cacheDir, "profile_pic.jpeg")
                file.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                inputStream?.close()

                // Save the image to the gallery
                saveImageToGallery(this, file)

                file
            } catch (e: FileNotFoundException) {
                Log.e("PersonalInfo ProfileData", "File not found for URI: $uri", e)
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show()
                null
            }
        }

        Log.d("ProfileData", "Name: $name")
        Log.d("ProfileData", "Instagram Handle: $instagramHandle")
        Log.d("ProfileData", "Followers Count: $followersCount")
        Log.d("ProfileData", "Niche: $niche")
        Log.d("ProfileData", "About: $about")
        Log.d("ProfileData", "Portfolio Link: $portfolioLink")
        Log.d("ProfileData", "UserID: $userID")

        return Profile_data(
            profilePic?.absolutePath ?: "",
            name,
            instagramHandle,
            followersCount,
            niche,
            about,
            portfolioLink,
            userID
        )
    }

    private fun sendProfileToServer(profile: Profile_data) {
        val gson = Gson()
        val jsonBody = gson.toJson(profile)

        val requestBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        requestBodyBuilder.addFormDataPart("profile", jsonBody)

        profile.profilePic?.takeIf { it.isNotEmpty() }?.let { path ->
            val file = File(path)
            if (file.exists()) {
                requestBodyBuilder.addFormDataPart("profilePic", file.name, file.asRequestBody("image/jpeg".toMediaType()))
            } else {
                Log.e("File Error", "Profile picture file does not exist at path: $path")
            }
//            requestBodyBuilder.addFormDataPart("profilePic", file.name, file.asRequestBody("image/jpeg".toMediaType()))
        }

        val requestBody = requestBodyBuilder.build()
        Log.d("Request Body Data", "Personal Info $jsonBody")
        val request = Request.Builder()
            .url("https://collab-api.hobo.video/api/addProfile") // Replace with your server's URL
            .post(requestBody)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API", "Failed to send profile", e)
                runOnUiThread {
                    Toast.makeText(this@Personal_Info_Screen, "Failed to send data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("API", "Profile sent successfully")
                    runOnUiThread {
                        Toast.makeText(this@Personal_Info_Screen, "Profile sent successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("API", "Failed with code ${response.code}")
                    runOnUiThread {
                        Toast.makeText(this@Personal_Info_Screen, "Failed: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

}