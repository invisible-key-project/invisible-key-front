package com.example.transparentkey_aos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.transparentkey_aos.databinding.FragmentEmbeddingSelectBinding

class EmbedSelectFragment: Fragment() {
    lateinit var binding : FragmentEmbeddingSelectBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbeddingSelectBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCam.setOnClickListener {
            replaceFragment(EmbedCamFragment())
        }
        binding.btnPhotos.setOnClickListener {
            replaceFragment(EmbedPhotosFragment())
        }
    }

    // fragment replace 함수
    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main, fragment)
            .commit()
    }
}