package com.inu.bar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.inu.bar.databinding.FragmentAccidentHistoryBinding

class AccidentHistoryFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentAccidentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_accident_history, container, false)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

}