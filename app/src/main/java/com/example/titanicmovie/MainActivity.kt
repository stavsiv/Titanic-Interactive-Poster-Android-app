package com.example.titanicmovie
import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var musicSwitch: SwitchCompat
    private lateinit var musicIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        enableEdgeToEdge()
        setContentView(R.layout.main_activity_xml)

        musicSwitch = findViewById(R.id.musicSwitch)
        musicIcon = findViewById(R.id.musicIcon)

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.titanic_piano)
            mediaPlayer?.isLooping = true
        } catch (e: Exception) {
            Log.e("TitanicApp", "Error initializing MediaPlayer", e)
        }

        musicSwitch.setOnCheckedChangeListener { _, isChecked ->
            try {
                if (isChecked) {
                    mediaPlayer?.start()
                } else {
                    mediaPlayer?.pause()
                }
            } catch (e: Exception) {
                Log.e("TitanicApp", "Error with MediaPlayer", e)
            }
        }

        val factsButton: ImageButton = findViewById(R.id.factsIB)
        val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.facts_button_animation)
        factsButton.startAnimation(pulseAnim)

        factsButton.setOnClickListener {
            factsButton.clearAnimation()
            val clickAnim = AnimationUtils.loadAnimation(this, R.anim.facts_button_animation)
            factsButton.startAnimation(clickAnim)
            factsButton.postDelayed({
                val factsDialog = FactsDialog(this)
                factsDialog.showTitanicFactsDialog()
            }, clickAnim.duration)
        }

        val storylineButton: Button = findViewById(R.id.infoButton)
        storylineButton.setOnClickListener {
            loadStorylineDialog()
        }

        val galleryButton: Button = findViewById(R.id.galleryButton)
        galleryButton.setOnClickListener {
            val galleryDialog = PhotoGalleryDialog(this)
            galleryDialog.show()
        }

        val orderButton: Button = findViewById(R.id.ticketsButton)
        orderButton.setOnClickListener {
            val orderDialog = OrderTicketsDialog(this)
            orderDialog.show()
        }

        val ticketDetailsButton: Button = findViewById(R.id.MyticketsButton)
        ticketDetailsButton.setOnClickListener {
            OrderDataManager.showOrderDetails(this)
        }
    }

    private fun loadStorylineDialog() {
        val storylineDialog = Dialog(this)
        storylineDialog.setContentView(R.layout.dialog_storyline)
        storylineDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        storylineDialog.findViewById<View>(R.id.closeButton)?.setOnClickListener {
            storylineDialog.dismiss()
        }

        val gifImageView = storylineDialog.findViewById<ImageView>(R.id.storylineImageview)
        if (gifImageView?.visibility == View.VISIBLE) {
            try {
                Glide.with(this)
                    .load(R.drawable.titanic_sinking)
                    .into(gifImageView)
            } catch (e: Exception) {
                Log.e("StorylineDialog", "Error loading image: ${e.message}")
                gifImageView.visibility = View.GONE
            }
        }
        storylineDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        Log.d("TitanicApp", "MediaPlayer released")
    }
}