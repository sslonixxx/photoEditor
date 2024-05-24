package com.example.photoeditor.ui.recognize

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.photoeditor.R
import com.example.photoeditor.databinding.FragmentRecognizeBinding
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class RecognizeFragment : Fragment() {

    private lateinit var binding: FragmentRecognizeBinding
    private lateinit var imageView: ImageView
    private var spinner: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OpenCVLoader.initDebug())
            Log.e("OpenCV", "Unable to load OpenCV!")
        else
            Log.d("OpenCV", "OpenCV loaded Successfully!")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRecognizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = activity?.findViewById(R.id.imageView)!!
        spinner = activity?.findViewById(R.id.progressBar1)!!
        val compareButton = activity?.findViewById<ImageView>(R.id.compareButton)!!

        binding.recognizeButton.setOnClickListener {
            val drawable = imageView.drawable

            if (drawable is BitmapDrawable) {
                spinner!!.visibility = View.VISIBLE

                val originalImage = drawable.bitmap
                val originalMat = Mat()
                Utils.bitmapToMat(originalImage, originalMat)

                val resultMat = getRecognizeFaces(originalMat, requireContext())

                val resultBitmap = Bitmap.createBitmap(resultMat.cols(), resultMat.rows(),
                    Bitmap.Config.ARGB_8888)

                Utils.matToBitmap(resultMat, resultBitmap)
                imageView.setImageBitmap(resultBitmap)

                spinner!!.visibility = View.GONE
            } else {
                Toast.makeText(activity, "Image not set or invalid image format",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRecognizeFaces(originalMat: Mat, context: Context): Mat {
        val cascades = listOf(
            R.raw.haarcascade_frontalface_alt,
        )

        val cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE)

        val cascadeFiles = cascades.map { rawId ->
            val inputStream: InputStream = context.resources.openRawResource(rawId)
            val cascadeFile = File(cascadeDir, context.resources.getResourceEntryName(rawId) + ".xml")
            val outputStream: OutputStream = FileOutputStream(cascadeFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            bytesRead = inputStream.read(buffer)

            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead)

                bytesRead = inputStream.read(buffer)
            }
            inputStream.close()
            outputStream.close()

            cascadeFile.absolutePath
        }

        val faceDetectors = cascadeFiles.map { cascadeFilePath ->
            CascadeClassifier(cascadeFilePath)
        }

        val grayscaleMat = Mat()
        Imgproc.cvtColor(originalMat, grayscaleMat, Imgproc.COLOR_BGR2GRAY)

        val combinedFaces = mutableListOf<Rect>()


        faceDetectors.forEach { detector ->
            val faces = MatOfRect()
            detector.detectMultiScale(grayscaleMat, faces)
            combinedFaces.addAll(faces.toList())
        }

        val uniqueFaces = combinedFaces.distinctBy { rect -> rect }

        uniqueFaces.forEach { rect ->
            Imgproc.rectangle(
                originalMat, rect.tl(), rect.br(),
                Scalar(255.0, 0.0, 0.0, 255.0), 2
            )
        }
        return originalMat
    }
}


