package com.example.transparentkey_aos

import android.content.res.ColorStateList
import android.graphics.Color
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.transparentkey_aos.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val mainFragment = MainFragment()
    private val manageFragment = ManageFragment()
    private val settingsFragment = SettingsFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 권한 체크
        checkPermission()

        //MainFragment로 시작
        replaceFragment(MainFragment())

        val nav = binding.bottomNavigationView
        val ripple = ColorStateList.valueOf(Color.TRANSPARENT)
        nav.itemRippleColor = ripple

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            // 이전에 선택되었던 아이템의 아이콘 색상 원래 색상으로 되돌리기
            resetMenuItemColors()

            // 선택된 아이콘의 색상 변경
            menuItem.icon?.setTint(ContextCompat.getColor(this, R.color.black))

            when (menuItem.itemId) {
                R.id.tap_home -> {
                    replaceFragment(mainFragment)
                }
                R.id.tap_manage -> {
                    replaceFragment(manageFragment)
                }
                R.id.tap_settings -> {
                    replaceFragment(settingsFragment)
                }
            }
            true
        }

    }
    /**
     * 권한 체크
     */
    fun checkPermission() {
//        Toast.makeText(this, "stark check permission", Toast.LENGTH_SHORT).show()
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (cameraPermission == PackageManager.PERMISSION_GRANTED){
//            Toast.makeText(this, "이미 권한이 승인됨", Toast.LENGTH_SHORT).show()
        } else {
            requestPermission()
        }
    }

    /**
     * 실제 권한 요청
     */
    fun requestPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE),
            99)
    }

    /**
     * 권한 요청 처리
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            99 -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED
                ) {
                    // 권한 승인됨
//                    Toast.makeText(this, "권한 승인", Toast.LENGTH_SHORT).show()
                    return
                } else {
                    Toast.makeText(this, "권한을 승인하지 않으면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * menu item color reset
     */
    private fun resetMenuItemColors() {
        val menu = binding.bottomNavigationView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            menuItem.icon?.setTint(ContextCompat.getColor(this, R.color.selected_icon_color))
        }
    }
}