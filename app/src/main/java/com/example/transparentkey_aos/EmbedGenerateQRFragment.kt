package com.example.transparentkey_aos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.transparentkey_aos.databinding.FragmentEmbedGenerateQrBinding

class EmbedGenerateQRFragment : Fragment() {
    lateinit var binding: FragmentEmbedGenerateQrBinding
    lateinit var id: String
    lateinit var date: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("qrData") { requestKey, bundle ->
            id = bundle.getString("id", "idVal")
            date = bundle.getString("date", "dateVal")
//            Toast.makeText(context, "id: $id    date: $date", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmbedGenerateQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}