package com.inu.bar

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.inu.bar.base.Constants
import com.inu.bar.bluetooth.BluetoothLeService
import com.inu.bar.bluetooth.SampleGattAttributes
import com.inu.bar.databinding.ActivityLoadingDrivingBinding
import java.util.ArrayList
import java.util.HashMap

class LoadingDrivingActivity : AppCompatActivity() {
    private  lateinit var binding : ActivityLoadingDrivingBinding

    private var mDrivingDeviceAddress: String? = null

    var mBluetoothLeService: BluetoothLeService? = null
    private var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? =
        ArrayList()
    private var mConnected = false
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null

    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"
    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            if (!mBluetoothLeService!!.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService!!.connect(mDrivingDeviceAddress)
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
            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
                mConnected = true

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                mConnected = false

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService!!.supportedGattServices)
                activeNotification()
                Log.e("eleutheria", "ACTION_GATT_SERVICES_DISCOVERED")

//                val intent = Intent(this@LoadingDrivingActivity, LoadingFrontActivity::class.java)
                val intent = Intent(this@LoadingDrivingActivity, LoadingFrontClassicActivity::class.java)
//                val intent = Intent(this@LoadingDrivingActivity, MainActivity::class.java)
                startActivity(intent)
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == action) {
                parsingData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
//                Log.e("eleutheria", "ACTION_DATA_AVAILABLE")
//                val intent = Intent(this@LoadingDrivingActivity, LoadingFrontActivity::class.java)
//                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoadingDrivingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar!!.hide()

        Log.e("eleutheria", "MODULE_ADDRESS_DRIVING : ${Constants.MODULE_ADDRESS_DRIVING}")
        mDrivingDeviceAddress = Constants.MODULE_ADDRESS_DRIVING

        Toast.makeText(this, "DrivingAddress : $mDrivingDeviceAddress", Toast.LENGTH_SHORT).show()

        Constants.curState = Constants.STATE_CONNECT_DRIVING

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

        binding.tvLoading.setOnClickListener {
            val intent = Intent(this@LoadingDrivingActivity, MainActivity::class.java)
            startActivity(intent)
        }

        binding.ivLogo.setOnClickListener {
            val intent = Intent(this@LoadingDrivingActivity, SettingDeviceActivity::class.java)
            startActivity(intent)
        }




    }


    override fun onResume() {
        super.onResume()

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result = mBluetoothLeService!!.connect(mDrivingDeviceAddress)
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
            Log.e("eleutheria", "driving data : ${data}")
//          G:0!/
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
                    mBluetoothLeService!!.setDrivingCharacteristicNotification(
                        mNotifyCharacteristic!!, false)
                    mNotifyCharacteristic = null
                }

                Log.e("eleutheria", "activeNotification")
//                mBluetoothLeService!!.readCharacteristic(characteristic)
            }

            if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {

                Log.e("eleutheria", "charaProp : $charaProp, notify : ${BluetoothGattCharacteristic.PROPERTY_NOTIFY}")
                mNotifyCharacteristic = characteristic
                mBluetoothLeService!!.setDrivingCharacteristicNotification(
                    characteristic, true)
            }
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.getSimpleName()

        private fun makeGattUpdateIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
            return intentFilter
        }
    }

}