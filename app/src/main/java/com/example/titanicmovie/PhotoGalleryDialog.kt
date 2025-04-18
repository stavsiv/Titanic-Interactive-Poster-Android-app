package com.example.titanicmovie

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Button
import android.widget.ImageButton

//Displays a photo gallery dialog with navigation buttons

class PhotoGalleryDialog(private val context: Context) {

    private val images = mutableListOf(
        R.drawable.movie_image1,
        R.drawable.movie_image2,
        R.drawable.movie_image3,
        R.drawable.movie_image4,
        R.drawable.movie_image5,
        R.drawable.movie_image6,
        R.drawable.movie_image7,
        R.drawable.movie_image8,
        R.drawable.movie_image9,
    )

    private var currentIndex = 0

    fun show() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_photo_gallery, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.imageView)
        val btnNext = dialogView.findViewById<Button>(R.id.btnNext)
        val btnPrev = dialogView.findViewById<Button>(R.id.btnPrev)
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        updateImage(imageView, btnPrev, btnNext, true)

        btnNext.setOnClickListener {
            if (currentIndex < images.size - 1) {
                currentIndex++
                updateImage(imageView, btnPrev, btnNext, true)
            }
        }

        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                updateImage(imageView, btnPrev, btnNext, false)
            }

        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

   // Uses slide_right and slide_left animations for transitions.

    private fun updateImage(imageView: ImageView, btnPrev: Button, btnNext: Button, direction: Boolean) {
        val slideIn = if (direction) {
            AnimationUtils.loadAnimation(context, R.anim.slide_right)
        } else {
            AnimationUtils.loadAnimation(context, R.anim.slide_left)
        }

        imageView.setImageResource(images[currentIndex])
        imageView.startAnimation(slideIn)

        btnPrev.isEnabled = currentIndex > 0
        btnNext.isEnabled = currentIndex < images.size - 1
    }


    fun addImage(imageResId: Int) {
        images.add(imageResId)
    }

    fun removeImage(imageResId: Int) {
        images.remove(imageResId)
    }

    fun updateImageAtIndex(index: Int, newImageResId: Int) {
        if (index in images.indices) {
            images[index] = newImageResId
        }
    }
}
