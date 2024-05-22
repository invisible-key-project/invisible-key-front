package com.example.transparentkey_aos

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
    lateinit var selected_img_path: String
    var bitmap: Bitmap? = null


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

        // 워터마크 할 이미지 수신
        setFragmentResultListener("selected_embed_img_path") { key, bundle ->
            val filePath = bundle.getString("selected_embed_img_path")
            if (filePath != null) {
                selected_img_path = filePath
                Log.d("fraglog", "selected_img_path initialized: $selected_img_path")
            } else {
                Toast.makeText(context, "선택한 이미지 경로를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // type 수신
        setFragmentResultListener("wmSelection") { key, bundle ->
            val selection = bundle.getInt("selection", -1)
            when (selection) {
                // qr 선택한 경우 실행
                1 -> {
                    setFragmentResultListener("qr_img") { key, bundle ->
                        val img: Bitmap? = bundle.getParcelable("qr_img")
                        if (img != null) {
                            wmImg = img
                            binding.ivWmImage.setImageBitmap(wmImg)

                            if (::selected_img_path.isInitialized) {
                                val selected_img = BitmapFactory.decodeFile(selected_img_path)
                                uploadImages(selected_img, wmImg)
                            } else {
                                Toast.makeText(context, "이미지 경로가 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "워터마크 이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                // img
                // 워터마크 이미지 수신
                2 -> {
                    setFragmentResultListener("wmimg_embed") { key, bundle ->
                        val img: Bitmap? = bundle.getParcelable("wmimg_embed")
                        if (img != null) {
                            wmImg = img
                            binding.ivWmImage.setImageBitmap(wmImg)

                            if (::selected_img_path.isInitialized) {
                                val selected_img = BitmapFactory.decodeFile(selected_img_path)
                                uploadImages(selected_img, wmImg)
                            } else {
                                Toast.makeText(context, "이미지 경로가 초기화되지 않았습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "워터마크 이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                else -> {
                    Toast.makeText(context, "버튼 선택 결과 전송 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // save button click listener
        binding.btnSaveImg.setOnClickListener {
            bitmap?.let { safeBitmap ->
                saveImageToStorage(safeBitmap)
            } ?: Toast.makeText(context, "이미지가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Apply watermark
     */
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
        val body1 =
            MultipartBody.Part.createFormData("background_img", "background_img.jpg", requestFile1)

        val requestFile2 = RequestBody.create(MediaType.parse("image/png"), byteArray2)
        val body2 = MultipartBody.Part.createFormData("wm_img", "wm_img.png", requestFile2)

        RetrofitClient.instance.uploadImages(body1, body2).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
//                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                    // 이미지 업로드 성공 시 처리
                    val inputStream = response.body()?.byteStream()
                    bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    activity?.runOnUiThread {
                        // view에 배치
                        binding.ivWmImage.setImageBitmap(bitmap)
                        binding.tvEmbedWatermark.text = "워터마킹 완료!"
                        binding.btnSaveImg.visibility = View.VISIBLE
                        binding.btnSaveImg.isEnabled = true
                    }
                } else {
                    Toast.makeText(context, "Error : 워터마킹 실패", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API_CALL", "Network call failed: ${t.message}")
                Toast.makeText(context, "Network call failed: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }

        })
    }

    /**
     * Save watermarked image
     */
    fun saveImageToStorage(bitmap: Bitmap) {
        val appName = getString(R.string.app_name)  // 앱 이름 가져오기
        val filename = "watermarked_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/$appName"
            )  // 경로 설정
        }

        val resolver = context?.contentResolver
        val uri = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                Toast.makeText(context, "이미지 저장 성공: $filename", Toast.LENGTH_LONG).show()
            } ?: Toast.makeText(context, "파일을 저장할 수 없습니다.", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(context, "이미지 저장 실패", Toast.LENGTH_SHORT).show()
    }


}