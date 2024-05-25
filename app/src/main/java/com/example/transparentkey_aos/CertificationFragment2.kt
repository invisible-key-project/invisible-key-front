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

        // LiveData 관찰
        viewModel.serverResponse.observe(viewLifecycleOwner, Observer { serverResponse ->
            // serverResponse 사용
            binding.cert2IdTv.text = serverResponse.userId

            val formattedDate = simpleFormatDateString(serverResponse.date)
            println(formattedDate)
            binding.cert2DateTv.text = formattedDate
        })

        binding.cert2EndBtn.setOnClickListener {
            // ImageStorage 초기화
            CertificationFragment1.ImageStorage.clear()

            // MainFragment로 돌아가기
            returnToMainFragment()
        }


        return binding.root
    }

    fun simpleFormatDateString(date: String): String {
        // 날짜 형식: YYMMDD
        val year = date.substring(0..1)
        val month = date.substring(2..3)
        val day = date.substring(4..5)

        return "$year 년 $month 월 $day 일"
    }

    fun returnToMainFragment() {
        (context as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MainFragment())
            .commitAllowingStateLoss()
    }

}