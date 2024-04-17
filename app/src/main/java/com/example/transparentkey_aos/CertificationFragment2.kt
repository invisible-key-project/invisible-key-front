package com.example.transparentkey_aos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.transparentkey_aos.databinding.FragmentCertification2Binding


class CertificationFragment2 : Fragment() {
    lateinit var binding : FragmentCertification2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCertification2Binding.inflate(inflater, container, false)

        // ImageStorage에서 이미지를 가져와서 표시
        CertificationFragment1.ImageStorage.currentImage?.let { image ->
            binding.cert2ResultIv.setImageBitmap(image)
        }

        binding.cert2EndBtn.setOnClickListener {
            // ImageStorage 초기화
            CertificationFragment1.ImageStorage.clear()

            // MainFragment로 돌아가기
            returnToMainFragment()
        }

        return binding.root
    }

    fun returnToMainFragment() {
        (context as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.main, MainFragment())
            .commitAllowingStateLoss()
    }

}