package com.example.photoeditor.ui.retouch

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.photoeditor.databinding.FragmentRetouchBinding
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import com.example.photoeditor.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


class RetouchFragment : Fragment() {

    private var _binding: FragmentRetouchBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageView: ImageView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRetouchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var rateOfRetouching: Float = 0f
        var sizeOfBrush: Float = 0f

        binding.rateOfRetouchingSlider.addOnChangeListener { _, value, _ ->
            rateOfRetouching = value
        }
        binding.sizeOfBrushSlider.addOnChangeListener { _, value, _ ->
            sizeOfBrush = value
        }
        var lastX = 0
        var lastY = 0
        imageView = activity?.findViewById(R.id.imageView)!!
        imageView.setOnTouchListener { _, event ->
            try {
                val imageMatrix = imageView.imageMatrix
                val drawable = imageView.drawable
                val rect = drawable.bounds
                val transformedPoint = FloatArray(2)

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        transformedPoint[0] = event.x
                        transformedPoint[1] = event.y
                        imageMatrix.invert(imageMatrix)
                        imageMatrix.mapPoints(transformedPoint)
                        lastX = transformedPoint[0].toInt()
                        lastY = transformedPoint[1].toInt()
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        transformedPoint[0] = event.x
                        transformedPoint[1] = event.y
                        imageMatrix.invert(imageMatrix)
                        imageMatrix.mapPoints(transformedPoint)
                        val x = transformedPoint[0].toInt()
                        val y = transformedPoint[1].toInt()
                        val deltaX = x - lastX
                        val deltaY = y - lastY

                        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                        val newBitmap = calculatingAverageColors(bitmap, x, y,
                            sizeOfBrush.toInt(), rateOfRetouching)
                        imageView.setImageBitmap(newBitmap)

                        lastX = x
                        lastY = y
                        true
                    }
                    else -> false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    }


    fun calculatingAverageColors(
        bitmap: Bitmap,
        x: Int,
        y: Int,
        sizeOfBrush: Int,
        rateOfRetouching: Float
    ): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val centerX = x
        val centerY = y

        val radiusSquared = sizeOfBrush * sizeOfBrush

        // Суммируем цвета пикселей внутри круга
        val colorsInsideCircle = mutableListOf<Int>()
        for (i in max(0, centerX - sizeOfBrush) until min(bitmap.width, centerX + sizeOfBrush)) {
            for (j in max(0, centerY - sizeOfBrush) until min(
                bitmap.height,
                centerY + sizeOfBrush
            )) {
                val distanceSquared = (i - centerX) * (i - centerX) + (j - centerY) * (j - centerY)
                if (distanceSquared <= radiusSquared) {
                    colorsInsideCircle.add(bitmap.getPixel(i, j))
                }
            }
        }

        // Вычисляем средний цвет
        var sumRed = 0
        var sumGreen = 0
        var sumBlue = 0
        for (color in colorsInsideCircle) {
            sumRed += Color.red(color)
            sumGreen += Color.green(color)
            sumBlue += Color.blue(color)
        }
        val avgRed = sumRed / colorsInsideCircle.size
        val avgGreen = sumGreen / colorsInsideCircle.size
        val avgBlue = sumBlue / colorsInsideCircle.size

        // Заполняем пиксели внутри круга цветом, близким к изначальному цвету пикселя, с учетом rateOfRetouching
        for (i in max(0, centerX - sizeOfBrush) until min(bitmap.width, centerX + sizeOfBrush)) {
            for (j in max(0, centerY - sizeOfBrush) until min(
                bitmap.height,
                centerY + sizeOfBrush
            )) {
                val distanceSquared = (i - centerX) * (i - centerX) + (j - centerY) * (j - centerY)
                if (distanceSquared <= radiusSquared) {
                    val distanceFactor = sqrt(distanceSquared.toFloat()) / sizeOfBrush
                    val red = (avgRed + (Color.red(
                        bitmap.getPixel(
                            i,
                            j
                        )
                    ) - avgRed) * distanceFactor * rateOfRetouching).toInt()
                    val green = (avgGreen + (Color.green(
                        bitmap.getPixel(
                            i,
                            j
                        )
                    ) - avgGreen) * distanceFactor * rateOfRetouching).toInt()
                    val blue = (avgBlue + (Color.blue(
                        bitmap.getPixel(
                            i,
                            j
                        )
                    ) - avgBlue) * distanceFactor * rateOfRetouching).toInt()
                    mutableBitmap.setPixel(i, j, Color.rgb(red, green, blue))
                }
            }
        }

        return mutableBitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
