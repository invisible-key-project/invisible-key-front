package com.example.transparentkey_aos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.transparentkey_aos.databinding.FragmentEmbedDialogImgBinding
import java.io.File

class EmbedDialogImgFragment : DialogFragment() {
    private lateinit var binding: FragmentEmbedDialogImgBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedDialogImgBinding.inflate(inflater, container, false)
        // 이미지 경로 수신, 이미지뷰에 설정
        parentFragmentManager.setFragmentResultListener("wm_img_path", this) { _, bundle ->
            val imgPath = bundle.getString("wm_img_path")
            Log.d("fraglog", "dialog---onCreateView: imgPath = $imgPath")
            imgPath?.let {
                val file = File(it)
                if (file.exists()) {
                    Log.d("fraglog", "File exists: $it")
                    val bitmap = BitmapFactory.decodeFile(it)
                    if (bitmap != null) {
                        binding.ivDialog.setImageBitmap(bitmap)
                    } else {
                        Log.e("fraglog", "BitmapFactory.decodeFile returned null for path: $it")
                    }
                } else {
                    Log.e("fraglog", "File does not exist: $it")
                }
            }
        }

        isCancelable = true

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDlConfirm.setOnClickListener {
            dismiss()
            replaceFragment(EmbedFragment())

        }
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val params = window?.attributes

        params?.dimAmount = 0.5f // 배경 어두움 정도 설정
        dialog?.setCanceledOnTouchOutside(true) // 외부 터치 시 닫히게 설정
        window?.attributes = params
        window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    /**
     * fragment replace
     */
    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

}