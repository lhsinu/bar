package com.inu.bar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.SyncStateContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.inu.bar.base.Constants
import com.inu.bar.base.Constants.Companion.PREF_112_CALL_NUMBER
import com.inu.bar.databinding.ActivityCallSettingBinding

class CallSettingActivity : AppCompatActivity()  {
    private  lateinit var binding : ActivityCallSettingBinding
    private var settings: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCallSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = getString(R.string.strSettingTitle)

        settings = getSharedPreferences(Constants.SHARED_PREF_SEUPDATA, Context.MODE_PRIVATE)

        Log.e("eleutheria", "Setting str119Number : ${Constants.str119Number}, str112Number : ${Constants.str112Number}")

        binding.tv119.text = settings!!.getString(Constants.PREF_119_CALL_NUMBER, Constants.str119Number)
        binding.tv112.text = settings!!.getString(Constants.PREF_112_CALL_NUMBER, Constants.str112Number)

        Log.e("eleutheria", "Setting Next str119Number : ${Constants.str119Number}, str112Number : ${Constants.str112Number}")

//        binding.tv119.text = Constants.str119Number
//        binding.tv112.text = Constants.str112Number

        binding.btContact119.setOnClickListener {
            val contactIntent = Intent(Intent.ACTION_PICK)
            contactIntent.data = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            startActivityForResult(contactIntent, 0)
        }
        binding.btSetting119.setOnClickListener {
            binding.tv119.text = getString(R.string.strNumber119)
        }
        binding.btContact112.setOnClickListener {
            val contactIntent = Intent(Intent.ACTION_PICK)
            contactIntent.data = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            startActivityForResult(contactIntent, 1)
        }
        binding.btSetting112.setOnClickListener {
            binding.tv112.text = getString(R.string.strNumber112)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {

            val cursor = contentResolver.query(
                data!!.getData()!!,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ), null, null, null
            )
            cursor!!.moveToFirst()
            val name = cursor.getString(0)        //이름 얻어오기
            val number = cursor.getString(1)

            if(requestCode == 0) {
                Constants.str119Number = number
                binding.tv119.text = Constants.str119Number

                val editor = settings!!.edit()
                editor.putString(Constants.PREF_119_CALL_NUMBER, Constants.str119Number)
                editor.apply()

            } else if(requestCode == 1) {
                Constants.str112Number = number
                binding.tv112.text = Constants.str112Number

                val editor = settings!!.edit()
                editor.putString(Constants.PREF_112_CALL_NUMBER, Constants.str112Number)
                editor.apply()
            }
        }
    }
}