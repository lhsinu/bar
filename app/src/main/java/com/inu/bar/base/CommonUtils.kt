package com.inu.bar.base

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.inu.bar.LocationProvider
import com.inu.bar.R
import com.inu.bar.mmslib.ContentType
import com.inu.bar.mmslib.InvalidHeaderValueException
import com.inu.bar.mmslib.pdu.*
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import java.io.File
import java.nio.charset.Charset
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


        var arSplit = startTime.split("/")

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

    fun sendMMS(phoneNumber : String) {
        Log.e("eleutheria", "sendMMS(Method) : " + "start");

        var subject : String = "Subject"
        var text : String = "body Text"

        var imagePath : String = MyApplication.ApplicationContext().filesDir.path + "/20220805_053236.jpg"

        Log.e("eleutheria", "phoneNumber : $phoneNumber")
        Log.e("eleutheria", "Subject : $subject")
        Log.e("eleutheria", "text : $text")
        Log.e("eleutheria", "imagePath : $imagePath")

        val mmsSettings : Settings = Settings()
        mmsSettings.useSystemSending = true

        val transaction : Transaction = Transaction(MyApplication.ApplicationContext(), mmsSettings)

        val mmsMessage : Message = Message(text, phoneNumber, subject)

        if(!imagePath.equals("") && imagePath != null) {
            val mBitmap : Bitmap = BitmapFactory.decodeFile(imagePath)
            mmsMessage.addImage(mBitmap)
        }

        val id : Long = android.os.Process.getThreadPriority(android.os.Process.myTid()).toLong()

        transaction.sendNewMessage(mmsMessage, id)
    }

    private fun buildPdu(
        context: Context, recipients: String, subject: String,
        text: String
    ): ByteArray? {
        val req = SendReq()
        // From, per spec
        val lineNumber: String? = getSimNumber(context)
        if (!TextUtils.isEmpty(lineNumber)) {
            req.setFrom(EncodedStringValue(lineNumber))
        }
        // To
        val encodedNumbers: Array<EncodedStringValue> =
            EncodedStringValue.encodeStrings(recipients.split(" ").toTypedArray())
        if (encodedNumbers != null) {
            req.setTo(encodedNumbers)
        }
        // Subject
        if (!TextUtils.isEmpty(subject)) {
            req.setSubject(EncodedStringValue(subject))
        }
        // Date
        req.setDate(System.currentTimeMillis() / 1000)
        // Body
        val body = PduBody()
        // Add text part. Always add a smil part for compatibility, without it there
        // may be issues on some carriers/client apps
        val size: Int = addTextPart(body, text, true /* add text smil */)
        req.setBody(body)
        // Message size
        req.setMessageSize(size.toLong())
        // Message class
        req.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.toByteArray(Charset.defaultCharset()))
        // Expiry
        req.setExpiry(Constants.DEFAULT_EXPIRY_TIME)
        try {
            // Priority
            req.setPriority(Constants.DEFAULT_PRIORITY)
            // Delivery report
            req.setDeliveryReport(PduHeaders.VALUE_NO)
            // Read report
            req.setReadReport(PduHeaders.VALUE_NO)
        } catch (e: InvalidHeaderValueException) {
        }
        return PduComposer(context, req).make()
    }

    private fun getSimNumber(context: Context): String? {
        val telephonyManager = context.getSystemService(
            Context.TELEPHONY_SERVICE
        ) as TelephonyManager
        return telephonyManager.line1Number
    }

    private fun addTextPart(pb: PduBody, message: String, addTextSmil: Boolean): Int {
        var TEXT_PART_FILENAME = "text_0.txt";
        var sSmilText =
            "<smil>" +
                    "<head>" +
                    "<layout>" +
                    "<root-layout/>" +
                    "<region height=\"100%%\" id=\"Text\" left=\"0%%\" top=\"0%%\" width=\"100%%\"/>" +
                    "</layout>" +
                    "</head>" +
                    "<body>" +
                    "<par dur=\"8000ms\">" +
                    "<text src=\"%s\" region=\"Text\"/>" +
                    "</par>" +
                    "</body>" +
                    "</smil>"

        val part = PduPart()
        // Set Charset if it's a text media.
        part.charset = CharacterSets.UTF_8
        // Set Content-Type.
        part.contentType = ContentType.TEXT_PLAIN.toByteArray(Charset.defaultCharset())
        // Set Content-Location.
        part.contentLocation = TEXT_PART_FILENAME.toByteArray(Charset.defaultCharset())
        val index: Int = TEXT_PART_FILENAME.lastIndexOf(".")
        val contentId: String =
            if (index == -1) TEXT_PART_FILENAME else TEXT_PART_FILENAME.substring(0, index)
        part.contentId = contentId.toByteArray()
        part.data = message.toByteArray()
        pb.addPart(part)
        if (addTextSmil) {
            val smil: String = java.lang.String.format(sSmilText, TEXT_PART_FILENAME)
            addSmilPart(pb, smil)
        }
        return part.data.size
    }

    private fun addSmilPart(pb: PduBody, smil: String) {
        val smilPart = PduPart()
        smilPart.contentId = "smil".toByteArray()
        smilPart.contentLocation = "smil.xml".toByteArray()
        smilPart.contentType = ContentType.APP_SMIL.toByteArray()
        smilPart.data = smil.toByteArray()
        pb.addPart(0, smilPart)
    }
}