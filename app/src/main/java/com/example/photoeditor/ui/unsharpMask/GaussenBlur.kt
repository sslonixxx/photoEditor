package com.example.photoeditor.ui.unsharpMask

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.exp

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

    // Normalize the kernel
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