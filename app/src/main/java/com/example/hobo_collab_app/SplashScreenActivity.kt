package com.example.hobo_collab_app

import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.Animator
import androidx.core.animation.AnimatorListenerAdapter
import androidx.core.animation.ObjectAnimator
import androidx.core.animation.OvershootInterpolator


class SplashScreenActivity: ComponentActivity() {

    private lateinit var logo: ImageView
    private lateinit var background: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_activity)

        logo = findViewById(R.id.logo)
        background = findViewById(R.id.background_layout)

        // Set initial white background
        background.setBackgroundColor(Color.parseColor("#E6E6E6"))

        // Set logo to be visible in the center
        logo.visibility = ImageView.VISIBLE

        // After 300ms, start spreading yellow color from logo
        Handler(Looper.getMainLooper()).postDelayed({
//            val displayMetrics = resources.displayMetrics
//            val screenWidth = displayMetrics.widthPixels
//            val screenHeight = displayMetrics.heightPixels
////            val diagonal = Math.sqrt((screenWidth * screenWidth + screenHeight * screenHeight).toDouble()).toFloat()

//            val ovalDrawable = GradientDrawable(GradientDrawable.Orientation.BL_TR, intArrayOf(Color.parseColor("#FFFD00"), Color.parseColor("#FFFD00")))
            val ovalDrawable = GradientDrawable()
            ovalDrawable.shape = GradientDrawable.OVAL
            ovalDrawable.setColor(Color.parseColor("#FFFD00"))
//            ovalDrawable.gradientType = GradientDrawable.RADIAL_GRADIENT
//            ovalDrawable.gradientRadius = diagonal / 2
//            ovalDrawable.setBounds(-screenWidth / 2, -screenHeight / 2, screenWidth * 2, screenHeight * 2)

            background.background = ovalDrawable

            val scaleAnimation = ScaleAnimation(
                0f, 5f,  // Start scaling from 0 to 5 times the original size
                0f, 5f,  // Scale on both X and Y axis
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,  // Pivot on the center X
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f   // Pivot on the center Y
            )
            scaleAnimation.duration = 1500 // Duration of the scaling animation
            scaleAnimation.fillAfter = true // Make sure the animation stays after completion
            background.startAnimation(scaleAnimation)

            // Animate the logo to move up without scaling
            val logoAnimator = ObjectAnimator.ofFloat(logo, View.TRANSLATION_Y, 0f, -background.height.toFloat() / 2)
            logoAnimator.duration = 1000 // adjust this value to control the movement duration
            logoAnimator.interpolator = OvershootInterpolator()
            logoAnimator.start()


            // Start new activity after spreading yellow color
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, OnBoardScreen_1::class.java)
                startActivity(intent)
                finish()
            }, 1500)
        }, 2000)
    }
}