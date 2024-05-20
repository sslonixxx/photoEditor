package com.example.photoeditor.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.photoeditor.databinding.FragmentHomeBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageView = binding.imageView

        val startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val resultCode = result.resultCode
                val data = result.data

                if (resultCode == Activity.RESULT_OK) {
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!

                    imageView.setImageURI(fileUri)
                }
            }

        binding.importButton.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        }

        var rateOfRetouching: Float = 0f
        var sizeOfBrush: Float = 0f
// Установка слушателя изменений для Slider
        binding.rateOfRetouchingSlider.addOnChangeListener { _, value, _ ->
            // Обновляем значение переменной при изменении пользователем Slider
            rateOfRetouching = value
        }
        binding.sizeOfBrushSlider.addOnChangeListener { _, value, _ ->
            // Обновляем значение переменной при изменении пользователем Slider
            sizeOfBrush = value
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
                for (j in max(0, centerY - sizeOfBrush) until min(bitmap.height, centerY + sizeOfBrush)) {
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
                for (j in max(0, centerY - sizeOfBrush) until min(bitmap.height, centerY + sizeOfBrush)) {
                    val distanceSquared = (i - centerX) * (i - centerX) + (j - centerY) * (j - centerY)
                    if (distanceSquared <= radiusSquared) {
                        val distanceFactor = sqrt(distanceSquared.toFloat()) / sizeOfBrush
                        val red = (avgRed + (Color.red(bitmap.getPixel(i, j)) - avgRed) * distanceFactor * rateOfRetouching).toInt()
                        val green = (avgGreen + (Color.green(bitmap.getPixel(i, j)) - avgGreen) * distanceFactor * rateOfRetouching).toInt()
                        val blue = (avgBlue + (Color.blue(bitmap.getPixel(i, j)) - avgBlue) * distanceFactor * rateOfRetouching).toInt()
                        mutableBitmap.setPixel(i, j, Color.rgb(red, green, blue))
                    }
                }
            }

            return mutableBitmap
        }


        fun getBitmapFromImageView(imageView: ImageView): Bitmap {
            // Получаем размеры ImageView
            val width = imageView.width
            val height = imageView.height

            // Создаем Bitmap с такими же размерами, что и ImageView
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            // Создаем Canvas, связанный с этим Bitmap
            val canvas = Canvas(bitmap)

            // Рисуем содержимое ImageView на Canvas
            imageView.draw(canvas)

            return bitmap
        }
            var lastX = 0
            var lastY = 0

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
