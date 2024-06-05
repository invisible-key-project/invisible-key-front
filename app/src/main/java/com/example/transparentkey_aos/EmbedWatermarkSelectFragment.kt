package com.example.transparentkey_aos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.transparentkey_aos.databinding.FragmentEmbedWatermarkSelectBinding
import com.example.transparentkey_aos.databinding.FragmentEmbedSelectBinding
import java.io.File
import java.io.IOException

class EmbedWatermarkSelectFragment : Fragment() {
    lateinit var binding: FragmentEmbedWatermarkSelectBinding
    private val REQUEST_KEY = "selected_img_path" // 데이터 요청 키

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedWatermarkSelectBinding.inflate(inflater, container, false)

        // 이미지 경로 수신
        parentFragmentManager.setFragmentResultListener(REQUEST_KEY, this) { _, bundle ->
            val imgPath = bundle.getString("selected_img_path")
            Log.d("fraglog", "embedwmSelected-onCreateView: imgPath = $imgPath")

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
                            Log.d("fraglog", "wmselectfrag---glide load success : $imgUri")
                            // 비트맵 로드 성공 시 회전 처리
                            val rotatedBitmap = rotateImageIfRequired(resource, imgPath)
                            binding.ivSelected.setImageBitmap(rotatedBitmap)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // 필요 시 플레이스홀더 처리
                        }

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            Log.e("fraglog", "Glide failed to load image for URI: $imgUri")
                        }
                    })
            }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnQr.setOnClickListener {
            setFragmentResult("wmSelection", bundleOf("selection" to 1))
            showDialog()
        }
        binding.btnImg.setOnClickListener {
            setFragmentResult("wmSelection", bundleOf("selection" to 2))
            replaceFragment(EmbedImageSelectFragment())
        }
    }

    /**
     * fragment replace
     */
    fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment).commit()
    }

    /**
     * show dialog
     */
    private fun showDialog() {
        val dialogFragment = EmbedDialogFragment()
        dialogFragment.show(parentFragmentManager, "embedDialog")
    }

    /**
     * if Required, call Rotate Image
     */
    fun rotateImageIfRequired(img: Bitmap, imagePath: String): Bitmap {
        val ei: ExifInterface = try {
            ExifInterface(imagePath)
        } catch (e: IOException) {
            e.printStackTrace()
            return img
        }

        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
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


}