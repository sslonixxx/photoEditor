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
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentFiltersBinding

class ColorFiltersFragment : Fragment() {
    private var _binding: FragmentFiltersBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageView: ImageView
    private var originalBitmap: Bitmap? = null
    private var isBWFilterApplied = false

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

        originalBitmap = (imageView.drawable as BitmapDrawable).bitmap

        binding.blackAndWhite.setOnClickListener {
            toggleBWFilter()
        }
        binding.mosaic.setOnClickListener {
            findNavController().navigate(R.id.mosaicFragment)
        }
        binding.gaussingBlurFilter.setOnClickListener{
            findNavController().navigate(R.id.gaussianBlurFragment)
        }
        binding.noir.setOnClickListener{
            findNavController().navigate(R.id.noirFragment)
        }
        binding.warm.setOnClickListener{
            findNavController().navigate(R.id.warmFragment)
        }
    }

    private fun toggleBWFilter() {
        if (isBWFilterApplied) {
            imageView.setImageBitmap(originalBitmap)
            isBWFilterApplied = false
        } else {
            val blackAndWhiteFilter = BlackAndWhite()
            blackAndWhiteFilter.setImageViewWithBWFilter(imageView)
            isBWFilterApplied = true
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

        fun setImageViewWithBWFilter(imageView: ImageView) {
            val drawable = imageView.drawable as BitmapDrawable
            val originalBitmap = drawable.bitmap
            val bwBitmap = applyBlackAndWhiteFilter(originalBitmap)
            imageView.setImageBitmap(bwBitmap)
        }
    }
}
