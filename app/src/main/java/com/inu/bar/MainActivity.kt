package com.inu.bar

import android.bluetooth.*
import android.content.*
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.inu.bar.base.CommonUtils
import com.inu.bar.base.Constants
import com.inu.bar.bluetooth.BluetoothLeService
import com.inu.bar.bluetooth.FrontBluetoothLeService
import com.inu.bar.bluetooth.SampleGattAttributes
import com.inu.bar.databinding.ActivityMainBinding
import com.inu.bar.db.AppDatabase
import com.inu.bar.db.BarDao
import com.inu.bar.db.BarDataStore
import com.inu.bar.db.BarEntity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.text.SimpleDateFormat
import kotlin.concurrent.thread
import kotlin.experimental.and


class MainActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var binding: ActivityMainBinding

    lateinit var dataStore: BarDataStore

    lateinit var db : AppDatabase
    lateinit var accidentDao: BarDao
    private lateinit var barData: BarEntity
    private lateinit var barList: ArrayList<BarEntity>
    private lateinit var adapter : DataRecyclerViewAdapter
    val commonUtils = CommonUtils()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.strMainTitle)

        db = AppDatabase.getInstance(this)!!
        accidentDao = db.getBarDao()
        barData = Constants.recentData
        dataStore = BarDataStore.getInstance()

//        var strStart : String = "str"
//        var baByteArray : ByteArray = strStart.toByteArray()
//
//        Log.e("eleutheria", "baByteArray : ${String(baByteArray)}")

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

        startClient()

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

    fun startClient() {
        thread {
            try {
                val wifiManager =
                    applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                val ipAddress = "192.168.4.1" // Replace with the IP address you want to connect to
                val portNumber = 80 // Replace with the port number you want to connect to

                // Connect to the Wi-Fi network
                if (!wifiManager.isWifiEnabled) {
                    wifiManager.isWifiEnabled = true
                }

                // Create a socket and connect to the IP address and port number
                val socket = Socket()
                val socketAddress = InetSocketAddress(ipAddress, portNumber)
                socket.connect(socketAddress, 5000) // Timeout after 5000 milliseconds


                // Send data over the socket
//        val outputStream = socket.getOutputStream()
//        outputStream.write("Hello, World!".toByteArray())
                val digits = "0123456789ABCDEF"
                var strResult = "result"
                var strResultByte = "resultByte"

                while(true) {
                    // Receive data from the socket
                    val inputStream = socket.getInputStream()
                    val buffer = ByteArray(1024)

                    val bytesRead = inputStream.read(buffer)
                    val byteData = if (bytesRead != buffer.size) buffer.copyOfRange(0, bytesRead) else buffer
                    val data = String(buffer, 0, bytesRead)


//                    val hexChars = CharArray(buffer.size * 2)
//                    for (i in buffer.indices) {
//                        val v = buffer[i].toInt() and 0xff
//                        hexChars[i * 2] = digits[v shr 4]
//                        hexChars[i * 2 + 1] = digits[v and 0xf]
//                    }
//                    strResult = String(hexChars)

//                    Log.e("eleutheria", "strResult : $strResult")
//                    Log.e("eleutheria", "data : $data")


//                    val hexChars2 = CharArray(byteData.size * 2)
//                    for (i in byteData.indices) {
//                        val v = byteData[i].toInt() and 0xff
//                        hexChars2[i * 2] = digits[v shr 4]
//                        hexChars2[i * 2 + 1] = digits[v and 0xf]
//                    }
//                    strResultByte = String(hexChars2)
//                    Log.e("eleutheria", "strResultByte : $strResultByte")
//                    Log.e("eleutheria", "data : $data")

//                    commonUtils.writeTextToFile(strResult)


                    val arSpData = byteData.copyOfRange(0, 3)
                    if(commonUtils.compareStartBytes(arSpData)) {
                        if(Constants.bStartReading == Constants.READING_STATE_READY) {

                            Constants.bStartReading = Constants.READING_STATE_BACK

                            Constants.imageDataBack += byteData
                        } else {
                            Constants.bStartReading = Constants.READING_STATE_FRONT
                            Log.e("eleutheria", "1BACK_READING_STATE_START : ${Constants.imageDataBack.size}")

                            Constants.imageDataFront += byteData

                            if(Constants.imageDataBack.size > 0) {
                                Thread(Runnable {
                                    val jpeg = Constants.imageDataBack
                                    val simpleDateFormat = SimpleDateFormat("yyyyMMdd_hhmmss")
                                    val dateString = simpleDateFormat.format(System.currentTimeMillis())
                                    val fileName = "Back_" + dateString + ".jpg"
                                    val photo = File(filesDir, fileName)

                                    if (photo.exists()) {
                                        photo.delete()
                                    }
                                    try {
                                        val fos = FileOutputStream(photo.getPath())
                                        fos.write(jpeg)
                                        fos.flush()
                                        fos.close()

                                        Constants.imageDataBack = byteArrayOf()
                                        Log.e("eleutheria", "2Back file path : " + filesDir.path + "/" + fileName)
                                        dataStore.setCamValue(Constants.DATA_STORE_INDEX_BACK_CAM, filesDir.path + "/" + fileName)
                                    }
                                    catch (e: IOException) {
                                        Log.e("eleutheria", "Exception in photoCallback", e)
                                    } }).start()
                            }
                        }
                    } else if(commonUtils.compareMiddleBytes(arSpData)) {
                        Constants.bStartReading = Constants.READING_STATE_READY

                        if(byteData.size > 5) {
                            val arSpMidData = byteData.copyOfRange(3, 6)
                            if(commonUtils.compareStartBytes(arSpMidData)) {
                                Constants.bStartReading = Constants.READING_STATE_BACK
                                Log.e("eleutheria", "2FRONT_READING_STATE_START : ${Constants.imageDataFront.size}")

                                Constants.imageDataBack += byteData.copyOfRange(
                                    3,
                                    byteData.size
                                )

                                if(Constants.imageDataFront.size > 0) {
                                    Thread(Runnable {
                                        val jpeg = Constants.imageDataFront
                                        val simpleDateFormat = SimpleDateFormat("yyyyMMdd_hhmmss")
                                        val dateString =
                                            simpleDateFormat.format(System.currentTimeMillis())
                                        val fileName = "Front_" + dateString + ".jpg"
                                        val photo = File(filesDir, fileName);

                                        if (photo.exists()) {
                                            photo.delete()
                                        }
                                        try {
                                            val fos = FileOutputStream(photo.getPath())
                                            fos.write(jpeg)
                                            fos.flush()
                                            fos.close()

                                            Constants.imageDataFront = byteArrayOf()
                                            Log.e(
                                                "eleutheria",
                                                "3Front file path : " + filesDir.path + "/" + fileName
                                            )
                                            dataStore.setCamValue(
                                                Constants.DATA_STORE_INDEX_FRONT_CAM,
                                                filesDir.path + "/" + fileName
                                            )
                                        } catch (e: IOException) {
                                            Log.e("eleutheria", "Exception in photoCallback", e)
                                        }
                                    }).start()
                                }
                            }
                        } else {
                            Log.e("eleutheria", "5FRONT_READING_STATE_START : ${Constants.imageDataFront.size}")

                            if(Constants.imageDataFront.size > 0) {
                                Thread(Runnable {
                                    val jpeg = Constants.imageDataFront
                                    val simpleDateFormat = SimpleDateFormat("yyyyMMdd_hhmmss")
                                    val dateString =
                                        simpleDateFormat.format(System.currentTimeMillis())
                                    val fileName = "Front_" + dateString + ".jpg"
                                    val photo = File(filesDir, fileName);

                                    if (photo.exists()) {
                                        photo.delete()
                                    }
                                    try {
                                        val fos = FileOutputStream(photo.getPath())
                                        fos.write(jpeg)
                                        fos.flush()
                                        fos.close()

                                        Constants.imageDataFront = byteArrayOf()
                                        Log.e(
                                            "eleutheria",
                                            "4Front file path : " + filesDir.path + "/" + fileName
                                        )
                                        dataStore.setCamValue(
                                            Constants.DATA_STORE_INDEX_FRONT_CAM,
                                            filesDir.path + "/" + fileName
                                        )
                                    } catch (e: IOException) {
                                        Log.e("eleutheria", "Exception in photoCallback", e)
                                    }
                                }).start()
                            }
                        }
                    } else {
                        Log.e("eleutheria", "bStartReading : ${Constants.bStartReading}")
                        if(Constants.bStartReading == Constants.READING_STATE_FRONT) {
                            Constants.imageDataFront += byteData
//                            Log.e("eleutheria", "Front receive : ${String(buffer)}")
                        } else if(Constants.bStartReading == Constants.READING_STATE_BACK) {
                            Constants.imageDataBack += byteData
//                            Log.e("eleutheria", "Back receive : ${String(buffer)}")
                        }
                    }
                }

                // Close the socket
                socket.close()
            } catch (e: Exception) {
                Log.e("eleutheria", "Exception : $e")

            } catch (e: SocketException) {
                Log.e("eleutheria", "SocketException : $e")

            }
        }
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
            val barRecentData: BarEntity = accidentDao.getRecent()
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
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
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

    companion object {
        private val TAG = MainActivity::class.java.getSimpleName()

    }
}