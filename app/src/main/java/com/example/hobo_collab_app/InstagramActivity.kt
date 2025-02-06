package com.example.hobo_collab_app


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hobo_collab_app.ui.theme.HoboCollabAppTheme
import okio.IOException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class InstagramActivity:ComponentActivity() {

    private val instagramApi: InstagramApi by lazy {
        RetrofitClient.retrofit.create(InstagramApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HoboCollabAppTheme {
                InstagramLoginScreen()
            }
        }
    }

    private fun initiateInstagramLogin() {
        val call = instagramApi.getAuthorizationUrl(
            clientId = "Your_Client_Id",
            redirectUri = "Your_redirect_uri",
            responseType = "code",
            scope = "user_profile, user_media"
        )
        call.enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful) {
                    val authorizationUrl = response.body()
                    authorizationUrl?.let {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                        startActivity(intent)
                    }
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("InstagramLogin", "Request failed", t)

                if(t is IOException) {
                    Toast.makeText(this@InstagramActivity, "Network error. Please check your connection.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@InstagramActivity,"An unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show()
                }
            }

        })
    }
    @Preview(showBackground = true)
    @Composable
    fun InstagramLoginPreview() {
        HoboCollabAppTheme {
            InstagramLoginScreen()
        }
    }
    @Composable
    fun InstagramLoginScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {initiateInstagramLogin() }) {
                Text(text = "Login with Instagram")
            }
        }
    }
}