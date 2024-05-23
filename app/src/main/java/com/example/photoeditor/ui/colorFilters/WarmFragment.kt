package com.example.photoeditor.ui.colorFilters

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentWarmBinding
import kotlin.math.exp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WarmFragment : Fragment() {
    private var _binding: FragmentWarmBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWarmBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = activity?.findViewById(R.id.imageView)!!
        val warmFilter = WarmFilter()
        var slider = binding.warmSlider
        var warmValue = 0

        val originalBitmap = warmFilter.getBitmapFromImageView(imageView)

        slider.addOnChangeListener { slider, value, fromUser ->
            warmValue = value.toInt()
            lifecycleScope.launch {
                if (originalBitmap != null) {
                    val warmBitmap = withContext(Dispatchers.Default) {
                        warmFilter.applyWarmFilter(originalBitmap, warmValue)
                    }
                    imageView.setImageBitmap(warmBitmap)
                }
            }
        }
    }


    class WarmFilter {
        fun getBitmapFromImageView(imageView: ImageView): Bitmap? {
            return if (imageView.drawable != null) {
                val drawable = imageView.drawable as BitmapDrawable
                drawable.bitmap
            } else {
                null
            }
        }

        fun applyWarmFilter(src: Bitmap, warmth: Int): Bitmap {
            val width = src.width
            val height = src.height
            val result = Bitmap.createBitmap(width, height, src.config)

            // The warmth value will range from -100 to 100
            val redScale = 1 + (warmth / 100.0)
            val blueScale = 1 - (warmth / 100.0)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    val pixel = src.getPixel(x, y)
                    val red = (Color.red(pixel) * redScale).coerceIn(0.0, 255.0).toInt()
                    val green = Color.green(pixel)
                    val blue = (Color.blue(pixel) * blueScale).coerceIn(0.0, 255.0).toInt()

                    result.setPixel(x, y, Color.rgb(red, green, blue))
                }
            }

            return result
        }
    }
}
