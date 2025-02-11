package com.atarusov.avitotest.features.apitracks.presentation

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.atarusov.avitotest.base.BaseTrackListFragment

class ApiTrackListFragment : BaseTrackListFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.trackRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.trackRecyclerView.adapter = ApiTrackAdapter()
    }
}