package com.example.transparentkey_aos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.example.transparentkey_aos.databinding.FragmentEmbedWatermarkSelectBinding
import com.example.transparentkey_aos.databinding.FragmentEmbedSelectBinding

class EmbedWatermarkSelectFragment : Fragment() {
    lateinit var binding: FragmentEmbedWatermarkSelectBinding
    private val REQUEST_KEY = "selected_img_path" // 데이터 요청 키

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedWatermarkSelectBinding.inflate(inflater, container, false)

//        Toast.makeText(context, "watermark select fragment", Toast.LENGTH_SHORT).show()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnQr.setOnClickListener {
            setFragmentResult("wmSelection", bundleOf("selection" to 1))
            showDialog()
        }
        binding.btnImg.setOnClickListener {
            setFragmentResult("wmSelection", bundleOf("selection" to 2))
            replaceFragment(EmbedImageSelectFragment())
        }
    }

    override fun onStart() {
        super.onStart()

        // 이미지 수신
        @Suppress("DEPRECATION")
        setFragmentResultListener(REQUEST_KEY) { key, bundle ->
            val filePath = bundle.getString("selected_img_path")
            if (filePath != null) { // null이 아닐 때만 사용
                // 파일에서 이미지 불러오기
                val bitmap = BitmapFactory.decodeFile(filePath)
                binding.ivSelected.setImageBitmap(bitmap) // 이미지 iv에 배치
            }
        }
    }

    /**
     * fragment replace
     */
    fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * show dialog
     */
    private fun showDialog() {
        val dialogFragment = EmbedDialogFragment()
        dialogFragment.show(parentFragmentManager, "embedDialog")
    }




}