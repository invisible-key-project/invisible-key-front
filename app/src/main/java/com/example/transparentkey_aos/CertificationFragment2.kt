package com.example.transparentkey_aos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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

        // ViewModel 인스턴스 가져오기
        val viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        viewModel.serverResponse.observe(viewLifecycleOwner, Observer { serverResponse ->
            if (serverResponse.userId.isNullOrEmpty() || serverResponse.date.isNullOrEmpty()) {
                // 데이터가 비어 있으면 에러 메시지 표시
                binding.cret2ErrorTv.visibility = View.VISIBLE
                binding.cert1Gridlayout.visibility = View.GONE
            } else {
                // 데이터가 있으면 정상적으로 GridLayout 표시
                binding.cret2ErrorTv.visibility = View.GONE
                binding.cert1Gridlayout.visibility = View.VISIBLE

                // 데이터가 있을 경우 설정
                binding.cert2IdTv.text = serverResponse.userId
                val formattedDate = simpleFormatDateString(serverResponse.date)
                binding.cert2DateTv.text = formattedDate
            }
        })

        // arguments에서 에러 상태 확인
        arguments?.let {
            val isError = it.getBoolean("isError", false)
            val errorMessage = it.getString("errorMessage", "")

            if (isError) {
                binding.cret2ErrorTv.text = errorMessage
                binding.cret2ErrorTv.visibility = View.VISIBLE
                binding.cert1Gridlayout.visibility = View.GONE
            } else {
                binding.cret2ErrorTv.visibility = View.GONE
                binding.cert1Gridlayout.visibility = View.VISIBLE
            }
        }

        binding.cert2EndBtn.setOnClickListener {
            CertificationFragment1.ImageStorage.clear()
            returnToMainFragment()
        }

        return binding.root
    }

    fun simpleFormatDateString(date: String): String {
        if (date.isNullOrEmpty()) {
            // 기본 메시지를 반환하거나 예외를 처리
            return "날짜 정보 없음"
        }
        // 날짜 형식: YYMMDD
        val year = date.substring(0..1)
        val month = date.substring(2..3)
        val day = date.substring(4..5)
        val hour = date.substring(6 .. 7)
        val minute = date.substring(8 .. 9)

        return "${year}년 ${month}월 ${day}일 ${hour}:${minute}"

    }

    fun returnToMainFragment() {
        (context as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MainFragment())
            .commitAllowingStateLoss()
    }

}