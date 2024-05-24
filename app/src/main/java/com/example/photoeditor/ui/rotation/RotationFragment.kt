package com.example.photoeditor.ui.rotation

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentRotationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        slider.addOnChangeListener { _, value, _ ->
            lifecycleScope.launch(Dispatchers.Default) {
                val matrix = getPixelsFromImageView(imageView)
                if (matrix != null) {
                    val transposedMatrix = if (value > previousSliderValue) {
                        transposePixelsMatrixby90(matrix)
                    } else {
                        transposePixelsMatrixby270(matrix)
                    }
                    val transposedBitmap = createBitmapFromMatrix(transposedMatrix)
                    imageView.post {
                        imageView.setImageBitmap(transposedBitmap)
                    }
                }
                previousSliderValue = value
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
            // Проходимся по каждому пикселю изображения и сохраняем его в матрицу
            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y][x] = bitmap.getPixel(x, y)
                }
            }
            // Возвращаем матрицу пикселей
            return pixels
        }
        // Возвращаем null, если не удалось получить битмапу
        return null
    }


    fun transposePixelsMatrixby270(matrix: Array<IntArray>): Array<IntArray> {
        val rows = matrix.size
        val cols = matrix[0].size
        // Создаем новую матрицу для транспонированных пикселей
        val transposedMatrix = Array(cols) { IntArray(rows) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                transposedMatrix[cols - 1 - j][i] = matrix[i][j]
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

    fun createBitmapFromMatrix(matrix: Array<IntArray>): Bitmap {
        val rows = matrix.size
        val cols = matrix[0].size
        val bitmap = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                // Отражаем каждую строку
                bitmap.setPixel(j, rows - 1 - i, matrix[i][j])
            }
        }
        return bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
