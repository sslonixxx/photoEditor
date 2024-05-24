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
import com.example.photoeditor.databinding.FragmentGaussianBlurBinding
import kotlin.math.exp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GaussianBlurFragment : Fragment() {
    private var _binding: FragmentGaussianBlurBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageView: ImageView
    private var spinner: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGaussianBlurBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = activity?.findViewById(R.id.imageView)!!
        spinner = activity?.findViewById(R.id.progressBar1)!!
        val compareButton = activity?.findViewById<ImageView>(R.id.compareButton)!!
        val userImage = imageView.drawable
        val gaussianBlurFilter = GaussingBlurFilter()
        val slider = binding.radiusSlider
        var radius = 0

        slider.addOnChangeListener { _, value, _ ->
            radius = value.toInt()
        }
        val originalBitmap = gaussianBlurFilter.getBitmapFromImageView(imageView)

        binding.start.setOnClickListener {
            spinner?.visibility = View.VISIBLE
            lifecycleScope.launch {
                if (originalBitmap != null) {
                    val blurredBitmap = withContext(Dispatchers.Default) {
                        gaussianBlurFilter.applyGaussianBlur(originalBitmap, radius)
                    }
                    imageView.setImageBitmap(blurredBitmap)
                    spinner?.visibility = View.GONE
                }
            }
        }
        compareButton.setOnClickListener {
            imageView.setImageDrawable(userImage);
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    class GaussingBlurFilter {
        fun getBitmapFromImageView(imageView: ImageView): Bitmap? {
            return if (imageView.drawable != null) {
                val drawable = imageView.drawable as BitmapDrawable
                drawable.bitmap
            } else {
                null
            }
        }

        fun createGaussianKernel(radius: Int, sigma: Double): Array<DoubleArray> {
            val size = 2 * radius + 1
            val kernel = Array(size) { DoubleArray(size) }
            val sigma2 = 2 * sigma * sigma
            val piSigma2 = Math.PI * sigma2
            var sum = 0.0

            for (y in -radius..radius) {
                for (x in -radius..radius) {
                    val distance = (x * x + y * y).toDouble()
                    kernel[y + radius][x + radius] = exp(-distance / sigma2) / piSigma2
                    sum += kernel[y + radius][x + radius]
                }
            }

            for (y in 0 until size) {
                for (x in 0 until size) {
                    kernel[y][x] /= sum
                }
            }

            return kernel
        }

        fun applyGaussianBlur(src: Bitmap, radius: Int): Bitmap {
            val width = src.width
            val height = src.height
            val result = Bitmap.createBitmap(width, height, src.config)
            val kernel = createGaussianKernel(radius, radius / 3.0)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    var r = 0.0
                    var g = 0.0
                    var b = 0.0

                    for (kx in -radius..radius) {
                        for (ky in -radius..radius) {
                            val px = (x + kx).coerceIn(0, width - 1)
                            val py = (y + ky).coerceIn(0, height - 1)

                            val pixel = src.getPixel(px, py)
                            val kernelValue = kernel[kx + radius][ky + radius]

                            r += Color.red(pixel) * kernelValue
                            g += Color.green(pixel) * kernelValue
                            b += Color.blue(pixel) * kernelValue
                        }
                    }

                    val rInt = r.coerceIn(0.0, 255.0).toInt()
                    val gInt = g.coerceIn(0.0, 255.0).toInt()
                    val bInt = b.coerceIn(0.0, 255.0).toInt()

                    result.setPixel(x, y, Color.rgb(rInt, gInt, bInt))
                }
            }

            return result
        }
    }
}
