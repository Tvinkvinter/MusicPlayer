package com.atarusov.avitotest.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.atarusov.avitotest.databinding.FragmentBaseTrackListBinding

class BaseTrackListFragment : Fragment() {

    private var _binding: FragmentBaseTrackListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBaseTrackListBinding.inflate(inflater, container, false)
        return binding.root
    }
}