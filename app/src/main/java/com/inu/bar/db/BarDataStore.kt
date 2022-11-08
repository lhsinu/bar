package com.inu.bar.db

import android.util.Log
import android.widget.Toast
import com.inu.bar.LocationProvider
import com.inu.bar.base.CommonUtils
import com.inu.bar.base.Constants
import com.inu.bar.base.MyApplication

class BarDataStore {
    companion object {

        const val STATE_NONE                        = false
        const val STATE_STORE                       = true


//        const val INDEX_DRIVING                     = 0
//        const val INDEX_FRONT_CAM                   = 1
//        const val INDEX_BACK_CAM                    = 2


        private var mInstance : BarDataStore = BarDataStore()

        fun getInstance() : BarDataStore {
            return mInstance
        }
    }

    var bStateDriving = STATE_NONE
    var bStateFrontCam = STATE_NONE
    var bStateBackCam = STATE_NONE

    var bDrivingValue = false
    var strGpsData : String = "0.0, 0.0"

    var strFrontCamPath : String = ""
    var strBackCamPath : String = ""

    var db : AppDatabase
    var accidentDao: BarDao

    init {
        bStateDriving = STATE_NONE
        bStateFrontCam = STATE_NONE
        bStateBackCam = STATE_NONE

        db = AppDatabase.getInstance(MyApplication.ApplicationContext())!!
        accidentDao = db.getBarDao()
    }

    fun setDrivingValue(nValue : Boolean) {
        if(bDrivingValue == false) {
            bDrivingValue = nValue
        }
        bStateDriving = true

        if(bStateDriving && bStateFrontCam && bStateBackCam) {
//        if(bStateDriving && bStateFrontCam) {
                getGPSInfo()

                insertData()

                bStateDriving = false
                bStateFrontCam = false
                bStateBackCam = false
        }

    }

    fun setCamValue(nIndex : Int, strCamPath : String) {
        if(Constants.DATA_STORE_INDEX_FRONT_CAM == nIndex) {
            if(!bStateFrontCam) {

                strFrontCamPath = strCamPath
                bStateFrontCam = true
            }
        } else if(Constants.DATA_STORE_INDEX_BACK_CAM == nIndex) {
            if(!bStateBackCam) {

                strBackCamPath = strCamPath
                bStateBackCam = true
            }
        }

        bStateDriving = true
        if(bStateDriving && bStateFrontCam && bStateBackCam) {
//        if(bStateDriving && bStateFrontCam) {
            bDrivingValue = false
            getGPSInfo()

            insertData()

            bStateDriving = false
            bStateFrontCam = false
            bStateBackCam = false
        }

    }

    private fun getGPSInfo() {
        val locationProvider = LocationProvider(MyApplication.ApplicationContext())

        val latitude = locationProvider.getLocationLatitude()
        val longitude = locationProvider.getLocationLongitude()
        strGpsData = latitude.toString() + ", " + longitude.toString()
//        Toast.makeText(MyApplication.ApplicationContext(), "lat : $latitude, lon : $longitude", Toast.LENGTH_SHORT).show()

    }
    private fun insertData() {
        //yyyy-MM-dd'T'HH:mm:ss.SSS

        val commonUtils = CommonUtils()
        val curTime = commonUtils.currentTimeToLong()
        val curDate = commonUtils.convertLongToTime(curTime)

        Log.e("eleutheria", "curTime : $curTime, curDate : $curDate, bDrivingValue : $bDrivingValue")

//        Toast.makeText(MyApplication.ApplicationContext(), "InsertData", Toast.LENGTH_SHORT).show()

        val accidentDate : Long = curTime
//        val driving : Boolean = bDrivingValue
//        val front_cam : String = "/data/user/0/com.inu.bar/files/driving/back01_Small.jpeg"
//        val back_cam : String = "/data/user/0/com.inu.bar/files/driving/front01_Small.jpeg"
//        strBackCamPath = "/data/user/0/com.inu.bar/files/driving/front01_Small.jpeg"

        Thread {
            accidentDao.insertTodo(
                BarEntity(
                    null,
                    accidentDate,
                    bDrivingValue,
                    strFrontCamPath,
                    strBackCamPath,
                    strGpsData
                )
            )
        }.start()
    }
}