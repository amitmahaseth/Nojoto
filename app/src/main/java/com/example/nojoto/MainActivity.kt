package com.example.nojoto

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.nojoto.adapter.UserAdapter
import com.example.nojoto.databinding.ActivityMainBinding
import com.example.nojoto.uploadPhoto.UploadPhotoActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userAdapter: UserAdapter

    private var PHOTO_CODE = 135
    private lateinit var currentPhotoPath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**set adapter**/
        userAdapter = UserAdapter(this)
        binding.rvUserData.adapter = userAdapter

        init()
    }
    private fun init(){
        binding.cameraItem.Camera.setOnClickListener {
            checkAllPermission()
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PHOTO_CODE && resultCode == RESULT_OK) {
           /* BitmapFactory.decodeFile(currentPhotoPath)?.also { bitmap ->

            }*/
            val intent = Intent(this, UploadPhotoActivity::class.java)
            intent.putExtra("Photo", currentPhotoPath)
            startActivity(intent)

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