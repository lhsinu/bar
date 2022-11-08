package com.inu.bar

import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.inu.bar.base.Constants
import com.inu.bar.databinding.ActivityAccidentPhotoBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class AccidentPhotoActivity : AppCompatActivity() {
    private  lateinit var binding : ActivityAccidentPhotoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccidentPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.strAccidentHistory)

//        val intent = intent

        val photoPath = intent.extras!!.getString(Constants.INTENT_PHOTO_PATH)

//        var photoPath : String = filesDir.toString() + "/picture222.jpg"

        Log.e("eleutheria", "photoPath : $photoPath")

        val imgFile = File(photoPath)

        if (imgFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath())
            binding.ivPhoto.setImageBitmap(myBitmap)

            val arPath = photoPath!!.split("/")
            if(arPath.size > 6) {
                Toast.makeText(this, "file : ${arPath[6]}", Toast.LENGTH_SHORT).show()
            }


//            val bitmap = BitmapFactory.decodeFile("/path/images/image.jpg")
//            val blob = ByteArrayOutputStream()
//            myBitmap.compress(CompressFormat.JPEG, 90 /* Ignored for PNGs */, blob)
//            val bitmapdata: ByteArray = blob.toByteArray()
//
//            val jpeg = bitmapdata
//            val fileName = System.currentTimeMillis().toString() + ".jpg"
////                    val photo = File(Environment.getExternalStorageDirectory(), "/data/user/0/com.inu.bar/files/driving/$fileName.jpeg");
//            val photo = File(filesDir, fileName);
//
//            if (photo.exists()) {
//                photo.delete();
//            }
//            try {
//                val fos = FileOutputStream(photo.getPath());
//                fos.write(jpeg);
//                fos.close();
//
//                Log.e("eleutheria", "Constants.imageData : ${Constants.imageData}")
//                Constants.imageData = byteArrayOf()
//                Log.e("eleutheria", "FileOutputStream close")
//            }
//            catch (e: IOException) {
//                Log.e("eleutheria", "Exception in photoCallback", e)
//            }
        }
    }
}