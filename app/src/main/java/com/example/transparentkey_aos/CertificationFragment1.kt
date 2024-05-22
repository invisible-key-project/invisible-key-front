package com.example.transparentkey_aos

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.transparentkey_aos.databinding.FragmentCertification1Binding
import com.example.transparentkey_aosdata.ServerResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.InputStream


class CertificationFragment1 : Fragment() {
    private lateinit var binding: FragmentCertification1Binding

    // 결과를 처리할 ActivityResultLauncher 초기화
    private val imageRequestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 선택된 이미지의 URI를 가져와 ImageView에 설정
            val imageUri = result.data?.data
            imageUri?.let {
                binding.cert1Img1Iv.setImageURI(it)
                binding.cert1Img1Iv.visibility = View.VISIBLE
                val imagePart = createImagePart(it, requireContext())
                sendImageToServer(imagePart)
            }
        }
    }

    val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.45.122:8000/") // 서버 URL
        .addConverterFactory(GsonConverterFactory.create()) // JSON 변환기
        .build()

    val imageApi = retrofit.create(ImageApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCertification1Binding.inflate(inflater, container, false)


        binding.cert1GalleryBtn.setOnClickListener {
            // 권한 체크 및 요청
            if (hasPermissions()) {
                openGallery()
            } else {
                requestPermissions()
            }
        }
        binding.cert1ExtractBtn.setOnClickListener {
            (context as MainActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CertificationFragment2())
                .commitAllowingStateLoss()
        }

        binding.certification1Back.setOnClickListener {
            // MainFragment로 돌아가기
            returnToMainFragment()
        }
        return binding.root
    }

    fun returnToMainFragment() {
        (context as MainActivity).supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MainFragment())
            .commitAllowingStateLoss()
    }

    private fun hasPermissions(): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), 200)
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            // 권한이 거부되었을 때의 처리. 여기서는 간단하게 로그만 출력
            // 실제 앱에서는 사용자에게 권한이 필요한 이유를 설명하는 대화 상자를 표시하는 것이 좋습니다.
            println("Permission denied by user")
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imageRequestLauncher.launch(galleryIntent)
    }

    fun createImagePart(imageUri: Uri, context: Context): MultipartBody.Part {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val byteArray = inputStream.use { it?.readBytes() } // use 블록 안에서 자동으로 스트림을 닫아줍니다.
        val requestBody = RequestBody.create(MediaType.parse("image/jpeg"), byteArray)

        return MultipartBody.Part.createFormData("imageFile", "watermarked_image.png", requestBody)
    }


    object ImageStorage {
        var currentImage: Bitmap? = null

        fun clear() {
            currentImage = null
        }
    }

    fun sendImageToServer(imagePart: MultipartBody.Part) {
        val call = imageApi.sendImage(imagePart)
        call.enqueue(object : Callback<ServerResponse> {
            override fun onResponse(call: Call<ServerResponse>, response: Response<ServerResponse>) {
                if (response.isSuccessful) {
                    // 서버로부터의 응답을 받음
                    val serverResponse = response.body()
                    // ViewModel 인스턴스 가져오기
                    val viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
                    // ViewModel에 ServerResponse 저장
                    viewModel.serverResponse.value = serverResponse

                    val base64EncodedImage = serverResponse?.watermark ?: ""

                    // Base64 문자열을 바이트 배열로 디코딩
                    val decodedBytes = Base64.decode(base64EncodedImage, Base64.DEFAULT)

                    // 바이트 배열을 Bitmap으로 변환
                    val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    // 이미지 저장소에 Bitmap 저장
                    ImageStorage.currentImage = decodedBitmap

                    // 메인 스레드에서 UI 업데이트
                    activity?.runOnUiThread {
                        // 이미지 저장소에 Bitmap 저장
                        ImageStorage.currentImage = decodedBitmap

                        binding.cert1ExtractBtn.isEnabled=true
                        // 에러 메시지 숨기기
                        binding.cert1ErrorTv.visibility = View.GONE
                    }
                    Log.d("Upload", "Upload successful")
                } else {
                    // 서버 에러 처리
                    binding.cert1ErrorTv.visibility = View.VISIBLE
                    binding.cert1ExtractBtn.isEnabled=false
                    Log.e("Upload", "Upload failed")
                }
            }

            override fun onFailure(call: Call<ServerResponse>, t: Throwable) {
                // 네트워크 에러 처리
                activity?.runOnUiThread {
                    // 에러 메시지 보이기
                    binding.cert1ErrorTv.visibility = View.VISIBLE
                    // 에러 텍스트 설정 (선택적)
                    binding.cert1ErrorTv.text = "Network error"

                    binding.cert1ExtractBtn.isEnabled=false

                }
                    Log.e("Upload", "Upload error: ${t.message}")
            }
        })
    }

}