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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmbedDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentEmbedDialogBinding
    private var id: Int = 0
    private lateinit var name: String
    private var date: Long = 0

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

        id = 12345678
        name = "홍길동"

        val stringDate = getCurrentDateFormatted() // get current time
        val formattedDate = formatDateStringUsingChunk(stringDate) // formatting date
        date = stringDate.toLong() // 포맷하지 않은 날짜정보를 Long으로 변환


        binding.tvDlIdRes.text = id.toString()
        binding.tvDlNameRes.text = name
        binding.tvDlDateRes.text = formattedDate

        binding.btnDlConfirm.setOnClickListener {
            setFragmentResult("qrData", bundleOf("id" to id, "date" to date))
            dismiss()
            replaceFragment(EmbedGenerateQRFragment()) // QR 삽입 선택 시 실행
            // EmbedGenateQrFragment로 QR에 담을 정보를 넘겨준다.
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

    /**
     * get Current Date
     */

    fun getCurrentDateFormatted(): String {
        val dateFormat = SimpleDateFormat("yyMMddHHmm", Locale.getDefault())
        val currentDate = Date() // 현재 날짜와 시간을 가져옴
        return dateFormat.format(currentDate) // 'yyMMdd' 형식으로 날짜를 포맷
    }

    /**
     * chunk date string
     */
    fun formatDateStringUsingChunk(dateStr: String): String {
        // 날짜와 시간 부분을 분리
        val datePart = dateStr.substring(0, 6) // "230603"
        val timePart = dateStr.substring(6) // "1230"

        // 날짜 부분을 포맷
        val formattedDatePart = datePart.chunked(2).joinToString(separator = ".")

        // 시간 부분을 포맷
        val formattedTimePart = timePart.chunked(2).joinToString(separator = ":")

        // 날짜와 시간 부분을 결합하여 반환
        return "$formattedDatePart. $formattedTimePart"
    }

}