package com.inu.bar.bluetooth

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.system.Os.close
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.inu.bar.base.CommonUtils
import com.inu.bar.base.Constants
import com.inu.bar.db.BarDataStore
import java.util.*

class BluetoothLeService : Service() {

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mConnectionState = STATE_DISCONNECTED

    lateinit var dataStore: BarDataStore
    lateinit var commonUtils: CommonUtils

    var timer : Timer? = null
    var bDrivingState : Boolean = false

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                gatt.requestMtu(517)
                mConnectionState = STATE_CONNECTED
                broadcastUpdate(intentAction)
                Log.i(TAG, "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt!!.discoverServices())

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
//                disconnectGattServer()
                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status)
            }
            setMCNotification(true)
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic
        ) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)

            Log.e("eleutheria", "onMtuChanged mtu : $mtu")
            if(status == BluetoothGatt.GATT_SUCCESS) {
                gatt!!.discoverServices()
            }
        }
    }

    fun disconnectGattServer() {
        Log.d("eleutheria", "Closing Gatt connection")
        // reset the connection flag
        mConnectionState = STATE_DISCONNECTED

        // disconnect and close the gatt
        if (mBluetoothGatt != null) {
            mBluetoothGatt!!.disconnect()
            mBluetoothGatt!!.close()
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String,
                                characteristic: BluetoothGattCharacteristic
    ) {
        val intent = Intent(action)
//        Log.e("eleutheria", "broadcastUpdate")

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
            val flag = characteristic.properties
            var format = -1
            if (flag and 0x01 != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16
                Log.d(TAG, "Heart rate format UINT16.")
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8
                Log.d(TAG, "Heart rate format UINT8.")
            }
            val heartRate = characteristic.getIntValue(format, 1)!!
            Log.d(TAG, String.format("Received heart rate: %d", heartRate))
            intent.putExtra(EXTRA_DATA, heartRate.toString())
        } else {
            // For all other profiles, writes the data formatted in HEX.
            val data = characteristic.value

//            Log.e("eleutheria", "Driving receive data")
            if (data != null && data.size > 0) {
                val stringBuilder = StringBuilder(data.size)
                for (byteChar in data)
                    stringBuilder.append(String.format("%02X ", byteChar))
                intent.putExtra(EXTRA_DATA, String(data) + "\n" + stringBuilder.toString())

                if(String(data).contains("1")) {
                    dataStore.setDrivingValue(true)
                    Log.e("eleutheria", "Driving Data True")
                    if(!bDrivingState) {
                        bDrivingState = true
//                        Log.e("eleutheria", "bDrivingState : $bDrivingState")

                        commonUtils.send119SMS()
                        commonUtils.send112SMS()
                        commonUtils.sendCall()

                        Handler(Looper.getMainLooper()).postDelayed({
                            bDrivingState = false
//                            Log.e("eleutheria", "Handler bDrivingState : $bDrivingState")
                        }, 3000)
                    }
//                    commonUtils.sendCall()
                } else if(String(data).contains("0")) {
                    dataStore.setDrivingValue(false)
                }


//                Log.e("eleutheria", "Driving receive : ${String(data)}")
//
//                val strReceiveData = String(data)
//
//                val subData = strReceiveData.substring(2, strReceiveData.length - 1)
//                Log.d("eleutheria", String.format("Received Temperature : ${subData}"))
            }
        }
        sendBroadcast(intent)
    }


    inner class LocalBinder : Binder() {
        internal val service: BluetoothLeService
            get() = this@BluetoothLeService
    }

    override fun onBind(intent: Intent): IBinder {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    private val mBinder = LocalBinder()

    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        dataStore = BarDataStore.getInstance()
        commonUtils = CommonUtils()

        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                Log.e("eleutheria", "Unable to initialize BluetoothManager.")
                return false
            }
        }

        mBluetoothAdapter = mBluetoothManager!!.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            Log.e("eleutheria", "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            Log.w("eleutheria", "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress
            && mBluetoothGatt != null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            Log.w("eleutheria", "BluetoothAdapter not initialized or unspecified address.")
            if (mBluetoothGatt!!.connect()) {
                mConnectionState = STATE_CONNECTING
                return true
            } else {
                return false
            }
        }

        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.disconnect()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.

     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        Log.e("eleutheria", "characteristic: BluetoothGattCharacteristic")
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

//    fun writeCharacteristic(value: String) {
//        Log.e("eleutheria", "send heartbeatCharacteristic : $value")
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized")
//            return
//        }
//        /*check if the service is available on the device*/
//        val mCustomService =
//            mBluetoothGatt!!.
//            getService(UUID.fromString(SampleGattAttributes.SERVICE_DRIVING_STATE))
//        if (mCustomService == null) {
//            Log.w(TAG, "Custom BLE Service not found")
//            return
//        }
//        /*get the read characteristic from the service*/
//        val mWriteCharacteristic =
//            mCustomService.getCharacteristic(
//                UUID.
//                fromString(SampleGattAttributes.CHARACTERISTIC_DRIVING_STATE))
////        mWriteCharacteristic.value = BigInteger(value, 16).toByteArray()
//        val strValue : String = value
//        mWriteCharacteristic.value = strValue.toByteArray()
//        if (!mBluetoothGatt!!.writeCharacteristic(mWriteCharacteristic)) {
//            Log.e("eleutheria", "send fail heartbeatCharacteristic : $value")
//        }
//    }

    /**
     * Enables or disables notification on a give characteristic.

     * @param characteristic Characteristic to act on.
     * *
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setDrivingCharacteristicNotification(characteristic: BluetoothGattCharacteristic,
                                      enabled: Boolean) {

        Log.e("eleutheria", "characteristic : ${characteristic.uuid}")
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)

        // This is specific to Heart Rate Measurement.
        Log.e("eleutheria", "Driving setCharacteristicNotification characteristic.uuid : ${characteristic.uuid}")
        if (UUID_DRIVING_STATE == characteristic.uuid) {
//            Log.e("eleutheria", "strart")
//            SystemClock.sleep(5000)
//            Log.e("eleutheria", "end")
            val descriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            var mDevice: BluetoothDevice? = mBluetoothGatt!!.device
            var mServices : List<BluetoothGattService> = mBluetoothGatt!!.services

            val characteristic = descriptor.characteristic

            if(characteristic != null) {
                Log.e("eleutheria", "characteristic != null")
            }

            val result : Boolean = mBluetoothGatt!!.writeDescriptor(descriptor)
            Log.e("eleutheria", "driving result : $result, mDevice : $mDevice, mServices : $mServices")
        }
    }

    fun setMCNotification(enabled: Boolean) {

        var uuidMCCharateristic = SampleGattAttributes.CHARACTERISTIC_DRIVING_STATE
        var uuidMCService = SampleGattAttributes.SERVICE_DRIVING_STATE

        if(Constants.curState == Constants.STATE_CONNECT_DRIVING) {
            Log.e("eleutheria", "STATE_CONNECT_DRIVING")
            uuidMCCharateristic = SampleGattAttributes.CHARACTERISTIC_DRIVING_STATE
            uuidMCService = SampleGattAttributes.SERVICE_DRIVING_STATE
        } else if(Constants.curState == Constants.STATE_CONNECT_FRONT) {
            Log.e("eleutheria", "STATE_CONNECT_FRONT")
            uuidMCCharateristic = SampleGattAttributes.CHARACTERISTIC_FRONT_CAMERA
            uuidMCService = SampleGattAttributes.SERVICE_FRONT_CAMERA
        } else if(Constants.curState == Constants.STATE_CONNECT_BACK) {
            Log.e("eleutheria", "STATE_CONNECT_BACK")
            uuidMCCharateristic = SampleGattAttributes.CHARACTERISTIC_BACK_CAMERA
            uuidMCService = SampleGattAttributes.SERVICE_BACK_CAMERA
        }

        var mBluetoothLeService: BluetoothGattService? = null
        var mBluetoothGattCharacteristic: BluetoothGattCharacteristic? = null

        for(service: BluetoothGattService in mBluetoothGatt!!.getServices()) {
            if((service==null)||(service.uuid==null)) continue
            if(uuidMCService.equals(service.uuid.toString(), true)) {
                mBluetoothLeService = service
            }
        }
        if(mBluetoothLeService != null) {
            mBluetoothGattCharacteristic = mBluetoothLeService.getCharacteristic(UUID.fromString(uuidMCCharateristic))
        } else {
            Log.e("eleuthria", "mBluetoothLeService is null")
        }

        if(mBluetoothGattCharacteristic != null) {
            Log.e("eleuthria", "mBluetoothGattCharacteristic != null")
            setDrivingCharacteristicNotification(mBluetoothGattCharacteristic, enabled)
        }
    }

//    private fun sendMessageToActivity(msg: String) {
//        var strActionName = "NoAction"
//
//        when(Constants.nCurFunctionIndex) {
//            Constants.MAIN_FUNCTION_INDEX_HB -> {
//                strActionName = Constants.MESSAGE_SEND_HB
//            }
//            Constants.MAIN_FUNCTION_INDEX_PRESSURE -> {
//                strActionName = Constants.MESSAGE_SEND_PRESSURE
//            }
//            Constants.MAIN_FUNCTION_INDEX_GYRO -> {
//                strActionName = Constants.MESSAGE_SEND_GYRO
//            }
//            Constants.MAIN_FUNCTION_INDEX_REAR -> {
//                strActionName = Constants.MESSAGE_SEND_GYRO
//            }
//            Constants.MAIN_FUNCTION_INDEX_TEMPERATURE -> {
//                strActionName = Constants.MESSAGE_SEND_TEMPERATURE
//            }
//        }
//
//        val intent = Intent(strActionName)
//        // You can also include some extra data.
//        intent.putExtra("value", msg)
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.

     * @return A `List` of supported services.
     */
    val supportedGattServices: List<BluetoothGattService>?
        get() {
            if (mBluetoothGatt == null) return null

            return mBluetoothGatt!!.services
        }

    companion object {
        private val TAG = BluetoothLeService::class.java.simpleName

        private val STATE_DISCONNECTED = 0
        private val STATE_CONNECTING = 1
        private val STATE_CONNECTED = 2

        val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"


        val UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT)
        val UUID_DRIVING_STATE = UUID.fromString(SampleGattAttributes.CHARACTERISTIC_DRIVING_STATE)
    }
}