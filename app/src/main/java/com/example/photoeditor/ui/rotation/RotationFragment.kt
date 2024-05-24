package com.example.photoeditor.ui.rotation

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentRotationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RotationFragment : Fragment() {

    private var _binding: FragmentRotationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRotationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val imageView = activity?.findViewById<ImageView>(R.id.imageView)!!
        val slider = binding.slider
        var previousSliderValue = slider.value // Сохраняем изначальное значение слайдера
        val matrix = getPixelsFromImageView(imageView)
        slider.addOnChangeListener { _, value, _ ->
            if (matrix != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val transposedBitmap = withContext(Dispatchers.Default) {
                        when (value) {
                            90.toFloat() -> {
                                val transposedMatrix = transposePixelsMatrixby90(matrix)
                                createBitmapFromMatrix(transposedMatrix)
                            }

                            180.toFloat() -> {
                                val transposedMatrix = transposePixelsMatrixby180(matrix)
                                createBitmapFromMatrix(transposedMatrix)

                            }

                            270.toFloat() -> {
                                val transposedMatrix = transposePixelsMatrixby270(matrix)
                                createBitmapFromMatrix(transposedMatrix)
                            }

                            else -> {
                                val transposedMatrix = matrix
                                createBitmapFromMatrix(transposedMatrix)

                            }
                        }
                    }
                    imageView.setImageBitmap(transposedBitmap)
                }
            }
        }
        return root
    }


    fun getPixelsFromImageView(imageView: ImageView): Array<IntArray>? {
        val drawable = imageView.drawable
        // Проверяем, является ли Drawable экземпляром BitmapDrawable
        if (drawable is BitmapDrawable) {
            // Получаем битмапу изображения
            val bitmap = drawable.bitmap
            val width = bitmap.width
            val height = bitmap.height
            // Создаем матрицу для хранения пикселей
            val pixels = Array(height) { IntArray(width) }
            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y][x] = bitmap.getPixel(x, y)
                }
            }
            return pixels
        }
        return null
    }


    fun transposePixelsMatrixby270(matrix: Array<IntArray>): Array<IntArray> {
        val rows = matrix.size
        val cols = matrix[0].size
        // Создаем новую матрицу для транспонированных пикселей
        val transposedMatrix = Array(cols) { IntArray(rows) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                transposedMatrix[j][rows - 1 - i] = matrix[i][j]
            }
        }
        return transposedMatrix
    }


    fun transposePixelsMatrixby90(matrix: Array<IntArray>): Array<IntArray> {
        val rows = matrix.size
        val cols = matrix[0].size
        // Создаем новую матрицу для транспонированных пикселей
        val transposedMatrix = Array(cols) { IntArray(rows) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                transposedMatrix[j][i] = matrix[i][j]
            }
        }
        return transposedMatrix
    }

    private fun transposePixelsMatrixby180(matrix: Array<IntArray>): Array<IntArray> {
        val rows = matrix.size
        val cols = matrix[0].size
        val rotatedMatrix = Array(rows) { IntArray(cols) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                rotatedMatrix[rows - 1 - i][cols - 1 - j] = matrix[i][j]
            }
        }
        return rotatedMatrix
    }

    fun createBitmapFromMatrix(matrix: Array<IntArray>): Bitmap {
        val rows = matrix.size
        val cols = matrix[0].size
        val bitmap = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                bitmap.setPixel(j, i, matrix[i][j]) // Пиксели не отражаются
            }
        }
        return bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
