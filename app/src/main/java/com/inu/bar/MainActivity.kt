package com.inu.bar

import android.bluetooth.*
import android.content.*
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.inu.bar.base.CommonUtils
import com.inu.bar.base.Constants
import com.inu.bar.base.Constants.Companion.recentData
import com.inu.bar.bluetooth.BluetoothLeService
import com.inu.bar.bluetooth.FrontBluetoothLeService
import com.inu.bar.bluetooth.SampleGattAttributes
import com.inu.bar.databinding.ActivityMainBinding
import com.inu.bar.db.AppDatabase
import com.inu.bar.db.BarDao
import com.inu.bar.db.BarEntity
import java.io.File
import java.util.ArrayList
import java.util.HashMap


class MainActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var binding: ActivityMainBinding

    lateinit var db : AppDatabase
    lateinit var accidentDao: BarDao
    private lateinit var barData: BarEntity
    private lateinit var barList: ArrayList<BarEntity>
    private lateinit var adapter : DataRecyclerViewAdapter
    val commonUtils = CommonUtils()

    private var mDrivingDeviceAddress: String? = null
    private var mFrontCamDeviceAddress: String? = null
    private var mBackCamDeviceAddress: String? = null

//    private var mDeviceName: String? = null
//    private var mDeviceAddress: String? = null
    var mBluetoothLeService: BluetoothLeService? = null
    private var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? =
        ArrayList()
    private var mConnected = false
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null

    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"


    // Front Cam
    var mFrontBluetoothLeService: FrontBluetoothLeService? = null
    private var mFrontGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? =
        ArrayList()
    private var mFrontConnected = false
    private var mFrontNotifyCharacteristic: BluetoothGattCharacteristic? = null

    private val FRONT_LIST_NAME = "NAME"
    private val FRONT_LIST_UUID = "UUID"


    // Back Cam
    var mBackBluetoothLeService: BluetoothLeService? = null
    private var mBackGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? =
        ArrayList()
    private var mBackConnected = false
    private var mBackNotifyCharacteristic: BluetoothGattCharacteristic? = null

    private val BACK_LIST_NAME = "NAME"
    private val BACK_LIST_UUID = "UUID"

    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Constants.curState = Constants.STATE_CONNECT_DRIVING

            Log.d("eleutheria", "mServiceConnection driving")
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
                Log.e("eleutheria", "ACTION_GATT_CONNECTED")

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                mConnected = false
                Log.e("eleutheria", "ACTION_GATT_DISCONNECTED")

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService!!.supportedGattServices)
                Log.e("eleutheria", "ACTION_GATT_SERVICES_DISCOVERED")
                activeNotification()
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == action) {
                parsingData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
                Log.e("eleutheria", "ACTION_DATA_AVAILABLE")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mDrivingDeviceAddress = Constants.MODULE_ADDRESS_DRIVING
//        mFrontCamDeviceAddress = Constants.MODULE_ADDRESS_FRONT_CAM
//        mBackCamDeviceAddress = Constants.MODULE_ADDRESS_BACK_CAM

        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.strMainTitle)

        db = AppDatabase.getInstance(this)!!
        accidentDao = db.getBarDao()
        barData = Constants.recentData

//        Thread {
//            barData = accidentDao.getRecent()
//            Constants.recentData = barData
//
////            Log.e("eleutheria", "date : ${barData.accidentdate}")
//
//            val strDate = commonUtils.convertLongToTime(barData.accidentdate)
//            val spString : List<String> = strDate.split("T")
//
//            if(spString.size > 1) {
//                binding.tvRecentDetail.text = spString[0]
//            } else {
//                binding.tvRecentDetail.text = strDate
//            }
//
//        }.start()


        getAllDataList()


        binding.clRecent.setOnClickListener {
            val intent = Intent(this@MainActivity, AccidentActivity::class.java)
            startActivity(intent)
        }


//        val folderName : String = "driving"
//
//        val fileFileDir : File = filesDir
//        val pathFilePath : String = fileFileDir.path + "/driving"
//
//        val dir : File = File(pathFilePath)
//
//        if(!dir.exists()){
//            dir.mkdirs();
//        }
//
//
//        Log.e("eleutheria", "pathFilePath : $pathFilePath")
//
//        val fileName = pathFilePath + "/writememo1.txt"
//        val myfile = File(fileName)
//
//        val content = "Today snow is falling.\n write multiLine."
//
//        myfile.writeText(content)
//
//        Log.e("eleutheria", "Writed to file")
//        println("Writed to file")

//        Log.d("eleutheria", "bindService driving")
//        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
//        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)

//        Log.d("eleutheria", "bindService Front")
//        val FrontgattServiceIntent = Intent(this, FrontBluetoothLeService::class.java)
//        bindService(FrontgattServiceIntent, mFrontServiceConnection, Context.BIND_AUTO_CREATE)

//        val BackgattServiceIntent = Intent(this, BluetoothLeService::class.java)
//        bindService(BackgattServiceIntent, mBackServiceConnection, Context.BIND_AUTO_CREATE)

//        val transaction = supportFragmentManager.beginTransaction();
//        transaction.replace(R.id.fmFrament, DrivingHistoryFragment())
//        transaction.addToBackStack(null)
//        transaction.commit()

//        val fragment1 = DrivingHistoryFragment()
//        supportFragmentManager
//            .beginTransaction()
//            .add(R.id.clDrivingFragment, fragment1)
//            .commit()

//        binding.vpViewPager.adapter = ViewPagerAdapter(this)
//
//        binding.vpViewPager.registerOnPageChangeCallback (
//            object  : ViewPager2.OnPageChangeCallback() {
//                override fun onPageSelected(position: Int) {
//                    super.onPageSelected(position)
//                    binding.bnBottomNavigationView.menu.getItem(position).isChecked = true
//                }
//            }
//        )
//        binding.bnBottomNavigationView.setOnNavigationItemSelectedListener(this)
//        binding.btDrivingHistory.setOnClickListener(this)
//        binding.btHealthHistory.setOnClickListener(this)
//        binding.btAccidentPicture.setOnClickListener(this)
//        binding.btSetupCall.setOnClickListener(this)


//        Log.e("eleutheria", "densidy : " + resources.displayMetrics.density)
//        Log.e("eleutheria", "densityDpi : " + resources.displayMetrics.densityDpi)
//        Log.e("eleutheria", "scaledDensity : " + resources.displayMetrics.scaledDensity)
//        Log.e("eleutheria", "heightPixels : " + resources.displayMetrics.heightPixels)
//        Log.e("eleutheria", "widthPixels : " + resources.displayMetrics.widthPixels)
//        Log.e("eleutheria", "xdpi : " + resources.displayMetrics.xdpi)
//        Log.e("eleutheria", "ydpi : " + resources.displayMetrics.ydpi)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_option_item, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.menu_call_setting -> {
                val intent = Intent(this@MainActivity, CallSettingActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_room_database -> {
                val intent = Intent(this@MainActivity, DatabaseActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getRecentData() {
        Thread {
            barData = accidentDao.getRecent()
            Constants.recentData = barData

//            Log.e("eleutheria", "date : ${barData.accidentdate}")

            val strDate = commonUtils.convertLongToTime(barData.accidentdate)
            val spString : List<String> = strDate.split("T")

            if(spString.size > 1) {
                binding.tvRecentDetail.text = spString[0]
            } else {
                binding.tvRecentDetail.text = strDate
            }

        }.start()
    }

    private fun getAllDataList() {

        var strDate = ""
        var spString : List<String> = emptyList()

        Thread {
            var barRecentData: BarEntity = accidentDao.getRecent()
            if(barRecentData != null) {
                barData = barRecentData
                Constants.recentData = barData
                strDate = commonUtils.convertLongToTime(barData.accidentdate)
                spString = strDate.split("T")
            }

//            Log.e("eleutheria", "date : ${barData.accidentdate}")



            runOnUiThread {
                if(spString.size > 1) {
                    binding.tvRecentDetail.text = spString[0]
                } else {
                    binding.tvRecentDetail.text = strDate
                }
            }


            barList = ArrayList(accidentDao.getAll())
            setRecyclerView()
        }.start()
    }

    private fun setRecyclerView() {
        runOnUiThread {
            adapter = DataRecyclerViewAdapter(barList, this)
            binding.rvPastData.adapter = adapter
            binding.rvPastData.layoutManager = LinearLayoutManager(this)
        }
    }

    override fun onResume() {
        super.onResume()

//        Log.d("eleutheria", "onResume driving")
//        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
//        if (mBluetoothLeService != null) {
//            val result = mBluetoothLeService!!.connect(mDrivingDeviceAddress)
//            Log.d("eleutheria", "Connect request result=" + result)
//        }

//        Log.d("eleutheria", "onResume Front")
//        registerReceiver(mFrontGattUpdateReceiver, makeGattUpdateIntentFilter())
//        if (mFrontBluetoothLeService != null) {
//            val result = mFrontBluetoothLeService!!.connect(mFrontCamDeviceAddress)
//            Log.d("eleutheria", "Front Connect request result=" + result)
//        }

//        registerReceiver(mBackGattUpdateReceiver, makeGattUpdateIntentFilter())
//        if (mBackBluetoothLeService != null) {
//            val result = mBackBluetoothLeService!!.connect(mBackCamDeviceAddress)
//            Log.d("eleutheria", "Back Connect request result=" + result)
//        }
    }

    override fun onPause() {
        super.onPause()
//        unregisterReceiver(mGattUpdateReceiver)
//        unregisterReceiver(mFrontGattUpdateReceiver)
//        unregisterReceiver(mBackGattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()

//        unbindService(mServiceConnection)
//        mBluetoothLeService = null

//        unbindService(mFrontServiceConnection)
//        mFrontBluetoothLeService = null

//        unbindService(mFrontServiceConnection)
//        mBackBluetoothLeService = null
    }

    override fun onRestart() {
        super.onRestart()
        getAllDataList()
    }

    override fun onDataItemClick(position: Int) {
        Constants.recentData = barList[position]
        val intent = Intent(this@MainActivity, AccidentActivity::class.java)
        startActivity(intent)
    }

    private fun parsingData(data: String?) {
        if (data != null) {
            Log.e("eleutheria", "Main data : ${data}")

            //System.out.println(data);
        }
    }

    private fun FrontparsingData(data: String?) {
        if (data != null) {
            Log.e("eleutheria", "Main Front data : ${data}")

            //System.out.println(data);
        }
    }

    private fun BackparsingData(data: String?) {
        if (data != null) {
            Log.e("eleutheria", "Main Back data : ${data}")

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
//                    mBluetoothLeService!!.setCharacteristicNotification(
//                        mNotifyCharacteristic!!, false)
                    mNotifyCharacteristic = null
                }

                Log.e("eleutheria", "activeNotification")
                mBluetoothLeService!!.readCharacteristic(characteristic)
            }

            if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                mNotifyCharacteristic = characteristic
//                mBluetoothLeService!!.setCharacteristicNotification(
//                    characteristic, true)
            }
        }
    }

    // Front
    private val mFrontServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Constants.curState = Constants.STATE_CONNECT_FRONT
            Log.e("eleutheria", "mFrontServiceConnection")
            mFrontBluetoothLeService = (service as FrontBluetoothLeService.LocalBinder).service
            if (!mFrontBluetoothLeService!!.initialize()) {
                Log.e(TAG, "Front Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mFrontBluetoothLeService!!.connect(mFrontCamDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mFrontBluetoothLeService = null
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val mFrontGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (FrontBluetoothLeService.ACTION_GATT_CONNECTED == action) {
                mFrontConnected = true

            } else if (FrontBluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                mFrontConnected = false

            } else if (FrontBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                FrontdisplayGattServices(mFrontBluetoothLeService!!.supportedGattServices)
                FrontactiveNotification()
            } else if (FrontBluetoothLeService.ACTION_DATA_AVAILABLE == action) {
                FrontparsingData(intent.getStringExtra(FrontBluetoothLeService.EXTRA_DATA))
            }
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private fun FrontdisplayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String? = null
        val unknownServiceString = resources.getString(R.string.str_bluetooth_unknown_service)
        val unknownCharaString = resources.getString(R.string.str_bluetooth_unknown_characteristic)
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()
        mFrontGattCharacteristics = ArrayList<ArrayList<BluetoothGattCharacteristic>>()

        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            Log.e("eleutheria", "Front uuid : " + uuid)
            println(uuid)
            currentServiceData.put(
                FRONT_LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString)
            )
            currentServiceData.put(FRONT_LIST_UUID, uuid)
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
                    FRONT_LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString)
                )
                currentCharaData.put(FRONT_LIST_UUID, uuid)
                gattCharacteristicGroupData.add(currentCharaData)
            }
            mFrontGattCharacteristics!!.add(charas)
            gattCharacteristicData.add(gattCharacteristicGroupData)
        }
    }

    private fun FrontactiveNotification() {
        if (mFrontGattCharacteristics != null) {
            val characteristic = mFrontGattCharacteristics!![2][0]
            val charaProp = characteristic.properties
            if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mFrontNotifyCharacteristic != null) {
                    mFrontBluetoothLeService!!.setCharacteristicNotification(
                        mFrontNotifyCharacteristic!!, false)
                    mFrontNotifyCharacteristic = null
                }
                mFrontBluetoothLeService!!.readCharacteristic(characteristic)
            }

            if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                mFrontNotifyCharacteristic = characteristic
                mFrontBluetoothLeService!!.setCharacteristicNotification(
                    characteristic, true)
            }
        }
    }


    // Back
    private val mBackServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBackBluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            if (!mBackBluetoothLeService!!.initialize()) {
                Log.e(TAG, "Front Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBackBluetoothLeService!!.connect(mBackCamDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBackBluetoothLeService = null
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val mBackGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
                mBackConnected = true

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                mBackConnected = false

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                BackdisplayGattServices(mBackBluetoothLeService!!.supportedGattServices)
//                BackactiveNotification()
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == action) {
                BackparsingData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
            }
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private fun BackdisplayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String? = null
        val unknownServiceString = resources.getString(R.string.str_bluetooth_unknown_service)
        val unknownCharaString = resources.getString(R.string.str_bluetooth_unknown_characteristic)
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()
        mBackGattCharacteristics = ArrayList<ArrayList<BluetoothGattCharacteristic>>()

        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            Log.e("eleutheria", "Back uuid : " + uuid)
            println(uuid)
            currentServiceData.put(
                BACK_LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString)
            )
            currentServiceData.put(BACK_LIST_UUID, uuid)
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
                    BACK_LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString)
                )
                currentCharaData.put(BACK_LIST_UUID, uuid)
                gattCharacteristicGroupData.add(currentCharaData)
            }
            mBackGattCharacteristics!!.add(charas)
            gattCharacteristicData.add(gattCharacteristicGroupData)
        }
    }

    private fun BackactiveNotification() {
        if (mBackGattCharacteristics != null) {
            val characteristic = mBackGattCharacteristics!![2][0]
            val charaProp = characteristic.properties
            if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mBackNotifyCharacteristic != null) {
//                    mBackBluetoothLeService!!.setCharacteristicNotification(
//                        mBackNotifyCharacteristic!!, false)
                    mBackNotifyCharacteristic = null
                }
                mBackBluetoothLeService!!.readCharacteristic(characteristic)
            }

            if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                mBackNotifyCharacteristic = characteristic
//                mBackBluetoothLeService!!.setCharacteristicNotification(
//                    characteristic, true)
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



    //    override fun onClick(v: View?) {
//        if (v != null) {
//            when(v.id) {
//                binding.btDrivingHistory.id -> {
//                    Log.e("eleutheria", "Click btDrivingHistory")
//                }
//
//                binding.btHealthHistory.id -> {
//                    Log.e("eleutheria", "Click btHealthHistory")
//                }
//
//                binding.btAccidentPicture.id -> {
//                    Log.e("eleutheria", "Click btAccidentPicture")
//                }
//
//                binding.btSetupCall.id -> {
//                    Log.e("eleutheria", "Click btSetupCall")
//                }
//            }
//        }
//    }

//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        when(item.itemId) {
//            R.id.menu_driving_history -> {
////                binding.vpViewPager.currentItem = 0
//                val transaction = supportFragmentManager.beginTransaction();
//                transaction.replace(R.id.fmFrament, DrivingHistoryFragment())
//                transaction.addToBackStack(null)
//                transaction.commit()
//                return true
//            }
//            R.id.menu_health_history -> {
////                binding.vpViewPager.currentItem = 1
//                val transaction = supportFragmentManager.beginTransaction();
//                transaction.replace(R.id.fmFrament, HealthHistoryFragment())
//                transaction.addToBackStack(null)
//                transaction.commit()
//                return true
//            }
//            R.id.menu_accident_history -> {
////                binding.vpViewPager.currentItem = 2
//                val transaction = supportFragmentManager.beginTransaction();
//                transaction.replace(R.id.fmFrament, AccidentHistoryFragment())
//                transaction.addToBackStack(null)
//                transaction.commit()
//                return true
//            }
//            R.id.menu_call_setting -> {
////                binding.vpViewPager.currentItem = 3
//                val transaction = supportFragmentManager.beginTransaction();
//                transaction.replace(R.id.fmFrament, CallSettingFragment())
//                transaction.addToBackStack(null)
//                transaction.commit()
//                return true
//            }
//            else -> {
//                return false
//            }
//
//        }
//    }
}