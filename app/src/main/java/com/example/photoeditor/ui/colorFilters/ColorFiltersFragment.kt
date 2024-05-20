package com.example.photoeditor.ui.colorFilters

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentFiltersBinding


class ColorFiltersFragment : Fragment() {
    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFiltersBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = activity?.findViewById(R.id.imageView)!!
        val blackAndWhiteFilter = blackAndWhite()
        binding.blackAndWhite.setOnClickListener {
            blackAndWhiteFilter.setImageViewWithBWFilter(imageView)
        }
    }

    class blackAndWhite {
        fun getBitmapFromImageView(imageView: ImageView): Bitmap? {
            // Make sure the ImageView has a drawable set
            if (imageView.drawable == null) {
                return null
            }

            // Get the drawable from the ImageView
            val drawable = imageView.drawable as BitmapDrawable
            // Get the bitmap from the drawable
            val bitmap = drawable.bitmap

            return bitmap
        }

        fun applyBlackAndWhiteFilter(original: Bitmap): Bitmap {
            // Create a mutable bitmap with the same dimensions as the original
            val bwBitmap = Bitmap.createBitmap(original.width, original.height, original.config)

            // Loop through the pixels and convert them to grayscale
            for (i in 0 until original.width) {
                for (j in 0 until original.height) {
                    // Get the current pixel color
                    val pixel = original.getPixel(i, j)
                    // Extract the red, green, and blue components
                    val red: Int = Color.red(pixel)
                    val green: Int = Color.green(pixel)
                    val blue: Int = Color.blue(pixel)

                    // Calculate the grayscale value
                    val gray = (red + green + blue) / 3

                    // Set the new pixel color to grayscale
                    val newPixel: Int = Color.rgb(gray, gray, gray)
                    bwBitmap.setPixel(i, j, newPixel)
                }
            }

            return bwBitmap
        }

        fun setImageViewWithBWFilter(imageView: ImageView) {
            val originalBitmap = getBitmapFromImageView(imageView)
            if (originalBitmap != null) {
                val bwBitmap = applyBlackAndWhiteFilter(originalBitmap)
                imageView.setImageBitmap(bwBitmap)
            }
        }
    }

}