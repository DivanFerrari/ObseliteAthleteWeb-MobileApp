package com.example.athletesync

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var runnerIcon: ImageView
    private val splashDuration = 4000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        runnerIcon = findViewById(R.id.runnerIcon)

        val totalDistance = 600f

        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = splashDuration
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            progressBar.progress = progress
            val fraction = progress / 100f
            runnerIcon.translationX = fraction * totalDistance
        }
        animator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@MainActivity, GetStartedActivity::class.java))
            finish()
        }, splashDuration)
    }
}