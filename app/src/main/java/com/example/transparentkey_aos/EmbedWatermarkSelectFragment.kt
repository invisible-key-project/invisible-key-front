package com.example.transparentkey_aos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
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
import java.io.File

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
        // 이미지 수신
        // 이미지 경로를 가져와서 이미지뷰에 설정
        parentFragmentManager.setFragmentResultListener(REQUEST_KEY, this) { _, bundle ->
            val imgPath = bundle.getString("selected_img_path")
            Log.d("fraglog", "onCreateView: imgPath = $imgPath")
            imgPath?.let {
                val file = File(it)
                if (file.exists()) {
                    Log.d("fraglog", "File exists: $it")
                    val bitmap = BitmapFactory.decodeFile(it)
                    if (bitmap != null) {
                        binding.ivSelected.setImageBitmap(bitmap)
                    } else {
                        Log.e("fraglog", "BitmapFactory.decodeFile returned null for path: $it")
                    }
                } else {
                    Log.e("fraglog", "File does not exist: $it")
                }
            }
        }

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