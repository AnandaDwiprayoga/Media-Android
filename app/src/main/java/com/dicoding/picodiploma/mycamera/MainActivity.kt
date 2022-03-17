package com.dicoding.picodiploma.mycamera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dicoding.picodiploma.mycamera.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentPhotoPath: String

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == CAMERA_X_RESULT){
            val myFile = it.data?.getSerializableExtra(KEY_IMAGE_PICTURE) as File
            val isBackCamera = it.data?.getBooleanExtra(KEY_IS_BACK_CAMERA, true) as Boolean

            val result = BitmapFactory.decodeFile(myFile.path)
            binding.previewImageView.setImageBitmap(result.rotateBitmap(isBackCamera))
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == RESULT_OK){
            val myFile = File(currentPhotoPath)
            val result = BitmapFactory.decodeFile(myFile.path)
            binding.previewImageView.setImageBitmap(result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(!allPermissionGranted()){
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSION
            )
        }

        binding.cameraXButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            launcherIntentCameraX.launch(intent)
        }
        binding.cameraButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.resolveActivity(packageManager)

            createTempFile(application).also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this@MainActivity,
                    "com.dicoding.picodiploma.mycamera",
                    it
                )

                currentPhotoPath = it.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                launcherIntentCamera.launch(intent)
            }
        }
        binding.galleryButton.setOnClickListener {
            Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show()
        }
        binding.uploadButton.setOnClickListener {
            Toast.makeText(this, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSION){
            if(!allPermissionGranted()){
                Toast.makeText(
                    this,
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()

            }
        }
    }

    fun Bitmap.rotateBitmap(isBackCamera: Boolean = false): Bitmap {
        val matrix = Matrix()
        return if(!isBackCamera){
//            matrix.postRotate(-90f)
            //to flip horizontally
            matrix.postScale(-1f,1f, this.width / 2f, this.height / 2f)
            Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
        } else this
    }

    private fun allPermissionGranted(): Boolean  = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val CAMERA_X_RESULT = 200

        const val KEY_IMAGE_PICTURE = "PICTURE"
        const val KEY_IS_BACK_CAMERA = "KEY_IS_BACK_CAMERA"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSION = 10
    }

}