package com.example.transparentkey_aos

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.example.transparentkey_aos.databinding.FragmentEmbedDialogBinding
import com.example.transparentkey_aos.databinding.FragmentEmbedDialogImgBinding
import com.example.transparentkey_aos.databinding.FragmentEmbedSelectBinding
import java.util.Date

class EmbedDialogImgFragment : DialogFragment() {
    private lateinit var binding: FragmentEmbedDialogImgBinding
    private lateinit var wm_img: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
        // 이미지 수신
        @Suppress("DEPRECATION")
        setFragmentResultListener("wm_img") { key, bundle ->
            val img: Bitmap? = bundle.getParcelable("wm_img")
            if (img != null) { // null이 아닐 때만 사용
                wm_img = img
                binding.ivDialog.setImageBitmap(wm_img) // 이미지 iv에 배치
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedDialogImgBinding.inflate(inflater, container, false)
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

        params?.dimAmount = 0.9f // 배경 어두움 정도 설정
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