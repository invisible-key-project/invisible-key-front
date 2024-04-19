package com.example.transparentkey_aos

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.transparentkey_aos.databinding.ActivityEmbedSelectBinding


class EmbedSelectActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1 // 카메라 사진 촬영 요청 코드
    lateinit var curPhotoPath: String // 문자열 형태 사진 경로 값
    lateinit var binding: ActivityEmbedSelectBinding
    private var launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { it ->
        setGallery(uri = it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Toast.makeText(this, "select activity", Toast.LENGTH_SHORT).show()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmbedSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            // 이전에 선택되었던 아이템의 아이콘 색상 원래 색상으로 되돌리기
            resetMenuItemColors()

            // 선택된 아이콘의 색상 변경
            menuItem.icon?.setTint(ContextCompat.getColor(this, R.color.black))

            when (menuItem.itemId) {
                R.id.tap_home -> {
                    replaceFragment(MainFragment())
                }
                R.id.tap_manage -> {
                    replaceFragment(ManageFragment())
                }
                R.id.tap_settings -> {
                    replaceFragment(SettingsFragment())
                }
            }
            true
        }

        binding.btnCam.setOnClickListener {
            // 카메라 실행
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)//.also {
            /*imageCaptureIntent -> imageCaptureIntent.resolveActivity(packageManager)?.also {
                activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())  { result ->
                    print(result.resultCode)
                    if (result.resultCode == 1) {

                    }
                }
                }*/
            //}
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            binding.ivCam.setImageBitmap(imageBitmap)
            replaceFragment(EmbedWatermarkSelectFragment())

        }
    }

    /**
     * 갤러리 실행
     */
    fun setGallery(uri: Uri?) {
        binding.ivCam.setImageURI(uri)
        replaceFragment(EmbedWatermarkSelectFragment())
    }

    /**
     * replace fragment
     */
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.cam_fragment_container, fragment)
            .commit()
    }

    /**
     * menu item color reset
     */
    private fun resetMenuItemColors() {
        val menu = binding.bottomNavigationView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            menuItem.icon?.setTint(ContextCompat.getColor(this, R.color.color_gray_light))
        }
    }

}