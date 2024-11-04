package com.example.transparentkey_aos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.transparentkey_aos.databinding.FragmentEmbedImageSelectBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class EmbedImageSelectFragment : Fragment() {
    lateinit var binding: FragmentEmbedImageSelectBinding
    private val REQUEST_KEY = "wm_img_path" // api요청 키
    private lateinit var selectedWatermark: Bitmap
    private val imageRequestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                setGallery(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedImageSelectBinding.inflate(inflater, container, false)
//        Toast.makeText(context, "Image select fragment", Toast.LENGTH_SHORT).show()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        openGallery()
    }

    /**
     * 갤러리 실행
     */
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imageRequestLauncher.launch(galleryIntent)
    }

    /**
     * 갤러리 실행, selectedWatermark에 bitmap으로 저장
     */
    fun setGallery(uri: Uri?) {
        uri?.let {
            // 복사한 코드
            lifecycleScope.launch {
                try {
                    // 버튼 비활성화 및 텍스트뷰 업데이트
                    withContext(Dispatchers.Main) {
                        binding.tvStatus.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    val imgPath = it.toString()
                    Log.d("fraglog", "setGallery: imgPath = $imgPath")

                    // 다음 프래그먼트로 전환
                    withContext(Dispatchers.Main) {
                        // 다음 다이얼로그로 전환
                        setFragmentResult(REQUEST_KEY, bundleOf("wm_img_path" to imgPath))
                        setFragmentResult("wmimg_embed", bundleOf("wmimg_embed" to imgPath))
                        showImgDialog()

                        // 로딩 애니메이션 중지
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.tvStatus.visibility = View.INVISIBLE
                    }
                } catch (e: Exception) {
                    Log.e("fraglog", "Image selection failed", e)
                    // 로딩 애니메이션 중지
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    /**
     * if Required, call Rotate Image
     */
    fun rotateImageIfRequired(img: Bitmap, selectedImage: String): Bitmap {
        // ExifInterface를 사용해 이미지 파일의 EXIF 메타데이터를 읽어옴
        val ei: ExifInterface = try {
            ExifInterface(selectedImage)
        } catch (e: IOException) {
            e.printStackTrace()
            return img // 예외 발생 시 원본 이미지를 반환
        }

        // EXIF에서 방향 정보를 가져옴
        val orientation: Int = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        // 방향 정보에 따라 이미지를 회전시킴
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img // 회전이 필요 없는 경우 원본 이미지를 반환
        }
    }

    /**
     * Rotate Img
     */
    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        // Matrix 객체를 생성하여 회전 변환을 적용
        val matrix = Matrix()
        matrix.postRotate(degree)
        // 회전된 비트맵을 생성하여 반환
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }

    /**
     * delete temp img automatically (15min)
     */
    private fun scheduleFileDeletion(file: File) {
        val data = Data.Builder()
            .putString("file_path", file.absolutePath)
            .build()

        val deleteRequest = OneTimeWorkRequestBuilder<DeleteFileWorker>()
            .setInitialDelay(15, TimeUnit.MINUTES) // 15분 후 삭제
            .setInputData(data)
            .build()

        WorkManager.getInstance(requireContext()).enqueue(deleteRequest)
    }

    /**
     * show img dialog
     */
    private fun showImgDialog() {
        val dialogFragment = EmbedDialogImgFragment()
        dialogFragment.show(parentFragmentManager, "embedImgDialog")
    }
}