package com.example.transparentkey_aos

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import com.example.transparentkey_aos.databinding.FragmentEmbedWatermarkSelectBinding
import com.example.transparentkey_aos.databinding.FragmentEmbedSelectBinding

class EmbedWatermarkSelectFragment : Fragment() {
    lateinit var binding : FragmentEmbedWatermarkSelectBinding
    lateinit var selectedImg : Bitmap
    private val REQUEST_KEY = "request_key" // api요청 키

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedWatermarkSelectBinding.inflate(inflater, container, false)

//        binding.ivSelected.setColorFilter(Color.parseColor("#BDBDBD"), PorterDuff.Mode.MULTIPLY)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnQr.setOnClickListener {
            showDialog()
        }
        binding.btnImg.setOnClickListener {
            showDialog()
        }
    }

    override fun onStart() {
        super.onStart()

        // 이미지 수신
        setFragmentResultListener(REQUEST_KEY) { key, bundle ->
            val img:Bitmap? = bundle.getParcelable("selected_img")
            if(img != null) { // null이 아닐 때만 사용
                selectedImg = img
                binding.ivSelected.setImageBitmap(selectedImg) // 이미지 iv에 배치
            }
        }
    }

    /**
     * fragment replace
     */
    fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentCotainer, fragment)
            .commit()
    }

    /**
     * show dialog
     */
    private fun showDialog(){
        val dialogFragment = EmbedDialogFragment()
        dialogFragment.show(parentFragmentManager, "embedDialog")
    }


}