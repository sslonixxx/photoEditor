package com.example.photoeditor.ui.notifications

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.photoeditor.databinding.FragmentNotificationsBinding


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
        val imageView = binding.imageView;
        val slider = binding.slider
        val button = binding.scalingButton
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
        button.setOnClickListener {
            var savedImage = newImage
            val k = slider.value
            if (k > 1) {
                savedImage = bilinearInterpolation(newImage, k)
                imageView.setImageBitmap(savedImage)

            }
            else if (k < 1) {
                savedImage = trilinearInterpolation(newImage, k)
                imageView.setImageBitmap(savedImage)
            }
            else{
                savedImage = newImage;
                imageView.setImageBitmap(savedImage)
            }

        }

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
                val d3 = (1 - newX) * newY
                val d4 = newX * newY

                val blue = (p1 and 0xff) * d1 + (p2 and 0xff) * d2 + (p3 and 0xff) * d3 + (p4 and 0xff) * d4
                val green = (p1 shr 8 and 0xff) * d1 + (p2 shr 8 and 0xff) * d2 + (p3 shr 8 and 0xff) * d3 + (p4 shr 8 and 0xff) * d4
                val red = (p1 shr 16 and 0xff) * d1 + (p2 shr 16 and 0xff) * d2 + (p3 shr 16 and 0xff) * d3 + (p4 shr 16 and 0xff) * d4

                newImageArray[newIndex++] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
            }
        }
        return Bitmap.createBitmap(newImageArray, newWidth, newHeight, Bitmap.Config.ARGB_8888)
    }
    fun trilinearInterpolation(bitmap: Bitmap, k: Float): Bitmap {
        var largerScale = 1.0
        while (k < largerScale / 2) {
            largerScale /= 2
        }
        val smallerScale = largerScale / 2

        val largerBitmap = bilinearInterpolation(bitmap, largerScale.toFloat())
        val smallerBitmap = bilinearInterpolation(bitmap, smallerScale.toFloat())

        val largerBitmapPixels = IntArray(largerBitmap.width * largerBitmap.height)
        val smallerBitmapPixels = IntArray(smallerBitmap.width * smallerBitmap.height)
        largerBitmap.getPixels(largerBitmapPixels, 0, largerBitmap.width, 0, 0, largerBitmap.width, largerBitmap.height)
        smallerBitmap.getPixels(smallerBitmapPixels, 0, smallerBitmap.width, 0, 0, smallerBitmap.width, smallerBitmap.height)

        val newWidth = (bitmap.width * k).toInt()
        val newHeight = (bitmap.height * k).toInt()
        val newBitmapPixels = IntArray(newWidth * newHeight)
        var pixelIndex = 0

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                val largerX = ((x / k).toInt() * largerScale).toInt()
                val largerY = ((y / k).toInt() * largerScale).toInt()

                val smallerX = ((x / k).toInt() * smallerScale).toInt()
                val smallerY = ((y / k).toInt() * smallerScale).toInt()

                val largerIndex = (largerX * largerBitmap.height + largerY)
                val smallerIndex = (smallerX * smallerBitmap.height + smallerY)

                var pixelLarger = 0
                var pixelSmaller = 0
                try {
                    pixelLarger = largerBitmapPixels[largerIndex]
                    pixelSmaller = smallerBitmapPixels[smallerIndex]
                } catch (e: Exception) {
                    pixelLarger = largerBitmapPixels[largerIndex - 1]
                    pixelSmaller = smallerBitmapPixels[smallerIndex - 1]
                }

                val redLarger = Color.red(pixelLarger)
                val greenLarger = Color.green(pixelLarger)
                val blueLarger = Color.blue(pixelLarger)

                val redSmaller = Color.red(pixelSmaller)
                val greenSmaller = Color.green(pixelSmaller)
                val blueSmaller = Color.blue(pixelSmaller)

                val red = (redLarger * (1 / smallerScale - k) + redSmaller * (k - (1 / largerScale))) / (1 / largerScale)
                val green = (greenLarger * (1 / smallerScale - k) + greenSmaller * (k - (1 / largerScale))) / (1 / largerScale)
                val blue = (blueLarger * (1 / smallerScale - k) + blueSmaller * (k - (1 / largerScale))) / (1 / largerScale)

                newBitmapPixels[pixelIndex++] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
            }
        }
        return Bitmap.createBitmap(newBitmapPixels, newWidth, newHeight, Bitmap.Config.ARGB_8888)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}