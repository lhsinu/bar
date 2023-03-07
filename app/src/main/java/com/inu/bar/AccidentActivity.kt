package com.inu.bar

import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.inu.bar.base.CommonUtils
import com.inu.bar.base.Constants
import com.inu.bar.databinding.ActivityAccidentBinding
import com.inu.bar.db.BarEntity
import java.io.File
import java.util.*


class AccidentActivity : AppCompatActivity() {
    private  lateinit var binding : ActivityAccidentBinding
    private  lateinit var recentData : BarEntity
    val commonUtils = CommonUtils()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccidentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.strAccidentHistory)

        recentData = Constants.recentData

        binding.tvAccidentDetail.text = commonUtils.convertLongToTime(recentData.accidentdate)
        val spString : List<String> = recentData.gps.split(", ")

        if(spString.size > 10) { // temporary 10
            val lat: Double = spString[0].toDouble()
            val lon: Double = spString[1].toDouble()

            Locale.setDefault(Locale("en", "GB"))
            val new_locale = Locale.getDefault()
            val geocoder = Geocoder(this@AccidentActivity, new_locale)

//            val geocoder = Geocoder(this@AccidentActivity, Locale.getDefault())

            val addresses: List<Address> = geocoder.getFromLocation(lat, lon, 1)
            var address: String = getString(R.string.strAccidentAddress)

            if(addresses.size > 0) {
                address = addresses[0].getAddressLine(0)

                binding.tvGPSAddress.text = address
            }

            Log.e("eleutheria", "lat : $lat, lon : $lon, address : $address")
        } else {
            binding.tvGPSAddress.text = getString(R.string.strAccidentAddress)
        }

        binding.ivFront.setOnClickListener {
            val intent = Intent(this@AccidentActivity, AccidentPhotoActivity::class.java)
            intent.putExtra(Constants.INTENT_PHOTO_PATH, recentData.camfront)
            startActivity(intent)
        }

        binding.ivBack.setOnClickListener {
            val intent = Intent(this@AccidentActivity, AccidentPhotoActivity::class.java)
            intent.putExtra(Constants.INTENT_PHOTO_PATH, recentData.camback)
            startActivity(intent)
        }

        val imgFrontFile = File(recentData.camfront)

        if (imgFrontFile.exists()) {
            val myFrontBitmap = BitmapFactory.decodeFile(imgFrontFile.getAbsolutePath())
            binding.ivFront.setImageBitmap(myFrontBitmap)
        }

        val imgBackFile = File(recentData.camback)

        if (imgBackFile.exists()) {
            val myBackBitmap = BitmapFactory.decodeFile(imgBackFile.getAbsolutePath())
            binding.ivBack.setImageBitmap(myBackBitmap)
        }
    }
}