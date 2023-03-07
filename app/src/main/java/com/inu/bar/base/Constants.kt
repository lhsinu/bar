package com.inu.bar.base

import com.inu.bar.db.BarEntity




class Constants {
    companion object {

        var strDeviceAddress = "Not Connected"



//        var str112Number: String = "112"
//        var str119Number: String = "119"
        var str112Number: String = "01027199460"
        var str119Number: String = "01027199460"


        var bRealStart = false

        val SHARED_PREF_SEUPDATA : String                           = "setupData"
        val PREF_112_CALL_NUMBER : String                           = "pref112CallNumber"
        val PREF_119_CALL_NUMBER : String                           = "pref119CallNumber"

        val SHARED_PREF_DEVICEDATA : String                         = "deviceData"
        val PREF_DRIVING_DEVICE : String                            = "prefDrivingDevice"
        val PREF_FRONT_DEVICE : String                              = "prefFrontDevice"
        val PREF_BACK_DEVICE : String                               = "prefBackDevice"
        val PREF_WIFI_DEVICE : String                               = "prefWifiDevice"
        val PREF_WIFIPORT_DEVICE : String                           = "prefWifiPortDevice"


        var curState : Int                                          = 0
        val STATE_CONNECT_DRIVING : Int                              = 1
        val STATE_CONNECT_FRONT : Int                                = 2
        val STATE_CONNECT_BACK : Int                                 = 3

        val INTENT_PHOTO_PATH : String                              = "photoPath"


        const val DATA_STORE_INDEX_DRIVING                           = 0
        const val DATA_STORE_INDEX_FRONT_CAM                         = 1
        const val DATA_STORE_INDEX_BACK_CAM                          = 2

        const val PREFIX_START                                      = "FFD8FF"
        const val PREFIX_MIDDLE                                     = "212121"
        const val PREFIX_END                                        = "FFD9"

//        var baStartBytes : ByteArray                                    = PREFIX_START.toByteArray()
        var baStartBytes : ByteArray                                    = byteArrayOf(-1, -40, -1)
//            var baMiddleBytes : ByteArray                               = PREFIX_MIDDLE.toByteArray()
        var baMiddleBytes : ByteArray                               = byteArrayOf(33, 33, 33)
//            var baEndBytes : ByteArray                                  = PREFIX_END.toByteArray()
        var baEndBytes : ByteArray                                  = byteArrayOf(-1, -39)

//        var baEndEndBytes : ByteArray                               = byteArrayOf(-34, 0xFF, 0xFF)
        //0xFF, 0xD9, 0xFF

        var bStartReading : Int                                     = 0
        var nCurrentState : Int                                     = 0
        const val READING_STATE_NONE                                = 0
        const val READING_STATE_FRONT                               = 1
        const val READING_STATE_BACK                                = 2
        const val READING_STATE_READY                               = 3
        const val READING_STATE_STOP                                = 11
        const val READING_STATE_START                               = 12

        var recentData: BarEntity                                   = BarEntity(
                                                                    1,
                                                                    0,
                                                                    false,
                                                                    "front_cam",
                                                                    "back_cam",
                                                                    "0.0, 0.0")

        var pastData: BarEntity                                   = BarEntity(
                                                                    1,
                                                                    0,
                                                                    false,
                                                                    "front_cam",
                                                                    "back_cam",
                                                                    "0.0, 0.0")

        var imageDataFront : ByteArray = byteArrayOf()
        var imageDataBack : ByteArray = byteArrayOf()

        val REQUEST_ENABLE_BT = 1
        // Stops scanning after 10 seconds.
        val SCAN_PERIOD: Long = 10000

        const val MSG_CALLBACK_STATE = "callback_state"


        const val ACTION_MMS_SENT = "MMS_SENT_ACTION"
        const val DEFAULT_EXPIRY_TIME = (7 * 24 * 60 * 60).toLong()

        var default_driving_address                                 = "50:02:91:95:8A:D2"
//        var default_driving_address                                 = "50:02:91:95:69:FA"
//        var default_front_address                                   = "30:C6:F7:04:4F:5A"
//        var default_front_address                                   = "30:C6:F7:04:32:7A"
//        var default_front_address                                   = "30:C6:F7:04:2E:F2"
        var default_front_address                                   = "30:C6:F7:04:43:E6"
//        var default_back_address                                    = "30:C6:F7:04:35:46"
//        var default_back_address                                    = "30:C6:F7:04:43:E6"
//        var default_back_address                                    = "30:C6:F7:04:35:46"
//        var default_back_address                                    = "30:C6:F7:04:4F:5A"
        var default_back_address                                    = "30:C6:F7:04:2E:F2"

        var default_wifi_ip                                             = "192.168.4.1"
        var default_wifi_port                                           = "80"


        var MODULE_ADDRESS_WIFI_CAM                                     = default_wifi_ip       // "192.168.4.1"
        var MODULE_ADDRESS_WIFI_PORT                                    = default_wifi_port     // "80"

        // Classic Address
        var MODULE_ADDRESS_CLASSIC_FRONTCAM                             = default_front_address//"30:C6:F7:04:43:E6"
        var MODULE_ADDRESS_CLASSIC_BACKCAM                              = default_back_address//"30:C6:F7:04:4F:5A"

        // Address
//        val MODULE_ADDRESS_DRIVING                                  = "50:02:91:95:83:E6"
//        val MODULE_ADDRESS_FRONT_CAM                                = "98:DA:D0:00:46:3A"
//        val MODULE_ADDRESS_BACK_CAM                                 = "4D:B1:2D:AB:1F:31"


        // Service UUID
//        val MODULE_SERVICE_UUID_DRIVING                             = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
//        val MODULE_SERVICE_UUID_FRONT_CAM                           = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
//        val MODULE_SERVICE_UUID_BACK_CAM                            = "19b10001-e8f2-537e-4f6c-d104768a1214"

        // Characteristic UUID
//        val MODULE_CHARACTERISTIC_UUID_DRIVING                      = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
//        val MODULE_CHARACTERISTIC_UUID_FRONT_CAM                    = "6E400003-B5A3-F393-E0A9-E50E24DCCA9F"
//        val MODULE_CHARACTERISTIC_UUID_BACK_CAM                     = "00002902-0000-1000-8000-00805f9b34fb"

        // ESP32_2
//        val MODULE_ADDRESS_DRIVING                                  = "50:02:91:95:6E:6A"
//        val MODULE_SERVICE_UUID_DRIVING                             = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
//        val MODULE_CHARACTERISTIC_UUID_DRIVING                      = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"

        // ESP32_1
//        val MODULE_ADDRESS_DRIVING                                  = "50:02:91:95:66:FA"
//        val MODULE_SERVICE_UUID_DRIVING                             = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
//        val MODULE_CHARACTERISTIC_UUID_DRIVING                      = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

        // ESP32_5
//        val MODULE_ADDRESS_DRIVING                                  = "50:02:91:95:83:E6"
//        val MODULE_SERVICE_UUID_DRIVING                             = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
//        val MODULE_CHARACTERISTIC_UUID_DRIVING                      = "beb5483e-36e1-4688-b7f5-ea07361b26a8"

        // ESP_helmet_2
//        val MODULE_ADDRESS_DRIVING                                  = "50:02:91:95:58:46"
//        val MODULE_ADDRESS_DRIVING                                  = "50:02:91:95:69:FA"
        var MODULE_ADDRESS_DRIVING                                  = default_driving_address
        val MODULE_SERVICE_UUID_DRIVING                             = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        val MODULE_CHARACTERISTIC_UUID_DRIVING                      = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"

//        val MODULE_ADDRESS_FRONT_CAM                                = "50:02:91:95:66:FA"
//        val MODULE_SERVICE_UUID_FRONT_CAM                           = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"
//        val MODULE_CHARACTERISTIC_UUID_FRONT_CAM                    = "6E400003-B5A3-F393-E0A9-E50E24DCCA9F"

        // cam all
//        val MODULE_ADDRESS_DRIVING                                  = "30:C6:F7:04:35:46"
//        val MODULE_SERVICE_UUID_DRIVING                             = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
//        val MODULE_CHARACTERISTIC_UUID_DRIVING                      = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
//        val MODULE_ADDRESS_FRONT_CAM                                = "30:C6:F7:04:35:46"
//        val MODULE_SERVICE_UUID_FRONT_CAM                           = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
//        val MODULE_CHARACTERISTIC_UUID_FRONT_CAM                    = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"

//        val MODULE_ADDRESS_FRONT_CAM                                = "30:C6:F7:04:4B:92"
//        val MODULE_ADDRESS_FRONT_CAM                                = "30:C6:F7:04:32:7A"
//        val MODULE_ADDRESS_FRONT_CAM                                = "30:C6:F7:04:4F:5A"
//        val MODULE_ADDRESS_FRONT_CAM                                = "30:C6:F7:04:35:46"
        var MODULE_ADDRESS_FRONT_CAM                                = default_front_address
        val MODULE_SERVICE_UUID_FRONT_CAM                           = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        val MODULE_CHARACTERISTIC_UUID_FRONT_CAM                    = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"

//        val MODULE_ADDRESS_BACK_CAM                                = "30:C6:F7:04:4B:92"
//        val MODULE_ADDRESS_BACK_CAM                                = "30:C6:F7:04:4F:5A"
//        val MODULE_ADDRESS_BACK_CAM                                = "30:C6:F7:04:35:46"
        var MODULE_ADDRESS_BACK_CAM                                = default_back_address
        val MODULE_SERVICE_UUID_BACK_CAM                           = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
        val MODULE_CHARACTERISTIC_UUID_BACK_CAM                    = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E"
    }
}