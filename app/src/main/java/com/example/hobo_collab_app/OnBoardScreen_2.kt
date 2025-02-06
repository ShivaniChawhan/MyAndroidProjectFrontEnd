package com.example.hobo_collab_app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.animation.doOnEnd

class OnBoardScreen_2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboard_screen2)

        val titleText = findViewById<TextView>(R.id.slide_2)
        val descriptionText = findViewById<TextView>(R.id.body2)
        val horizontalLine = findViewById<View>(R.id.line)

        // Trigger animations after layout is fully measured
        titleText.viewTreeObserver.addOnGlobalLayoutListener {
            animateTextSlideUp(titleText, horizontalLine)
        }

        descriptionText.viewTreeObserver.addOnGlobalLayoutListener {
            animateTextSlideUp(descriptionText, horizontalLine)
        }


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
                val intent = Intent(this, OnBoardScreen_3::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Skip button logic
        findViewById<TextView>(R.id.skip_button).setOnClickListener {
            val intent = Intent(this, OnBoardScreen_3::class.java)
            startActivity(intent)
            finish()
        }
    }

   private fun animateTextSlideUp(textView: TextView, horizontalLine: View) {
        // Start the text behind the horizontal line
        val startTranslationY = horizontalLine.y - textView.y
        textView.translationY = startTranslationY
        textView.alpha = 0f

        // Slide the text upwards into view and fade in
        val slideAnimator = ObjectAnimator.ofFloat(
            textView,
            View.TRANSLATION_Y,
            startTranslationY, // Start below the line
            0f // End at its original position
        )

        val fadeAnimator = ObjectAnimator.ofFloat(
            textView,
            View.ALPHA,
            0f,
            1f // Fully visible
        )

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(slideAnimator, fadeAnimator)
        animatorSet.duration = 500
        animatorSet.start()
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
