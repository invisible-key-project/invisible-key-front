package com.example.transparentkey_aos


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.example.transparentkey_aos.databinding.FragmentEmbedSelectBinding

class EmbedSelectFragment : Fragment() {
    lateinit var binding: FragmentEmbedSelectBinding
    val REQUEST_IMAGE_CAPTURE = 1 // 카메라 사진 촬영 요청 코드
    private lateinit var selectedImg: Bitmap
    private val REQUEST_KEY = "selected_img" // 요청 키
    lateinit var curPhotoPath: String // 문자열 형태 사진 경로 값
    private var launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { it ->
        setGallery(uri = it)
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
//        Toast.makeText(context, "select fragment", Toast.LENGTH_SHORT).show()
        binding.btnCam.setOnClickListener {
            // 카메라 실행
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)//.also {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
        binding.btnPhotos.setOnClickListener {
            launcher.launch("image/*")
        }
    }

    /**
     * 사진 촬영 완료 시 실행
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            binding.ivCam.setImageBitmap(imageBitmap)
            selectedImg = imageBitmap
            replaceFragment(EmbedWatermarkSelectFragment(), selectedImg)

        }
    }

    /**
     * 갤러리 실행, selectedWatermark에 bitmap으로 저장
     */
    fun setGallery(uri: Uri?) {
        uri?.let {
            try {
                val inputStream = requireActivity().contentResolver.openInputStream(uri)
                selectedImg = BitmapFactory.decodeStream(inputStream)

                // 다음 프래그먼트로 전환
                replaceFragment(EmbedWatermarkSelectFragment(), selectedImg)
            } catch (e: Exception) {
                Log.e("EmbedImageSelectFragment", "Image selection failed", e)
            }
        }
    }

    /**
     * replace fragment
     */
    fun replaceFragment(fragment: Fragment, img: Bitmap) {
        // 데이터 전송
        setFragmentResult(REQUEST_KEY, bundleOf("selected_img" to img))
        setFragmentResult("selected_embed_img", bundleOf("selected_embed_img" to img)) //embed fragment로 넘겨줄 리스너

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentCotainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}