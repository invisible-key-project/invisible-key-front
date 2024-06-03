package com.example.transparentkey_aos

import android.graphics.BitmapFactory
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.example.transparentkey_aos.databinding.FragmentEmbedGenerateQrBinding
import com.example.transparentkey_aos.retrofit2.ApiQRCode
import com.example.transparentkey_aos.retrofit2.QRModel

import com.example.transparentkey_aos.retrofit2.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EmbedGenerateQRFragment : Fragment() {
    lateinit var binding: FragmentEmbedGenerateQrBinding
    var uid: Int = 0
    var date: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // dialog로부터 QR에 넣을 정보를 받는다.
        setFragmentResultListener("qrData") { requestKey, bundle ->
            // string -> int, long로 형변환
            uid = bundle.getInt("id", 0)
            date = bundle.getLong("date", 0)

            // 데이터를 받은 후 네트워크 요청 시작
            loadData()
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

    }

    /**
     * 네트워크 요청을 시작한다.
     */
    private fun loadData() {
        RetrofitClient.instance.sendQRData(QRModel(uid, date)).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
//                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                    // 이미지 데이터로부터 Bitmap 생성
                    val imageStream = response.body()?.byteStream()
                    val bitmap = BitmapFactory.decodeStream(imageStream)
                    imageStream?.close()
                    activity?.runOnUiThread {
                        // 이미지 embed fragment로 전달
                        setFragmentResult("qr_img", bundleOf("qr_img" to bitmap))
                        replaceFragment(EmbedFragment())
                    }
                } else {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API_CALL", "Network call failed: ${t.message}")
                Toast.makeText(requireContext(), "Network call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
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