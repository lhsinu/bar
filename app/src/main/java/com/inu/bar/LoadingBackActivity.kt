package com.inu.bar

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.inu.bar.base.CommonUtils
import com.inu.bar.base.Constants
import com.inu.bar.bluetooth.BackBluetoothLeService
import com.inu.bar.bluetooth.FrontBluetoothLeService
import com.inu.bar.bluetooth.SampleGattAttributes
import com.inu.bar.databinding.ActivityLoadingBackBinding
import java.util.ArrayList
import java.util.HashMap

class LoadingBackActivity : AppCompatActivity() {
    private  lateinit var binding : ActivityLoadingBackBinding

    private var mBackCamDeviceAddress: String? = null

    var mBluetoothLeService: BackBluetoothLeService? = null
    private var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? =
        ArrayList()
    private var mConnected = false
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null

    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"

    val commonUtils = CommonUtils()

    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BackBluetoothLeService.LocalBinder).service
            if (!mBluetoothLeService!!.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService!!.connect(mBackCamDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BackBluetoothLeService.ACTION_GATT_CONNECTED == action) {
                mConnected = true

            } else if (BackBluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                mConnected = false

            } else if (BackBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService!!.supportedGattServices)
                activeNotification()
                Log.e("eleutheria", "ACTION_GATT_SERVICES_DISCOVERED")

//                val intent = Intent(this@LoadingBackActivity, MainActivity::class.java)
//                startActivity(intent)
            } else if (BackBluetoothLeService.ACTION_DATA_AVAILABLE == action) {
                parsingData(intent.getStringExtra(BackBluetoothLeService.EXTRA_DATA))
//                Log.e("eleutheria", "ACTION_DATA_AVAILABLE")
            } else if(BackBluetoothLeService.ACTION_GATT_WRITE_SUCCESSED == action) {

                val intent = Intent(this@LoadingBackActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoadingBackBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        supportActionBar!!.hide()

        Log.d("eleutheria", "onCreate Back Activity")
        mBackCamDeviceAddress = Constants.MODULE_ADDRESS_BACK_CAM

        Toast.makeText(this, "BackAddress : $mBackCamDeviceAddress", Toast.LENGTH_SHORT).show()

        Constants.curState = Constants.STATE_CONNECT_BACK

        val gattServiceIntent = Intent(this, BackBluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

        binding.ivLogo.setOnClickListener {
            val intent = Intent(this@LoadingBackActivity, SettingDeviceActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onResume() {
        super.onResume()

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mBackCamDeviceAddress)
            Log.d("eleutheria", "Connect request result=" + result)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(mServiceConnection)
        mBluetoothLeService = null

    }

    private fun parsingData(data: String?) {
        if (data != null) {
            Log.e("eleutheria", "Back data : ${data}")

            //System.out.println(data);
        }
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String? = null
        val unknownServiceString = resources.getString(R.string.str_bluetooth_unknown_service)
        val unknownCharaString = resources.getString(R.string.str_bluetooth_unknown_characteristic)
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()
        mGattCharacteristics = ArrayList<ArrayList<BluetoothGattCharacteristic>>()

        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            Log.e("eleutheria", "uuid : " + uuid)
            println(uuid)
            currentServiceData.put(
                LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString)
            )
            currentServiceData.put(LIST_UUID, uuid)
            gattServiceData.add(currentServiceData)

            val gattCharacteristicGroupData = ArrayList<HashMap<String, String>>()
            val gattCharacteristics = gattService.characteristics
            val charas = ArrayList<BluetoothGattCharacteristic>()

            // Loops through available Characteristics.
            for (gattCharacteristic in gattCharacteristics) {
                charas.add(gattCharacteristic)
                val currentCharaData = HashMap<String, String>()
                uuid = gattCharacteristic.uuid.toString()
                println(uuid)
                println(currentCharaData)

                currentCharaData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString)
                )
                currentCharaData.put(LIST_UUID, uuid)
                gattCharacteristicGroupData.add(currentCharaData)
            }
            mGattCharacteristics!!.add(charas)
            gattCharacteristicData.add(gattCharacteristicGroupData)
        }
    }

    private fun activeNotification() {
        if (mGattCharacteristics != null) {
            val characteristic = mGattCharacteristics!![2][0]
            val charaProp = characteristic.properties
            if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService!!.setCharacteristicNotification(
                        mNotifyCharacteristic!!, false)
                    mNotifyCharacteristic = null
                }

                Log.e("eleutheria", "activeNotification - characteristic : ${characteristic.uuid}")
//                mBluetoothLeService!!.readCharacteristic(characteristic)
                mBluetoothLeService!!.writeCharacteristic(commonUtils.getStartTime())
            }

            if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                mNotifyCharacteristic = characteristic
                mBluetoothLeService!!.setCharacteristicNotification(
                    characteristic, true)
            }
        }
    }

    companion object {
        private val TAG = LoadingBackActivity::class.java.getSimpleName()

        private fun makeGattUpdateIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BackBluetoothLeService.ACTION_GATT_CONNECTED)
            intentFilter.addAction(BackBluetoothLeService.ACTION_GATT_DISCONNECTED)
            intentFilter.addAction(BackBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            intentFilter.addAction(FrontBluetoothLeService.ACTION_GATT_WRITE_SUCCESSED)
            intentFilter.addAction(BackBluetoothLeService.ACTION_DATA_AVAILABLE)
            return intentFilter
        }
    }

}