package com.inu.bar.bluetooth

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.system.Os.close
import android.util.Log
import android.widget.Toast
import com.inu.bar.base.Constants
import com.inu.bar.base.MyApplication
import com.inu.bar.db.BarDataStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FrontBluetoothLeService : Service() {

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mConnectionState = STATE_DISCONNECTED

    lateinit var dataStore: BarDataStore
    var bDrivingState : Boolean = false

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                gatt.requestMtu(517)
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

//            var imageData : ByteArray = byteArrayOf()

//            Log.e("eleutheria", "Front receive data")
            if (data != null && data.size > 0) {
                val stringBuilder = StringBuilder(data.size)
//                val ubDataArray = data.toUByteArray()
//                Log.e("eleutheria", "Front ubDataArray : $ubDataArray")
                for (byteChar in data) {

                    stringBuilder.append(String.format("%02X ", byteChar))
                }
//                intent.putExtra(EXTRA_DATA, String(data) + "\n" + stringBuilder.toString())
//                Log.e("eleutheria", "Front receive : ${String(data)}, Constants.bStartReading : ${Constants.bStartReading}")
//                Log.e("eleutheria", "Front receive : ${String(data)}, String : ${stringBuilder.toString()}")
//                if(String(data).contains("\n")) {
//                    Log.e("eleutheria", "String Data : new line")
//                }
//                if(stringBuilder.toString().contains("\n")) {
//                    Log.e("eleutheria", "StringBuilder : new line")
//                }

                if(String(data).contains("PF")) {
                    Constants.bStartReading = Constants.READING_STATE_START
                    Log.e("eleutheria", "FRONT_READING_STATE_START : ${Constants.imageDataFront.size}")

                    if(Constants.imageDataFront.size > 0) {
                        val jpeg = Constants.imageDataFront
                        val simpleDateFormat = SimpleDateFormat("yyyyMMdd_hhmmss")
                        val dateString = simpleDateFormat.format(System.currentTimeMillis())
                        val fileName = dateString + ".jpg"
//                    val fileName = System.currentTimeMillis().toString() + ".jpg"
//                    val photo = File(Environment.getExternalStorageDirectory(), "/data/user/0/com.inu.bar/files/driving/$fileName.jpeg");
                        val photo = File(filesDir, fileName);

                        if (photo.exists()) {
                            photo.delete();
                        }
                        try {
                            val fos = FileOutputStream(photo.getPath());
                            fos.write(jpeg);
                            fos.close();

//                        Log.e("eleutheria", "Constants.imageData : ${Constants.imageData}")
                            Constants.imageDataFront = byteArrayOf()
                            Log.e("eleutheria", "Front file path : " + filesDir.path + "/" + fileName)
                            dataStore.setCamValue(Constants.DATA_STORE_INDEX_FRONT_CAM, filesDir.path + "/" + fileName)
//                        Log.e("eleutheria", "FileOutputStream close : ${Constants.imageData.size}")
                        }
                        catch (e: IOException) {
                            Log.e("eleutheria", "Exception in photoCallback", e)
                        }
                    }

                } else if(String(data).contains("!/")) {
                    Constants.bStartReading = Constants.READING_STATE_STOP
                    Log.e("eleutheria", "FRONT_READING_STATE_STOP")

//                    Constants.bStartReading == Constants.READING_STATE_STOP
//                    Log.e("eleutheria", "READING_STATE_STOP + save image")

                    if(Constants.imageDataFront.size > 0) {
                        val jpeg = Constants.imageDataFront
                        val simpleDateFormat = SimpleDateFormat("yyyyMMdd_hhmmss")
                        val dateString = simpleDateFormat.format(System.currentTimeMillis())
                        val fileName = "Front_" + dateString + ".jpg"
//                    val fileName = System.currentTimeMillis().toString() + ".jpg"
//                    val photo = File(Environment.getExternalStorageDirectory(), "/data/user/0/com.inu.bar/files/driving/$fileName.jpeg");
                        val photo = File(filesDir, fileName);

                        if (photo.exists()) {
                            photo.delete();
                        }
                        try {
                            val fos = FileOutputStream(photo.getPath());
                            fos.write(jpeg);
                            fos.close();

//                        Log.e("eleutheria", "Constants.imageData : ${Constants.imageData}")
                            Constants.imageDataFront = byteArrayOf()
                            Log.e(
                                "eleutheria",
                                "Front file path : " + filesDir.path + "/" + fileName
                            )
                            dataStore.setCamValue(
                                Constants.DATA_STORE_INDEX_FRONT_CAM,
                                filesDir.path + "/" + fileName
                            )
//                        Log.e("eleutheria", "FileOutputStream close : ${Constants.imageData.size}")
                        } catch (e: IOException) {
                            Log.e("eleutheria", "Exception in photoCallback", e)
                        }
                    }
                } else {
                    if(Constants.bStartReading == Constants.READING_STATE_START) {
//                    Constants.imageData += stringBuilder.toString().toByteArray()

                        Constants.imageDataFront += data
                        Log.e("eleutheria", "Front receive : ${String(data)}")

//                    Log.e("eleutheria", "READING_STATE_START + data")
                    }
                }

//                if(Constants.bStartReading == Constants.READING_STATE_START) {
////                    Constants.imageData += stringBuilder.toString().toByteArray()
//
//                    Constants.imageData += data
//                    Log.e("eleutheria", "Front receive : ${String(data)}, String : ${stringBuilder.toString()}")
//
////                    Log.e("eleutheria", "READING_STATE_START + data")
//                } else if(Constants.bStartReading == Constants.READING_STATE_STOP) {
//                    Constants.bStartReading == Constants.READING_STATE_STOP
////                    Log.e("eleutheria", "READING_STATE_STOP + save image")
//
//                    val jpeg = Constants.imageData
//                    val fileName = System.currentTimeMillis().toString() + ".jpg"
////                    val photo = File(Environment.getExternalStorageDirectory(), "/data/user/0/com.inu.bar/files/driving/$fileName.jpeg");
//                    val photo = File(filesDir, fileName);
//
//                    if (photo.exists()) {
//                        photo.delete();
//                    }
//                    try {
//                        val fos = FileOutputStream(photo.getPath());
//                        fos.write(jpeg);
//                        fos.close();
//
//                        Log.e("eleutheria", "Constants.imageData : ${Constants.imageData}")
//                        Constants.imageData = byteArrayOf()
//                        Log.e("eleutheria", "FileOutputStream close")
//                    }
//                    catch (e: IOException) {
//                        Log.e("eleutheria", "Exception in photoCallback", e)
//                    }

//                    try {
//                        val storage: File =
//                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
//                        val fname: String = "${UUID.randomUUID()}.jpg"
//                        val tp = File(storage, fname)
//                        val bm: Bitmap = BitmapFactory.decodeByteArray(stringBuilder.toString().toByteArray(), 0, 0)
////                        createBitmap(1280, 1024, )
//                        bm.compress(Bitmap.CompressFormat.JPEG, 100, )
//
//                        tp.createNewFile() // Result of File.createNewFile() ignored
//                        val ot = FileOutputStream(tp)
//                        bm.compress(Bitmap.CompressFormat.JPEG, 100, ot)
//                        ot.close()
//                    } catch (e: Exception) {
//                        Log.d(
//                            "onBtnSavePng",
//                            e.toString()
//                        ) // java.io.IOException: Operation not permitted
//                    }
//                }


//                var aaa = stringBuilder.toString().toByteArray()
//                var bmp: Bitmap = createBitmap(1280, 1024)
//                val wrapper = ContextWrapper(applicationContext)
//
//                // Initializing a new file
//                // The bellow line return a directory in internal storage
//                var file = wrapper.getDir("images", Context.MODE_PRIVATE)
//
//
//                // Create a file to save the image
//                file = File(file, "${UUID.randomUUID()}.jpg")
//
//                try {
//                    // Get the file output stream
//                    val stream: OutputStream = FileOutputStream(file)
//
//                    // Compress bitmap
//                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//
//                    // Flush the stream
//                    stream.flush()
//
//                    // Close stream
//                    stream.close()
//                } catch (e: IOException){ // Catch the exception
//                    e.printStackTrace()
//                }
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
        internal val service: FrontBluetoothLeService
            get() = this@FrontBluetoothLeService
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
//                mBluetoothGatt!!.requestMtu(256)
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
        Log.e("eleutheria", "Front characteristic: BluetoothGattCharacteristic")
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

    fun writeCharacteristic(value: String) {
        Log.e("eleutheria", "send Front Camera Time : $value")
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        /*check if the service is available on the device*/
        val mCustomService =
            mBluetoothGatt!!.
            getService(UUID.fromString(SampleGattAttributes.SERVICE_FRONT_CAMERA))
        if (mCustomService == null) {
            Log.w(TAG, "Custom BLE Service not found")
            return
        }
        /*get the read characteristic from the service*/
        val mWriteCharacteristic =
            mCustomService.getCharacteristic(
                UUID.
                fromString(SampleGattAttributes.CHARACTERISTIC_FRONT_CAMERA))
//        mWriteCharacteristic.value = BigInteger(value, 16).toByteArray()
        val strValue : String = value
        mWriteCharacteristic.value = strValue.toByteArray()
        if (!mBluetoothGatt!!.writeCharacteristic(mWriteCharacteristic)) {
            Log.e("eleutheria", "send fail Front Camera Time : $value")
            Toast.makeText(MyApplication.ApplicationContext(), "Front WriteCharacteristic Fail!", Toast.LENGTH_SHORT).show()
        } else {
            broadcastUpdate(ACTION_GATT_WRITE_SUCCESSED)
        }
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

    fun bleReceiverRequest() {
//        broadcastUpdate(ACTION_GATT_CONNECTED)
//        broadcastUpdate(ACTION_GATT_WRITE_SUCCESSED)
//        Toast.makeText(MyApplication.ApplicationContext(), "WriteCharacteristic Fail!", Toast.LENGTH_SHORT).show()
        Log.w(TAG, "Bluetooth bleReceiverRequest")
    }

    /**
     * Enables or disables notification on a give characteristic.

     * @param characteristic Characteristic to act on.
     * *
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic,
                                      enabled: Boolean) {

        Log.e("eleutheria", "characteristic : ${characteristic.uuid}")
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)

        // This is specific to Heart Rate Measurement.
        Log.e("eleutheria", "Front setCharacteristicNotification characteristic.uuid : ${characteristic.uuid}")
        if (UUID_FRONT_STATE == characteristic.uuid) {
//            Log.e("eleutheria", "strart")
            SystemClock.sleep(5000)
//            Log.e("eleutheria", "end")
            val descriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            mBluetoothGatt!!.writeDescriptor(descriptor)
        }
    }

    fun setMCNotification(enabled: Boolean) {

        var uuidMCCharateristic = SampleGattAttributes.CHARACTERISTIC_FRONT_CAMERA
        var uuidMCService = SampleGattAttributes.SERVICE_FRONT_CAMERA

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
            setCharacteristicNotification(mBluetoothGattCharacteristic, enabled)
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
        private val TAG = FrontBluetoothLeService::class.java.simpleName

        private val STATE_DISCONNECTED = 0
        private val STATE_CONNECTING = 1
        private val STATE_CONNECTED = 2

        val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_GATT_WRITE_SUCCESSED = "com.example.bluetooth.le.ACTION_GATT_WRITE_SUCCESSED"
        val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"


        val UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT)
        val UUID_FRONT_STATE = UUID.fromString(SampleGattAttributes.CHARACTERISTIC_FRONT_CAMERA)
    }
}