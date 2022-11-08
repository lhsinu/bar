package com.inu.bar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.inu.bar.databinding.FragmentAccidentHistoryBinding

class DrivingHistoryFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentAccidentHistoryBinding
    private val dataSet: ArrayList<String> = arrayListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.fragment_driving_history, container, false)
//        addData()


        return binding.rootView
//        return inflater.inflate(R.layout.fragment_driving_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    private fun addData() {
        for (i in 0..99) {
            dataSet.add("2022 - 03 - $i Driving History")
        }
    }
}