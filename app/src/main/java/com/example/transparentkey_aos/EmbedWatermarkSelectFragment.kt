package com.example.transparentkey_aos

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.transparentkey_aos.databinding.FragmentEmbedWatermarkSelectBinding
import com.example.transparentkey_aos.databinding.FragmentEmbedSelectBinding

class EmbedWatermarkSelectFragment : Fragment() {
    lateinit var binding : FragmentEmbedWatermarkSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedWatermarkSelectBinding.inflate(inflater, container, false)
        return binding.root

        binding.ivSelected.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY)
        //Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY
    }


}