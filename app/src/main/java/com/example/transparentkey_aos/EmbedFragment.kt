package com.example.transparentkey_aos

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.transparentkey_aos.databinding.FragmentEmbedBinding

class EmbedFragment : Fragment() {
    lateinit var binding: FragmentEmbedBinding
    lateinit var wmImg: Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 이미지 수신
        @Suppress("DEPRECATION")
        setFragmentResultListener("wm_img") { key, bundle ->
            val img: Bitmap? = bundle.getParcelable("wm_img")
            if (img != null) { // null이 아닐 때만 사용
                wmImg = img
                binding.ivWmImage.setImageBitmap(wmImg) // 이미지 iv에 배치
            }
        }

        @Suppress("DEPRECATION")
        setFragmentResultListener("qr_img") { key, bundle ->
            val img: Bitmap? = bundle.getParcelable("qr_img")
            if (img != null) { // null이 아닐 때만 사용
                wmImg = img
                binding.ivWmImage.setImageBitmap(wmImg) // 이미지 iv에 배치
            }
        }
    }

}