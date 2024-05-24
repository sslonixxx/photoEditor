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
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentNoirBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.ProgressBar

class NoirFragment : Fragment() {
    private var _binding: FragmentNoirBinding? = null
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
        _binding = FragmentNoirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = activity?.findViewById(R.id.imageView)!!
        spinner = activity?.findViewById(R.id.progressBar1)!!
        val compareButton = activity?.findViewById<ImageView>(R.id.compareButton)!!
        val userImage = imageView.drawable
        currentImage = imageView.drawable
        val noirFilter = NoirFilter()
        val grainSlider = binding.grainSlider
        val brightnessSlider = binding.brightnessSlider
        var grainLevel = 0
        var brightness = 0

        grainSlider.addOnChangeListener { _, value, _ ->
            grainLevel = value.toInt()
        }

        brightnessSlider.addOnChangeListener { _, value, _ ->
            brightness = value.toInt()
        }
        val originalBitmap = getBitmapFromImageView(imageView)

        binding.start.setOnClickListener {
            spinner?.visibility = View.VISIBLE
            lifecycleScope.launch {
                if (originalBitmap != null) {
                    val noirBitmap = withContext(Dispatchers.Default) {
                        noirFilter.applyNoirFilter(originalBitmap, grainLevel, brightness)
                    }
                    imageView.setImageBitmap(noirBitmap)
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
    private fun getBitmapFromImageView(imageView: ImageView): Bitmap? {
        return if (imageView.drawable != null) {
            val drawable = imageView.drawable as BitmapDrawable
            drawable.bitmap
        } else {
            null
        }
    }
}
class NoirFilter {
    fun applyNoirFilter(src: Bitmap, grainLevel: Int, brightness: Int): Bitmap {
        val width = src.width
        val height = src.height
        val result = Bitmap.createBitmap(width, height, src.config)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = src.getPixel(x, y)
                val red = Color.red(pixel)
                val green = Color.green(pixel)
                val blue = Color.blue(pixel)

                val gray = (0.3 * red + 0.59 * green + 0.11 * blue).toInt()

                val brightGray = (gray + brightness).coerceIn(0, 255)

                val grain = (Math.random() * grainLevel).toInt()
                val finalGray = (brightGray + grain).coerceIn(0, 255)

                result.setPixel(x, y, Color.rgb(finalGray, finalGray, finalGray))
            }
        }
        return result
    }
}
