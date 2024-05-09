package com.example.photoeditor.ui.unsharpMask

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.photoeditor.databinding.FragmentUnsharpBinding


class UnsharpFragment : Fragment() {

    private lateinit var binding: FragmentUnsharpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUnsharpBinding.inflate(inflater, container, false)
        return binding.root
    }
}
