package com.example.photoeditor.ui.colorFilters

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentMosaicBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MosaicFragment : Fragment() {
    private var _binding: FragmentMosaicBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageView: ImageView
    private var spinner: ProgressBar? = null
    private var currentImage: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMosaicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = activity?.findViewById(R.id.imageView)!!
        spinner = activity?.findViewById(R.id.progressBar1)!!
        val compareButton = activity?.findViewById<ImageView>(R.id.compareButton)!!
        val userImage = imageView.drawable
        currentImage = imageView.drawable
        val mosaicFilter = MosaicFilter()
        val slider = binding.sizeOfBrushSlider
        var mosaicFactor = 1 // Initializing with 1 to avoid division by zero

        slider.addOnChangeListener { _, value, _ ->
            mosaicFactor = value.toInt() + 1 // Adding 1 to avoid division by zero
        }

        val originalBitmap = mosaicFilter.getBitmapFromImageView(imageView)

        binding.start.setOnClickListener {
            if (originalBitmap != null) {
                spinner?.visibility = View.VISIBLE
                lifecycleScope.launch {
                    val mosaicBitmap = withContext(Dispatchers.Default) {
                        mosaicFilter.createMosaicBitmap(originalBitmap, mosaicFactor)
                    }
                    imageView.setImageBitmap(mosaicBitmap)
                    currentImage = imageView.drawable
                    spinner?.visibility = View.GONE
                }
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class MosaicFilter {
        fun getBitmapFromImageView(imageView: ImageView): Bitmap? {
            return if (imageView.drawable != null) {
                val drawable = imageView.drawable as BitmapDrawable
                drawable.bitmap
            } else {
                null
            }
        }

        fun createMosaicBitmap(original: Bitmap, factor: Int): Bitmap {
            val width = original.width
            val height = original.height
            val mosaicBitmap = Bitmap.createBitmap(width, height, original.config)

            for (y in 0 until height step factor) {
                for (x in 0 until width step factor) {
                    val averageColor = calculateAverageColor(original, x, y, factor)

                    for (dy in 0 until factor) {
                        for (dx in 0 until factor) {
                            if (x + dx < width && y + dy < height) {
                                mosaicBitmap.setPixel(x + dx, y + dy, averageColor)
                            }
                        }
                    }
                }
            }

            return mosaicBitmap
        }

        private fun calculateAverageColor(
            bitmap: Bitmap,
            startX: Int,
            startY: Int,
            factor: Int
        ): Int {
            var redSum = 0
            var greenSum = 0
            var blueSum = 0
            var pixelCount = 0

            for (y in startY until startY + factor) {
                for (x in startX until startX + factor) {
                    if (x < bitmap.width && y < bitmap.height) {
                        val pixel = bitmap.getPixel(x, y)
                        redSum += Color.red(pixel)
                        greenSum += Color.green(pixel)
                        blueSum += Color.blue(pixel)
                        pixelCount++
                    }
                }
            }

            return if (pixelCount == 0) {
                Color.BLACK
            } else {
                val averageRed = redSum / pixelCount
                val averageGreen = greenSum / pixelCount
                val averageBlue = blueSum / pixelCount
                Color.rgb(averageRed, averageGreen, averageBlue)
            }
        }
    }
}
