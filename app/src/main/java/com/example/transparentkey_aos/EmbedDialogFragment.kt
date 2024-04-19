package com.example.transparentkey_aos

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
import com.example.transparentkey_aos.databinding.FragmentEmbedSelectBinding
import java.util.Date

class EmbedDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentEmbedDialogBinding
    private lateinit var id: String
    private lateinit var name: String
    private lateinit var date: String
    private var wmSelection: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true

        // 결과 수신을 위한 리스너 등록
        setFragmentResultListener("wmSelection") { key, bundle ->
            val selection = bundle.getInt("selection", -1)
            when (selection) {
                1 -> {
                    // btn1을 선택한 경우
                    wmSelection = 1
                }

                2 -> {
                    wmSelection = 2
                }

                else -> {
                    // 예외 처리
                    Toast.makeText(context, "버튼 선택 결과 전송 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

        id = "helloworld"
        name = "홍길동"
        date = "24.03.23."

        binding.tvDlIdRes.text = id
        binding.tvDlNameRes.text = name
        binding.tvDlDateRes.text = date

        binding.btnDlConfirm.setOnClickListener {
            dismiss()
            if (wmSelection == 1) {
                replaceFragment(EmbedGenerateQRFragment()) // QR 삽입 선택 시 실행
                // EmbedGenateQrFragment로 QR에 담을 정보를 넘겨준다.
                setFragmentResult("qrData", bundleOf("id" to id, "date" to date))
            } else {
                replaceFragment(EmbedImageSelectFragment()) // 이미지 삽입 선택 시 실행
            }

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
            .replace(R.id.fragmentCotainer, fragment)
            .commit()
    }

}