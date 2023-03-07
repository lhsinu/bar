package com.inu.bar

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.inu.bar.base.Constants
import com.inu.bar.databinding.ActivityCallSettingBinding
import com.inu.bar.databinding.ActivitySettingDeviceBinding

class SettingDeviceActivity : AppCompatActivity()  {
    private  lateinit var binding : ActivitySettingDeviceBinding
    private var settings: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.strSettingDeviceTitle)

        settings = getSharedPreferences(Constants.SHARED_PREF_DEVICEDATA, Context.MODE_PRIVATE)

//        String data1=sharedPref.getString("data1", "none");
        val strDrivingData : String? = settings!!.getString(Constants.PREF_DRIVING_DEVICE, Constants.MODULE_ADDRESS_DRIVING)
//        val strFrontData : String? = settings!!.getString(Constants.PREF_FRONT_DEVICE, Constants.MODULE_ADDRESS_FRONT_CAM)
//        val strBackData : String? = settings!!.getString(Constants.PREF_BACK_DEVICE, Constants.MODULE_ADDRESS_BACK_CAM)
        val strFrontData : String? = settings!!.getString(Constants.PREF_FRONT_DEVICE, Constants.MODULE_ADDRESS_CLASSIC_FRONTCAM)
        val strBackData : String? = settings!!.getString(Constants.PREF_BACK_DEVICE, Constants.MODULE_ADDRESS_CLASSIC_BACKCAM)
        val strWifiData : String? = settings!!.getString(Constants.PREF_WIFI_DEVICE, Constants.MODULE_ADDRESS_WIFI_CAM)
        val strWifiPortData : String? = settings!!.getString(Constants.PREF_WIFIPORT_DEVICE, Constants.MODULE_ADDRESS_WIFI_PORT)

        if (strDrivingData != null) {
            val arDrivingData = strDrivingData.split(":")

            if(arDrivingData.size > 5) {
                binding.etDriving1.setText(arDrivingData[0])
                binding.etDriving2.setText(arDrivingData[1])
                binding.etDriving3.setText(arDrivingData[2])
                binding.etDriving4.setText(arDrivingData[3])
                binding.etDriving5.setText(arDrivingData[4])
                binding.etDriving6.setText(arDrivingData[5])
            }
        }

        if (strFrontData != null) {
            val arFrontData = strFrontData.split(":")

            if(arFrontData.size > 5) {
                binding.etFront1.setText(arFrontData[0])
                binding.etFront2.setText(arFrontData[1])
                binding.etFront3.setText(arFrontData[2])
                binding.etFront4.setText(arFrontData[3])
                binding.etFront5.setText(arFrontData[4])
                binding.etFront6.setText(arFrontData[5])
            }
        }

        if (strBackData != null)
        {
            val arBackData = strBackData.split(":")

            if(arBackData.size > 5) {
                binding.etBack1.setText(arBackData[0])
                binding.etBack2.setText(arBackData[1])
                binding.etBack3.setText(arBackData[2])
                binding.etBack4.setText(arBackData[3])
                binding.etBack5.setText(arBackData[4])
                binding.etBack6.setText(arBackData[5])
            }
        }

        if (strWifiData != null)
        {
            val arWifiData = strWifiData.split(".")

            if(arWifiData.size > 3) {
                binding.etWifi1.setText(arWifiData[0])
                binding.etWifi2.setText(arWifiData[1])
                binding.etWifi3.setText(arWifiData[2])
                binding.etWifi4.setText(arWifiData[3])
            }
        }

        if (strWifiPortData != null)
        {
                binding.etWifi5.setText(strWifiPortData)
        }

        binding.btDrivingOK.setOnClickListener {
            val strDrivingAddress = binding.etDriving1.text.toString() + ":" + binding.etDriving2.text.toString() + ":" + binding.etDriving3.text.toString() + ":" + binding.etDriving4.text.toString() + ":" + binding.etDriving5.text.toString() + ":" + binding.etDriving6.text.toString()

            Log.e("eleutheria", "strDrivingAddress : $strDrivingAddress")
            val editor = settings!!.edit()
            editor.putString(Constants.PREF_DRIVING_DEVICE, strDrivingAddress)
            editor.apply()

            Toast.makeText(this, "DrivingAddress : $strDrivingAddress", Toast.LENGTH_SHORT).show()
        }


        binding.btFrontOK.setOnClickListener {
            val strFrontAddress = binding.etFront1.text.toString() + ":" + binding.etFront2.text.toString() + ":" + binding.etFront3.text.toString() + ":" + binding.etFront4.text.toString() + ":" + binding.etFront5.text.toString() + ":" + binding.etFront6.text.toString()

            Log.e("eleutheria", "strFrontAddress : $strFrontAddress")
            val editor = settings!!.edit()
            editor.putString(Constants.PREF_FRONT_DEVICE, strFrontAddress)
            editor.apply()

            Toast.makeText(this, "FrontAddress : $strFrontAddress", Toast.LENGTH_SHORT).show()
        }


        binding.btBackOK.setOnClickListener {
            val strBackAddress = binding.etBack1.text.toString() + ":" + binding.etBack2.text.toString() + ":" + binding.etBack3.text.toString() + ":" + binding.etBack4.text.toString() + ":" + binding.etBack5.text.toString() + ":" + binding.etBack6.text.toString()

            Log.e("eleutheria", "strBackAddress : $strBackAddress")
            val editor = settings!!.edit()
            editor.putString(Constants.PREF_BACK_DEVICE, strBackAddress)
            editor.apply()

            Toast.makeText(this, "BackAddress : $strBackAddress", Toast.LENGTH_SHORT).show()
        }

        binding.btWifiOK.setOnClickListener {
            val strWifiAddress = binding.etWifi1.text.toString() + "." + binding.etWifi2.text.toString() + "." + binding.etWifi3.text.toString() + "." + binding.etWifi4.text.toString()

            Log.e("eleutheria", "strWifiAddress : $strWifiAddress")
            val editor = settings!!.edit()
            editor.putString(Constants.PREF_WIFI_DEVICE, strWifiAddress)
            editor.apply()

            val strPortAddress = binding.etWifi5.text.toString()

            Log.e("eleutheria", "strPortAddress : $strPortAddress")
            val ipEditor = settings!!.edit()
            ipEditor.putString(Constants.PREF_WIFIPORT_DEVICE , strPortAddress)
            ipEditor.apply()

            Toast.makeText(this, "PortAddress : $strPortAddress", Toast.LENGTH_SHORT).show()
        }

        binding.btReset.setOnClickListener {
            val strDrivingAddress = Constants.default_driving_address

            Log.e("eleutheria", "strDrivingAddress : $strDrivingAddress")
            val editor = settings!!.edit()
            editor.putString(Constants.PREF_DRIVING_DEVICE, strDrivingAddress)
            editor.apply()

            val strFrontAddress = Constants.default_front_address

            Log.e("eleutheria", "strFrontAddress : $strFrontAddress")
            val editorFront = settings!!.edit()
            editorFront.putString(Constants.PREF_FRONT_DEVICE, strFrontAddress)
            editorFront.apply()

            val strBackAddress = Constants.default_back_address

            Log.e("eleutheria", "strBackAddress : $strBackAddress")
            val editorBack = settings!!.edit()
            editorBack.putString(Constants.PREF_BACK_DEVICE, strBackAddress)
            editorBack.apply()

            val strWifiAddress = Constants.default_wifi_ip

            Log.e("eleutheria", "strWifiAddress : $strWifiAddress")
            val editorWifi = settings!!.edit()
            editorWifi.putString(Constants.PREF_WIFI_DEVICE, strWifiAddress)
            editorWifi.apply()

            val strWifiPortAddress = Constants.default_wifi_port

            Log.e("eleutheria", "strWifiPortAddress : $strWifiPortAddress")
            val editorPort = settings!!.edit()
            editorPort.putString(Constants.PREF_WIFIPORT_DEVICE, strWifiPortAddress)
            editorPort.apply()

            if (strDrivingAddress != null) {
                val arDrivingData = strDrivingAddress.split(":")

                if(arDrivingData.size > 5) {
                    binding.etDriving1.setText(arDrivingData[0])
                    binding.etDriving2.setText(arDrivingData[1])
                    binding.etDriving3.setText(arDrivingData[2])
                    binding.etDriving4.setText(arDrivingData[3])
                    binding.etDriving5.setText(arDrivingData[4])
                    binding.etDriving6.setText(arDrivingData[5])
                }
            }

            if (strFrontAddress != null) {
                val arFrontData = strFrontAddress.split(":")

                if(arFrontData.size > 5) {
                    binding.etFront1.setText(arFrontData[0])
                    binding.etFront2.setText(arFrontData[1])
                    binding.etFront3.setText(arFrontData[2])
                    binding.etFront4.setText(arFrontData[3])
                    binding.etFront5.setText(arFrontData[4])
                    binding.etFront6.setText(arFrontData[5])
                }
            }

            if (strBackAddress != null)
            {
                val arBackData = strBackAddress.split(":")

                if(arBackData.size > 5) {
                    binding.etBack1.setText(arBackData[0])
                    binding.etBack2.setText(arBackData[1])
                    binding.etBack3.setText(arBackData[2])
                    binding.etBack4.setText(arBackData[3])
                    binding.etBack5.setText(arBackData[4])
                    binding.etBack6.setText(arBackData[5])
                }
            }

            if (strWifiAddress != null)
            {
                val arWifiData = strWifiAddress.split(".")

                if(arWifiData.size > 3) {
                    binding.etWifi1.setText(arWifiData[0])
                    binding.etWifi2.setText(arWifiData[1])
                    binding.etWifi3.setText(arWifiData[2])
                    binding.etWifi4.setText(arWifiData[3])
                }
            }

            if (strWifiPortAddress != null)
            {
                    binding.etWifi5.setText(strWifiPortAddress)
            }
        }


    }




}