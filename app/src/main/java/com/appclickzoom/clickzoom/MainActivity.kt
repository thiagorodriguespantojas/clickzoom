package com.appclickzoom.clickzoom

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 1001
    private val REQUEST_CAMERA_PERMISSION = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkCameraPermission()) {
            scheduleCameraTask()
        } else {
            requestCameraPermission()
        }
    }

    private fun scheduleCameraTask() {
        // Configura o WorkManager para tirar foto a cada 59 minutos
        val periodicWorkRequest =
            PeriodicWorkRequestBuilder<CameraWorker>(59, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(this).enqueue(periodicWorkRequest)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CAMERA_PERMISSION
        )
    }

    private fun checkCameraPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    }

    // Tirando a foto usando Intent da Câmera
    private fun takePicture() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            saveImageToExternalStorage(photo)
        }
    }

    private fun saveImageToExternalStorage(bitmap: Bitmap) {
        val filePath = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(filePath, "photo_${System.currentTimeMillis()}.jpg")

        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(this, "Foto salva com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.e("MainActivity", "Erro ao salvar imagem: ${e.localizedMessage}")
        }
    }
}