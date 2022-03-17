package com.dicoding.picodiploma.mycamera

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var imageFile: File? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var currentPhotoPath: String

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == CAMERA_X_RESULT){
            imageFile = it.data?.getSerializableExtra(KEY_IMAGE_PICTURE) as? File
            val isBackCamera = it.data?.getBooleanExtra(KEY_IS_BACK_CAMERA, true) as Boolean

            val result = BitmapFactory.decodeFile(imageFile?.path)
            binding.previewImageView.setImageBitmap(result.rotateBitmap(isBackCamera))
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == RESULT_OK){
            imageFile = File(currentPhotoPath)
            val result = BitmapFactory.decodeFile(imageFile?.path)
            binding.previewImageView.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == RESULT_OK){
            val selectedImg: Uri = it.data?.data as Uri
            imageFile = selectedImg.toFile(this)
            binding.previewImageView.setImageURI(selectedImg)
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
            val intent = Intent()
            intent.action = ACTION_GET_CONTENT
            intent.type = "image/*"
            val chooser = Intent.createChooser(intent, "Choose a Picture")
            launcherIntentGallery.launch(chooser)
        }
        binding.uploadButton.setOnClickListener {
            uploadImage()
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

    private fun uploadImage(){
        if(imageFile != null){
            val image = imageFile!!.reduceFileImage()
            val description = "Ini adalah deskripsi gambar".toRequestBody("text/plain".toMediaType())
            val requestImageFile = image.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                image.name,
                requestImageFile
            )

            val service = ApiConfig().getApiService().uploadImage(imageMultipart, description)
            service.enqueue(object: Callback<FileUploadResponse> {
                override fun onResponse(
                    call: Call<FileUploadResponse>,
                    response: Response<FileUploadResponse>
                ) {
                    if(response.isSuccessful){
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error) {
                            Toast.makeText(this@MainActivity, responseBody.message, Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this@MainActivity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<FileUploadResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Gagal instance Retrofit", Toast.LENGTH_SHORT).show()
                }

            })
        }else{
            Toast.makeText(this@MainActivity, "Silakan masukkan berkas gambar terlebih dahulu.", Toast.LENGTH_SHORT).show()
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

    fun File.reduceFileImage(): File {
        val bitmap = BitmapFactory.decodeFile(this.path)
        var compressQuality = 100
        var streamLength: Int

        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        }while(streamLength > 1000000) //convert until size less than 1MB

        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(this))
        return this
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