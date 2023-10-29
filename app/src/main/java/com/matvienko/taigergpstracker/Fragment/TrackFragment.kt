package com.matvienko.taigergpstracker.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.matvienko.taigergpstracker.DB.TrackAdaptor
import com.matvienko.taigergpstracker.DB.TrackItem
import com.matvienko.taigergpstracker.MainApp
import com.matvienko.taigergpstracker.MainViewModel
import com.matvienko.taigergpstracker.databinding.TrackBinding
import com.matvienko.taigergpstracker.utils.openFragment


class TrackFragment : Fragment(), TrackAdaptor.Listener {
    private lateinit var binding: TrackBinding
    private lateinit var adapter: TrackAdaptor
    private val model: MainViewModel by activityViewModels {
        MainViewModel.ViewModelFactory(
            (requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        getTracks()
    }

    private fun getTracks() {
        model.tracks.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmpty.visibility =
                if (it.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun initRcView () = with(binding) {
        adapter = TrackAdaptor(this@TrackFragment)
        rcView.layoutManager = LinearLayoutManager(requireContext())
        rcView.adapter = adapter
    }



    companion object {

        @JvmStatic
        fun newInstance() = TrackFragment ()

    }

    override fun onClick(track: TrackItem, type: TrackAdaptor.ClickTape) {
when (type) {
    TrackAdaptor.ClickTape.DELETE -> {
        model.deleteTrack(track)
    }
    TrackAdaptor.ClickTape.OPEN ->{
        model.currentTrack.value = track
        openFragment(ViewTrackFragment.newInstance())
    }
}

//        Log.d("MyLog", "Type ${type}")
    }
}
