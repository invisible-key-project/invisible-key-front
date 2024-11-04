package com.example.transparentkey_aos

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.transparentkey_aos.databinding.FragmentEmbedBinding
import com.example.transparentkey_aos.retrofit2.QRModel
import com.example.transparentkey_aos.retrofit2.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class EmbedFragment : Fragment() {
    lateinit var binding: FragmentEmbedBinding
    lateinit var wmImg: Bitmap
    lateinit var selectedImgUri: Uri
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
        setFragmentResultListener("selected_embed_img_path") { _, bundle ->
            val imgPath = bundle.getString("selected_embed_img_path")
            val imgUri = Uri.parse(imgPath)
            Log.d("fraglog", "embedfragment-onCreateView: imgUri = $imgUri")

            if (imgPath != null) {
                selectedImgUri = imgUri
                Log.d("EmbedFragment", "selected_img_path initialized: $selectedImgUri")

                val file = File("/data/user/0/com.example.transparentkey_aos/files/selected_img.png")
                if (file.exists()) {
                    Log.d("EmbedFragment", "File exists at the specified path")
                } else {
                    Log.e("EmbedFragment", "File does not exist at the specified path")
                }
            } else {
                Toast.makeText(context, "선택한 이미지 경로를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // type 수신
        setFragmentResultListener("wmSelection") { _, bundle ->
            val selection = bundle.getInt("selection", -1)
            when (selection) {
                // qr 선택한 경우 실행
                1 -> {
                    setFragmentResultListener("qr_img") { _, bundle ->
                        val img: Bitmap? = bundle.getParcelable("qr_img")
                        if (img != null) {
                            wmImg = img
                            binding.ivWmImage.setImageBitmap(wmImg)

                            if (::selectedImgUri.isInitialized) {
                                loadImageAndUpload()
                            } else {
                                Toast.makeText(context, "이미지 경로가 초기화되지 않았습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            Toast.makeText(context, "워터마크 이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                // img
                // 워터마크 이미지 수신
                2 -> {
                    setFragmentResultListener("wmimg_embed") { _, bundle ->
                        val imgPath = bundle.getString("wmimg_embed")
                        Log.d(
                            "EmbedFragment",
                            "embedFragment(image selected)---onCreateView: imgPath = $imgPath"
                        )
                        imgPath?.let { path ->
                            // content URI로 처리
                            val imgUri = Uri.parse(imgPath)
                            Glide.with(requireContext())
                                .asBitmap()
                                .load(imgUri)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        Log.d(
                                            "EmbedFragment",
                                            "embedfragment---glide load success : $imgUri"
                                        )
                                        // 비트맵 로드 성공 시
                                        if (::selectedImgUri.isInitialized) {
                                            loadImageAndWatermark(imgUri)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "이미지 경로가 초기화되지 않았습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        // 필요 시 플레이스홀더 처리
                                    }

                                    override fun onLoadFailed(errorDrawable: Drawable?) {
                                        Log.e("EmbedFragment", "Glide failed to load image for URI: $imgUri, check if file exists or format is supported")
                                    }
                                })
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
     * Load image rotate and watermark : qr watermark
     */
    private fun loadImageAndUpload() {
        lifecycleScope.launch {
            val rotatedBitmap = withContext(Dispatchers.IO) {
                    Glide.with(requireContext()).asBitmap().load(selectedImgUri).submit().get()
            }
            uploadImages(rotatedBitmap, wmImg)
        }
    }

    private fun loadImageAndWatermark(path: Uri) {
        lifecycleScope.launch {
            val selectedImg = withContext(Dispatchers.IO) {
                Glide.with(requireContext()).asBitmap().load(selectedImgUri).submit().get()
            }
            val wmImgBitmap = withContext(Dispatchers.IO) {
                Glide.with(requireContext()).asBitmap().load(path).submit().get()
            }
            wmImg = wmImgBitmap
            binding.ivWmImage.setImageBitmap(wmImg)
            uploadImages(selectedImg, wmImg)
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

        val requestFile1 = byteArray1.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body1 =
            MultipartBody.Part.createFormData("background_img", "background_img.jpg", requestFile1)

        val requestFile2 = byteArray2.toRequestBody("image/jpeg".toMediaTypeOrNull())
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
    private fun saveImageToStorage(bitmap: Bitmap) {
        val filename = "watermarked_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)  // 파일 이름
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")  // MIME 타입
            // Oreo에서는 Scoped Storage가 적용되지 않으므로, 상대 경로 대신 절대 경로를 사용
        }

        // 절대 경로를 사용하여 파일 경로 설정
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val file = File(picturesDir, filename)

        // Content Resolver를 통해 URI 생성
        val resolver = context?.contentResolver
        val uri = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        Log.d("SaveImage", "Generated URI: $uri") // URI 디버깅 로그
        Log.d("SaveImage", "ContentValues: $contentValues") // ContentValues 로그

        if (uri != null) {
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                        Toast.makeText(context, "이미지 저장 성공: $filename", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "이미지 저장 실패", Toast.LENGTH_SHORT).show()
                    }
                } ?: Toast.makeText(context, "파일을 저장할 수 없습니다.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SaveImage", "Error saving image: ${e.message}")
                Toast.makeText(context, "파일을 저장하는 동안 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("SaveImage", "Failed to get URI. Check your ContentValues.")
            Toast.makeText(context, "이미지 저장 실패: URI가 null입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * if Required, call Rotate Image
     */
//    fun rotateImageIfRequired(img: Bitmap, imageUri: Uri): Bitmap {
//        val ei: ExifInterface = try {
//            ExifInterface(requireContext().contentResolver.openInputStream(imageUri)!!)
//        } catch (e: IOException) {
//            e.printStackTrace()
//            return img
//        }
//
//        val orientation: Int =
//            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
//        return when (orientation) {
//            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
//            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
//            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
//            else -> img
//        }
//    }

    /**
     * Rotate Img
     */

//    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
//        // Matrix 객체를 생성하여 회전 변환을 적용
//        val matrix = Matrix()
//        matrix.postRotate(degree)
//        // 회전된 비트맵을 생성하여 반환
//        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
//    }

}