package com.example.transparentkey_aos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.transparentkey_aos.databinding.FragmentEmbedDialogBinding
import com.example.transparentkey_aos.databinding.FragmentEmbedSelectBinding
import java.util.Date

class EmbedDialogFragment: DialogFragment() {
    private lateinit var binding : FragmentEmbedDialogBinding
    private lateinit var id : String
    private lateinit var name : String
    private lateinit var date : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        id = "hello world"
        name = "진실을 찾아서"
        date = "24.03.23."

        binding.tvDlIdRes.text = id
        binding.tvDlNameRes.text = name
        binding.tvDlDateRes.text = date

        binding.btnDlConfirm.setOnClickListener {
            dismiss()
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

}