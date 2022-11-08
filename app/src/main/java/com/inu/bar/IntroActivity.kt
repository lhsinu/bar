package com.inu.bar

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.inu.bar.base.Constants
import com.inu.bar.databinding.ActivityIntroBinding


class IntroActivity : AppCompatActivity() {
    private  lateinit var binding : ActivityIntroBinding

    private val PERMISSIONS_REQUEST_CODE = 100

    lateinit var locationProvider: LocationProvider
    private var settings: SharedPreferences? = null
    private var deviceSettings: SharedPreferences? = null

//var latitude : Double = 0.0
//var longitude : Double = 0.0

    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )


    lateinit var getGPSPermissionLauncher : ActivityResultLauncher<Intent>

    val startMapActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
        object : ActivityResultCallback<ActivityResult> {
            override fun onActivityResult(result: ActivityResult?) {
                if(result?.resultCode ?: 0 == Activity.RESULT_OK) {
//                latitude = result?.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
//                longitude = result?.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()
//        Log.e("eleutheria", "Intro str119Number : ${Constants.str119Number}, str112Number : ${Constants.str112Number}, MODULE_ADDRESS_DRIVING : ${Constants.MODULE_ADDRESS_DRIVING}, MODULE_ADDRESS_FRONT_CAM : ${Constants.MODULE_ADDRESS_FRONT_CAM}, MODULE_ADDRESS_BACK_CAM : ${Constants.MODULE_ADDRESS_BACK_CAM}")

        settings = getSharedPreferences(Constants.SHARED_PREF_SEUPDATA, Context.MODE_PRIVATE)
        deviceSettings = getSharedPreferences(Constants.SHARED_PREF_DEVICEDATA, Context.MODE_PRIVATE)

        Constants.str119Number =
            settings!!.getString(Constants.PREF_119_CALL_NUMBER, Constants.str119Number).toString()
        Constants.str112Number =
            settings!!.getString(Constants.PREF_112_CALL_NUMBER, Constants.str112Number).toString()

//        val strDrivingData : String? = settings!!.getString(Constants.PREF_DRIVING_DEVICE, Constants.MODULE_ADDRESS_DRIVING)
//        val strFrontData : String? = settings!!.getString(Constants.PREF_FRONT_DEVICE, Constants.MODULE_ADDRESS_FRONT_CAM)
//        val strBackData : String? = settings!!.getString(Constants.PREF_BACK_DEVICE, Constants.MODULE_ADDRESS_BACK_CAM)

        Constants.MODULE_ADDRESS_DRIVING =
            deviceSettings!!.getString(Constants.PREF_DRIVING_DEVICE, Constants.default_driving_address).toString()
        Constants.MODULE_ADDRESS_CLASSIC_FRONTCAM =
            deviceSettings!!.getString(Constants.PREF_FRONT_DEVICE, Constants.default_front_address).toString()
        Constants.MODULE_ADDRESS_CLASSIC_BACKCAM =
            deviceSettings!!.getString(Constants.PREF_BACK_DEVICE, Constants.default_back_address).toString()
//        Log.e("eleutheria", "Intro Next str119Number : ${Constants.str119Number}, str112Number : ${Constants.str112Number}, MODULE_ADDRESS_DRIVING : ${Constants.MODULE_ADDRESS_DRIVING}, MODULE_ADDRESS_FRONT_CAM : ${Constants.MODULE_ADDRESS_FRONT_CAM}, MODULE_ADDRESS_BACK_CAM : ${Constants.MODULE_ADDRESS_BACK_CAM}")


        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            supportActionBar!!.hide()
        }
        Log.e("eleutheria", "Intro Start")
        checkAllPermissions()

    }

    private fun checkAllPermissions() {
        if(!isLocationServicesAvailable()) {
            showDialogForLocationServiceSetting()
        } else {
            isRunTimePermissionsGranted()
        }
    }

    fun isLocationServicesAvailable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER))
    }

    private fun isRunTimePermissionsGranted() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(this@IntroActivity, Manifest.permission.ACCESS_FINE_LOCATION)

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this@IntroActivity, Manifest.permission.ACCESS_COARSE_LOCATION)

        if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@IntroActivity, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        } else {
            val intent = Intent(this@IntroActivity, LoadingDrivingActivity::class.java)
//            val intent = Intent(this@IntroActivity, LoadingFrontClassicActivity::class.java)
//            val intent = Intent(this@IntroActivity, LoagindBackClassicActivity::class.java)
//            val intent = Intent(this@IntroActivity, LoadingFrontActivity::class.java)
//            val intent = Intent(this@IntroActivity, LoadingBackActivity::class.java)
//            val intent = Intent(this@IntroActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {
            var checkResult = true

            for(result in grantResults) {
                if(result != PackageManager.PERMISSION_GRANTED) {
                    checkResult = false
                    break
                }
            }

            if(checkResult) {
                val intent = Intent(this@IntroActivity, LoadingDrivingActivity::class.java)
//                val intent = Intent(this@IntroActivity, LoadingFrontClassicActivity::class.java)
//                val intent = Intent(this@IntroActivity, LoagindBackClassicActivity::class.java)
//                val intent = Intent(this@IntroActivity, LoadingFrontActivity::class.java)
//                val intent = Intent(this@IntroActivity, LoadingBackActivity::class.java)
//                val intent = Intent(this@IntroActivity, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this@IntroActivity, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun showDialogForLocationServiceSetting() {
        getGPSPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
                result ->
            if(result.resultCode == Activity.RESULT_OK) {
                if(isLocationServicesAvailable()) {
                    isRunTimePermissionsGranted()
                } else {
                    Toast.makeText(this@IntroActivity, "위치 서비스를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val builder : AlertDialog.Builder = AlertDialog.Builder(this@IntroActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage("위치 서비스가 꺼져 있습니다. 설정해야 앱을 사용할 수 있습니다.")
        builder.setCancelable(true)
        builder.setPositiveButton("설정", DialogInterface.OnClickListener {
                dialog, id ->
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            getGPSPermissionLauncher.launch(callGPSSettingIntent)
        })
        builder.setNegativeButton("취소", DialogInterface.OnClickListener{
                dialog, id ->
            dialog.cancel()
            Toast.makeText(this@IntroActivity, "기기에서 위치서비스(GPS) 설정 후 사용 해 주세요.", Toast.LENGTH_SHORT).show()
            finish()
        })
        builder.create().show()
    }
}