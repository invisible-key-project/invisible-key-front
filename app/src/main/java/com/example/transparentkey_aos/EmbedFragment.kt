package com.example.transparentkey_aos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.transparentkey_aos.databinding.FragmentEmbedBinding
import com.example.transparentkey_aos.retrofit2.QRModel
import com.example.transparentkey_aos.retrofit2.RetrofitClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class EmbedFragment : Fragment() {
    lateinit var binding: FragmentEmbedBinding
    lateinit var wmImg: Bitmap
    lateinit var selected_img:Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // type 수신
        setFragmentResultListener("wmSelection") { key, bundle ->
            val selection = bundle.getInt("selection", -1)
            when (selection) {
                1 -> {
                    // qr 선택한 경우 실행
                    @Suppress("DEPRECATION")
                    setFragmentResultListener("qr_img") { key, bundle ->
                        val img: Bitmap? = bundle.getParcelable("qr_img")
                        if (img != null) { // null이 아닐 때만 사용
                            wmImg = img
                            binding.ivWmImage.setImageBitmap(wmImg) // 이미지 iv에 배치
                        } else {
                            Toast.makeText(context, "워터마크 이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // 워터마크 할 이미지 수신
                    @Suppress("DEPRECATION")
                    setFragmentResultListener("selected_embed_img") { key, bundle ->
                        val img: Bitmap? = bundle.getParcelable("selected_embed_img")
                        if (img != null) { // null이 아닐 때만 사용
                            selected_img = img
//                binding.ivWmImage.setImageBitmap(selected_img)
                            uploadImages(selected_img, wmImg)
                        }else {
                            // img가 null인 경우의 처리 로직
                            Toast.makeText(context, "배경 이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }

                }

                2 -> {
                    // img
                    // 워터마크 이미지 수신
                    @Suppress("DEPRECATION")
                    setFragmentResultListener("wm_img2") { key, bundle ->
                        val img: Bitmap? = bundle.getParcelable("wm_img2")
                        if (img != null) { // null이 아닐 때만 사용
                            wmImg = img
                            binding.ivWmImage.setImageBitmap(wmImg) // 이미지 iv에 배치
                        } else {
                            Toast.makeText(context, "워터마크 이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // 워터마크 할 배경 이미지 수신
                    @Suppress("DEPRECATION")
                    setFragmentResultListener("selected_embed_img") { key, bundle ->
                        // 이미지 수신 안 되는 오류 : 다른 키 사용으로 해결
                        val img: Bitmap? = bundle.getParcelable("selected_embed_img")
                        if (img != null) { // null이 아닐 때만 사용
                            selected_img = img
//                            binding.ivWmImage.setImageBitmap(selected_img)
                            uploadImages(selected_img, wmImg)
                        }
                    }

                }

                else -> {
                    // 예외 처리
                    Toast.makeText(context, "버튼 선택 결과 전송 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    private fun uploadImages(background_img: Bitmap, wm_img: Bitmap?) {
        if (!::wmImg.isInitialized) {
            Toast.makeText(context, "워터마크 이미지가 설정되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val stream1 = ByteArrayOutputStream()
        background_img.compress(Bitmap.CompressFormat.JPEG, 100, stream1)
        val byteArray1 = stream1.toByteArray()

        val stream2 = ByteArrayOutputStream()
        wm_img?.compress(Bitmap.CompressFormat.PNG, 100, stream2)
        val byteArray2 = stream2.toByteArray()

        val requestFile1 = RequestBody.create(MediaType.parse("image/jpeg"), byteArray1)
        val body1 = MultipartBody.Part.createFormData("background_img", "background_img.jpg", requestFile1)

        val requestFile2 = RequestBody.create(MediaType.parse("image/png"), byteArray2)
        val body2 = MultipartBody.Part.createFormData("wm_img", "wm_img.png", requestFile2)

        RetrofitClient.instance.uploadImages(body1, body2).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                    // 이미지 업로드 성공 시 처리
                    val inputStream = response.body()?.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    activity?.runOnUiThread {
                        binding.ivWmImage.setImageBitmap(bitmap)
                        binding.tvEmbedWatermark.text = "워터마킹 완료!"
                    }
                } else {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API_CALL", "Network call failed: ${t.message}")
                Toast.makeText(context, "Network call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

}