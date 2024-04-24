package com.example.transparentkey_aos

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.transparentkey_aosdata.ServerResponse

class SharedViewModel : ViewModel() {
    val serverResponse = MutableLiveData<ServerResponse>()
}
