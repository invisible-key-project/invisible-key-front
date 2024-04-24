package com.example.transparentkey_aos

import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.transparentkey_aos.databinding.FragmentEmbedGenerateQrBinding
import com.example.transparentkey_aos.retrofit2.QRModel
import com.example.transparentkey_aos.retrofit2.ResponseQRModel
import com.example.transparentkey_aos.retrofit2.RetrofitClient
import retrofit2.Call
import retrofit2.Response

class EmbedGenerateQRFragment : Fragment() {
    lateinit var binding: FragmentEmbedGenerateQrBinding
    var uid: Int = 0
    var date: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // dialog로부터 QR에 넣을 정보를 받는다.
        setFragmentResultListener("qrData") { requestKey, bundle ->
            // string -> int로 형변환
            uid = bundle.getString("id", "idVal").toInt()
            val strDate = bundle.getString("date", "dateVal")
            val formattedDate = strDate.replace(".", "")
            date = formattedDate.toInt()
//            Toast.makeText(context, "id: $id    date: $date", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedGenerateQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    /**
     * 네트워크 요청을 시작한다.
     */
    private fun loadData() {
        RetrofitClient.instance.sendQRData(QRModel(uid, date))
            .enqueue(object : retrofit2.Callback<ResponseQRModel> {
                override fun onResponse(
                    call: Call<ResponseQRModel>,
                    response: Response<ResponseQRModel>
                ) {
                    if (response.isSuccessful) {
                        // 서버 응답 성공 처리
                        Toast.makeText(context, "Success: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    } else {
                        // 서버 응답 에러 처리
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseQRModel>, t: Throwable) {
                    // 통신 실패 처리
                    Toast.makeText(context, "Failure: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.d("tag", "Failure: ${t.message}")
                }
            })
    }
}