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
import com.inu.bar.base.BluetoothClassicBackManager
import com.inu.bar.base.Constants
import com.inu.bar.databinding.ActivityLoadingBackBinding
import com.inu.bar.db.BarDataStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat

class LoagindBackClassicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoadingBackBinding

    private val mBtHandler = BluetoothHandler()
    private val mBluetoothClassicManager: BluetoothClassicBackManager = BluetoothClassicBackManager.getInstance()
    private var mIsConnected = false

    lateinit var dataStore: BarDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadingBackBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_loagind_back_classic)

        dataStore = BarDataStore.getInstance()

        supportActionBar!!.hide()

        mBluetoothClassicManager.setHandler(mBtHandler)

        binding.ivLogo.setOnClickListener {
            val intent = Intent(this@LoagindBackClassicActivity, SettingDeviceActivity::class.java)
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

    private fun moveMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
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
                Log.e("eleutheria", "address : ${strDeviceAddress}, Back Module : ${Constants.MODULE_ADDRESS_CLASSIC_BACKCAM}")

                if(strDeviceAddress.equals(Constants.MODULE_ADDRESS_CLASSIC_BACKCAM)) {
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
                BluetoothClassicBackManager.MESSAGE_READ -> {
                    if (msg.obj != null) {

                        val readBuf = msg.obj as ByteArray
                        // construct a string from the valid bytes in the buffer
                        val readMessage = String(readBuf, 0, msg.arg1)
//                        Log.e("eleutheria", "MESSAGE_READ_BACK : $readMessage")

//                        sendMessageToActivity(readMessage)

                        if(String(readBuf).contains("PF")) {
                            Constants.bStartReading = Constants.READING_STATE_START
                            Log.e("eleutheria", "BACK_READING_STATE_START : ${Constants.imageDataBack.size}")

                            if(Constants.imageDataBack.size > 0) {
                                val jpeg = Constants.imageDataBack
                                val simpleDateFormat = SimpleDateFormat("yyyyMMdd_hhmmss")
                                val dateString = simpleDateFormat.format(System.currentTimeMillis())
                                val fileName = "Back_" + dateString + ".jpg"
//                    val fileName = System.currentTimeMillis().toString() + ".jpg"
//                    val photo = File(Environment.getExternalStorageDirectory(), "/data/user/0/com.inu.bar/files/driving/$fileName.jpeg");
                                val photo = File(filesDir, fileName);

                                if (photo.exists()) {
                                    photo.delete();
                                }
                                try {
                                    val fos = FileOutputStream(photo.getPath());
                                    fos.write(jpeg)
                                    fos.flush()
                                    fos.close()

//                        Log.e("eleutheria", "Constants.imageData : ${Constants.imageData}")
                                    Constants.imageDataBack = byteArrayOf()
                                    Log.e("eleutheria", "Back file path : " + filesDir.path + "/" + fileName)
                                    dataStore.setCamValue(Constants.DATA_STORE_INDEX_BACK_CAM, filesDir.path + "/" + fileName)
//                        Log.e("eleutheria", "FileOutputStream close : ${Constants.imageData.size}")
                                }
                                catch (e: IOException) {
                                    Log.e("eleutheria", "Exception in photoCallback", e)
                                }
                            }

                        } else if(String(readBuf).contains("!!!///")) {
                            Constants.bStartReading = Constants.READING_STATE_STOP
                            Log.e("eleutheria", "BACK_READING_STATE_STOP")
                            val readMessage = String(readBuf, 0, msg.arg1)

//                        Log.e("eleutheria", "MESSAGE_READ_FRONT: $readMessage")

                            if(readMessage.endsWith("!!!///")){
                                Constants.imageDataBack += readBuf.copyOfRange(0, msg.arg1 - 2)
                            }
//                            val strPartHex = toHexString(readBuf)
//                            Log.e("eleutheria", "strPartHex : $strPartHex, readMessage : $readMessage")
//                    Constants.bStartReading == Constants.READING_STATE_STOP
//                    Log.e("eleutheria", "READING_STATE_STOP + save image")

                            if(Constants.imageDataBack.size > 0) {
                                val jpeg = Constants.imageDataBack
                                val simpleDateFormat = SimpleDateFormat("yyyyMMdd_hhmmss")
                                val dateString = simpleDateFormat.format(System.currentTimeMillis())
                                val fileName = "Back_" + dateString + ".jpg"
//                    val fileName = System.currentTimeMillis().toString() + ".jpg"
//                    val photo = File(Environment.getExternalStorageDirectory(), "/data/user/0/com.inu.bar/files/driving/$fileName.jpeg");
                                val photo = File(filesDir, fileName);

                                if (photo.exists()) {
                                    photo.delete();
                                }
                                try {
                                    val fos = FileOutputStream(photo.getPath());
                                    fos.write(jpeg)
                                    fos.flush()
                                    fos.close()

//                        Log.e("eleutheria", "Constants.imageData : ${Constants.imageData}")
                                    Constants.imageDataBack = byteArrayOf()
                                    Log.e(
                                        "eleutheria",
                                        "Back file path : " + filesDir.path + "/" + fileName
                                    )
                                    dataStore.setCamValue(
                                        Constants.DATA_STORE_INDEX_BACK_CAM,
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

                                Constants.imageDataBack += readBuf.copyOfRange(0, msg.arg1)
//                                Log.e("eleutheria", "Back receive : ${String(readBuf)}")

//                    Log.e("eleutheria", "READING_STATE_START + data")
                            }
                        }
                    }
                }
                BluetoothClassicBackManager.MESSAGE_STATE_CHANGE -> {
                    when(msg.arg1) {
                        BluetoothClassicBackManager.STATE_NONE -> {    // we're doing nothing
                            Log.e("eleutheria", "STATE_NONE")
                            mIsConnected = false
                        }
                        BluetoothClassicBackManager.STATE_LISTEN -> {  // now listening for incoming connections
                            Log.e("eleutheria", "STATE_LISTEN")
                            mIsConnected = false
                        }
                        BluetoothClassicBackManager.STATE_CONNECTING -> {  // connecting to remote
                            Log.e("eleutheria", "STATE_CONNECTING")

                        }
                        BluetoothClassicBackManager.STATE_CONNECTED -> {   // now connected to a remote device
                            Log.e("eleutheria", "STATE_CONNECTED")
                            mIsConnected = true
                            moveMainActivity()
                        }
                    }
                }
                BluetoothClassicBackManager.MESSAGE_DEVICE_NAME -> {
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