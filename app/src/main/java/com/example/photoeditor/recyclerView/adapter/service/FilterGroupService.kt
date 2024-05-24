package com.example.photoeditor.recyclerView.adapter.service

import com.example.photoeditor.R
import com.example.photoeditor.recyclerView.adapter.model.FilterEntity

object FilterGroupService {

    val filterList = listOf(
        FilterEntity(image = R.drawable.ic_rotate, text = R.string.rotation),
        FilterEntity(image = R.drawable.ic_filters, text = R.string.color_filters),
        FilterEntity(image = R.drawable.ic_scaling, text = R.string.scaling),
        FilterEntity(image = R.drawable.ic_recognize, text = R.string.recognize),
        FilterEntity(image = R.drawable.ic_retouch, text = R.string.retouching),
        FilterEntity(image = R.drawable.ic_unsharp, text = R.string.unsharp_mask),
        FilterEntity(image = R.drawable.ic_vector, text = R.string.vector_editor),
        FilterEntity(image = R.drawable.ic_affine, text = R.string.affine_transform),
        FilterEntity(image = R.drawable.ic_cube, text = R.string.cube3d)
    )
}