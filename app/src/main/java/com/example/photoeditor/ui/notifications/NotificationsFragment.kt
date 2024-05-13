package com.example.photoeditor.ui.notifications

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.content.ContentResolver;
import android.content.pm.PackageManager
import java.io.File
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.Contacts.SettingsColumns.KEY
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.photoeditor.databinding.FragmentNotificationsBinding
import java.io.FileOutputStream
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.photoeditor.Manifest
import com.example.photoeditor.R
import java.io.OutputStream
import java.lang.Exception
import java.util.*


@Suppress("DEPRECATION")

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val imageView = binding.imageView2;
        val slider = binding.slider
        val button = binding.scalingButton
        /*val matrix = getPixelsFromImageView(imageView)
        val newImage = matrix?.let { createBitmapFromMatrixWithRowFlip(it) }
        var savedImage = newImage*/
        imageView.setDrawingCacheEnabled(true)
        imageView.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        imageView.layout(
            0, 0,
            imageView.measuredWidth, imageView.measuredHeight
        )
        imageView.buildDrawingCache(true)
        val newImage = Bitmap.createBitmap(imageView.drawingCache)
        imageView.setDrawingCacheEnabled(false)
        /*button.setOnClickListener {


            var savedImage = newImage
            val k = slider.value
            if (k > 1) {
                savedImage = bilinearInterpolation(newImage, k)
                imageView.setImageBitmap(savedImage)
                //saveImageToStorage(savedImage)

            }
            //imageView.setImageBitmap(savedImage)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permissionWRITE_EXTERNAL_STORAGE, 100))
                }

        }*/

        return root
    }
    fun bilinearInterpolation(bitmap: Bitmap, k: Float): Bitmap {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val newWidth = (bitmap.width * k).toInt()
        val newHeight = (bitmap.height * k).toInt()
        val newImageArray = IntArray(newWidth * newHeight)

        val proportionByX = (bitmap.width - 1).toDouble() / newWidth
        val proportionByY = (bitmap.height - 1).toDouble() / newHeight

        var newIndex = 0
        var index: Int

        for (i in 0 until newHeight) {
            for (j in 0 until newWidth) {
                val x = (proportionByX * j).toInt()
                val y = (proportionByY * i).toInt()

                val newX = proportionByX * j - x
                val newY = proportionByY * i - y

                index = y * bitmap.width + x
                val p1 = pixels[index]
                val p2 = pixels[index + 1]
                val p3 = pixels[index + bitmap.width]
                val p4 = pixels[index + bitmap.width + 1]

                val d1 = (1 - newX) * (1 - newY)
                val d2 = newX * (1 - newY)
                val d3 = (1 - newX) *  newY
                val d4 = newX * newY


                val blue = (p1 and 0xff) * d1 + (p2 and 0xff) * d2 + (p3 and 0xff) * d3 + (p4 and 0xff) * d4
                val green = (p1 shr 8 and 0xff) * d1 + (p2 shr 8 and 0xff) * d2 + (p3 shr 8 and 0xff) * d3 + (p4 shr 8 and 0xff) * d4
                val red = (p1 shr 16 and 0xff) * d1 + (p2 shr 16 and 0xff) * d2 + (p3 shr 16 and 0xff) * d3 + (p4 shr 16 and 0xff) * d4

                newImageArray[newIndex++] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
            }
        }
        return Bitmap.createBitmap(newImageArray, newWidth, newHeight, Bitmap.Config.ARGB_8888)
    }

    /*private fun saveImageToStorage(bitmap: Bitmap) {
        try {
            val filename = "scaled_image_${System.currentTimeMillis()}.png"
            val directory = File(requireContext().filesDir, "images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, filename)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
            Toast.makeText(requireContext(), "Изображение сохранено: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка при сохранении изображения", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    */}
     fun getPixelsFromImageView(imageView: ImageView): Array<IntArray>? {
         val drawable = imageView.drawable
         if (drawable is BitmapDrawable) {
             val bitmap = drawable.bitmap
             val width = bitmap.width
             val height = bitmap.height
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

   /* override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageToStorage()
            }
            else {
                Toast.makeText(this, "Разрешение не получено, поэтому изображение невозможно сохранить", Toast.LENGTH_SHORT).show()

            }
        }
    }
    public fun saveImageToStorage() {
        val externalStorageState = Environment.getExternalStorageState()
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            val storageDirectory = Environment.getExternalStorageDirectory().toString()
            val file = File(storageDirectory, "test_image.jpg")
            try {
                val stream: OutputStream = FileOutputStream(file)
                var drawable = ContextCompat.getDrawable(ap, R.drawable.one)
                var bitmap = (drawable as BitmapDrawable).bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                stream.close()
                imageView2.setImageURI(Uri.parse(file.absolutePath))
                Toast.makeText(
                    this,
                    "Изображение успешно сохранено ${Uri.parse(file.absolutePath)}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(this, "Unable to access the storage", Toast.LENGTH_SHORT).show()
        }
    }
*/

    /*override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }*/
