package com.example.photoeditor.ui.scaling

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentScalingBinding

@Suppress("DEPRECATION")
class ScalingFragment : Fragment() {

    private lateinit var binding: FragmentScalingBinding
    lateinit var imageView: ImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScalingBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = activity?.findViewById(R.id.imageView)!!
        val compareButton = activity?.findViewById<ImageView>(R.id.compareButton)!!

        val originalImage = imageView.drawable ?: return

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

            } else if (k < 1) {
                savedImage = trilinearInterpolation(newImage, k)
                imageView.setImageBitmap(savedImage)
            } else {
                savedImage = newImage;
                imageView.setImageBitmap(savedImage)
            }

        }
        compareButton.setOnClickListener {
            imageView.setImageDrawable(originalImage);
        }
    }


    private fun bilinearInterpolation(bitmap: Bitmap, k: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newWidth = (width * k).toInt()
        val newHeight = (height * k).toInt()
        val resultBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888) // новый Bitmap, куда мы будем записывать результаты интерполяции

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height) //массив, содержащий пиксели исходного изображения

        for (y in 0 until newHeight) { //Внешний цикл проходит по каждой строке пикселей в новом изображении
            val srcY = y / k
            val yFloor = srcY.toInt() //левый пиксель в квадрате 2x2
            val yCeil = (srcY + 1).toInt()
                .coerceAtMost(height - 1) // координаты соседних пикселей, которые составляют квадрат 2x2
            val yWeight = srcY - yFloor //дробные части координат, которые будут использоваться в весовых коэффициентах для интерполяции

            for (x in 0 until newWidth) { //Внутренний цикл проходит по каждому столбцу пикселей в новом изображении
                val srcX = x / k
                val xFloor = srcX.toInt() //левый пиксель в квадрате 2x2
                val xCeil = (srcX + 1).toInt()
                    .coerceAtMost(width - 1) // координаты соседних пикселей, которые составляют квадрат 2x2
                val xWeight =
                    srcX - xFloor //дробные части координат, которые будут использоваться в весовых коэффициентах для интерполяции

                //четыре пикселя из исходного изображения, которые окружают координаты (srcX, srcY)
                val p1 = pixels[yFloor * width + xFloor]
                val p2 = pixels[yFloor * width + xCeil]
                val p3 = pixels[yCeil * width + xFloor]
                val p4 = pixels[yCeil * width + xCeil]

                val blue = (Color.blue(p1) * (1 - xWeight) * (1 - yWeight) +
                        Color.blue(p2) * xWeight * (1 - yWeight) +
                        Color.blue(p3) * (1 - xWeight) * yWeight +
                        Color.blue(p4) * xWeight * yWeight).toInt()

                val green = (Color.green(p1) * (1 - xWeight) * (1 - yWeight) +
                        Color.green(p2) * xWeight * (1 - yWeight) +
                        Color.green(p3) * (1 - xWeight) * yWeight +
                        Color.green(p4) * xWeight * yWeight).toInt()

                val red = (Color.red(p1) * (1 - xWeight) * (1 - yWeight) +
                        Color.red(p2) * xWeight * (1 - yWeight) +
                        Color.red(p3) * (1 - xWeight) * yWeight +
                        Color.red(p4) * xWeight * yWeight).toInt()

                resultBitmap.setPixel(x, y, Color.rgb(red, green, blue)) //Установка нового цвета пикселя
            }
        }
        return resultBitmap
    }

    private fun generateMipmaps(bitmap: Bitmap): List<Bitmap> {
        val mipmaps = mutableListOf<Bitmap>() // список для хранения всех уровней mipmaps
        var currentBitmap = bitmap // текущее изображение, начиная с исходного

        mipmaps.add(currentBitmap)
        while (currentBitmap.width > 1 && currentBitmap.height > 1) { //создаем уменьшенные версии currentBitmap и добавляем их в список mipmaps, пока не дойдем до 1х1
            val newWidth = currentBitmap.width / 2
            val newHeight = currentBitmap.height / 2
            val resizedBitmap = Bitmap.createScaledBitmap(currentBitmap, newWidth, newHeight, true)
            mipmaps.add(resizedBitmap)
            currentBitmap = resizedBitmap
        }

        return mipmaps
    }

    private fun trilinearInterpolation(bitmap: Bitmap, k: Float): Bitmap {
        // Генерация mipmap
        val mipmaps = generateMipmaps(bitmap)

        // Определение уровня детализации (LOD)
        val mipLevel = (Math.log(k.toDouble()) / Math.log(2.0)).toFloat()
            .coerceIn(0.0f, (mipmaps.size - 1).toFloat())
        val level1 = mipLevel.toInt() //выбираются два ближайших уровня mipmaps
        val level2 = (level1 + 1).coerceAtMost(mipmaps.size - 1)

        val alpha = mipLevel - level1 //весовой коэф

        //билинейная интерполяции для двух уровней mipmap
        val scaledBitmap1 = bilinearInterpolation(mipmaps[level1], k / (1 shl level1))
        val scaledBitmap2 = bilinearInterpolation(mipmaps[level2], k / (1 shl level2))

        val newWidth = (bitmap.width * k).toInt()
        val newHeight = (bitmap.height * k).toInt()
        val resultBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                // получаем цветовое значение пикселя из двух интерполированных изображений
                val color1 = scaledBitmap1.getPixel(
                    x.coerceIn(0, scaledBitmap1.width - 1),
                    y.coerceIn(0, scaledBitmap1.height - 1)
                )
                val color2 = scaledBitmap2.getPixel(
                    x.coerceIn(0, scaledBitmap2.width - 1),
                    y.coerceIn(0, scaledBitmap2.height - 1)
                )

                // Интерполяция цветовых каналов
                val red = (Color.red(color1) * (1 - alpha) + Color.red(color2) * alpha).toInt()
                val green = (Color.green(color1) * (1 - alpha) + Color.green(color2) * alpha).toInt()
                val blue = (Color.blue(color1) * (1 - alpha) + Color.blue(color2) * alpha).toInt()

                // Установка нового цвета пикселя
                resultBitmap.setPixel(x, y, Color.rgb(red, green, blue))
            }
        }
        return resultBitmap
    }

}