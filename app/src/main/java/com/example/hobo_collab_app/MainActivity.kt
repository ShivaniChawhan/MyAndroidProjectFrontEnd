package com.example.hobo_collab_app

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hobo_collab_app.ui.theme.HoboCollabAppTheme

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.applinks.AppLinkData
import okhttp3.*
import java.io.IOException


class MainActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firestore: FirebaseFirestore

    private val instagramLoginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val uri = data?.data
                val authorizationCode = uri?.getQueryParameter("code")

                Log.d("InstagramLogin", "Authorization Code: $authorizationCode")

                saveAuthorizationCodeToFirestore(authorizationCode)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HoboCollabAppTheme {
                //GreetingScreen()
            }
        }

        // Initialize Firebase Analytics and Firestore
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firestore = FirebaseFirestore.getInstance()

        // Trigger the Facebook login flow
        initiateFacebookLogin()

        // Handle deep linking
        handleDeepLink(intent)

        // Fetch deferred deep link
        fetchDeferredDeepLink()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep linking when the activity is already running
        handleDeepLink(intent)

        intent?.data?.let { uri ->
            if (uri.scheme == "http" && uri.host == "10.0.2.2:9001/auth/facebook" && uri.path == "/callback") {
                val authorizationCode = uri.getQueryParameter("code")
                Log.d("FacebookCallback", "Authorization Code: $authorizationCode")

                // Call the /facebook/callback REST endpoint if needed
                authorizationCode?.let {
                    callCallbackEndpoint(it)
                }
            }
        }
    }

    private fun initiateFacebookLogin() {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://collab-api.hobo.video/facebook") // Use your local backend URL or deployed URL
            .build()

        // Make the network request on a background thread
        Thread {
            try {
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val redirectUrl = response.request.url.toString() // Get the redirect URL (Facebook auth URL)
                    // Open the URL in a WebView or browser
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
                    startActivity(intent)
                } else {
                    // Handle the error
                    runOnUiThread {
                        Log.e("FacebookLogin", "Error: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            val authorizationCode = uri.getQueryParameter("code")
            Log.d("InstagramLogin", "Authorization Code: $authorizationCode")
            saveAuthorizationCodeToFirestore(authorizationCode)
        }
    }

    private fun fetchDeferredDeepLink() {
        AppLinkData.fetchDeferredAppLinkData(this) { appLinkData ->
            appLinkData?.targetUri?.let { uri ->
                val authorizationCode = uri.getQueryParameter("code")
                Log.d("DeferredDeepLink", "Authorization Code: $authorizationCode")
                saveAuthorizationCodeToFirestore(authorizationCode)
            }
        }
    }

    private fun saveAuthorizationCodeToFirestore(authorizationCode: String?) {
        authorizationCode?.let {
            val userData = hashMapOf(
                "authorization_code" to it,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .add(userData)
                .addOnSuccessListener { documentReference ->
                    Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error adding document", e)
                }
        }
    }
    fun callCallbackEndpoint(authorizationCode: String) {
        val client = OkHttpClient()

        // Build the URL with the authorization code as a query parameter
        val url = "https://collab-api.hobo.video/facebook/callback?code=$authorizationCode"

        // Create the request
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        // Make the network request
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("CallbackError", "Error calling callback endpoint", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("CallbackResponse", "Response from /facebook/callback: $responseBody")
                    // Handle response (e.g., parse JSON and proceed)
                } else {
                    Log.e("CallbackError", "Unsuccessful response: ${response.code}")
                }
            }
        })
    }
}
