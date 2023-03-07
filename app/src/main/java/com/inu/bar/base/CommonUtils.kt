package com.inu.bar.base

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.inu.bar.LocationProvider
import com.inu.bar.R
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*


class CommonUtils {
    fun convertLongToTime(time: Long): String {
        val date = Date(time)
//        val format = SimpleDateFormat("yyyy.MM.dd HH:mm")
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")

        return format.format(date)
    }

    fun currentTimeToLong(): Long {
        return System.currentTimeMillis()
    }

    fun convertDateToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy.MM.dd HH:mm")
        return df.parse(date)!!.time
    }

    fun getStartTime(): String {
//        val calendar = Calendar.getInstance()
        val mdformat = SimpleDateFormat("YYYY/MM/dd/hh/mm/ss")
        val startTime = mdformat.format(Date())
        var returnTime = "220824181818"
        var strYear = "22"
        var strMonth = "08"
        var strDay = "24"
        var strHour = "18"
        var strMin = "18"
        var strSecond = "18"


        val arSplit = startTime.split("/")

        if(arSplit.size > 5) {
            strYear = arSplit[0]
            strMonth = arSplit[1]
            strDay = arSplit[2]
            strHour = arSplit[3]
            strMin = arSplit[4]
            strSecond = arSplit[5]

            returnTime = strYear.substring(2) + strMonth + strDay + strHour + strMin + strSecond
//            returnTime = "%02x".format((strYear.substring(2)).toInt()) + "%02x".format(strMonth.toInt()) + "%02x".format(strDay.toInt()) + "%02x".format(strHour.toInt()) + "%02x".format(strMin.toInt()) + "%02x".format(strSecond.toInt())
        }


        Log.e("eleutheria", "returnTime : " + returnTime)

        return returnTime

    }

    fun getAddress(lat : Double, lon : Double, context: Context): String {

//        Locale.setDefault(Locale("en", "GB"))
//        val new_locale = Locale.getDefault()
//
//        val geocoder = Geocoder(context, Locale.getDefault())
        Locale.setDefault(Locale("en", "GB"))
        val new_locale = Locale.getDefault()
        val geocoder = Geocoder(context, new_locale)

        val addresses: List<Address> = geocoder.getFromLocation(lat, lon, 1)
        val address: String = addresses[0].getAddressLine(0)

        return address
    }

    fun send119SMS() {
        val str119Number = Constants.str119Number

        val smsManager = SmsManager.getDefault()

        val locationProvider = LocationProvider(MyApplication.ApplicationContext())

        val latitude = locationProvider.getLocationLatitude()
        val longitude = locationProvider.getLocationLongitude()

        Locale.setDefault(Locale("en", "GB"))
        val new_locale = Locale.getDefault()
        val geocoder = Geocoder(MyApplication.ApplicationContext(), new_locale)
//        val geocoder = Geocoder(MyApplication.ApplicationContext(), Locale.getDefault())

        val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
        var strAddress = MyApplication.ApplicationContext().getString(R.string.strAccidentAddress)

        if(addresses.size > 0) {
            strAddress = addresses[0].getAddressLine(0)
        }

//        val message = "Emergency Message!! "
        val message = "Emergency!! I need rescue!! track my location!! Location : $strAddress"
        val strFirstString = str119Number.substring(0, 1)

        if(strFirstString.equals("0")) {
            Log.e("eleutheria", "str119Number : $str119Number")
            smsManager.sendTextMessage(str119Number, null, message, null, null)
//            smsManager.sendMultimediaMessage()
        }
    }

    fun send112SMS() {
        val str112Number = Constants.str112Number

        val smsManager = SmsManager.getDefault()

        val locationProvider = LocationProvider(MyApplication.ApplicationContext())

        val latitude = locationProvider.getLocationLatitude()
        val longitude = locationProvider.getLocationLongitude()

        Locale.setDefault(Locale("en", "GB"))
        val new_locale = Locale.getDefault()
        val geocoder = Geocoder(MyApplication.ApplicationContext(), new_locale)
//        val geocoder = Geocoder(MyApplication.ApplicationContext(), Locale.getDefault())

        val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
        var strAddress = MyApplication.ApplicationContext().getString(R.string.strAccidentAddress)

        if(addresses.size > 0) {
            strAddress = addresses[0].getAddressLine(0)
        }

//        val message = "Emergency Message!! "
        val message = "Emergency!! I need rescue!! track my location!! Location : $strAddress"
        val strFirstString = str112Number.substring(0, 1)

        if(strFirstString.equals("0")) {
            Log.e("eleutheria", "str112Number : $str112Number")
            smsManager.sendTextMessage(str112Number, null, message, null, null)
//            Toast.makeText(MyApplication.ApplicationContext(), strAddress, Toast.LENGTH_SHORT).show()
        }
    }

    fun send112PhotoSMS() {
        val str112Number = Constants.str112Number

        val smsManager = SmsManager.getDefault()

        val message = "Emergency!! I need rescue!! track my location!!"
        val strFirstString = str112Number.substring(0, 1)

        if(strFirstString.equals("0")) {
            Log.e("eleutheria", "str112Number : $str112Number")
            smsManager.sendTextMessage(str112Number, null, message, null, null)
        }
        val uri: Uri = Uri.parse(
            "file://" + MyApplication.ApplicationContext().filesDir.path + "/20220805_081921.jpg"
//            "file://" + Environment.getExternalStorageDirectory().toString() + "/test.png"
        )
        val i = Intent(Intent.ACTION_SEND)
        i.putExtra("address", str112Number)
        i.putExtra("sms_body", message)
        i.putExtra(Intent.EXTRA_STREAM, "file:/$uri")
        i.type = "image/png"
        startActivity(MyApplication.ApplicationContext(), i, null)
    }

    fun send119PhotoSMS() {
        val str119Number = Constants.str119Number
//        Log.e("eleutheria", "str119Number : $str119Number")

        val smsManager = SmsManager.getDefault()

        val locationProvider = LocationProvider(MyApplication.ApplicationContext())

        val latitude = locationProvider.getLocationLatitude()
        val longitude = locationProvider.getLocationLongitude()

        Locale.setDefault(Locale("en", "GB"))
        val new_locale = Locale.getDefault()
        val geocoder = Geocoder(MyApplication.ApplicationContext(), new_locale)
//        val geocoder = Geocoder(MyApplication.ApplicationContext(), Locale.getDefault())

        val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
        var strAddress = MyApplication.ApplicationContext().getString(R.string.strAccidentAddress)

        if(addresses.size > 0) {
            strAddress = addresses[0].getAddressLine(0)
        }

//        val message = "Emergency Message!! "
        val message = "Emergency!! I need rescue!! track my location!! Location : $strAddress"
        val strFirstString = str119Number.substring(0, 1)

        if(strFirstString.equals("0")) {
            Log.e("eleutheria", "str119Number : $str119Number")
//            val pendingIntent : PendingIntent = PendingIntent.getBroadcast(MyApplication.ApplicationContext(), 0, Intent("com.example.android.apis.os.MMS_SENT_ACTION"), PendingIntent.FLAG_MUTABLE)

//            val contentProviderUri: Uri = Uri.parse(
//                "file:/" + MyApplication.ApplicationContext().filesDir.path + "/20220805_053236.jpg")
//            val contentProviderUri: Uri = Uri.parse(
//                "file://data/data/com.inu.bar/files/20220805_081921.jpg")
//            val contentProviderUri: Uri = Uri.parse(
//                "file://sdcard/DCIM/Camera/20220808_155140.jpg")

//            val contentProviderUri: Uri = Uri.parse(
//                "sdcard/DCIM/Camera/20220808_155140.jpg")

//            Log.e("eleutheria", "contentProviderUri : $contentProviderUri")
//
//            try {
//                smsManager.sendMultimediaMessage(MyApplication.ApplicationContext(), contentProviderUri, str119Number, null, null)
//
//            } catch (e: Exception) {
//                Log.e("eleutheria", "SEND.exception ${e.toString()}")
//                throw e
//            }

//            val uri = Uri.parse("content://media/external/images/media/23")
//            val uri: Uri = Uri.parse(
//                "file:/" + MyApplication.ApplicationContext().filesDir.path + "/20220805_053236.jpg")

//            val FileUriProvider = BuildConfig.APPLICATION_ID + ".fileProvider"
            val FileUriProvider = "com.inu.bar.fileProvider"
            val file = File(MyApplication.ApplicationContext().filesDir.path + "/20220805_053236.jpg")
            // 기존 방법
            // val fileUri = Uri.fromFile(file)

            // FileProvider 사용
            val fileUri = FileProvider.getUriForFile(
                MyApplication.ApplicationContext(),
                FileUriProvider,
                file
            )

            MyApplication.ApplicationContext().grantUriPermission("com.inu.bar", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

//            val smsNumber = String.format("smsto:$str119Number")

            Log.e("eleutheria", "smsNumber : $str119Number")
            val it = Intent(Intent.ACTION_SEND)
//            val it = Intent(Intent.ACTION_SENDTO)
//            it.data = Uri.parse(smsNumber)
            it.putExtra("sms_body", "some text")
            it.putExtra("address", str119Number)
            it.putExtra(Intent.EXTRA_STREAM, fileUri)
//            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            it.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            it.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            it.type = "image/jpg"
            startActivity(MyApplication.ApplicationContext(), it, null)


//            try {
//                smsManager.sendMultimediaMessage(MyApplication.ApplicationContext(), fileUri, "01057613147", null, null)
//
//            } catch (e: Exception) {
//                Log.e("eleutheria", "SEND.exception ${e.toString()}")
//                throw e
//            }

//            val mSendfile = File(MyApplication.ApplicationContext().filesDir.path + "/20220805_053236.jpg")
//
//            val pdu: ByteArray? = buildPdu(MyApplication.ApplicationContext(), "01027199460", "message Title", "message Body")
//            val writerUri = Uri.Builder()
//                .authority("com.inu.bar.mmslib.MmsFileProvider")
//                .path("20220805_053236.jpg")
//                .scheme(ContentResolver.SCHEME_CONTENT)
//                .build()
//            val pendingIntent = PendingIntent.getBroadcast(
//                MyApplication.ApplicationContext(), 0, Intent(Constants.ACTION_MMS_SENT), PendingIntent.FLAG_MUTABLE
//            )
//            var writer: FileOutputStream? = null
//            var contentUri: Uri? = null
//
//            try {
//                writer = FileOutputStream(mSendfile)
//                writer.write(pdu)
//                contentUri = writerUri
//            } catch (e: IOException) {
//                Log.e("eleutheria", "Error writing send file", e)
//            } finally {
//                if (writer != null) {
//                    try {
//                        writer.close()
//                    } catch (e: IOException) {
//                    }
//                }
//            }
//
//            if (contentUri != null) {
//                SmsManager.getDefault().sendMultimediaMessage(
//                    MyApplication.ApplicationContext(),
//                    contentUri, null /*locationUrl*/, null /*configOverrides*/,
//                    pendingIntent
//                )
//            } else {
//                Log.e("eleutheria", "Error writing sending Mms")
//                try {
//                    pendingIntent.send(SmsManager.MMS_ERROR_IO_ERROR)
//                } catch (ex: CanceledException) {
//                    Log.e("eleutheria", "Mms pending intent cancelled?", ex)
//                }
//            }

//            val fileName =
//                "send." + java.lang.String.valueOf(Math.abs(Random().nextLong())) + ".dat"
//            val mSendFile: File = File(context.getCacheDir(), fileName)
//            val pdu: ByteArray =
//                MmsMessagingDemo.buildPdu(context, phoneNumber, "hello", messageText)
//            var writer: FileOutputStream? = null
//            try {
//                writer = FileOutputStream(mSendFile)
//                writer.write(pdu)
//            } finally {
//                if (writer != null) {
//                    writer.close()
//                }
//            }
//            val writerUri = Uri.Builder()
//                .authority("com.example.android.apis.os.MmsFileProvider")
//                .path(fileName)
//                .scheme(ContentResolver.SCHEME_CONTENT)
//                .build()
//
//            val sms = SmsManager.getDefault()
//            sms.sendMultimediaMessage(context, writerUri, null, null, null)

        }
    }

    fun sendCall() {
        var strPhoneNumber = Constants.str119Number
//        Log.e("eleutheria", "strPhoneNumber : $strPhoneNumber")

        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$strPhoneNumber"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(MyApplication.ApplicationContext(), intent, null)
    }



    fun writeTextToFile(strMessage : String) {
//        val mdformat = SimpleDateFormat("YYYY/MM/dd/hh/mm/ss")
//        val startTime = mdformat.format(Date())
//        val filePath = context.filesDir.path + "/bar_receive_" + startTime +".txt"
        val filePath = MyApplication.ApplicationContext().filesDir.path + "/bar_receive.txt"
        val file = File(filePath)
        val fileWriter = FileWriter(file, true)
        val bufferedWriter = BufferedWriter(fileWriter)

        bufferedWriter.append(strMessage)
        bufferedWriter.newLine()
        bufferedWriter.close()
    }

    fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        val formatter = Formatter(sb)
        for (b in bytes) {
            formatter.format("%02x", b)
        }
        return sb.toString()
    }

    fun compareStartBytes(array1: ByteArray): Boolean {
        if (array1.size < 3) {
            return false // both arrays should have at least 3 bytes
        }
        for (i in 0 until 3) {
            Log.e("eleutheria", "i : $i, array : ${array1[i]}, Constants.baStartBytes[i] : ${Constants.baStartBytes[i]}")
            if (array1[i] != Constants.baStartBytes[i]) {
                return false // the first three bytes are not equal
            }
        }
        Log.e("eleutheria", "start is true")
        return true // the first three bytes are equal
    }

    fun compareMiddleBytes(array1: ByteArray): Boolean {
        if (array1.size < 3) {
            return false // both arrays should have at least 3 bytes
        }
        for (i in 0 until 3) {
            Log.e("eleutheria", "i : $i, array : ${array1[i]}, Constants.baMiddleBytes[i] : ${Constants.baMiddleBytes[i]}")
            if (array1[i] != Constants.baMiddleBytes[i]) {
                return false // the first three bytes are not equal
            }
        }
        Log.e("eleutheria", "middle is true")
        return true // the first three bytes are equal
    }

    fun compareEndBytes(array1: ByteArray): Boolean {
        if (array1.size < 2) {
            return false // both arrays should have at least 3 bytes
        }
        for (i in 0 until 2) {
            if (array1[i] != Constants.baEndBytes[i]) {
                return false // the first three bytes are not equal
            }
        }
        return true // the first three bytes are equal
    }
}