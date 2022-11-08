package com.inu.bar

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.inu.bar.base.BluetoothClassicManager
import com.inu.bar.base.Constants
import com.inu.bar.databinding.ActivityLoadingFrontClassicBinding
import com.inu.bar.db.BarDataStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat

class LoadingFrontClassicActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoadingFrontClassicBinding

    private val mBtHandler = BluetoothHandler()
    private val mBluetoothClassicManager: BluetoothClassicManager = BluetoothClassicManager.getInstance()
    private var mIsConnected = false
    private var mIsLastSymbol = false

    lateinit var dataStore: BarDataStore

    private val digits = "0123456789ABCDEF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingFrontClassicBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_loading_front_classic)
        dataStore = BarDataStore.getInstance()

        supportActionBar!!.hide()

        mBluetoothClassicManager.setHandler(mBtHandler)

        binding.ivLogo.setOnClickListener {
            val intent = Intent(this@LoadingFrontClassicActivity, SettingDeviceActivity::class.java)
            startActivity(intent)
        }

        // Register for broadcasts when a device is discovered
        var filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mReceiver, filter)

        // Register for broadcasts when discovery has finished
        filter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        this.registerReceiver(mReceiver, filter)


        // Register for broadcasts when a device is discovered
        filter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        this.registerReceiver(mReceiver, filter)

        doDiscovery()
    }

    private fun doDiscovery() {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true)

        // If we're already discovering, stop it
        if (mBluetoothClassicManager.isDiscovering()) {
            mBluetoothClassicManager.cancelDiscovery()
        }

        // Request discover from BluetoothAdapter
        mBluetoothClassicManager.startDiscovery()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Make sure we're not doing discovery anymore
        mBluetoothClassicManager.cancelDiscovery()
        mBluetoothClassicManager.stop()
        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver)
    }

    private fun moveBackActivity() {
//        val intent = Intent(this, MainActivity::class.java)
        val intent = Intent(this, LoagindBackClassicActivity::class.java)
        startActivity(intent)
    }


        private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                var strDeviceAddress = device!!.address
                Constants.strDeviceAddress = strDeviceAddress
                Log.e("eleutheria", "address : ${strDeviceAddress}, Front Module : ${Constants.MODULE_ADDRESS_CLASSIC_FRONTCAM}")

                if(strDeviceAddress.equals(Constants.MODULE_ADDRESS_CLASSIC_FRONTCAM)) {
                    Log.e("eleutheria", "find device Oximetry")
                    if(mBluetoothClassicManager.state != 2 ) {
                        mBluetoothClassicManager.connect(Constants.strDeviceAddress)
                    }
                }
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
            }

            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED == action) {
                val scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, -1)
                val prevMode = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, -1)
                when(scanMode) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
                        mBluetoothClassicManager.start()
                        Log.e("eleutheria", "SCAN_MODE_CONNECTABLE_DISCOVERABLE")
                    }
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE -> {
                        Log.e("eleutheria", "SCAN_MODE_CONNECTABLE")
                    }
                    BluetoothAdapter.SCAN_MODE_NONE -> {
                        // Bluetooth is not enabled
                        Log.e("eleutheria", "SCAN_MODE_NONE")
                    }
                }
            }
        }
    }

    inner class BluetoothHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BluetoothClassicManager.MESSAGE_READ -> {
                    if (msg.obj != null) {

                        val readBuf = msg.obj as ByteArray
                        // construct a string from the valid bytes in the buffer
                        val readMessage = String(readBuf, 0, msg.arg1)

//                        Log.e("eleutheria", "MESSAGE_READ_FRONT: $readMessage")

//                        sendMessageToActivity(readMessage)
//                        val strPartHex = toHexString(readBuf)
//                        Log.e("eleutheria", "strPartHex : $strPartHex")

                        if(String(readBuf).contains("PF")) {
                            Constants.bStartReading = Constants.READING_STATE_START
                            Log.e("eleutheria", "FRONT_READING_STATE_START : ${Constants.imageDataFront.size}")

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
                                    Byte
                                    fos.write(jpeg);
                                    fos.flush()
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

                        } else if(String(readBuf).contains("!!!///")) {
                            Constants.bStartReading = Constants.READING_STATE_STOP
                            Log.e("eleutheria", "FRONT_READING_STATE_STOP : $readBuf")
                            val readMessage = String(readBuf, 0, msg.arg1)

//                        Log.e("eleutheria", "MESSAGE_READ_FRONT: $readMessage")

                            if(readMessage.endsWith("!!!///")){
                                Constants.imageDataFront += readBuf.copyOfRange(0, msg.arg1 - 2)
                            }
//                            val strPartHex = toHexString(readBuf)
//                            Log.e("eleutheria", "strPartHex : $strPartHex, readMessage : $readMessage")
                            mIsLastSymbol = true
//                    Constants.bStartReading == Constants.READING_STATE_STOP
//                    Log.e("eleutheria", "READING_STATE_STOP + save image")

                            if(Constants.imageDataFront.size > 0) {
                                val jpeg = Constants.imageDataFront
                                val simpleDateFormat = SimpleDateFormat("yyyyMMdd_hhmmss")
                                val dateString = simpleDateFormat.format(System.currentTimeMillis())
                                val fileName = "Front_" + dateString + ".jpg"
//                    val fileName = System.currentTimeMillis().toString() + ".jpg"
//                    val photo = File(Environment.getExternalStorageDirectory(), "/data/user/0/com.inu.bar/files/driving/$fileName.jpeg");
                                val photo = File(filesDir, fileName)

                                if (photo.exists()) {
                                    photo.delete()
                                }
                                try {
                                    val fos = FileOutputStream(photo.getPath())
                                    fos.write(jpeg)
                                    fos.flush()
                                    fos.close()

//                        Log.e("eleutheria", "Constants.imageData : ${Constants.imageDataFront}")

//                                    val hexChars = CharArray(Constants.imageDataFront.size * 2)
//                                    for(i in Constants.imageDataFront.indices) {
//                                        val v = Constants.imageDataFront[i].toInt() and 0xff
//                                        hexChars[i * 2] = digits[v shr 4]
//                                        hexChars[i * 2 + 1] = digits[v and 0xff]
//                                    }
//                                    Log.e("eleutheria", "hexChars : ${hexChars.toString()}")

//                                    val strResultHex = toHexString(Constants.imageDataFront)
//                                    Log.e("eleutheria", "strResultHex : $strResultHex")

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

                                Constants.imageDataFront += readBuf.copyOfRange(0, msg.arg1)
//                                if(mIsLastSymbol) {
//                                    Constants.imageDataFront += readBuf.copyOfRange(0, msg.arg1 - 1)
//                                    mIsLastSymbol = false
//                                } else {
//                                    Constants.imageDataFront += readBuf.copyOfRange(0, msg.arg1)
//                                }
//                                Log.e("eleutheria", "Front receive : ${String(readBuf)}")

//                    Log.e("eleutheria", "READING_STATE_START + data")
                            }
                        }
                    }
                }
                BluetoothClassicManager.MESSAGE_STATE_CHANGE -> {
                    when(msg.arg1) {
                        BluetoothClassicManager.STATE_NONE -> {    // we're doing nothing
                            Log.e("eleutheria", "STATE_NONE")
                            mIsConnected = false
                        }
                        BluetoothClassicManager.STATE_LISTEN -> {  // now listening for incoming connections
                            Log.e("eleutheria", "STATE_LISTEN")
                            mIsConnected = false
                        }
                        BluetoothClassicManager.STATE_CONNECTING -> {  // connecting to remote
                            Log.e("eleutheria", "STATE_CONNECTING")

                        }
                        BluetoothClassicManager.STATE_CONNECTED -> {   // now connected to a remote device
                            Log.e("eleutheria", "STATE_CONNECTED")
                            mIsConnected = true
                            moveBackActivity()
                        }
                    }
                }
                BluetoothClassicManager.MESSAGE_DEVICE_NAME -> {
                    if(msg.data != null) {
                        Log.e("eleutheria", "MESSAGE_DEVICE_NAME")
                    }
                }
            }

            super.handleMessage(msg)
        }
    }

    fun toHexString(byteArray: ByteArray): String {
        val sbx = StringBuilder()
        for (i in byteArray.indices) {
            sbx.append(String.format("%02X", byteArray[i]))
        }
        return sbx.toString()
    }
}