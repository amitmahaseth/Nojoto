package com.example.nojoto.uploadPhoto

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.example.nojoto.BuildConfig
import com.example.nojoto.databinding.ActivityUploadPhotoBinding
import com.example.nojoto.pojo.UploadImages
import com.example.nojoto.uploadPhoto.viewModel.PhotoUploadViewModel
import com.example.nojoto.utils.Constant
import com.example.nojoto.utils.Extensions
import com.example.nojoto.utils.isInternetAvailable
import com.example.nojoto.utils.makeToast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException


class UploadPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadPhotoBinding
    private lateinit var photoUploadVM: PhotoUploadViewModel

    private var PHOTO_CODE = 135
    private lateinit var currentPhotoPath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPhotoBinding.inflate(layoutInflater)
        photoUploadVM = PhotoUploadViewModel()
        setContentView(binding.root)
        val intent = intent
        val data = intent.getStringExtra("Photo")

        if (data != null) {
            apiPhotoUpload(data)
        }

        init()
    }

    private fun init() {
        binding.btnUpload.setOnClickListener {
            checkAllPermission()
        }
    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    private fun apiPhotoUpload(currentPhotoPath: String) {
        CoroutineScope(Dispatchers.Main).launch {
            if (isInternetAvailable()) {
                Extensions.showProgess(this@UploadPhotoActivity)
                val imageParts: MultipartBody.Part?
                val file = File(currentPhotoPath)
                val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                imageParts = filePart

                photoUploadVM.PhotoUpload(imageParts)
                    .observe(this@UploadPhotoActivity,
                        Observer {
                            Extensions.stopProgress()
                            var response = it as UploadImages
                            BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->

                                val ei: ExifInterface = ExifInterface(currentPhotoPath)
                                val orientation = ei.getAttributeInt(
                                    ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED
                                )

                                var rotatedBitmap: Bitmap? = null
                                when (orientation) {
                                    ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap =
                                        rotateImage(bitmap, 90F)
                                    ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap =
                                        rotateImage(bitmap, 180F)
                                    ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap =
                                        rotateImage(bitmap, 270F)
                                    ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
                                    else -> rotatedBitmap = bitmap
                                }

                                binding.imgUpload.setImageBitmap(rotatedBitmap)
                            }
                        })

                photoUploadVM.errorLiveData.observe(this@UploadPhotoActivity, Observer {
                    val errorMessage = it as String
                    Extensions.stopProgress()
                    this@UploadPhotoActivity.makeToast(errorMessage)
                })

            } else {
                Extensions.stopProgress()
                this@UploadPhotoActivity.makeToast(Constant.NETWORK_NOT_AVAILABLE)
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PHOTO_CODE && resultCode == RESULT_OK) {
            /* BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->

             }*/
            apiPhotoUpload(currentPhotoPath)

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(this.packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, PHOTO_CODE)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val imageFileName = "sample_" + System.currentTimeMillis() + "_"
        val storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        return image
    }

    private fun checkAllPermission() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            // toast("OK")
                            dispatchTakePictureIntent()
//                            binding.camera!!.onStart()
                        } else {

                            showGalleryPopup()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // Remember to invoke this method when the custom rationale is closed
                    // or just by default if you don't want to use any custom rationale.
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener {
                //   toast(it.name)
            }
            .check()
    }

    private fun showGalleryPopup() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Camera Permissions Required")
            .setMessage(
                "You have forcefully denied some of the required permissions " +
                        "for this action. Please open settings, go to permissions and allow them."
            )
            .setPositiveButton("Settings") { dialog, _ ->
                dialog.dismiss()
                val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", this.packageName, null)
                settingsIntent.data = uri
                startActivityForResult(settingsIntent, 567)
            }
            /*.setNegativeButton(
                "Cancel"
            ) { dialog, which -> }*/
            .setCancelable(false)
            .create()
            .show()
    }
}