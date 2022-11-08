package com.inu.bar.bluetooth

import com.inu.bar.base.Constants
import java.util.HashMap

object SampleGattAttributes {
    var attributes: HashMap<String, String> = HashMap()
    var HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"
    var CLIENT_CHARACTERISTIC_CONFIG =              "00002902-0000-1000-8000-00805f9b34fb"

    // service
    var SERVICE_DRIVING_STATE                                        = Constants.MODULE_SERVICE_UUID_DRIVING
    var SERVICE_FRONT_CAMERA                                         = Constants.MODULE_SERVICE_UUID_FRONT_CAM
    var SERVICE_BACK_CAMERA                                          = Constants.MODULE_SERVICE_UUID_BACK_CAM

    //characteristic
    var CHARACTERISTIC_DRIVING_STATE                                 = Constants.MODULE_CHARACTERISTIC_UUID_DRIVING
    var CHARACTERISTIC_FRONT_CAMERA                                  = Constants.MODULE_CHARACTERISTIC_UUID_FRONT_CAM
    var CHARACTERISTIC_BACK_CAMERA                                   = Constants.MODULE_CHARACTERISTIC_UUID_BACK_CAM

    init {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service")
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service")
        // Sample Characteristics.
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement")
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String")


        // Using unknown GATT profile, must debug other end
        attributes.put("19B10000-E8F2-537E-4F6C-D104768A1214", "ioTank")
        attributes.put(SERVICE_DRIVING_STATE, "Driviing State")
        attributes.put(SERVICE_FRONT_CAMERA, "Front Camera")
        attributes.put(SERVICE_BACK_CAMERA, "Back Camera")
    }


    fun lookup(uuid: String, defaultName: String): String {
        val name = attributes.get(uuid)
        return name ?: defaultName
    }
}