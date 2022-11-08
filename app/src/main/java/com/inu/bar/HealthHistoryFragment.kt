package com.inu.bar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inu.bar.databinding.FragmentHealthHistoryBinding

class HealthHistoryFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentHealthHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHealthHistoryBinding.inflate(inflater, container, false)

        binding.btHealthDay.setOnClickListener{

        }
        binding.btHealthWeek.setOnClickListener{

        }
        binding.btHealthMonth.setOnClickListener{

        }

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }
}