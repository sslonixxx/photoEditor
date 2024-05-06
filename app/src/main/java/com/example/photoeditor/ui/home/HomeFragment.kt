package com.example.photoeditor.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.photoeditor.databinding.FragmentHomeBinding
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    // private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val imageView = binding.imageView;
        val slider = binding.slider
        var previousSliderValue = slider.value // Сохраняем изначальное значение слайдера
        slider.addOnChangeListener { _, value, _ ->
            val matrix = getPixelsFromImageView(imageView)
            if (matrix != null) {
                if (value > previousSliderValue) {
                    val transposedMatrix = transposePixelsMatrixby90(matrix)
                    val transposedBitmap = createBitmapFromMatrixWithRowFlip(transposedMatrix)
                    imageView.setImageBitmap(transposedBitmap)
                }
                // Проверяем, двигается ли слайдер в обратную сторону
                else{
                    val transposedMatrix = transposePixelsMatrixby270(matrix)
                    val transposedBitmap = createBitmapFromMatrixWithRowFlip(transposedMatrix)
                    imageView.setImageBitmap(transposedBitmap)
                }
                // Обновляем предыдущее значение слайдера

            }
            previousSliderValue = value
        }
        return root
    }
    fun getPixelsFromImageView(imageView: ImageView): Array<IntArray>? {
        // Получаем Drawable из ImageView
        val drawable = imageView.drawable
        // Проверяем, является ли Drawable экземпляром BitmapDrawable
        if (drawable is BitmapDrawable) {
            // Получаем битмапу изображения
            val bitmap = drawable.bitmap
            // Получаем размеры изображения
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
        // Проходимся по каждому элементу исходной матрицы и копируем его в транспонированную матрицу, меняя индексы
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
        // Проходимся по каждому элементу исходной матрицы и копируем его в транспонированную матрицу, меняя индексы
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                transposedMatrix[j][i] = matrix[i][j]
            }
        }
        return transposedMatrix
    }
    fun createBitmapFromMatrixWithRowFlip(matrix: Array<IntArray>): Bitmap {
        val rows = matrix.size
        val cols = matrix[0].size
        // Создаем новый битмап
        val bitmap = Bitmap.createBitmap(cols, rows, Bitmap.Config.ARGB_8888)
        // Устанавливаем пиксели из транспонированной матрицы с отражением каждой строки в битмап
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