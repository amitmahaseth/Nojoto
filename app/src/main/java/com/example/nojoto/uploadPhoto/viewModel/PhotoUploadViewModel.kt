package com.example.nojoto.uploadPhoto.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nojoto.network.ApiInterface
import com.example.nojoto.network.ServiceClient
import com.example.nojoto.pojo.UploadImages
import com.example.nojoto.utils.Extensions

import okhttp3.MultipartBody

class PhotoUploadViewModel : ViewModel() {
    val apiInterface = ServiceClient.apiClient().create(ApiInterface::class.java)

    val errorLiveData = MutableLiveData<String>()
    suspend fun PhotoUpload(photoData: MultipartBody.Part?): LiveData<UploadImages> {
        val mutableLiveData = MutableLiveData<UploadImages>()
        try {
            val response =
                apiInterface.updatePhoto(photoData!!)
            when {
                response.code() == 200 -> {
                    Extensions.stopProgress()
                    mutableLiveData.postValue(response.body())
                }
                else -> {
                    Extensions.stopProgress()
                    errorLiveData.value = Extensions.errorMessage(response.errorBody()!!)
                }
            }
        } catch (e: Exception) {
            Extensions.stopProgress()

        }
        return mutableLiveData
    }
}