package com.example.photoeditor

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.photoeditor.databinding.ActivityMainBinding
import com.example.photoeditor.recyclerView.adapter.FilterAdapter
import com.example.photoeditor.recyclerView.adapter.service.FilterGroupService
import com.github.dhaval2404.imagepicker.ImagePicker


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var adapter: FilterAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPermission()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        setupRecyclerView(navController)
    }

    override fun onStart() {
        super.onStart()
        val imageView = binding.imageView

        val startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val resultCode = result.resultCode
                val data = result.data

                if (resultCode == Activity.RESULT_OK) {
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
    }

    private fun setupRecyclerView(navController: NavController) {
        adapter = FilterAdapter { position ->
            when (position) {
                0 -> navController.navigate(R.id.rotationFragment)
                1 -> navController.navigate(R.id.colorFiltersFragment)
                2 -> navController.navigate(R.id.scalingFragment)
                3 -> navController.navigate(R.id.recognizeFragment)
                4 -> navController.navigate(R.id.vectorFragment)
                5 -> navController.navigate(R.id.retouchFragment)
                6 -> navController.navigate(R.id.unsharpFragment)
                7 -> navController.navigate(R.id.affineFragment)
                8 -> navController.navigate(R.id.cubeFragment)
                else -> navController.navigate(R.id.emptyFragment)
            }
        }
        adapter.data = FilterGroupService.filterList

        binding.bestRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.bestRecycler.adapter = adapter
    }

    private fun getPermission() {
        val requestPermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->

            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestPermissions.launch(
                arrayOf(
                    READ_MEDIA_IMAGES,
                    READ_MEDIA_VIDEO,
                    READ_MEDIA_VISUAL_USER_SELECTED
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions.launch(arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO))
        } else {
            requestPermissions.launch(arrayOf(READ_EXTERNAL_STORAGE))
        }
    }
}
