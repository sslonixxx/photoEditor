package com.example.photoeditor.ui.unsharpMask

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentUnsharpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UnsharpFragment : Fragment() {

    private lateinit var binding: FragmentUnsharpBinding
    private lateinit var imageView: ImageView
    private var spinner: ProgressBar? = null
    private var amount: Double = 0.0
    private var radius: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUnsharpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.slider1.addOnChangeListener { _, value, _ ->
            amount = value.toDouble()
        }
        binding.slider2.addOnChangeListener { _, value, _ ->
            radius = value.toInt()
        }

        imageView = activity?.findViewById(R.id.imageView)!!
        spinner = activity?.findViewById(R.id.progressBar1)!!

        binding.applyButton.setOnClickListener {
            val drawable = imageView.drawable
            if (drawable is BitmapDrawable) {
                spinner!!.visibility = View.VISIBLE
                val originalImage = drawable.bitmap
                applyUnsharpMask(originalImage, amount, radius)
            } else {
                Toast.makeText(activity, "Image not set or invalid image format", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyUnsharpMask(originalImage: Bitmap, amount: Double, radius: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.Default) {
                val blurredImage = applyGaussianBlur(originalImage, radius)
                applyUnsharpMasking(originalImage, blurredImage, amount)
            }
            imageView.setImageBitmap(result)
            spinner!!.visibility = View.GONE
        }
    }

    private fun applyUnsharpMasking(original: Bitmap, blurred: Bitmap, amount: Double): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, original.config)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val originalPixel = original.getPixel(x, y)
                val blurredPixel = blurred.getPixel(x, y)

                val r = Color.red(originalPixel)
                val g = Color.green(originalPixel)
                val b = Color.blue(originalPixel)

                val rBlurred = Color.red(blurredPixel)
                val gBlurred = Color.green(blurredPixel)
                val bBlurred = Color.blue(blurredPixel)

                val rResult = (r + amount * (r - rBlurred) * 2).toInt().coerceIn(0, 255)
                val gResult = (g + amount * (g - gBlurred) * 2).toInt().coerceIn(0, 255)
                val bResult = (b + amount * (b - bBlurred) * 2).toInt().coerceIn(0, 255)

                val resultPixel = Color.rgb(rResult, gResult, bResult)
                result.setPixel(x, y, resultPixel)
            }
        }
        return result
    }
}
