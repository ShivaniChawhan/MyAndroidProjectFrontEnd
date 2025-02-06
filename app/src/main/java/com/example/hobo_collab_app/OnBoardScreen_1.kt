package com.example.hobo_collab_app

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.animation.doOnEnd


class OnBoardScreen_1: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboard_screen1)

        val splashUpAnimatorY = ObjectAnimator.ofFloat(findViewById(R.id.onBoard_1), View.TRANSLATION_Y, 1000f, 0f)
        splashUpAnimatorY.duration = 500 // Adjust the duration as needed
        splashUpAnimatorY.interpolator = AccelerateDecelerateInterpolator()
        splashUpAnimatorY.start()

        val titleText = findViewById<TextView>(R.id.slide_1)
        val descriptionText = findViewById<TextView>(R.id.body1)
        val horizontalLine = findViewById<View>(R.id.line)


        findViewById<Button>(R.id.next_button).setOnClickListener {
            // Create title text animations
            val titleSlideDownAnimator = ObjectAnimator.ofFloat(
                titleText,
                View.TRANSLATION_Y,
                0f,
                horizontalLine.y - titleText.y
            )
            titleSlideDownAnimator.duration = 500

            val titleFadeOutAnimator = ObjectAnimator.ofFloat(
                titleText,
                View.ALPHA,
                1f,
                0f
            )
            titleFadeOutAnimator.duration = 500

            // Create description text animations
            val descriptionSlideDownAnimator = ObjectAnimator.ofFloat(
                descriptionText,
                View.TRANSLATION_Y,
                0f,
                horizontalLine.y - descriptionText.y
            )
            descriptionSlideDownAnimator.duration = 500

            val descriptionFadeOutAnimator = ObjectAnimator.ofFloat(
                descriptionText,
                View.ALPHA,
                1f,
                0f
            )
            descriptionFadeOutAnimator.duration = 500

            // Combine all animations
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(
                titleSlideDownAnimator,
                titleFadeOutAnimator,
                descriptionSlideDownAnimator,
                descriptionFadeOutAnimator
            )

            // Start animations and navigate to the next screen
            animatorSet.start()
            animatorSet.doOnEnd {
                val intent = Intent(this, OnBoardScreen_2::class.java)
                startActivity(intent)
                finish()
            }
        }

        findViewById<TextView>(R.id.skip_button).setOnClickListener {
            val intent = Intent(this, OnBoardScreen_3::class.java)
            startActivity(intent)
            finish()
        }
    }


    // Extension function to handle animation end event
    fun AnimatorSet.doOnEnd(action: () -> Unit) {
        this.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                action()
            }
        })
    }
}