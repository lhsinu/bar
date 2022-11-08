package com.inu.bar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inu.bar.databinding.FragmentCallSettingBinding

class CallSettingFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentCallSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallSettingBinding.inflate(inflater, container, false)

        binding.btContact112.setOnClickListener {
            binding.tv112.text = getString(R.string.strNumber112)
        }
        binding.btSetting112.setOnClickListener {


        }
        binding.btContact119.setOnClickListener {
            binding.tv112.text = getString(R.string.strNumber119)
        }
        binding.btSetting119.setOnClickListener {

        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
}