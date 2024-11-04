package com.example.transparentkey_aos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.example.transparentkey_aos.databinding.FragmentEmbedSelectBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class EmbedSelectFragment : Fragment() {
    lateinit var binding: FragmentEmbedSelectBinding
    val REQUEST_IMAGE_CAPTURE = 1 // 카메라 사진 촬영 요청 코드
    private lateinit var photoFile: File // 파일 생성 시 사용
    private val REQUEST_KEY = "selected_img_path" // 요청 키
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCam.setOnClickListener {
            // 카메라 실행
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(requireContext(), "com.example.transparentkey_aos.fileprovider", photoFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
        binding.btnPhotos.setOnClickListener {
            openGallery()
        }
    }

    /**
     * 갤러리 실행
     */
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imageRequestLauncher.launch(galleryIntent)
    }

    /**
     * 사진 촬영 완료 시 실행
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            lifecycleScope.launch {
                // 버튼 비활성화 및 텍스트뷰 업데이트
                withContext(Dispatchers.Main) {
                    binding.btnCam.visibility = View.INVISIBLE
                    binding.btnPhotos.visibility = View.INVISIBLE
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                }

                val filePath = Uri.fromFile(photoFile)

                // 프래그먼트 이동 및 데이터 전달
                withContext(Dispatchers.Main) {
                    replaceFragment(EmbedWatermarkSelectFragment(), filePath.toString()) // 사진의 경로 전송

                    // 로딩 애니메이션 중지
                    binding.progressBar.visibility = View.INVISIBLE
                }

                scheduleFileDeletion(photoFile) // 파일 삭제 작업 예약
            }
        }
    }

    /**
     * 갤러리 실행, imgPath에 파일 경로 저장
     */
    fun setGallery(uri: Uri?) {
        uri?.let {
            lifecycleScope.launch {
                try {
                    // 버튼 비활성화 및 텍스트뷰 업데이트
                    withContext(Dispatchers.Main) {
                        binding.btnCam.visibility = View.INVISIBLE
                        binding.btnPhotos.visibility = View.INVISIBLE
                        binding.tvStatus.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    val imgPath = it.toString()
                    Log.d("fraglog", "setGallery: imgPath = $imgPath")

                    // 다음 프래그먼트로 전환
                    withContext(Dispatchers.Main) {
                        replaceFragment(EmbedWatermarkSelectFragment(), imgPath)

                        // 로딩 애니메이션 중지
                        binding.progressBar.visibility = View.INVISIBLE
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
     * replace fragment
     */
    fun replaceFragment(fragment: Fragment, imgPath: String?) {
        // 데이터 전송
        setFragmentResult(REQUEST_KEY, bundleOf("selected_img_path" to imgPath))
        setFragmentResult("selected_embed_img_path", bundleOf("selected_embed_img_path" to imgPath))

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Create Image File at Internal Storage
     */
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA).format(Date())
        val storageDir: File = requireContext().filesDir
        return File.createTempFile(
            "PNG_${timeStamp}_",
            ".png",
            storageDir
        ).apply {
            // 파일 생성 시간을 저장
            setLastModified(System.currentTimeMillis())
        }
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
     * Save Bitmap To File
     */
    fun saveBitmapToFile(bitmap: Bitmap, fileName: String, context: Context): String {
        val storageDir: File = context.filesDir
        val imageFile = File(storageDir, fileName)
        try {
            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return imageFile.absolutePath
    }


    /**
     * Copy gallery img
     */
    private fun copyUriToInternalStorage(uri: Uri, context: Context): String? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = uri.lastPathSegment?.split("/")?.last() ?: "temp_img"
        val file = File(context.filesDir, fileName)

        if (inputStream == null) {
            Log.e("fraglog", "Failed to open input stream for URI: $uri")
            return null
        }

        try {
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            Log.e("fraglog", "Error copying file: ${e.message}")
            e.printStackTrace()
            return null
        }

        // Check if the file was successfully copied
        if (!file.exists()) {
            Log.e("fraglog", "File was not successfully copied")
            return null
        }

        // Decode the copied file to a bitmap
        val tempBitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (tempBitmap == null) {
            Log.e("fraglog", "decodeFile(${file.absolutePath}) returned null")
            return null
        }

        // Rotate the image if required
        val rotatedBitmap = rotateImageIfRequired(tempBitmap, file.absolutePath)

        // Save rotated bitmap to internal storage
        try {
            FileOutputStream(file).use { fos ->
                rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
        } catch (e: IOException) {
            Log.e("fraglog", "Error saving rotated bitmap to file: ${e.message}")
            e.printStackTrace()
            return null
        }

        return file.absolutePath
    }





}
