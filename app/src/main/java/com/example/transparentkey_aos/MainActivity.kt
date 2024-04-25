package com.example.transparentkey_aos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        //MainFragment로 시작
        replaceFragment(MainFragment())

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

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentCotainer, fragment)
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