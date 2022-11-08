package com.inu.bar

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.inu.bar.base.CommonUtils
import com.inu.bar.base.Constants
import com.inu.bar.base.Constants.Companion.str112Number
import com.inu.bar.base.MyApplication
import com.inu.bar.databinding.ActivityDatabaseBinding
import com.inu.bar.db.AppDatabase
import com.inu.bar.db.BarDao
import com.inu.bar.db.BarDataStore
import com.inu.bar.db.BarEntity
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import java.text.SimpleDateFormat


class DatabaseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDatabaseBinding
    val spinnerItems = arrayOf("0", "1")
    var bDriving : Boolean = false
    var strGpsData : String = "0.0, 0.0"

    lateinit var db : AppDatabase
    lateinit var accidentDao: BarDao
    private lateinit var barList: ArrayList<BarEntity>

    lateinit var dataStore: BarDataStore
    lateinit var commonUtils: CommonUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatabaseBinding.inflate(layoutInflater)

        setContentView(binding.root)

        db = AppDatabase.getInstance(this)!!
        accidentDao = db.getBarDao()

        dataStore = BarDataStore.getInstance()
        commonUtils = CommonUtils()

        binding.btConfirm.setOnClickListener {
            val locationProvider = LocationProvider(this@DatabaseActivity)

            val latitude = locationProvider.getLocationLatitude()
            val longitude = locationProvider.getLocationLongitude()
            strGpsData = latitude.toString() + ", " + longitude.toString()
            Toast.makeText(this@DatabaseActivity, "lat : $latitude, lon : $longitude", Toast.LENGTH_SHORT).show()

            insertData()
        }

        binding.btGetdb.setOnClickListener {

            Thread {
                barList = ArrayList(accidentDao.getAll())

                Log.e("eleutheria", "size : ${barList.size}")
                for(dataList in barList) {
                    Log.e("eleutheria", "id : ${dataList.id}, date : ${dataList.accidentdate}, " +
                            "driving : ${dataList.driving}, front : ${dataList.camfront}, back : ${dataList.camback}, gps : ${dataList.gps},")
                }
            }.start()


        }

        val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems)

        binding.spDriving.adapter = myAdapter

        binding.spDriving.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position) {
                    0 -> {
                        bDriving = false
                    }

                    1 -> {
                        bDriving = true
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        binding.btDrivingStore.setOnClickListener {
            dataStore.setDrivingValue(true)
        }


        binding.btFrontCamStore.setOnClickListener {
            dataStore.setCamValue(Constants.DATA_STORE_INDEX_FRONT_CAM, "/data/user/0/com.inu.bar/files/Front_20221005_012506.jpg")
        }


        binding.btBackCamStore.setOnClickListener {
            dataStore.setCamValue(Constants.DATA_STORE_INDEX_BACK_CAM, "/data/user/0/com.inu.bar/files/Back_20221005_012500.jpg")
        }

        binding.btDate.setOnClickListener {
            val simpleDateFormat = SimpleDateFormat("yyyyMMdd_hhmmss")
            val dateString = simpleDateFormat.format(System.currentTimeMillis())
            val fileName = dateString + ".jpg"
            binding.tvDate.text = fileName
        }

        binding.btEmergency.setOnClickListener {
//            commonUtils.send119SMS()
            commonUtils.send112SMS()
//            commonUtils.sendCall()
//            var strPhoneNumber = Constants.str119Number
//            Log.e("eleutheria", "strPhoneNumber : $strPhoneNumber")
//
//            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$strPhoneNumber"))
//            ContextCompat.startActivity(this, intent, null)

//            var strAddress = "No data"
//            strAddress = commonUtils.getAddress(37.375571, 126.634704, this)
//
//            Toast.makeText(this, strAddress, Toast.LENGTH_SHORT).show()
        }


        binding.btSms.setOnClickListener {

            commonUtils.send119PhotoSMS()
//            commonUtils.sendMMS("01037810561")
//            commonUtils.send119SMS()

////            val str112Number = "01031183428"
//
//            val smsManager = SmsManager.getDefault()
//
//            val message = "Emergency!! I need rescue!! track my location!!"
//            val strFirstString = Constants.str119Number.substring(0, 1)
//
//            if(strFirstString.equals("0")) {
//                Log.e("eleutheria", "str112Number : $str112Number")
//                smsManager.sendTextMessage(str112Number, null, message, null, null)
//            }
//            val uri: Uri = Uri.parse(
//                "file:/" + "/sdcard/DCIM/Camera/20220808_155140.jpg"
//
////            "file://" + Environment.getExternalStorageDirectory().toString() + "/test.png"
//            )
//            val i = Intent(Intent.ACTION_SEND)
//            i.putExtra("address", str112Number)
//            i.putExtra("sms_body", message)
//            i.putExtra(Intent.EXTRA_STREAM, "$uri")
//
//            Log.e("eleutheria", "uri : $uri")
//            i.type = "image/png"
//            startActivity(i)
        }

    }

    fun sendMMS(phoneNumber : String) {
        Log.e("eleutheria", "sendMMS(Method) : " + "start");

        var subject : String = "Subject"
        var text : String = "body Text"

        var imagePath : String = filesDir.path + "/20220805_053236.jpg"

        Log.e("eleutheria", "phoneNumber : $phoneNumber")
        Log.e("eleutheria", "Subject : $subject")
        Log.e("eleutheria", "text : $text")
        Log.e("eleutheria", "imagePath : $imagePath")

        val mmsSettings : Settings = Settings()
        mmsSettings.useSystemSending = true

        val transaction : Transaction = Transaction(this, mmsSettings)

        val mmsMessage : Message = Message(text, phoneNumber, subject)

        if(!imagePath.equals("") && imagePath != null) {
            val mBitmap : Bitmap = BitmapFactory.decodeFile(imagePath)
            mmsMessage.addImage(mBitmap)
        }

        val id : Long = android.os.Process.getThreadPriority(android.os.Process.myTid()).toLong()

        transaction.sendNewMessage(mmsMessage, id)
    }

    private fun insertData() {
        //yyyy-MM-dd'T'HH:mm:ss.SSS

        val commonUtils = CommonUtils()
        val curTime = commonUtils.currentTimeToLong()
        val curDate = commonUtils.convertLongToTime(curTime)

        Log.e("eleutheria", "curTime : $curTime, curDate : $curDate")

        val accidentDate : Long = curTime
        val driving : Boolean = bDriving
        val front_cam : String = "/data/user/0/com.inu.bar/files/driving/back01_Small.jpeg"
        val back_cam : String = "/data/user/0/com.inu.bar/files/driving/front01_Small.jpeg"

        Thread {
            accidentDao.insertTodo(
                BarEntity(
                    null,
                    accidentDate,
                    driving,
                    front_cam,
                    back_cam,
                    strGpsData
                )
            )
        }.start()
    }
}