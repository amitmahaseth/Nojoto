package com.example.nojoto.utils

import android.app.Dialog
import android.content.Context
import com.example.nojoto.R
import com.example.nojoto.pojo.ErrorResponse
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import okhttp3.ResponseBody
import java.io.*
import java.util.*

object Extensions {
    private var dialog: Dialog? = null

    fun errorMessage(error: ResponseBody): String {
        try {

            val gson = GsonBuilder().create()
            val mError: ErrorResponse
            try {
                mError = gson.fromJson(
                    error.string(),
                    ErrorResponse::class.java
                )
                return mError.message
            } catch (e: IOException) {
                // handle failure to read error
                return Constant.SOMETHING_WENT_WRONG
            }

        } catch (e: Exception) {
            return Constant.SOMETHING_WENT_WRONG
        }
    }


    fun showProgess(context: Context) {
//        if (dialog == null) {
        dialog = Dialog(context)
        dialog!!.setContentView(R.layout.dialog_progress)
        dialog!!.setCancelable(false)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog!!.show()

    }

    fun stopProgress() {
        if (dialog != null)
            dialog!!.cancel()

    }

}
