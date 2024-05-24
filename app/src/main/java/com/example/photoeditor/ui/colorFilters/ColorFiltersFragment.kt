package com.example.photoeditor.ui.colorFilters

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentFiltersBinding
import android.widget.ProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ColorFiltersFragment : Fragment() {
    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageView: ImageView
    private var originalBitmap: Bitmap? = null
    private var isBWFilterApplied = false
    private var spinner: ProgressBar? = null
    private var currentImage: Drawable? = null


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
        spinner = activity?.findViewById(R.id.progressBar1)!!
        val compareButton = activity?.findViewById<ImageView>(R.id.compareButton)!!
        val userImage = imageView.drawable
        currentImage = imageView.drawable

        originalBitmap = (imageView.drawable as? BitmapDrawable)?.bitmap


        binding.blackAndWhite.setOnClickListener {
            toggleBWFilter()
        }
        binding.mosaic.setOnClickListener {
            findNavController().navigate(R.id.mosaicFragment)
        }
        binding.gaussingBlurFilter.setOnClickListener {
            findNavController().navigate(R.id.gaussianBlurFragment)
        }
        binding.noir.setOnClickListener {
            findNavController().navigate(R.id.noirFragment)
        }
        binding.warm.setOnClickListener {
            findNavController().navigate(R.id.warmFragment)
        }

        compareButton.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    imageView.setImageDrawable(userImage)
                }

                MotionEvent.ACTION_UP -> {
                    imageView.setImageDrawable(currentImage)
                }
            }
            true
        }
    }

    private fun toggleBWFilter() {
        if (isBWFilterApplied) {
            imageView.setImageBitmap(originalBitmap)
            isBWFilterApplied = false
        } else {
            spinner?.visibility = View.VISIBLE
            lifecycleScope.launch {
                val blackAndWhiteFilter = BlackAndWhite()
                val bwBitmap = withContext(Dispatchers.Default) {
                    blackAndWhiteFilter.applyBlackAndWhiteFilter(originalBitmap!!)
                }
                imageView.setImageBitmap(bwBitmap)
                currentImage = imageView.drawable

                spinner?.visibility = View.GONE
                isBWFilterApplied = true
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class BlackAndWhite {
        fun applyBlackAndWhiteFilter(original: Bitmap): Bitmap {
            val bwBitmap = Bitmap.createBitmap(original.width, original.height, original.config)
            for (i in 0 until original.width) {
                for (j in 0 until original.height) {
                    val pixel = original.getPixel(i, j)
                    val red = Color.red(pixel)
                    val green = Color.green(pixel)
                    val blue = Color.blue(pixel)
                    val gray = (red + green + blue) / 3
                    val newPixel = Color.rgb(gray, gray, gray)
                    bwBitmap.setPixel(i, j, newPixel)
                }
            }
            return bwBitmap
        }
    }
}
